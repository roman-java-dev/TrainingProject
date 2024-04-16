package org.example.service.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculationServiceImplTest {
    @InjectMocks
    private CalculationServiceImpl calculationService;
    @Mock
    private JsonParser mockParser;

    @Test
    @DisplayName("givenCalculateAttributes_whenValidInput_thenReturnResult")
    public void calculateAttributes_validInput_ok() throws IOException {
        String attribute = "attribute";
        String attributeValue = "value";
        when(mockParser.getCurrentName()).thenReturn(attribute);
        when(mockParser.nextToken()).thenReturn(JsonToken.FIELD_NAME, JsonToken.VALUE_STRING);
        when(mockParser.getValueAsString()).thenReturn(attributeValue);

        Map<String, Integer> result = calculationService.calculateAttributes(mockParser, attribute, JsonToken.VALUE_STRING);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(attributeValue));
        assertEquals(1, result.get(attributeValue));
    }

    @Test
    @DisplayName("givenCalculateAttributes_whenInvalidFieldName_thenReturnEmptyResult")
    public void calculateAttributes_invalidFieldName_notOk() throws IOException {
        String attribute = "attribute";
        String invalidFieldName = "invalid";
        when(mockParser.getCurrentName()).thenReturn(invalidFieldName);

        Map<String, Integer> result = calculationService.calculateAttributes(mockParser, attribute, JsonToken.VALUE_STRING);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("givenCalculateAttributes_whenInvalidAttribute_thenGetException")
    public void calculateAttributes_invalidAttribute_notOk() throws IOException {
        String attribute = "attribute";
        IOException expectedException = new IOException("Mocked IOException");
        when(mockParser.getCurrentName()).thenThrow(expectedException);

        IOException thrownException = assertThrows(IOException.class,
                () -> calculationService.calculateAttributes(mockParser, attribute, JsonToken.VALUE_STRING));

        assertEquals(expectedException.getMessage(), thrownException.getMessage());
    }

    @Test
    @DisplayName("givenCalculateAttributes_whenValidInputWithDescriptionAttribute_thenReturnResult")
    public void calculateAttributes_validInputWithDescriptionAttribute_ok() throws IOException {
        String attribute = "description";
        String attributeValue = "value1, value2, value3";
        when(mockParser.getCurrentName()).thenReturn(attribute);
        when(mockParser.nextToken()).thenReturn(JsonToken.FIELD_NAME, JsonToken.VALUE_STRING);
        when(mockParser.getValueAsString()).thenReturn(attributeValue);

        Map<String, Integer> result = calculationService.calculateAttributes(mockParser, attribute, JsonToken.VALUE_STRING);

        assertEquals(3, result.size());
        Arrays.stream(attributeValue.split(","))
                .map(String::trim)
                .forEach(value -> {
                    assertTrue(result.containsKey(value));
                    assertEquals(1, result.get(value));
                });
    }
}
