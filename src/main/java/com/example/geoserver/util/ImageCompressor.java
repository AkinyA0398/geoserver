package com.example.geoserver.util;

import javax.imageio.ImageIO;

import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.util.List;
import java.awt.RenderingHints;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ImageCompressor {
    public static byte[] resizeAndCompressImage(byte[] originalImageBytes) throws Exception {
        BufferedImage originalImage = ImageIO.read(new java.io.ByteArrayInputStream(originalImageBytes));

        int maxDim = 800;
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        float scale = Math.min((float) maxDim / width, (float) maxDim / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resized.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resized, "jpg", baos);
        return baos.toByteArray();
    }

    public static List<byte[]> multipartFilesToBytes(List<MultipartFile> files) throws IOException {
        List<byte[]> bytesList = new ArrayList<>();
        if (files == null)
            return bytesList;

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                bytesList.add(file.getBytes());
            }
        }
        return bytesList;
    }

}
