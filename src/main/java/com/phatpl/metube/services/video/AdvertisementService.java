package com.phatpl.metube.services.video;

import com.phatpl.metube.models.enums.ResourceStatus;
import com.phatpl.metube.services.MinIOService;
import com.phatpl.metube.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdvertisementService {

    private MinIOService minIOService;
    private UserService userService;
    private ResourceService resourceService;

    @Autowired
    public AdvertisementService(MinIOService minIOService, UserService userService, ResourceService resourceService) {
        this.minIOService = minIOService;
        this.userService = userService;
        this.resourceService = resourceService;
    }


    public boolean addAdvertisementService(Integer videoId) throws Exception {
        var resources = resourceService.findById(videoId);

        if (resources == null || resources.getStatus() != ResourceStatus.READY) {
            return false;
        }

        var userId = userService.extractUserId();
        String urlResources = resources.getVideo().replaceAll("master", "1080p");

        var input = minIOService.getFile(urlResources);

        String hlsContent;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            hlsContent = reader.lines().collect(Collectors.joining("\n"));
        }

        log.info("Starting adding ad for id: " + videoId);

        List<String> adFiles = Arrays.asList(
                "QUANGCAO_5025f791abbf40a083264f2f6e2e3851.ts",
                "QUANGCAO_c043dc3c37d94b30a89c296316ec52da.ts",
                "QUANGCAO_24bcae61ff4043c180890c13c260dcd1.ts",
                "QUANGCAO_b8b30ea95d2d47f5bd1b42eedf113433.ts",
                "QUANGCAO_a3edf953e25545fda77b60845dd528e1.ts",
                "QUANGCAO_b9b2785b6b7a4d23b77d62de4f1d3f0b.ts",
                "QUANGCAO_9b0cec6a64c4470f88638a4b3012d806.ts",
                "QUANGCAO_606b8afa0c76410697cd9f4697093ede.ts",
                "QUANGCAO_a40f420eaac84a6e969ea89411932eb0.ts",
                "QUANGCAO_7a964a2cf81c4b118a28edfcc769d7a6.ts"
        );

        List<Double> durations = Arrays.asList(
                3.840000, 3.840000, 1.920000, 3.840000, 1.920000,
                3.840000, 1.920000, 3.840000, 3.840000, 1.840000
        );

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < adFiles.size(); i++) {
            sb.append(String.format("#EXTINF:%.6f,\n", durations.get(i)));
            sb.append(adFiles.get(i)).append("\n");
        }
        sb.append("#EXT-X-ENDLIST");


        String modifiedContent = hlsContent.replace("#EXT-X-ENDLIST", sb.toString());

        InputStream modifiedStream = new ByteArrayInputStream(modifiedContent.getBytes(StandardCharsets.UTF_8));

        log.info("Finished " + videoId);

        minIOService.uploadM3U8(modifiedStream, urlResources);

        return true;
    }

}
