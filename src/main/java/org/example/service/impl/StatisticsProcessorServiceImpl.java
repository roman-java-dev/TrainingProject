package org.example.service.impl;

import org.example.lib.Inject;
import org.example.lib.Service;
import org.example.model.Order;
import org.example.service.FileOperationService;
import org.example.service.StatisticsProcessorService;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Scanner;

import static org.example.util.SingletonObjectsUtil.getScanner;

/**
 * Implementation of the StatisticsProcessorService interface for processing statistics.
 */
@Service
public class StatisticsProcessorServiceImpl implements StatisticsProcessorService {
    private final Scanner scanner;
    @Inject
    private FileOperationService operationService;

    public StatisticsProcessorServiceImpl() {
        this.scanner = getScanner();
    }

    public void processStatistics() {
        String folderPath = readFolderPath(scanner);
        String attribute = readAttribute(scanner);

        Map<String, Integer> statistics = operationService.readFromFile(folderPath, attribute);

        operationService.saveToFile(statistics, attribute);

        scanner.close();
    }

    /**
     * Reads and validates a folder path entered by the user via the provided Scanner object.
     *
     * @param scanner The Scanner object used to read input from the user.
     * @return The validated folder path entered by the user.
     */
    private static String readFolderPath(Scanner scanner) {
        System.out.println("Enter folder path:");
        String folderPath = scanner.nextLine();

        while (!isFolderPathExists(folderPath)) {
            System.err.println("Please check the path to the folder is entered correctly");
            folderPath = scanner.nextLine();
        }

        return folderPath;
    }

    /**
     * Reads and validates an attribute name entered by the user via the provided Scanner object.
     *
     * @param scanner The Scanner object used to read input from the user.
     * @return The validated attribute name entered by the user.
     */
    private static String readAttribute(Scanner scanner) {
        System.out.println("Enter attribute:");
        String attribute = scanner.nextLine();

        while (!isAttributeValid(attribute)) {
            System.err.println("The entered attribute doesn't match the fields of the object. Please enter another one");
            attribute = scanner.nextLine();
        }

        return attribute;
    }

    /**
     * Checks if the specified folder path exists and represents a directory.
     *
     * @param folderPath The folder path to check.
     * @return True if the folder path exists and is a directory, otherwise false.
     */
    private static boolean isFolderPathExists(String folderPath) {
        File folder = new File(folderPath);
        return folder.exists() && folder.isDirectory();
    }

    /**
     * Checks if the specified attribute name matches any field name of the Order class.
     *
     * @param attributeName The attribute name to validate.
     * @return True if the attribute name matches a field name of the Order class, otherwise false.
     */
    private static boolean isAttributeValid(String attributeName) {
        Class<?> orderClass = Order.class;
        Field[] fields = orderClass.getDeclaredFields();

        for (Field field : fields) {
            if (field.getName().equals(attributeName)) {
                return true;
            }
        }
        return false;
    }
}
