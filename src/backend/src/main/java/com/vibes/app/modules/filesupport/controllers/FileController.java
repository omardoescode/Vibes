package com.vibes.app.modules.filesupport.controllers;

import com.vibes.app.modules.filesupport.factory.AbstractStorageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/filesupport")
public class FileController {

    private final AbstractStorageFactory storageFactory;

    @Autowired
    public FileController(AbstractStorageFactory storageFactory) {
        this.storageFactory = storageFactory;
    }

    @GetMapping("/profiles/view/{fileId}")
    public ResponseEntity<byte[]> viewProfilePicture(@PathVariable String fileId) {
        try {
            byte[] data = storageFactory.createProfilePictureStore().getProfilePicture(fileId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentDispositionFormData("inline", fileId);
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/files/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileId) {
        try {
            byte[] data = storageFactory.createFileStore().downloadFile(fileId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileId);
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/attachments/view/{fileId}")
    public ResponseEntity<byte[]> viewAttachment(@PathVariable String fileId) {
        try {
            byte[] data = storageFactory.createAttachmentStore().getAttachment(fileId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("inline", fileId);
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}