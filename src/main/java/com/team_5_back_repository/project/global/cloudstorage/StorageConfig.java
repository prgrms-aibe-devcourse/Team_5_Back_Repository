package com.team_5_back_repository.project.global.cloudstorage;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String accessSecret;

    @Value("${cloud.aws.region}")
    private String region;

    @Bean
    public AmazonS3Client amazonS3Client() {
        AWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKey, accessSecret);
        return (AmazonS3Client) AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                .withRegion(region)
                .build();
    }

}