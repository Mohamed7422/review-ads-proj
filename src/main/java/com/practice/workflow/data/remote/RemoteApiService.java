package com.practice.workflow.data.remote;


import com.practice.workflow.common.Constants;
import com.practice.workflow.models.AdModel;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class RemoteApiService {

    @Value("${google.api.key}")
    private String apiKey;

    /**
     * Sends an image to the detection model API and returns the prediction.
     *
     * @param imageBytes  Byte array of the image.
     * @param imageFormat The format of the image (e.g., jpg, png).
     * @param apiUrl      The URL of the API endpoint.
     * @return A JSONObject containing the prediction and other response data.
     */

    public JSONObject getPrediction(byte[] imageBytes, String imageFormat, String apiUrl, AdModel admodel) throws IOException {

        ByteArrayBody byteArrayBody = new ByteArrayBody(imageBytes, ContentType.DEFAULT_BINARY,"image."+imageFormat);
        System.out.println("image."+imageFormat);
        System.out.println("byteArrayBody: "+byteArrayBody);
        System.out.println("Admodel  : " + admodel.toString());

        try(CloseableHttpClient httpClient = HttpClients.createDefault()){
            HttpPost uploadImgFile = new HttpPost(apiUrl);

            HttpEntity imageHttpEntity = MultipartEntityBuilder.create()
                    .addTextBody("title",admodel.getTitle())
                    .addTextBody("description",admodel.getDescription())
                    .addTextBody("category",admodel.getCategory())
                    .addPart("image", byteArrayBody)
                    .build();

            uploadImgFile.setEntity(imageHttpEntity);

            try(CloseableHttpResponse response = httpClient.execute(uploadImgFile)){
                String jsonResponse = EntityUtils.toString(response.getEntity());
                System.out.println("jsonResponse : "+jsonResponse);
                return new JSONObject(jsonResponse);
            }

        }


    }


    /**
     * Example method for handling a different type of request to another API.
     *
     * @param payload Custom payload for the API request.
     * @param apiUrl  The URL of the API endpoint.
     * @return A JSONObject containing the response data.
     */
    public  String[] translateTitleDescription(String payload, String apiUrl) throws Exception {
        String[] translatedResponse = new String[2];

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            String apiUrlWithKey = apiUrl+"?key="+apiKey;


            HttpPost postRequest = new HttpPost(apiUrlWithKey);
            postRequest.setHeader("Content-Type","application/json");
            StringEntity payloadEntity = new StringEntity(payload,"UTF-8");
            postRequest.setEntity(payloadEntity);

            // Add payload as an HTTP entity if required
            // HttpEntity payloadEntity = ...
            // postRequest.setEntity(payloadEntity);

            // Execute the request and handle the response
            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                JSONObject translateTitleDescResponse = new JSONObject(jsonResponse);
                JSONArray translatesArray = translateTitleDescResponse.getJSONObject("data").getJSONArray("translations");
                String translatedTitle = translatesArray.getJSONObject(0).getString("translatedText");
                String translatedDescription = translatesArray.getJSONObject(1).getString("translatedText");
                translatedResponse[0]= translatedTitle;
                translatedResponse[1]= translatedDescription;
                return translatedResponse;
            }
        }
    }


}
