package com.github.grimsa.loggingpermits;

import com.github.grimsa.infrastructure.jsoup.LocalDocumentSource;
import com.github.grimsa.loggingpermits.LoggingPermitsPage.PagingInfo;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PagingInfoTest {
    @Test
    void pagingInfo_secondToLastPage() {
        Document page = new LocalDocumentSource("pages/Utenos-96.html").get();
        PagingInfo pagingInfo = new PagingInfo(page);

        assertEquals(97, pagingInfo.totalPages());
        assertEquals(96, pagingInfo.currentPage());
        assertEquals(97, pagingInfo.nextPageNumber().get());
    }

    @Test
    void pagingInfo_lastPage() {
        Document page = new LocalDocumentSource("pages/Utenos-97.html").get();
        PagingInfo pagingInfo = new PagingInfo(page);

        assertEquals(97, pagingInfo.totalPages());
        assertEquals(97, pagingInfo.currentPage());
        assertTrue(pagingInfo.nextPageNumber().isEmpty());
    }
}