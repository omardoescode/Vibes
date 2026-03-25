package com.vibes.app.modules.filesupport.adapters;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.PutObjectArgs;

import java.io.InputStream;

public class MinioAdapter implements S3Api {

    private static volatile MinioAdapter instance;

    private final io.minio.MinioClient adaptee;
    private final String bucketName;

    private MinioAdapter() {
        String endpoint = System.getenv().getOrDefault("MINIO_ENDPOINT", "http://localhost:9000");
        String accessKey = System.getenv().getOrDefault("MINIO_ROOT_USER", "vibes_admin");
        String secretKey = System.getenv().getOrDefault("MINIO_ROOT_PASSWORD", "vibes_super_secret");
        this.bucketName = System.getenv().getOrDefault("MINIO_BUCKET_NAME", "vibes-storage");

        try {
            this.adaptee = io.minio.MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            if (!adaptee.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                adaptee.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("Created new MinIO bucket: " + bucketName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MinIO Adaptee", e);
        }
    }

    public static MinioAdapter getInstance() {
        if (instance == null) {
            synchronized (MinioAdapter.class) {
                if (instance == null) {
                    instance = new MinioAdapter();
                }
            }
        }
        return instance;
    }

    @Override
    public String uploadFile(InputStream fileStream, String objectName) {
        try {
            adaptee.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(fileStream, -1, 10485760)
                            .build()
            );
            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("Error uploading via MinioAdapter", e);
        }
    }

    @Override
    public byte[] downloadFile(String objectName) {
        try (InputStream stream = adaptee.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(objectName).build())) {

            return stream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Error downloading via MinioAdapter: " + objectName, e);
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            return adaptee.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            return false;
        }
    }
}