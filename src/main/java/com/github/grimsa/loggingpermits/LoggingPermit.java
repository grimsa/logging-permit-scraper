package com.github.grimsa.loggingpermits;

import java.util.List;

public interface LoggingPermit {
    List<String> columnNames();

    List<String> columnValues();
}
