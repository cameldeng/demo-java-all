package org.aws.demo.secretsmanager;

public interface ISecretsManagerDemo {
    /**
     * get secret value from AWS Secrets Manager
     * @param secretId secretId
     * @return secret value
     */
    String getSecretsValue(String secretId);
}
