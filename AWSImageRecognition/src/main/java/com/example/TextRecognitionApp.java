package com.example;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import java.io.FileWriter;
import java.util.List;

public class TextRecognitionApp {

    private static final String BUCKET_NAME = "s3 bucket URL where image is stored";
    private static final String QUEUE_URL = "your SQS quere URL";  

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

        try (FileWriter fileWriter = new FileWriter("output.txt")) {
            boolean done = false;
            int emptyReceiveCount = 0; // To track consecutive empty queue responses
            final int maxEmptyReceives = 5; // Max empty receives before we exit

            while (!done) {
                // Receive up to 10 messages at a time, with long polling for better efficiency
                List<Message> messages = sqsClient.receiveMessage(new ReceiveMessageRequest(QUEUE_URL)
                                        .withMaxNumberOfMessages(10)  // Fetch up to 10 messages
                                        .withWaitTimeSeconds(10))     // Long polling (10 seconds)
                                        .getMessages();

                if (messages.isEmpty()) {
                    emptyReceiveCount++;
                    if (emptyReceiveCount >= maxEmptyReceives) {
                        System.out.println("Queue is empty after several attempts. Exiting.");
                        done = true;
                    }
                    continue; // Retry in case of empty messages
                }

                emptyReceiveCount = 0; // Reset the count since we received messages

                for (Message message : messages) {
                    String imageName = message.getBody();
                    System.out.println("Image name received: " + imageName);  // Print the image name

                    // If we receive -1, stop processing further messages
                    if (imageName.equals("-1")) {
                        System.out.println("Termination signal received. Exiting.");
                        done = true;
                        break;
                    }

                    // Check if the image contains text and log only if both car and text are found
                    detectTextInImage(rekognitionClient, s3Client, fileWriter, BUCKET_NAME, imageName);

                    // Delete the processed message from the queue
                    sqsClient.deleteMessage(QUEUE_URL, message.getReceiptHandle());
                }
            }

            // Optionally, summarize the results
            System.out.println("All messages have been processed. Exiting application.");
        } catch (Exception e) {
            System.err.println("Failed to recognize text: " + e.getMessage());
        }
    }

    // Method to detect text from an image in S3
    private static void detectTextInImage(AmazonRekognition rekognitionClient, AmazonS3 s3Client, FileWriter fileWriter, String bucket, String imageName) {
        DetectTextRequest request = new DetectTextRequest()
                .withImage(new Image().withS3Object(new S3Object().withBucket(bucket).withName(imageName)));

        try {
            DetectTextResult result = rekognitionClient.detectText(request);
            List<TextDetection> textDetections = result.getTextDetections();

            // Check if the image contains text
            if (!textDetections.isEmpty()) {
                StringBuilder detectedTextBuilder = new StringBuilder();
                for (TextDetection text : textDetections) {
                    detectedTextBuilder.append(text.getDetectedText()).append(" ");
                }

                // Only log the image if we found text, assuming it's also a car-related image
                System.out.println("Image with text detected: " + imageName);
                System.out.println("Detected text: " + detectedTextBuilder.toString().trim());

                // Write index and text to the output file
                fileWriter.write("Image with text detected: " + imageName + "\n");
                fileWriter.write("Detected text: " + detectedTextBuilder.toString().trim() + "\n\n");
                fileWriter.flush();
            } else {
                System.out.println("No text found in image " + imageName);
            }
        } catch (Exception e) {
            System.err.println("Failed to detect text in image " + imageName + ": " + e.getMessage());
        }
    }
}
