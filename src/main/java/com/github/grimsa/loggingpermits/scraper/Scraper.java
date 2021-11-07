package com.github.grimsa.loggingpermits.scraper;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.Failable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Collator;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;

public class Scraper {
    private static final Logger log = LoggerFactory.getLogger(Scraper.class);
    private static final Collator collator = Collator.getInstance(new Locale("lt", "LT"));
    private static final String README_FILE_NAME = "data/README.md";

    public static void main(String... args) throws IOException {
        boolean thisYearOnly = !EnumSet.of(JANUARY, DECEMBER).contains(LocalDate.now().getMonth());
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

        Map<String, List<LoggingPermit>> permitsByFilename = allPermits.stream()
                .collect(Collectors.groupingBy(LoggingPermit::year));
        permitsByFilename.forEach(Scraper::writePermitsToFile);
        updateReadme(permitsByFilename);
    }

    private static void writePermitsToFile(String year, List<LoggingPermit> allPermits) {
        try (FileWriter out = new FileWriter("data/leidimai-" + year + ".csv", StandardCharsets.UTF_8, false)) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(allPermits.get(0).columnNames().toArray(String[]::new)).build();
            try (CSVPrinter printer = new CSVPrinter(out, csvFormat)) {
                allPermits.stream()
                        .sorted(
                                Comparator.comparing((LoggingPermit loggingPermit) -> loggingPermit.getColumnValue("Regionas"), collator)
                                        .thenComparing(record -> record.getColumnValue("Rajonas"), collator)
                                        .thenComparing(record -> record.getColumnValue("Urėdija"), collator)
                                        .thenComparing(record -> record.getColumnValue("Girininkija"), collator)
                                        .thenComparing(record -> record.getColumnValue("Leidimo serija ir nr."))
                                        .thenComparing(record -> record.getColumnValue("Kvartalas"))
                                        .thenComparing(record -> record.getColumnValue("Sklypai"))
                                        .thenComparing(record -> record.getColumnValue("Plotas"))
                        )
                        .map(LoggingPermit::columnValues)
                        .forEach(Failable.asConsumer(printer::printRecord));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void updateReadme(Map<String, List<LoggingPermit>> permitsByYear) throws IOException {
        List<String> linesBeforeUpdate = Files.readAllLines(Paths.get(README_FILE_NAME));
        Map<String, Integer> countsByYear = linesBeforeUpdate.stream()
                .filter(line -> line.matches("\\|\\s*\\w+\\s*\\|\\s*\\d+\\s*\\|"))
                .map(line -> StringUtils.split(line, "|"))
                .collect(Collectors.toMap(
                        values -> values[0].strip(),
                        values -> Integer.parseInt(values[1].strip())
                ));
        permitsByYear.forEach((year, permits) -> countsByYear.put(year, permits.size()));

        try (FileWriter out = new FileWriter("data/README.md", StandardCharsets.UTF_8, false)) {
            out.write("""
                    | Metai | Leidimų skaičius |
                    |-------| ---------------- |
                    """);
            Failable.stream(countsByYear.keySet().stream().sorted())
                    .forEach(year -> out.write("| " + year + " | " + countsByYear.get(year) + " |\n"));
            out.write("\n**Duomenys atnaujinti:** " + Instant.now());
        }
    }
}
