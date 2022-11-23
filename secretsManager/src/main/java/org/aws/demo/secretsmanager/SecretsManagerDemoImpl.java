package org.aws.demo.secretsmanager;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class SecretsManagerDemoImpl implements ISecretsManagerDemo {

    //private final static ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();
    @Override
    public String getSecretsValue(String secretId) {
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.US_EAST_1)
                //.credentialsProvider(credentialsProvider)
                .build();

        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(secretId)
                .build();

        GetSecretValueResponse response = client.getSecretValue(request);
        return response.secretString();
    }
}
