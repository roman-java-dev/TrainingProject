package org.example.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.Map;

/**
 * This service interface defines methods for performing calculations on attributes
 * extracted from JSON data.
 */
public interface CalculationService {

    /**
     * Calculates the occurrences of a specific attribute from JSON data.
     *
     * @param parser      The JSON parser object.
     * @param attribute   The attribute to calculate occurrences for.
     * @param jsonToken   The current JSON token being processed.
     * @return A map containing the calculated statistics, sorted by values in descending order.
     * @throws IOException If an I/O error occurs during JSON parsing.
     */
    Map<String, Integer> calculateAttributes(JsonParser parser, String attribute, JsonToken jsonToken) throws IOException;
}
