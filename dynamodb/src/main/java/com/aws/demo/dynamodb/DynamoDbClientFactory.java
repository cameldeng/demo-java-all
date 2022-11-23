package com.aws.demo.dynamodb;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDbClientFactory {
    //use default profile. using Role
    private final static ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();

    /**
     * 获取 dynamodb client
     * @param region 指定region
     * @return dynamoDbClient
     */
    public static DynamoDbClient getDynamoDbClient(Region region) {
        DynamoDbClient ddb = DynamoDbClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
        return ddb;
    }

    /**
     * 获取 dynamodb enhanced client
     * @param dynamoDbClient DynamoDB client
     * @return DynamoDb Enhanced Client
     */
    public static DynamoDbEnhancedClient getEnhancedClient(DynamoDbClient dynamoDbClient) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        return enhancedClient;
    }
}
