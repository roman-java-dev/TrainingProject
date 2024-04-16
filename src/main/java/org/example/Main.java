package org.example;

import org.example.exception.CustomExceptionHandler;
import org.example.lib.Injector;
import org.example.service.StatisticsProcessorService;

public class Main {
    private static final Injector injector = Injector.getInstance("org.example");

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());

        StatisticsProcessorService service =
                (StatisticsProcessorService) injector.getInstance(StatisticsProcessorService.class);

        service.processStatistics();
    }
}
