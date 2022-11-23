package com.aws.demo.s3;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeWatch {
    private LocalDateTime start = null;

    public void start () {
        start = LocalDateTime.now();
        System.out.printf("Start at : " + start + "\n");
    }

    public void end () {
        LocalDateTime end = LocalDateTime.now();
        System.out.printf("End at : " + end + "\n");
        System.out.println("Time expended: " + ChronoUnit.SECONDS.between(start ,end) + " S \n");
    }
}
