package com.github.grimsa.infrastructure;

import com.github.grimsa.infrastructure.jsoup.LocalDocumentSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class TestData {
    private TestData() {
    }

    public static String fileAsString(String path) {
        return new String(fileAsBytes(path), StandardCharsets.UTF_8);
    }

    private static byte[] fileAsBytes(String path) {
        try {
            return Files.readAllBytes(path(path));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file at " + path, e);
        }
    }

    private static Path path(String fileName) {
        try {
            return Path.of(
                    Objects.requireNonNull(
                            LocalDocumentSource.class.getClassLoader().getResource(fileName),
                            "File not found: " + fileName
                    ).toURI()
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
