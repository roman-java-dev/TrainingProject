package org.example.service.impl;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.SneakyThrows;
import org.example.exception.CustomFileException;
import org.example.exception.ExecutorTimeoutException;
import org.example.model.Statistics;
import org.example.service.CalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.example.util.FileConstantsUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileOperationServiceImplTest {
    private final String TEST_FOLDER_PATH = "src/test/resources/orders";
    private final String BAD_FOLDER_PATH = "bad/path";
    private final String TEST_ATTRIBUTE = "customer";
    @InjectMocks
    private FileOperationServiceImpl fileOperationService;
    @Mock
    private CalculationService calculationService;
    @Mock
    private ExecutorService executorService;
    @Mock
    private XmlMapper xmlMapper;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        Field xmlMapperField = FileOperationServiceImpl.class.getDeclaredField("xmlMapper");
        xmlMapperField.setAccessible(true);
        xmlMapperField.set(fileOperationService, xmlMapper);

        Field executorField = FileOperationServiceImpl.class.getDeclaredField("executor");
        executorField.setAccessible(true);
        executorField.set(fileOperationService, executorService);
    }

    @Test
    @DisplayName("givenReadStatisticsFromFile_whenValidInput_thenSuccess")
    public void readStatisticsFromFile_validInput_ok() throws IOException, InterruptedException {
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        Map<String, Integer> expectedStatistics = new HashMap<>();
        expectedStatistics.put("value1", 1);
        expectedStatistics.put("value2", 2);

        when(executorService.awaitTermination(anyLong(), any())).thenReturn(true);
        when(calculationService.calculateAttributes(any(), any(), any())).thenReturn(expectedStatistics);
        when(executorService.submit(captor.capture())).thenAnswer(invocation -> {
            captor.getValue().run();
            return mock(Future.class);
        });

        Map<String, Integer> result = fileOperationService.readFromFile(TEST_FOLDER_PATH, TEST_ATTRIBUTE);

        assertEquals(expectedStatistics, result);
    }

    @Test
    @DisplayName("givenReadStatisticsFromFile_whenExecutorDidNotTerminate_thenGetTimeoutException")
    public void readStatisticsFromFile_TimeoutException() throws InterruptedException {
        when(executorService.awaitTermination(anyLong(), any())).thenReturn(false);

        assertThrows(ExecutorTimeoutException.class, () ->
                fileOperationService.readFromFile(TEST_FOLDER_PATH, TEST_ATTRIBUTE));
    }

    @Test
    @DisplayName("givenReadStatisticsFromFile_whenInvalidInput_thenGetException")
    public void readStatisticsFromFile_invalidInput_notOk() {
        assertThrows(CustomFileException.class, () ->
                fileOperationService.readFromFile(BAD_FOLDER_PATH, TEST_ATTRIBUTE));
    }

    @Test
    @DisplayName("givenSaveStatisticsToFile_whenValidInput_thenSuccess")
    public void saveStatisticsToFile_validInput_ok() throws IOException {
        Map<String, Integer> statistics = new HashMap<>();
        statistics.put("value1", 1);

        doNothing().when(xmlMapper).writeValue(any(File.class), any(Statistics.class));
        fileOperationService.saveToFile(statistics, TEST_ATTRIBUTE);

        verify(xmlMapper, times(1)).writeValue(
                eq(new File(DEFAULT_PATH_FOR_RESULTS + FILE_NAME_PREFIX + TEST_ATTRIBUTE + XML_FILE_EXTENSION)),
                any(Statistics.class)
        );
    }

    @Test
    @DisplayName("givenSaveStatisticsToFile_whenEmptyStatistics_thenResultNotCreatedInFolder")
    public void saveStatisticsToFile_emptyStatistics() throws IOException {
        Map<String, Integer> emptyStatistics = new HashMap<>();

        assertDoesNotThrow(() -> fileOperationService.saveToFile(emptyStatistics, "empty_attribute"));

        verify(xmlMapper, times(0)).writeValue(
                eq(new File(DEFAULT_PATH_FOR_RESULTS + FILE_NAME_PREFIX + TEST_ATTRIBUTE + XML_FILE_EXTENSION)),
                any(Statistics.class)
        );
    }

    @Test
    @DisplayName("givenSaveStatisticsToFile_whenMapperWriteValue_thenGetException")
    public void saveStatisticsToFile_validInput_mapperWriteFail_notOk() throws IOException {
        Map<String, Integer> statistics = new HashMap<>();
        statistics.put("value1", 1);
        statistics.put("value2", 2);

        when(xmlMapper.enable(SerializationFeature.INDENT_OUTPUT)).thenReturn(xmlMapper);
        doThrow(IOException.class).when(xmlMapper).writeValue(any(File.class), any(Statistics.class));

        assertThrows(CustomFileException.class, () ->
                fileOperationService.saveToFile(statistics, TEST_ATTRIBUTE));
    }
}
