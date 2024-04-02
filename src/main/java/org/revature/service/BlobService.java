package org.revature.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import com.azure.core.util.BinaryData;
import com.azure.identity.*;
import com.azure.storage.blob.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
public class BlobService {
    BlobContainerClient canonicals;
    BlobContainerClient saved;

    public BlobService(){
        // Retrieve the connection string for use with the application.
        String connectStr = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        // Create a BlobServiceClient object using a connection string
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectStr)
                .buildClient();

        this.canonicals = blobServiceClient.getBlobContainerClient("canonicals");
        this.saved = blobServiceClient.getBlobContainerClient("saved");
    }

    public String saveSavedBlob(long pkey, String name, byte[] bytes){
        String blobName = pkey+(name).toLowerCase().trim();
        BlobClient blobClient = saved.getBlobClient(blobName);
//        overwrite blobs is enabled
        InputStream inputStream = new ByteArrayInputStream(bytes);
        blobClient.upload(inputStream);
        return blobName;
    }
    public String saveCanonicalBlob(String name, File zip){
        String blobName = (name).toLowerCase().trim();
        BlobClient blobClient = canonicals.getBlobClient(blobName);
//        overwrite blobs is enabled
        blobClient.uploadFromFile(zip.getPath(), true);
        return blobName;
    }
    public ByteArrayResource getCanonicalFromAzure(String name) {
        String blobName = name.toLowerCase();
        BlobClient blobClient = canonicals.getBlobClient(blobName);
        BinaryData content = blobClient.downloadContent();
        return new ByteArrayResource(content.toBytes());
    }
    public ByteArrayResource getSavedFromAzure(long pkey, String name) {
        String blobName = pkey + name.toLowerCase();
        BlobClient blobClient = saved.getBlobClient(blobName);
        BinaryData content = blobClient.downloadContent();
        return new ByteArrayResource(content.toBytes());
    }
}
