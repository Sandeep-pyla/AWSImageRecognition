# AWSImageRecognition
Project and README file are added in master branch.
Basic Outline:
This is a project on CAR and text on CAR image detection using S3, SQS and Rekognition.
1. There will be two EC2 instances, EC2_A and EC2_B.
2. Input file read from s3 bucket by using its URL which is used as input.
3. EC2_A detects car images in input and pushes the image names which are identified as cars by recognition to SQS Queue.
4. EC2_B takes the image names from SQS Queue and prints the corresponding text by referring input file. The output image name, text in which the car is detected is written in output.txt file. 
