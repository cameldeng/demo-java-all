package com.aws.demo.s3;

public interface IS3Service {

    /**
     * download s3 object to local，
     * S3TransferManager refer to: https://github.com/awsdocs/aws-doc-sdk-examples/blob/main/javav2/example_code/s3/src/main/java/com/example/s3/transfermanager/GetObject.java
     * @param bucketName bucket name
     * @param objectKey object key
     * @param range bytes range
     * @param localPath local saving path
     * @return
     */
    String getS3ObjectBytes(String bucketName, String objectKey, String range, String localPath);
    /**
     * put object to s3
     *
     * @param objectPath object path
     */
    public String putS3Object(String bucketName, String objectKey, String objectPath);

    /**
     *
     * @param bucketName Bucket name
     * @param objectKey object key
     * @param miniPartSizeMB miniPartSize with unit MB
     * @param localPath local file path
     * @return
     */
    String getS3ObjectMultiPart(String bucketName, String objectKey, int miniPartSizeMB, String localPath);

    /**
     * * Multi Part upload object
     * @param bucketName
     * @param objectKey
     * @param objectPath
     * @return
     */
    String putS3ObjectMultiPart(String bucketName, String objectKey, int miniPartSizeMB,  String objectPath);

    /**
     * List objects
     * @param bucketName
     * @return
     */
    public String listBucketObjects (String bucketName, String prefix, String delimiter);
}
