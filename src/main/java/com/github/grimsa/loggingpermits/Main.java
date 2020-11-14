package com.github.grimsa.loggingpermits;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String... args) throws IOException {
        LoggingPermitsPage page = new LoggingPermitsPage();
        List<LoggingPermit> allPermits = page.getRegionOptions().stream()
                .map(page::retrieveLoggingPermits)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        // Print results
        allPermits.stream()
                .map(LoggingPermit::asTextLine)
                .forEachOrdered(System.out::println);
    }
}
