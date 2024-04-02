package org.revature.controller;


import org.revature.dto.CanonicalSlimDTO;
import org.revature.dto.SavedDTO;
import org.revature.dto.SavedSlimDTO;
import org.revature.exception.LabRetrievalException;
import org.revature.exception.LabZipException;
import org.revature.exception.UnauthorizedException;
import org.revature.service.LabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
public class LabController {
    LabService labService;
    @Autowired
    public LabController(LabService labService){
        this.labService = labService;
    }

    @GetMapping("/canonical")
    public ResponseEntity<List<CanonicalSlimDTO>> getCanonicalLabs(){
        return null;
    }
    @GetMapping("/saved")
    public ResponseEntity<List<SavedSlimDTO>> getSavedLabs(){
        return null;
    }
    @PutMapping("/saved/{name}")
    public ResponseEntity<List<SavedSlimDTO>> putSavedLab(@PathVariable String name, @RequestBody InputStream data){
        return null;
    }
    @GetMapping(value="/saved/{name}", produces = "application/zip")
    public ResponseEntity<?> getSavedLab(@PathVariable String name, @RequestHeader long product_key){
        try {
            ByteArrayResource byteArrayResource = labService.getSavedLab(product_key, name);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentLength(byteArrayResource.contentLength());
            return ResponseEntity.status(200).headers(headers).body(byteArrayResource);
        } catch (LabZipException | LabRetrievalException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

}
