package com.github.grimsa.loggingpermits.scraper;

import com.github.grimsa.infrastructure.jsoup.LocalDocumentSource;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggingPermitSearchResultsTableTest {
    @Test
    void parse_alytaus1_expectedEntries() {
        Document page = new LocalDocumentSource("pages/2022-Alytaus-01.html").get();

        List<LoggingPermit> results = new LoggingPermitSearchResultsTable(page).parse();

        assertEquals(50, results.size());
        List<String> lines = results.stream()
                .map(LoggingPermit::columnValues)
                .map(columnValues -> String.join(" | ", columnValues))
                .toList();
        assertEquals(
                "A - 58 | Alytaus | Lazdijų rajono kontroliuojama teritorija | privati | Veisiejų miškų urėdija | Kapčiamiesčio girininkija | 146 | 12a,14b,15a,19 | 1.4 | 5940 | 0001 | 0134 | Plynas kirtimas | 2020-02-04 | 2020-12-31",
                lines.get(0)
        );
    }

    @Test
    void parse_alytaus2_expectedEntries() {
        Document page = new LocalDocumentSource("pages/2022-Alytaus-02.html").get();

        List<LoggingPermit> results = new LoggingPermitSearchResultsTable(page).parse();

        assertEquals(50, results.size());
        List<String> lines = results.stream()
                .map(LoggingPermit::columnValues)
                .map(columnValues -> String.join(" | ", columnValues))
                .toList();
        assertEquals(
                "221 - 87 | Alytaus | Varėnos rajono kontroliuojama teritorija | valstybinė | Varėnos miškų urėdija | Rudnios girininkija | 706 | 4e | 1.6 | 0 | 0 | 0 | Plynas kirtimas | 2021-06-22 | 2021-12-31",
                lines.get(0)
        );
    }

    @Test
    void parse_lastPage() {
        Document page = new LocalDocumentSource("pages/2022-Utenos-97.html").get();
        LoggingPermitSearchResultsTable table = new LoggingPermitSearchResultsTable(page);

        List<LoggingPermit> results = table.parse();

        assertEquals(17, results.size());
    }

    @Test
    void parse_anyksciu1_expectedEntries() {
        Document page = new LocalDocumentSource("pages/2023-AnyksciuRP-01.html").get();

        List<LoggingPermit> results = new LoggingPermitSearchResultsTable(page).parse();

        assertEquals(50, results.size());
        List<String> lines = results.stream()
                .map(LoggingPermit::columnValues)
                .map(columnValues -> String.join(" | ", columnValues))
                .toList();
        assertEquals(
                "12300001 | Utenos | Anykščių rajono kontroliuojama teritorija | privati | Anykščių RP | Mickūnų girininkija | 1477 | 20 | 0.7 | 3426 | 0005 | 0020 | Plynas kirtimas | 2023-01-02 | 2023-12-31",
                lines.get(0)
        );
    }
}
