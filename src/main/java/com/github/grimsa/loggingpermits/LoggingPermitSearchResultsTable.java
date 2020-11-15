package com.github.grimsa.loggingpermits;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.function.Predicate.not;

class LoggingPermitSearchResultsTable {
    private final Element table;

    public LoggingPermitSearchResultsTable(Document document) {
        this.table = Objects.requireNonNull(
                document.getElementById("GridView2"),
                () -> "Search results table not found in document " + document
        );
    }

    public List<LoggingPermit> parse() {
        return table.select(":root > tbody > tr").stream()
                .map(tr -> tr.select(":root > td"))
                .filter(not(List::isEmpty))
                .filter(tds -> tds.size() > 1)
                .map(HtmlLoggingPermitRow::new)
                .collect(Collectors.toList());
    }

    private static class HtmlLoggingPermitRow implements LoggingPermit {
        private static final Logger log = LoggerFactory.getLogger(HtmlLoggingPermitRow.class);
        private static final List<String> COLUMN_NAMES = List.of(
                "Leidimo serija ir nr.",
                "Regionas",
                "Rajonas",
                "Nuosavybės forma",
                "Urėdija",
                "Girininkija",
                "Kvartalas",
                "Sklypai",
                "Plotas",
                "Kad. vietovė",
                "Kad. blokas",
                "Kad. nr.",
                "Kirtimo rūšis",
                "Galiojimo pradžia",
                "Galiojimo pabaiga"
        );
        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("uuuu/M/d");
        private final List<String> columnValues;

        HtmlLoggingPermitRow(Elements tds) {
            this.columnValues = tds.stream()
                    .map(Element::text)
                    .collect(Collectors.toList());
        }

        @Override
        public List<String> columnNames() {
            return COLUMN_NAMES;
        }

        @Override
        public List<String> columnValues() {
            return IntStream.range(0, COLUMN_NAMES.size())
                    .mapToObj(index -> transformColumnValue(COLUMN_NAMES.get(index), columnValues.get(index)))
                    .collect(Collectors.toList());
        }

        private String transformColumnValue(String columnName, String value) {
            if (!Set.of("Galiojimo pradžia", "Galiojimo pabaiga").contains(columnName)) {
                return value;
            }
            try {
                return LocalDate.parse(value, DATE_FORMAT).toString();
            } catch (DateTimeParseException e) {
                log.warn("Failed to parse date {} on record {}", value, columnValues);
                return value;
            }
        }
    }
}
