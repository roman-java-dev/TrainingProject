package org.example.service.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.example.lib.Service;
import org.example.service.CalculationService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the CalculationService interface for performing attribute calculations.
 */
@Service
public class CalculationServiceImpl implements CalculationService {
    private final static String ATTRIBUTE_WITH_SEVERAL_VALUES = "description";
    private final Map<String, Integer> statistics;

    public CalculationServiceImpl() {
        statistics = new ConcurrentHashMap<>();
    }

    @Override
    public Map<String, Integer> calculateAttributes(JsonParser parser, String attribute, JsonToken jsonToken) throws IOException {
        String fieldName = parser.getCurrentName();
        jsonToken = parser.nextToken();

        if (fieldName.equals(attribute)) {
           if (attribute.equals(ATTRIBUTE_WITH_SEVERAL_VALUES)) {
               divideAttribute(parser);
           } else {
               String attributeValue = parser.getValueAsString();
               statistics.merge(attributeValue, 1, Integer::sum);
           }
        }
        return sortByDescendingValues();
    }

    /**
     * Divides the attribute value obtained from the JSON parser by comma and updates
     * the statistics map with the count of each value.
     *
     * @param parser The JSON parser used to extract attribute values.
     * @throws IOException If an I/O error occurs while parsing the JSON.
     */
    private void divideAttribute(JsonParser parser) throws IOException {
        String attributeValue = parser.getValueAsString();
        Arrays.stream(attributeValue.split(","))
                .map(String::trim)
                .forEach(value -> statistics.merge(value, 1, Integer::sum));
    }

    /**
     * Sorts the statistics map by values in descending order.
     *
     * @return A sorted map with entries sorted by values in descending order.
     */
    private Map<String, Integer> sortByDescendingValues() {
        return statistics.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Map::putAll);
    }
}
