package com.github.grimsa.loggingpermits.splitter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.function.Failable;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ScrapedDataSplitter {
    private static final Collator collator = Collator.getInstance(new Locale("lt", "LT"));

    public static void main(String... args) throws IOException {
        new ScrapedDataSplitter().readPermitsFromFile("data/leidimai.csv");
    }

    private void readPermitsFromFile(String fileName) throws IOException {
        Reader in = new FileReader(fileName, StandardCharsets.UTF_8);
        Map<String, List<CSVRecord>> recordsByFile = StreamSupport.stream(CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in).spliterator(), false)
                .collect(Collectors.groupingBy(this::fileDiscriminator));
        recordsByFile.forEach(this::writeToFile);
        updateReadme(recordsByFile);
    }

    private String fileDiscriminator(CSVRecord record) {
        int year = LocalDate.parse(record.get("Galiojimo pradžia")).getYear();
        return Range.between(2019, 2021).contains(year)
                ? Integer.toString(year)
                : "kiti";
    }

    private void writeToFile(String year, List<CSVRecord> records) {
        List<String> headerNames = records.get(0).getParser().getHeaderNames();
        try (FileWriter out = new FileWriter("data/leidimai-" + year + ".csv", StandardCharsets.UTF_8, false)) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(headerNames.toArray(String[]::new));
            try (CSVPrinter printer = new CSVPrinter(out, csvFormat)) {
                records.stream()
                        .sorted(
                                Comparator.comparing((CSVRecord record) -> record.get("Regionas"), collator)
                                        .thenComparing(record -> record.get("Rajonas"), collator)
                                        .thenComparing(record -> record.get("Urėdija"), collator)
                                        .thenComparing(record -> record.get("Girininkija"), collator)
                                        .thenComparing(record -> record.get("Leidimo serija ir nr."))
                                        .thenComparing(record -> record.get("Kvartalas"))
                                        .thenComparing(record -> record.get("Sklypai"))
                                        .thenComparing(record -> record.get("Plotas"))
                        )
                        .forEach(Failable.asConsumer(printer::printRecord));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void updateReadme(Map<String, List<CSVRecord>> permitsByYear) throws IOException {
        try (FileWriter out = new FileWriter("data/README.md", StandardCharsets.UTF_8, false)) {
            out.write("""
                    | Metai | Leidimų skaičius |
                    |-------| ---------------- |
                    """);
            Failable.stream(permitsByYear.keySet().stream().sorted())
                    .forEach(year -> out.write("| " + year + " | " + permitsByYear.get(year).size() + " |\n"));
            out.write("**Duomenys atnaujinti:** " + DateTimeFormatter.ISO_DATE_TIME.format(Instant.now()));
        }
    }
}
