package com.phatpl.metube._utils.StreamingHelper;

import org.springframework.beans.factory.annotation.Value;

import com.phatpl.metube._services.MinIOService;

public class StreamingUploader extends Thread {
    private String streamKey;
    private String fileName;

//    @Value("${FOLDER_UPLOAD}")
//    private String parentFolder;
//
//    private MinIOService minIOService;
//    private
//
//    public StreamingUploader(String streamKey, String fileName, MinIOService minIOService) {
//        this.streamKey = streamKey;
//        this.fileName = fileName;
//        this.minIOService = minIOService;
//    }
//
//    @Override
//    public void run() {
//        try {
//            minIOService.uploadFile(parentFolder + "/" + streamKey, fileName);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
