package com.phatpl.metube.services.video;

import com.phatpl.metube.services.MinIOService;
import io.minio.errors.MinioException;
import lombok.extern.log4j.Log4j2;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


@Log4j2
public class RemoveVideoService implements Runnable {

    private final String prefix;
    private final MinIOService minIOService;

    public RemoveVideoService(String prefix, MinIOService minIOService) {
        this.prefix= prefix;
        this.minIOService = minIOService;
    }

    @Override
    public void run() {
        var allFile = minIOService.listFilesInFolderNonRecursive(prefix);
        for (var file : allFile) {
            try {
                log.info("Deleting file {}", file);
                minIOService.delete(file);
            } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
                log.warn("Failed to delete file {} {}", file, e.getCause());
            }
        }
    }
}
