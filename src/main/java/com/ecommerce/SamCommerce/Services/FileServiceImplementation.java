package com.ecommerce.SamCommerce.Services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImplementation implements FileService{

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        //GET THE FILL NAME OF THE CURRENT/ORIGINAL FILE
        String originalFileName = file.getOriginalFilename();
        //RENAME THE FILE WITH AN UNIQUE NAME
        String generatedRandomID = UUID.randomUUID().toString();  //GENERATED A RANDOM ID -> 1234
        String uniqueFileName = generatedRandomID.concat(originalFileName.substring(originalFileName.lastIndexOf("."))); // SAM.jpg -> 1234.jpg
        String filePath = path + File.separator + uniqueFileName; //CREATING THE FILE PATH - "images/" + "/" + "1234.jpg"
        //CHECK IF THE FILE EXISTS
        File folder = new File(path);
        if(!folder.exists()){
            folder.mkdir();
        }
        //UPLOAD TO SERVER
        Files.copy(file.getInputStream(), Paths.get(filePath));
        return uniqueFileName;
    }
}
