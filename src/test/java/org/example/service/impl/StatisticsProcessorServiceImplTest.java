package org.example.service.impl;

import org.example.service.FileOperationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsProcessorServiceImplTest {
    @Mock
    private FileOperationService fileOperationService;
    @InjectMocks
    private StatisticsProcessorServiceImpl statisticsProcessorService;
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpStreams() {
        System.setErr(new PrintStream(errContent));
    }

    @Test
    @DisplayName("givenProcessStatistics_whenInvalidFolderPath_thenGetErrorMessage")
    public void processStatistics_invalidFolderPath_notOk() throws NoSuchFieldException, IllegalAccessException {
        String invalidFolderPath = "invalid/path" + "\n" + "src/test/resources/orders" + "\n" + "date";
        setNeededScanner(invalidFolderPath);

        statisticsProcessorService.processStatistics();

        assertEquals("Please check the path to the folder is entered correctly", errContent.toString().trim());
        verify(fileOperationService).readFromFile(anyString(), anyString());
        verify(fileOperationService).saveToFile(anyMap(), anyString());
    }

    @Test
    @DisplayName("givenProcessStatistics_whenInvalidAttribute_thenGetErrorMessage")
    public void processStatistics_invalidAttribute_notOk() throws NoSuchFieldException, IllegalAccessException {
        String invalidAttribute = "src/test/resources/orders" + "\n" + "badAttribute" + "\n" + "customer";
        setNeededScanner(invalidAttribute);

        statisticsProcessorService.processStatistics();

        assertEquals("The entered attribute doesn't match the fields of the object. Please enter another one",
                errContent.toString().trim());
        verify(fileOperationService).readFromFile(anyString(), anyString());
        verify(fileOperationService).saveToFile(anyMap(), anyString());
    }

    @Test
    @DisplayName("givenProcessStatistics_whenValidInput_thenSuccess")
    public void processStatistics_validInput_ok() throws NoSuchFieldException, IllegalAccessException {
        String validInput = "src/test/resources/orders" + "\n" + "customer";
        setNeededScanner(validInput);

        statisticsProcessorService.processStatistics();

        assertTrue(errContent.toString().isEmpty());
        verify(fileOperationService).readFromFile(anyString(), anyString());
        verify(fileOperationService).saveToFile(anyMap(), anyString());
    }

    private void setNeededScanner(String input) throws NoSuchFieldException, IllegalAccessException {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);

        Field scannerField = StatisticsProcessorServiceImpl.class.getDeclaredField("scanner");
        scannerField.setAccessible(true);
        scannerField.set(statisticsProcessorService, scanner);
    }
}
