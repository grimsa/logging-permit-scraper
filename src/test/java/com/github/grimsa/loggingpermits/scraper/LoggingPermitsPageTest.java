package com.github.grimsa.loggingpermits.scraper;

import com.github.grimsa.infrastructure.jsoup.LocalDocumentSource;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggingPermitsPageTest {
    @Test
    void getRegionalUnitOptions_mainPage_optionsParsedFromSelect() {
        Document page = new LocalDocumentSource("pages/2023-main-page.html").get();
        var loggingPermitsPage = new LoggingPermitsPage(page);

        var result = loggingPermitsPage.getRegionalUnitOptions();

        assertEquals(29, result.size());
    }
}
