package com.example;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import java.util.List;

public class CarRecognitionApp {
    private static final String BUCKET_NAME = "your s3 bucket URL where input is stored";
    private static final String QUEUE_URL = "your SQS queue URL used for communication";  

    public static void main(String[] args) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion("us-east-1")
                .build();

        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion("us-east-1")
                .build();

        AmazonSQS sqsClient = AmazonSQSClientBuilder.standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion("us-east-1")
                .build();

        // Loop to read images from S3 bucket
        for (int i = 1; i <= 10; i++) {  // Assuming you have images named 1.jpg to 10.jpg
            String imageName = i + ".jpg";
            if (detectCarInImage(rekognitionClient, s3Client, BUCKET_NAME, imageName)) {
                // If a car is detected, send the image name to the SQS queue
                sqsClient.sendMessage(QUEUE_URL, imageName);
                System.out.println("Car detected in image: " + imageName);
            }
        }
    }

    private static boolean detectCarInImage(AmazonRekognition rekognitionClient, AmazonS3 s3Client, String bucket, String imageName) {
        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image().withS3Object(new S3Object().withBucket(bucket).withName(imageName)))
                .withMaxLabels(10)
                .withMinConfidence(90F);

        try {
            DetectLabelsResult result = rekognitionClient.detectLabels(request);
            List<Label> labels = result.getLabels();

            for (Label label : labels) {
                if ("Car".equals(label.getName())) {
                    return true;  // Car detected
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to detect labels for image " + imageName + ": " + e.getMessage());
        }
        return false;  // No car detected
    }
}
