package org.example.service;

import java.util.Map;

/**
 * This service interface defines methods for reading statistics from files
 * and saving statistics to files.
 */
public interface FileOperationService {

    /**
     * Reads statistics from JSON files within a specified folder for a given attribute.
     *
     * @param folderPath The path to the folder containing JSON files.
     * @param attribute  The attribute to calculate statistics for.
     * @return A map containing attribute values and their corresponding counts.
     */
    Map<String, Integer> readFromFile(String folderPath, String attribute);

    /**
     * Saves statistics to an XML file based on the provided map of attribute counts.
     *
     * @param statistics A map containing attribute values and their corresponding counts.
     * @param attribute  The attribute being analyzed.
     */
    void saveToFile(Map<String, Integer> statistics, String attribute);
}
