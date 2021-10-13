package com.github.grimsa.infrastructure.jsoup;

import org.jsoup.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoggingConnectionDecorator extends AbstractConnectionDecorator {
    private static final Logger log = LoggerFactory.getLogger(LoggingConnectionDecorator.class);

    public LoggingConnectionDecorator(Connection connection) {
        super(connection);
    }

    @Override
    public Connection.Response execute() throws IOException {
        Connection.Response response = super.execute();
        log.debug("Request: {} {}, cookies {}, data: {}", request().method(), request().url(), request().cookies(), request().data());
        log.debug(response.cookies())
        log.trace("Response headers: {}", response.headers());
        return response;
    }
}
