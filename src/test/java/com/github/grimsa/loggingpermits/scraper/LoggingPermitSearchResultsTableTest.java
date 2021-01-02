package com.github.grimsa.loggingpermits.scraper;

import com.github.grimsa.infrastructure.jsoup.LocalDocumentSource;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggingPermitSearchResultsTableTest {
    @Test
    void parse_alytaus1_expectedEntries() {
        Document page = new LocalDocumentSource("pages/Alytaus-01.html").get();

        List<LoggingPermit> results = new LoggingPermitSearchResultsTable(page).parse();

        assertEquals(50, results.size());
        List<String> lines = results.stream()
                .map(LoggingPermit::columnValues)
                .map(columnValues -> String.join(" | ", columnValues))
                .collect(Collectors.toList());
        assertEquals(
                "A - 58 | Alytaus | Lazdijų rajono kontroliuojama teritorija | privati | Veisiejų miškų urėdija | Kapčiamiesčio girininkija | 146 | 12a,14b,15a,19 | 1.4 | 5940 | 0001 | 0134 | Plynas kirtimas | 2020-02-04 | 2020-12-31",
                lines.get(0)
        );
    }

    @Test
    void parse_lastPage() {
        Document page = new LocalDocumentSource("pages/Utenos-97.html").get();
        LoggingPermitSearchResultsTable table = new LoggingPermitSearchResultsTable(page);

        List<LoggingPermit> results = table.parse();

        assertEquals(17, results.size());
    }
}