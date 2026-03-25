package com.vibes.app.modules.filesupport.decorators;

import com.vibes.app.modules.filesupport.products.FileStore;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressionFileStoreDecorator extends FileStoreDecorator {

    private final boolean isWindows;

    public CompressionFileStoreDecorator(FileStore wrapper) {
        super(wrapper);
        this.isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    }

    @Override
    public String upload(InputStream data, String filename) {
        try {
            File tempIn = File.createTempFile("upload-", ".tmp");
            Files.copy(data, tempIn.toPath(), StandardCopyOption.REPLACE_EXISTING);

            File compressedFile;

            try {
                compressedFile = compressUsingProcess(tempIn);
            } catch (Exception e) {
                System.out.println("[WARN] OS Gzip failed, falling back to Java GZIP.");
                compressedFile = compressUsingJava(tempIn);
            }

            try (InputStream compressedStream = new FileInputStream(compressedFile)) {
                String fileId = super.upload(compressedStream, filename + ".gz");

                tempIn.delete();
                compressedFile.delete();

                return fileId;
            }

        } catch (IOException e) {
            throw new RuntimeException("Compression upload failed", e);
        }
    }

    @Override
    public byte[] downloadFile(String fileId) {
        try {
            byte[] compressedBytes = super.downloadFile(fileId);

            File tempGz = File.createTempFile("download-", ".gz");
            Files.write(tempGz.toPath(), compressedBytes);

            File decompressedFile;
            try {
                decompressedFile = decompressUsingProcess(tempGz);
            } catch (Exception e) {
                System.out.println("[WARN] OS Gunzip failed, falling back to Java GZIP.");
                decompressedFile = decompressUsingJava(tempGz);
            }

            byte[] result = Files.readAllBytes(decompressedFile.toPath());

            tempGz.delete();
            decompressedFile.delete();

            return result;

        } catch (IOException e) {
            throw new RuntimeException("Decompression download failed", e);
        }
    }

    private File compressUsingProcess(File inputFile) throws Exception {
        ProcessBuilder pb;
        if (isWindows) {
            pb = new ProcessBuilder("cmd.exe", "/c", "gzip", "-f", inputFile.getAbsolutePath());
        } else {
            pb = new ProcessBuilder("gzip", "-f", inputFile.getAbsolutePath());
        }

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) throw new RuntimeException("Process gzip failed with code: " + exitCode);

        return new File(inputFile.getAbsolutePath() + ".gz");
    }

    private File decompressUsingProcess(File gzFile) throws Exception {
        ProcessBuilder pb;
        if (isWindows) {
            pb = new ProcessBuilder("cmd.exe", "/c", "gzip", "-d", "-f", gzFile.getAbsolutePath());
        } else {
            pb = new ProcessBuilder("gzip", "-d", "-f", gzFile.getAbsolutePath());
        }

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) throw new RuntimeException("Process gunzip failed with code: " + exitCode);

        return new File(gzFile.getAbsolutePath().replace(".gz", ""));
    }

    private File compressUsingJava(File inputFile) throws IOException {
        File outputFile = new File(inputFile.getAbsolutePath() + ".gz");
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile);
             GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {
            fis.transferTo(gzipOS);
        }
        return outputFile;
    }

    private File decompressUsingJava(File gzFile) throws IOException {
        File outputFile = new File(gzFile.getAbsolutePath().replace(".gz", ""));
        try (FileInputStream fis = new FileInputStream(gzFile);
             GZIPInputStream gzipIS = new GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            gzipIS.transferTo(fos);
        }
        return outputFile;
    }
}