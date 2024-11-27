package com.practice.workflow.utils;

import camundajar.impl.scala.Char;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Set;

public class ImageUtils {
    private static final Set<String> SUPPORTED_FORMATS = Set.of("jpg","jpeg","png","gif","bmp");

    public static String getImageFormat(String imageUrl) {
        String format;
        if (imageUrl == null || imageUrl.isEmpty()){
            throw new IllegalArgumentException("Image data cannot be null or empty.");
        }
        System.out.println("Image URL in Image Utils : "+imageUrl);

        if (imageUrl.startsWith("data:image/")){

            int semicolonIndex = imageUrl.indexOf(";");
            if (semicolonIndex < 0) {
                throw new IllegalArgumentException("Invalid Base64 image data: Missing ';' after MIME type.");
            }


            format = imageUrl.substring(11,semicolonIndex).toLowerCase();
            System.out.println("format in Image Utils : "+format);

        }else {
            int dotIndex =  imageUrl.lastIndexOf('.');
            if (dotIndex < 0 || dotIndex == imageUrl.length() - 1) {
                throw new IllegalArgumentException("Invalid image URL: Missing file extension.");
            }
            String cleanUrlWithoutQuery = imageUrl.split("\\?")[0];
            format = cleanUrlWithoutQuery.substring(dotIndex+1).toLowerCase();
        }

        if (!SUPPORTED_FORMATS.contains(format)){
            throw new IllegalArgumentException("Unsupported Image Format: "+ format);
        }

        System.out.println("format in Image Utils : "+format);
        return format;
    }

    public static BufferedImage getImageFromUrl(String imageUrl) throws IOException {

        if (imageUrl.startsWith("data:image/")){
            return getImageFromBase64(imageUrl);
        }else {
            try {
                System.out.println("BufferedImage: URL "+ imageUrl);
                URL url = new URL(imageUrl);
                return ImageIO.read(url);
            } catch (IOException e) {
                System.err.println("Failed to load image from URL: " + e.getMessage());
                return null;
            }
        }

    }

    public static BufferedImage getImageFromBase64(String base64Image) throws IOException {

        String base64Data =  base64Image.substring(base64Image.indexOf(",")+1);
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(bis);
    }

    public static byte[] getByteArrayFromImgUrl(String imgUrl) throws IOException {

        BufferedImage image = ImageUtils.getImageFromUrl(imgUrl);

        System.out.println("BufferedImage : "+image);

        if (image==null || image.getWidth() <= 0 || image.getHeight() <= 0){
            throw new IllegalArgumentException("No Decoded image found ");
        }

        String imageFormat = ImageUtils.getImageFormat(imgUrl);

//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        System.out.println("ByteArrayOutputStream : "+baos);
//
//        ImageIO.write(image,imageFormat,baos);
//        System.out.println("ByteArrayOutputStream : "+baos);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Write the BufferedImage to the output stream in the specified format
            boolean isWritten = ImageIO.write(image, imageFormat, baos);
            if (!isWritten) {
                throw new IOException("Failed to write the image to ByteArrayOutputStream");
            }
        return baos.toByteArray();
       }
    }
}
