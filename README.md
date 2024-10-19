# AWSImageRecognition
The Steps should be followed are as follows,
1. Create two EC2 instances, one for Car and other for text recognition.
**EC2_A creation steps:**
        a. start the AWS Lab.
        b. Go to services tab and search for EC2
        c. click on the running instance and then select launch a new instance.
        d. enter the name, EC2_A, Amazon Linux AMI, Instance Type: t2.micro(as this is
        space is enough), key pair: vockey(this is private key, public key is automatically
        created once we select this), Network Settings: select firewall with default, IAM
        instance profile: LabInstanceProfile(this sets up lab environment).
        e. Launch Instance and you can see the EC2_A is running.
        f. select running EC2_A and select security groups and edit them, add rules of
        SSH, HTTP, HTTPS and add them to access from anywhere by selecting
        0.0.0.0/0 and save them
**EC2_B creation steps:**
        a. Follow the same process as EC2_A except the Network settings where here
        we will select SSH HTTP, HTTPS traffic while launching the instance itself.
        B. Create SQS queue, which will be used to communicate between EC2
        instances.
        C. Connect to both the EC2 instances from two separate terminal windows. Create a project structure in order.
2.** Create SQS Queue** by giving it a name and select the default settings for other fields. Keep note of the Url of this after creation.
3. Download the PEM file and make the downloaded PEM file user as the only person who has complete access to that file.
4. Connect to EC2_A and EC2_B from two different terminal servers by running the following command.
        a. ssh -i “path to PEM key in local” ec2-user@publicipofEC2instance.
5. Install aws-cli,java and maven in each of the terminal servers,
        a. sudo yum install aws-cli
        b. sudo yum install maven
        c. sudo yum install java
6. After installations navigate to .aws path(cd .aws) and update(nano credentials) credentials file with aws_access_key_id, aws_secret_access_key, aws_session_token given in AWS CLI.
7. **Create the basic project directory structure** that can be used by maven to build projects.
        a. **AWSImageRecognition/src/main/java/com/example**
8. Add the pom.xml file which will be having the list of dependencies to be downloaded by maven in the root(AWSImageRecognition) directory.
9. Add the CarRecognitionApp.java and TextRecognitionApp.java code files in the example directory. Make sure to include “package com.example” in the first line of both the files.
10. Build maven project with following syntax. This will create the target, classes folder that contains downloaded dependencies and .jar files to be executed.
        a. maven clean install
11. After building the project execute the jar files with the following command.
        a. **java -cp target/AWSImageRecognition-1.0-SNAPSHOT-jar-with-dependencies.jar com.example.CarRecognitionApp** in first terminal server
        b. **java -cp target/AWSImageRecognition-1.0-SNAPSHOT-jar-with-dependencies.jar com.example.TextRecognitionApp** in the second terminal server.
