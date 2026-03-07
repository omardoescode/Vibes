package com.vibes.app.modules.filesupport.singleton;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.PutObjectArgs;
import java.io.InputStream;

public class MinIOClient {
    private static volatile MinIOClient instance;
    private final io.minio.MinioClient actualMinioClient;
    private final String bucketName;

    private MinIOClient() {
        String endpoint = System.getenv().getOrDefault("MINIO_ENDPOINT", "http://localhost:9000");
        String accessKey = System.getenv().getOrDefault("MINIO_ROOT_USER", "vibes_admin");
        String secretKey = System.getenv().getOrDefault("MINIO_ROOT_PASSWORD", "vibes_super_secret");
        this.bucketName = System.getenv().getOrDefault("MINIO_BUCKET_NAME", "vibes-storage");

        try {
            this.actualMinioClient = io.minio.MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            if (!actualMinioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                actualMinioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("Created new MinIO bucket: " + bucketName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MinIO Client", e);
        }
    }

    public static MinIOClient getInstance() {
        if (instance == null) {
            synchronized (MinIOClient.class) {
                if (instance == null) {
                    instance = new MinIOClient();
                }
            }
        }
        return instance;
    }

    public String uploadFile(InputStream fileStream, String objectName) {
        try {
            actualMinioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(fileStream, -1, 10485760)
                            .build()
            );
            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("Error uploading to MinIO", e);
        }
    }

    public byte[] downloadFile(String fileId) {
        try {
            InputStream stream = actualMinioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(fileId).build());
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Error downloading from MinIO", e);
        }
    }

    public boolean isHealthy() {
        try {
            return actualMinioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder().bucket(bucketName).build()
            );
        } catch (Exception e) {
            System.err.println("MinIO Health Check Failed: " + e.getMessage());
            return false;
        }
    }
}