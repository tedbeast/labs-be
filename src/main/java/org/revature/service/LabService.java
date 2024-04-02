package org.revature.service;

import org.revature.App;
import org.revature.dto.SavedDTO;
import org.revature.entity.Canonical;
import org.revature.entity.PKey;
import org.revature.entity.Saved;
import org.revature.exception.LabRetrievalException;
import org.revature.exception.LabZipException;
import org.revature.exception.UnauthorizedException;
import org.revature.repository.CanonicalRepository;
import org.revature.repository.SavedRepository;
import org.revature.util.BaseURLUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class LabService {
    CMDService cmdService;
    CanonicalRepository canonicalRepository;
    BlobService blobService;
    AuthService authService;
    SavedRepository savedRepository;
    @Autowired
    public LabService(CMDService cmdService, CanonicalRepository canonicalRepository, BlobService blobService, AuthService authService, SavedRepository savedRepository){
        this.cmdService = cmdService;
        this.canonicalRepository = canonicalRepository;
        this.blobService = blobService;
        this.authService = authService;
        this.savedRepository = savedRepository;
    }


    public ByteArrayResource getCanonicalByteArray(String name) throws LabZipException, LabRetrievalException, IOException, InterruptedException {
        Canonical labCanonical = getCanonicalLab(name);
        return blobService.getCanonicalFromAzure(labCanonical.getName());
    }
    /**
     * check for brand new labs or lab updates prior to returning the canonical
     * @param name
     * @return
     * @throws LabRetrievalException
     * @throws LabZipException
     * @throws IOException
     * @throws InterruptedException
     */
    public Canonical getCanonicalLab(String name) throws LabRetrievalException, LabZipException {
        if(name == null || name.length() < 2){
            App.log.info("malformed lab name: "+name);
            throw new LabRetrievalException("Encountered lab retrieval exception for lab "+name);
        }
        Canonical lab = canonicalRepository.findByName(name);
        String[] tuple = getLatestCanonicalCommit(name);
        if(tuple == null){
            App.log.info("no such lab found: "+name);
            throw new LabRetrievalException("Encountered lab retrieval exception for lab "+name);
        }
        String commit = tuple[0];
        String source = BaseURLUtil.baseUrl[Integer.parseInt(tuple[1])];
        if(lab == null){
            if(commit!=null){
                App.log.info("adding as a new canonical: "+name+", "+commit+", "+source);
                lab = addNewCanonicalLab(name, commit, source);
                return lab;
            }else{
                App.log.warn("commit hash null or malformed: "+name+", "+commit+", "+source);
                throw new LabRetrievalException("commit hash null or malformed: "+name+", "+commit+", "+source);
            }
        }else{
            if(checkForCanonicalLabUpdate(lab, commit)) {
                App.log.info("updating canonical: "+name+", "+commit+", "+source);
                lab = updateExistingCanonicalLabZip(name, commit, source);
                return lab;
            }else{
                App.log.info("loading canonical: "+name+", "+commit+"/"+lab.getCommitHash()+", "+source);
                return lab;
            }
        }
    }

    /**
     * take the git repo from the web, if there are no issues create the zip
     * @param name
     * @param source
     * @return
     * @throws InterruptedException
     * @throws LabZipException
     */
    public File getZipFromWeb(String name, String source) throws LabZipException {
        File zipfile;
        try{
            zipfile = generateCanonicalLabZip(source, name);
        }catch (IOException | InterruptedException e){
//            ensure that no artifacts remain when an issue occurs
            zipfile = new File(name+".zip");
            zipfile.delete();
            App.log.warn("an issue occurred while creating the zipfile: "+name+", "+source);
            throw new LabZipException("Exception during creation of zipfile: "+name+", "+source);
        }
        return zipfile;
    }

    /**
     * process for saving the new lab canonical entity, including transferring the zip to blob
     * @param name
     * @param commit
     * @param source
     * @return
     * @throws LabZipException
     * @throws IOException
     * @throws InterruptedException
     */
    public Canonical addNewCanonicalLab(String name, String commit, String source) throws LabZipException {
        File zipfile = getZipFromWeb(name, source);
        try {
            byte[] zipBytes = Files.readAllBytes(Path.of(zipfile.getPath()));
            Canonical labCanonical = new Canonical(name, commit, new Timestamp(System.currentTimeMillis()), zipBytes);
            blobService.saveCanonicalBlob(name, zipfile);
            zipfile.delete();
            return canonicalRepository.save(labCanonical);
        }catch (IOException e){
            throw new LabZipException("The lab "+name+" could not be added as a canonical lab ");
        }
    }

    /**
     * process for updating a lab zip
     * @param name
     * @param commit
     * @param source
     * @return
     * @throws LabZipException
     * @throws IOException
     * @throws InterruptedException
     */
    public Canonical updateExistingCanonicalLabZip(String name, String commit, String source) throws LabZipException, LabRetrievalException {
        File zipfile = getZipFromWeb(name, source);
        Canonical labCanonical = canonicalRepository.findByName(name);
        try{
            byte[] zipBytes = Files.readAllBytes(Path.of(zipfile.getPath()));
        }catch (IOException e){
            throw new LabRetrievalException("The lab "+name+" could not be updated to commit "+commit);
        }
        labCanonical.setCommitHash(commit);
        blobService.saveCanonicalBlob(name, zipfile);
        zipfile.delete();
        return canonicalRepository.save(labCanonical);
    }
    /**
     * check for the existence of the repo by polling all github repo urls
     * @return
     */
    public String[] getLatestCanonicalCommit(String name){
        for(int i = 0; i < BaseURLUtil.baseUrl.length; i++){
            String commit = checkCanonicalLabSource(BaseURLUtil.baseUrl[i]+name);
            if(commit!=null){
                return new String[]{commit, ""+i};
            }
        }
        return null;
    }
    /**
     * check for the existence of the repo by polling single github repo url
     * @return
     */
    public String checkCanonicalLabSource(String url){
        try{
            String output = cmdService.runCommandReturnOutput("git ls-remote "+url);
            if(output.length()>1){
                return output.substring(0, 40);
            }
        }catch(Exception e){
            return null;
        }
        return null;
    }
    /**
     * check for updates to the lab by comparing the commit hashes
     * @return
     */
    public boolean checkForCanonicalLabUpdate(Canonical labCanonical, String commit){
        if(labCanonical.getCommitHash().equals(commit)==false){
            return true;
        }else{
            return false;
        }
    }

    /**
     * clone git repo, convert to zip.
     */
    public File generateCanonicalLabZip(String ghorgPrefix, String name) throws IOException, InterruptedException, LabZipException {
        cmdService.runCommandReturnOutput("git clone "+ghorgPrefix+name);
        pack("./"+name, "./"+name+".zip");
        File dir = new File("./"+name);
        deleteDirectory(dir);
        File zip = new File("./"+name+".zip");
        return zip;
    }
    /**
     * git repo directory: "i dont wanna play with you any more"
     * @param directoryToBeDeleted
     * @return
     */
    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
    /**
     * package the git repo directory to zip
     * @param sourceDirPath
     * @param zipFilePath
     * @throws IOException
     */
    public void pack(String sourceDirPath, String zipFilePath) throws IOException {
        File zip = new File(zipFilePath);
        zip.delete();
        Path p = Files.createFile(Paths.get(zipFilePath));
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
            Path pp = Paths.get(sourceDirPath);
            Files.walk(pp)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            System.err.println(e);
                        }
                    });
        }
    }

    public List<Canonical> getAllCanonicals(long pkey) throws UnauthorizedException {
        if(authService.validateUser(pkey)){
            return canonicalRepository.findAll();
        }else{
            throw new UnauthorizedException("The given product key is not authorized to view all canonical labs.");
        }
    }

    /**
     * check if the user's already got an existing lab. if not (when the user starts a new lab for the first time),
     * start the process of copying from the canonical
     */
    public ByteArrayResource getSavedLab(long pkey, String name) throws UnauthorizedException, LabZipException, LabRetrievalException {
        if(!authService.validateUser(pkey)){
            throw new UnauthorizedException("The given product key is not authorized to get a saved lab.");
        }
        Saved saved = savedRepository.getSpecificSavedLab(pkey, name);
        if (saved == null) {
            addNewSavedLab(pkey, name);
            ByteArrayResource byteArray = blobService.getSavedFromAzure(pkey, name);
            return byteArray;
        }else{
//        REMOVE THIS LATER!!!
//        since there is no save functionality yet anyways, just reset the lab every time its requested
            resetLabProgress(pkey, name);
            ByteArrayResource byteArray = blobService.getSavedFromAzure(pkey, name);
            SavedDTO dto = new SavedDTO(saved.getId(), saved.getPKey().getId(),
                    saved.getCanonical().getId(), saved.getLastUpdated(), byteArray.getByteArray());
            dto.setData(byteArray.getByteArray());
            return byteArray;
        }
    }

    /**
     * grab a lab from the canonical set and make it the user's saved lab
     */
    public SavedDTO addNewSavedLab(long pkey, String name) throws LabZipException, LabRetrievalException {
        Canonical canonical = getCanonicalLab(name);
        PKey pKey = authService.getProductKey(pkey);
        Saved saved = new Saved();
        saved.setPKey(pKey);
        saved.setCanonical(canonical);
        saved = savedRepository.save(saved);
        byte[] canonicalLabBytes = blobService.getCanonicalFromAzure(name).getByteArray();
        blobService.saveSavedBlob(pkey, name, canonicalLabBytes);
        SavedDTO dto = new SavedDTO(saved.getId(), saved.getPKey().getId(),
                saved.getCanonical().getId(), saved.getLastUpdated(), canonicalLabBytes);
        return dto;
    }
    /**
     * provided the pkey and lab name, reset the user's lab to canonical
     * @return
     */
    public Saved resetLabProgress(long pkey, String name) throws LabZipException, LabRetrievalException, UnauthorizedException {
        if(!authService.validateUser(pkey)){
            throw new UnauthorizedException("The given product key is not authorized to get a saved lab.");
        }
        byte[] labBytes = blobService.getCanonicalFromAzure(name).getByteArray();
        blobService.saveSavedBlob(pkey, name, labBytes);
        Saved saved = savedRepository.getSpecificSavedLab(pkey, name);
        saved.setLastUpdated(new Timestamp(System.currentTimeMillis()));
        savedRepository.save(saved);
        return saved;
    }
    /**
     * provided the zip file bytes of an existing lab, update the lab zip stored in the db with respoect
     * to the provided pkey
     * @return
     */
    public Saved saveLabProgress(long pkey, String name, byte[] labBytes) throws UnauthorizedException {
        if(!authService.validateUser(pkey)){
            throw new UnauthorizedException("The given product key is not authorized to get a saved lab.");
        }
        blobService.saveSavedBlob(pkey, name, labBytes);
        Saved saved = savedRepository.getSpecificSavedLab(pkey, name);
        saved.setLastUpdated(new Timestamp(System.currentTimeMillis()));
        savedRepository.save(saved);
        return saved;
    }
}
