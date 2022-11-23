package com.aws.demo.s3;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class S3ServiceImplTest {
    S3ServiceImpl s3ServiceImpl = new S3ServiceImpl();
    String objectDir = "/Users/dengqb/Documents/test/sample-photos/";
    //String fileName = "employee-1.png";
    //big file size = 775.9 MB
    String fileName = "ideaIC-2021.3.2.dmg";

    //big file size = 2.62 GB
    //String fileName = "file-2.62-GB.ova";

    String bucketName = "demo-dev-c-001";
    String objectKey = "demo/java/" + fileName;
    String ObjectPath = objectDir + fileName;

    @Test
    void testPutObject_single() {
        IS3Service s3Service = s3ServiceImpl;
        TimeWatch timeWatch = new TimeWatch();
        timeWatch.start();
        String response = s3Service.putS3Object(bucketName, objectKey + "-single", ObjectPath);
        timeWatch.end();
        System.out.println(response);
    }

    @Test
    void putS3ObjectMultiPart_miniPartSize16MB() {
        IS3Service s3Service = s3ServiceImpl;
        TimeWatch timeWatch = new TimeWatch();
        timeWatch.start();
        String response = s3Service.putS3ObjectMultiPart(bucketName, objectKey + "-multi-16MB", 16, ObjectPath);
        timeWatch.end();
        System.out.println(response);
    }

    @Test
    void putS3ObjectMultiPart_miniPartSize64MB() {
        IS3Service s3Service = s3ServiceImpl;
        TimeWatch timeWatch = new TimeWatch();
        timeWatch.start();
        String response = s3Service.putS3ObjectMultiPart(bucketName, objectKey + "-multi-64MB", 64, ObjectPath);
        timeWatch.end();
        System.out.println(response);
    }

    @Test
    void listBucketObjects() {
        IS3Service s3Service = s3ServiceImpl;
        TimeWatch timeWatch = new TimeWatch();
        timeWatch.start();
        String response = s3Service.listBucketObjects(bucketName, "demo/java/", "/");
        timeWatch.end();
        System.out.println(response);
    }

    @Test
    void getS3Object_withRange() {
        IS3Service s3Service = s3ServiceImpl;
        TimeWatch timeWatch = new TimeWatch();
        timeWatch.start();
        //String response = s3Service.getS3ObjectBytes(bucketName, "demo/java/Jabra Elite 75t.pdf", objectDir+"download/s3.pdf");
        String response = s3Service.getS3ObjectBytes(bucketName, "demo/java/employee-1.png", "bytes=0-50240", objectDir+"download/s3.png");
        timeWatch.end();
        System.out.println(response);
    }

    @Test
    void getS3Object_noRange () {
        IS3Service s3Service = s3ServiceImpl;
        TimeWatch timeWatch = new TimeWatch();
        timeWatch.start();
        //String response = s3Service.getS3ObjectBytes(bucketName, "demo/java/Jabra Elite 75t.pdf", objectDir+"download/s3.pdf");
        String response = s3Service.getS3ObjectBytes(bucketName, objectKey + "-multi-16MB", "", objectDir+"download/bigFile-single.dmg");
        timeWatch.end();
        System.out.println(response);
    }

    @Test
    void getS3ObjectMultiPart_miniPartSize8MB() {
        IS3Service s3Service = s3ServiceImpl;
        TimeWatch timeWatch = new TimeWatch();
        timeWatch.start();
        String response = s3Service.getS3ObjectMultiPart(bucketName, objectKey + "-multi-8MB", 8,objectDir+"download/bigFile-multi-8MB.dmg");
        timeWatch.end();
        System.out.println(response);
    }

    @Test
    void getS3ObjectMultiPart_miniPartSize16MB() {
        IS3Service s3Service = s3ServiceImpl;
        TimeWatch timeWatch = new TimeWatch();
        timeWatch.start();
        String response = s3Service.getS3ObjectMultiPart(bucketName, objectKey + "-multi-16MB", 16,objectDir+"download/bigFile-multi-16MB.dmg");
        timeWatch.end();
        System.out.println(response);
    }

    @Test
    void getS3ObjectMultiPart_miniPartSize32MB() {
        IS3Service s3Service = s3ServiceImpl;
        TimeWatch timeWatch = new TimeWatch();
        timeWatch.start();
        String response = s3Service.getS3ObjectMultiPart(bucketName, objectKey + "-multi-32MB", 32,objectDir+"download/bigFile-multi-32MB.dmg");
        timeWatch.end();
        System.out.println(response);
    }

    @Test
    void diffTimeWithChronoUnit() throws InterruptedException {
        LocalDateTime start = LocalDateTime.now();
        System.out.printf("Start at : " + start + "\n");
        Thread.sleep(1000 * 63);
        LocalDateTime end = LocalDateTime.now();
        System.out.printf("End at : " + end + "\n");
        System.out.println("Time expended: " + ChronoUnit.SECONDS.between(start,end) + " S \n");
    }
}
