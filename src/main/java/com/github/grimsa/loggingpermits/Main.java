package com.github.grimsa.loggingpermits;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.function.Failable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws IOException {
        System.out.println(Charset.defaultCharset().name());
        boolean thisYearOnly = false;
        LoggingPermitsPage page = new LoggingPermitsPage();
        List<LoggingPermit> allPermits = page.getRegionOptions().stream()
                .map(region -> {
                    List<LoggingPermit> permitsForRegion = page.retrieveLoggingPermits(region, thisYearOnly);
                    log.info("Found {} permits for region {}", permitsForRegion.size(), region);
                    return permitsForRegion;
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());
        log.info("Retrieved results: " + allPermits.size());
        writePermitsToFile("data/leidimai.csv", allPermits);
        writeReadmeToFile("data/README.md", allPermits.size());
    }

    private static void writePermitsToFile(String fileName, List<LoggingPermit> allPermits) throws IOException {
        try (FileWriter out = new FileWriter(fileName, StandardCharsets.UTF_8, false)) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(allPermits.get(0).columnNames().toArray(String[]::new));
            try (CSVPrinter printer = new CSVPrinter(out, csvFormat)) {
                Failable.stream(allPermits.stream())
                        .map(LoggingPermit::columnValues)
                        .forEach(printer::printRecord);
            }
        }
    }

    private static void writeReadmeToFile(String fileName, int numberOfPermits) throws IOException {
        try (FileWriter out = new FileWriter(fileName, StandardCharsets.UTF_8, false)) {
            out.write("""
                    Duomenys atnaujinti: %s
                    Leidimų skaičius: %s
                    """.formatted(Instant.now().toString(), numberOfPermits)
            );
        }
    }
}
