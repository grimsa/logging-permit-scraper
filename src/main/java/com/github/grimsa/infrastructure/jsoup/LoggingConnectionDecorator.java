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
        log.info("Request: {} {}, data: {}, cookies: {}", request().method(), request().url(), request().data(), request().cookies());
        Connection.Response response = super.execute();
        log.info("Response headers: {}, body: {}", response.headers(), response.body().trim());
        return response;
    }
}
