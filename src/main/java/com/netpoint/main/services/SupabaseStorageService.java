package com.netpoint.main.services;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Service
@Data
public class SupabaseStorageService {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${supabase.storage.bucket}")
    private String bucket;
    public String uploadImage(MultipartFile image, String folder) {
        try {
            String originalName = image.getOriginalFilename() == null
                    ? "image"
                    : image.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");

            String path = folder + "/" + UUID.randomUUID() + "-" + originalName;

            String uploadUrl = supabaseUrl
                    + "/storage/v1/object/"
                    + bucket
                    + "/"
                    + path;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .header("Content-Type", image.getContentType() != null
                            ? image.getContentType()
                            : "application/octet-stream")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(image.getBytes()))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Supabase upload failed: " + response.body());
            }

            return supabaseUrl
                    + "/storage/v1/object/public/"
                    + bucket
                    + "/"
                    + path;

        } catch (Exception e) {
            throw new RuntimeException("Could not upload image", e);
        }
    }

    public String uploadProductImage(MultipartFile image) {
        return uploadImage(image, "products");
    }

}
