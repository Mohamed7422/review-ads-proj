package com.practice.workflow.controller;

import com.practice.workflow.common.Constants;
import com.practice.workflow.models.AdModel;
import com.practice.workflow.utils.ImageUtils;
import com.practice.workflow.data.remote.RemoteApiService;
import com.practice.workflow.utils.TextUtils;
import jakarta.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

@Named
public class PhotoReview implements JavaDelegate {

    @Autowired
    private RemoteApiService remoteApiService;

     boolean reviewDecision = true;
    private AdModel adModel;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        gettingAdData(execution);

        String imageFormat = ImageUtils.getImageFormat(adModel.getImageUrl());
        System.out.println("imageFormat : "+ imageFormat);

        byte[] imageBytes = ImageUtils.getByteArrayFromImgUrl(adModel.getImageUrl());
        System.out.println("imageBytes : "+ Arrays.toString(imageBytes));


        //trigger title, description and category from the ad payload of camunda


        JSONObject predictionResponse = remoteApiService.getPrediction(imageBytes, imageFormat,
                Constants.Detection_Model_Base_URL,  adModel);

        int prediction = predictionResponse.getInt("prediction");
        String reason = predictionResponse.getString("reason");

        reviewDecision = prediction == 1;
        execution.setVariable("reviewDecision", reviewDecision);
        System.out.println("\nSetting reviewDecision to: " + reviewDecision);
        System.out.println("\nReviewDecision Reason " + reason);


    }

    private void gettingAdData(DelegateExecution execution) throws Exception {
        String imgUrl = execution.getVariable("imgUrl").toString();
        String adTitle = execution.getVariable("adTitle").toString();
        String adCategory = execution.getVariable("adCategory").toString();
        String adDescription = execution.getVariable("adDescription").toString();
        System.out.println("TitleF: "+ adTitle +"\n"+"DescF: "+ adDescription +"\n"+
                "CategoryF: "+ adCategory + "\nImageURL : "+ imgUrl);

        //Translate the ad title and description and update the variables related.
        String payloadString = TextUtils.preparePayloadString(adTitle, adDescription);

        String[] translateTitleDescResponse= remoteApiService.translateTitleDescription(payloadString, Constants.Google_Translation_Base_URL);
          adTitle = translateTitleDescResponse[0];
          adDescription = translateTitleDescResponse[1];

        adModel = new AdModel(adTitle, adDescription, adCategory, imgUrl);

        System.out.println("Title: "+ adTitle +"\n"+"Desc: "+ adDescription +"\n"+
                "Category: "+ adCategory);


    }

}

//        String adTitle1 = "ايفون 11 برو ماكس";
//        String adDesc = "ايفون 11 برو ماكس من ورا نظيف جدًا مافيه خدوش الشاشه الاماميه سليمه من الكسور...";


//   try(CloseableHttpClient httpClient = HttpClients.createDefault()){

// ByteArrayBody byteArrayBody = new ByteArrayBody(imageBytes,ContentType.DEFAULT_BINARY,"image."+imageFormat);
//        System.out.println("image."+imageFormat);
//        System.out.println("byteArrayBody: "+byteArrayBody);

////
//ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        System.out.println("ByteArrayOutputStream : "+baos);
//
//        ImageIO.write(image,imageFormat,baos);
//        System.out.println("ByteArrayOutputStream : "+baos);

//
//BufferedImage image = ImageUtils.getImageFromUrl(imgUrl);
//        System.out.println("BufferedImage : "+image);
//

//HttpPost uploadImgFile = new HttpPost(Constants.Detection_Model_Base_URL);
//HttpEntity imageHttpEntity = MultipartEntityBuilder.create()
//        .addPart("image", byteArrayBody)
//        .build();
//                uploadImgFile.setEntity(imageHttpEntity);
//
//                try(CloseableHttpResponse response = httpClient.execute(uploadImgFile)){
//String jsonResponse = EntityUtils.toString(response.getEntity());
//                    System.out.println("jsonResponse: "+jsonResponse);
//JSONObject jsonObject = new JSONObject(jsonResponse);
//int prediction = jsonObject.getInt("prediction");
//String reason = jsonObject.getString("reason");
//                    System.out.println("Reason : "+reason);
//
//reviewDecision = prediction == 1;
//        }
//        }