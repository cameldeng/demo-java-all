package com.aws.demo.s3;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.transfer.s3.*;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;

import static software.amazon.awssdk.transfer.s3.SizeConstant.MB;

public class S3ServiceImpl implements IS3Service {

    private final static  Region region = Region.US_EAST_1;
    // 直接使用ak and sk
    // 如果使用role访问，pom增加依赖 sts
    private final static ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();

    @Override
    public String getS3ObjectBytes(String bucketName, String objectKey, String range, String localPath) {
        //construct S3 object request
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .range(range)
                .build();
        // construct S3 object response
        ResponseBytes<GetObjectResponse> objectBytes = getS3Client().getObjectAsBytes(request);
        byte[] data = objectBytes.asByteArray();

        //write the data to local file
        File localFile = new File(localPath);
        OutputStream output = null;
        try {
            output = new FileOutputStream(localFile);
            output.write(data);
            System.out.println(String.format("Successfully obtain object %s from S3",objectKey));
            output.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "Successfully";
    }

    @Override
    public String putS3Object(String bucketName, String objectKey, String objectPath) {
        // construct S3Client
        S3Client s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("x-amz-meta-myVal", "Test");
        //construct request
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .metadata(metadata)
                .build();

        //construct response
        PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(
                getObjectFile(objectPath)));

        return putObjectResponse.eTag();
    }

    @Override
    public String getS3ObjectMultiPart(String bucketName, String objectKey, int miniPartSizeMB, String localPath) {
        //high level, truly asynchronous, non-blocking API
        S3TransferManager s3tm = S3TransferManager.builder()
                .s3ClientConfiguration(cfg -> cfg.credentialsProvider(credentialsProvider)
                        .region(region)
                        .targetThroughputInGbps(5.0)
                        .minimumPartSizeInBytes(miniPartSizeMB * MB))
                .build();
        FileDownload fileDownload = s3tm.downloadFile(b -> b.getObjectRequest(
                q -> q.bucket(bucketName).key(objectKey))
                .destination(Paths.get(localPath))
                .overrideConfiguration(cfg -> cfg.addListener(LoggingTransferListener.create())));
        CompletedFileDownload result = fileDownload.completionFuture().join();
        return result.response().acceptRanges();
    }
    @Override
    public String putS3ObjectMultiPart(String bucketName, String objectKey, int miniPartSizeMB, String objectPath) {
        //high level, truly asynchronous, non-blocking API
        S3TransferManager s3tm = S3TransferManager.builder()
                .s3ClientConfiguration(cfg -> cfg.credentialsProvider(credentialsProvider)
                        .region(region)
                        .targetThroughputInGbps(5.0)
                        .minimumPartSizeInBytes(miniPartSizeMB * MB))
                .build();
        FileUpload upload = s3tm.uploadFile(upreq -> upreq.source(Paths.get(objectPath))
                                                            .putObjectRequest(putReq -> putReq.bucket(bucketName).key(objectKey))
                                                            .overrideConfiguration(cfg -> cfg.addListener(LoggingTransferListener.create())));
        CompletedFileUpload result = upload.completionFuture().join();
        System.out.printf("Object was successfully uploaded using the Transfer Manager");
        s3tm.close();
        return result.response().eTag();
    }

    @Override
    public String listBucketObjects (String bucketName, String prefix, String delimiter) {
        S3Client s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();

        //construct request
        ListObjectsRequest request = ListObjectsRequest.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .delimiter(delimiter)
                .build();

        //construct response
        ListObjectsResponse response = s3Client.listObjects(request);
        System.out.println("CommonPrefix: " + response.commonPrefixes());
        List<S3Object> objectList = response.contents();
        for (S3Object object : objectList) {
            String out = String.format("Name: %s, Size: %d KB, Owner: %s",object.key(), calKb(object.size()), object.owner().displayName());
            System.out.println(out);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(objectList);
    }

    /**
     * create S3Client
     * @return S3Client
     */
    private static S3Client getS3Client() {
        S3Client s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
        return s3Client;
    }

    private static byte[] getObjectFile(String objectPath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;
        try {
            File file = new File(objectPath);
            bytesArray = new byte[(int) file.length()];
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return bytesArray;
    }

    //convert bytes to kbs.
    private static long calKb(Long val) {
        return val/1024;
    }
}