package org.example.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is a utility class that provides singleton instances of commonly used objects.
 * It includes instances of ExecutorService, JsonFactory, XmlMapper, and Scanner.
 */
public class SingletonObjectsUtil {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(8);
    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final XmlMapper XML_MAPPER = new XmlMapper();
    private static final Scanner SCANNER = new Scanner(System.in);

    private SingletonObjectsUtil() {}

    public static ExecutorService getExecutorService() {
        return EXECUTOR_SERVICE;
    }

    public static JsonFactory getJsonFactory() {
        return JSON_FACTORY;
    }

    public static XmlMapper getXmlMapper() {
        return XML_MAPPER;
    }

    public static Scanner getScanner() {
        return SCANNER;
    }
}
