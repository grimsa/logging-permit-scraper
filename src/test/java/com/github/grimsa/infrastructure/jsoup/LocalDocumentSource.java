package com.github.grimsa.infrastructure.jsoup;

import com.github.grimsa.infrastructure.TestData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.function.Supplier;

public class LocalDocumentSource implements Supplier<Document> {
    private final String fileContent;

    public LocalDocumentSource(String fileName) {
        this.fileContent = TestData.fileAsString(fileName);
    }

    @Override
    public Document get() {
        return Jsoup.parse(fileContent);
    }
}
