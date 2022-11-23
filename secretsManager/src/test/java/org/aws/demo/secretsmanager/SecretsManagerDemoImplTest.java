package org.aws.demo.secretsmanager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecretsManagerDemoImplTest {

    @Test
    void testGetRDSCredentials_success() {
        String RDS_credentials = "demo/aurora";
        ISecretsManagerDemo secretsManagerDemo = new SecretsManagerDemoImpl();
        String secretValue = secretsManagerDemo.getSecretsValue(RDS_credentials);
        System.out.println("RDS secret Value = " + secretValue);
    }

    @Test
    void testGetRDSEndpoint_success() {
        String RDS_endpoint = "demo/rds-endpoint";
        ISecretsManagerDemo secretsManagerDemo = new SecretsManagerDemoImpl();
        String secretValue = secretsManagerDemo.getSecretsValue(RDS_endpoint);
        System.out.println("RDS secret Value = " + secretValue);
    }
}