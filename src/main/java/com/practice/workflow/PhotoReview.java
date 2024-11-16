package com.practice.workflow;

import jakarta.inject.Named;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Set;

@Named
public class PhotoReview implements JavaDelegate {

    private static final Set<String> SUPPORTED_FORMATS = Set.of("jpg","jpeg","png","gif","bmp");

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        boolean reviewDecision = false; // or true, based on your logic

        String imgUrl = execution.getVariable("imgUrl").toString();

        String blackRedDetectApi = "http://127.0.0.1:5000/detect_color";

        try(CloseableHttpClient httpClient = HttpClients.createDefault()){
            HttpPost uploadImgFile = new HttpPost(blackRedDetectApi);
            BufferedImage image = getImageFromUrl(imgUrl);
            if (image != null) {
            String imageFormat = getImageFormat(imgUrl);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            ImageIO.write(image,imageFormat,baos);

            byte[] imageBytes = baos.toByteArray();

//            File imgFile = new File(imgUrl);
//            FileBody fileBody = new FileBody(imgFile, ContentType.DEFAULT_BINARY);
            ByteArrayBody byteArrayBody = new ByteArrayBody(imageBytes,ContentType.DEFAULT_BINARY,"image.jpg");
            HttpEntity imageHttpEntity = MultipartEntityBuilder.create()
                    .addPart("image", byteArrayBody)
                    .build();
            uploadImgFile.setEntity(imageHttpEntity);

            try(CloseableHttpResponse response = httpClient.execute(uploadImgFile)){
                String jsonResponse = EntityUtils.toString(response.getEntity());

                JSONObject jsonObject = new JSONObject(jsonResponse);
                int prediction = jsonObject.getInt("prediction");

                reviewDecision = (prediction == 1);
                execution.setVariable("reviewDecision", reviewDecision);
                System.out.println("Setting reviewDecision to: " + reviewDecision);

            }

            }
        }


    }

    private String getImageFormat(String image) {
        if (image == null || image.isEmpty()){
            throw new IllegalArgumentException("Image data cannot be null or empty.");
        }

        String format;
        if (image.startsWith("data:image/")){

            int semicolonIndex = image.indexOf(";");
            if (semicolonIndex < 0) {
                throw new IllegalArgumentException("Invalid Base64 image data: Missing ';' after MIME type.");
            }

            format = image.substring(11,semicolonIndex).toLowerCase();

        }else {
            int dotIndex =  image.lastIndexOf('.');
            if (dotIndex < 0 || dotIndex == image.length() - 1) {
                throw new IllegalArgumentException("Invalid image URL: Missing file extension.");
            }

            format = image.substring(dotIndex+1).toLowerCase();
        }

            if (!SUPPORTED_FORMATS.contains(format)){
                throw new IllegalArgumentException("Unsupported Image Format: "+ format);
            }

            return format;
    }

    private BufferedImage getImageFromUrl(String imageUrl) throws IOException {

        if (imageUrl.startsWith("data:image/")){
            return getImageFromBase64(imageUrl);
        }else {
            try {
                URL url = new URL(imageUrl);
                return ImageIO.read(url);
            } catch (IOException e) {
                System.err.println("Failed to load image from URL: " + e.getMessage());
                return null;
            }
        }

    }

    private BufferedImage getImageFromBase64(String base64Image) throws IOException {

        String base64Data =  base64Image.substring(base64Image.indexOf(",")+1);
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(bis);
    }
}
