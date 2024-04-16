package org.example.service.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.example.exception.CustomFileException;
import org.example.exception.ExecutorTimeoutException;
import org.example.lib.Inject;
import org.example.lib.Service;
import org.example.model.Item;
import org.example.model.Statistics;
import org.example.service.CalculationService;
import org.example.service.FileOperationService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.example.util.FileConstantsUtil.*;
import static org.example.util.SingletonObjectsUtil.*;

/**
 * Implementation of the FileOperationService interface for reading statistics from files
 * and saving statistics to files.
 */
@Service
public class FileOperationServiceImpl implements FileOperationService {
    private Map<String, Integer> statistics;
    private final XmlMapper xmlMapper;
    private final ExecutorService executor;
    @Inject
    private CalculationService calculateService;

    public FileOperationServiceImpl() {
        this.executor = getExecutorService();
        this.xmlMapper = getXmlMapper();
        statistics = new HashMap<>();
    }

    @Override
    public Map<String, Integer> readFromFile(String folderPath, String attribute) {
        getFilesFromFolder(folderPath).forEach(file -> executor.submit(() -> readFileByLine(file, attribute)));
        executor.shutdown();

        try {
            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                throw new RuntimeException("Executor did not terminate within the specified time limit.");
            }
        } catch (Exception e) {
            throw new ExecutorTimeoutException(e.getMessage());
        }
        return statistics;
    }

    @Override
    public void saveToFile(Map<String, Integer> statistics, String attribute) {
        List<Item> itemList = statistics.entrySet().stream()
                .map(entry -> new Item(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        if (!itemList.isEmpty()) {
            Statistics stats = new Statistics(itemList);

            try {
                xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);

                xmlMapper.writeValue(new File(String.format(
                        "%s%s%s%s", DEFAULT_PATH_FOR_RESULTS, FILE_NAME_PREFIX, attribute, XML_FILE_EXTENSION)), stats);
            } catch (IOException e) {
                throw new CustomFileException("An error occurred while saving statistics to file.", e);
            }
        }
    }

    /**
     * Reads a JSON file line by line, extracting attribute values and updating statistics.
     *
     * @param file      The JSON file to read.
     * @param attribute The attribute to calculate statistics for.
     */
    private void readFileByLine(File file, String attribute) {
        try (JsonParser parser = getJsonFactory().createParser(file)) {
            while (!parser.isClosed()) {
                JsonToken jsonToken = parser.nextToken();

                if (jsonToken == null) {
                    break;
                }

                if (JsonToken.START_OBJECT.equals(jsonToken)) {
                    while (!JsonToken.END_OBJECT.equals(parser.nextToken())) {
                        if (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
                            statistics = calculateService.calculateAttributes(parser, attribute, jsonToken);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new CustomFileException("An error occurred while reading values from the file.", e);
        }
    }

    /**
     * Retrieves a list of files from the specified folder path that have the JSON file extension.
     *
     * @param folderPath The path to the folder containing JSON files.
     * @return A list of File objects representing JSON files in the folder.
     * @throws CustomFileException If an error occurs while searching for files.
     */
    private static List<File> getFilesFromFolder(String folderPath) {
        List<File> files = new ArrayList<>();
        Path directory = Paths.get(folderPath);

        try (var stream = Files.walk(directory)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(JSON_FILE_EXTENSION))
                    .forEach(path -> files.add(path.toFile()));
        } catch (IOException e) {
            throw new CustomFileException("An error occurred while searching for files." +
                    " Check the path to the specified folder : " + folderPath, e);
        }
        return files;
    }
}
