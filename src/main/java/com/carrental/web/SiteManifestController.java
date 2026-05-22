package com.carrental.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class SiteManifestController {

    private static final MediaType MANIFEST_MEDIA_TYPE =
            MediaType.parseMediaType("application/manifest+json;charset=UTF-8");

    @GetMapping(path = "/site.webmanifest")
    public ResponseEntity<String> manifest() throws IOException {
        Resource resource = new ClassPathResource("pwa-manifest.json");
        String json = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MANIFEST_MEDIA_TYPE)
                .body(json.trim());
    }
}
