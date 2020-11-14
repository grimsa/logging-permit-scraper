package com.github.grimsa.infrastructure.jsoup;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

public abstract class AbstractConnectionDecorator implements Connection {
    private final Connection delegate;

    public AbstractConnectionDecorator(Connection delegate) {
        this.delegate = delegate;
    }

    @Override
    public Connection url(URL url) {
        delegate.url(url);
        return this;
    }

    @Override
    public Connection url(String url) {
        delegate.url(url);
        return this;
    }

    @Override
    public Connection proxy(Proxy proxy) {
        delegate.proxy(proxy);
        return this;
    }

    @Override
    public Connection proxy(String host, int port) {
        delegate.proxy(host, port);
        return this;
    }

    @Override
    public Connection userAgent(String userAgent) {
        delegate.userAgent(userAgent);
        return this;
    }

    @Override
    public Connection timeout(int millis) {
        delegate.timeout(millis);
        return this;
    }

    @Override
    public Connection maxBodySize(int bytes) {
        delegate.maxBodySize(bytes);
        return this;
    }

    @Override
    public Connection referrer(String referrer) {
        delegate.referrer(referrer);
        return this;
    }

    @Override
    public Connection followRedirects(boolean followRedirects) {
        delegate.followRedirects(followRedirects);
        return this;
    }

    @Override
    public Connection method(Method method) {
        delegate.method(method);
        return this;
    }

    @Override
    public Connection ignoreHttpErrors(boolean ignoreHttpErrors) {
        delegate.ignoreHttpErrors(ignoreHttpErrors);
        return this;
    }

    @Override
    public Connection ignoreContentType(boolean ignoreContentType) {
        delegate.ignoreContentType(ignoreContentType);
        return this;
    }

    @Override
    public Connection sslSocketFactory(SSLSocketFactory sslSocketFactory) {
        delegate.sslSocketFactory(sslSocketFactory);
        return this;
    }

    @Override
    public Connection data(String key, String value) {
        delegate.data(key, value);
        return this;
    }

    @Override
    public Connection data(String key, String filename, InputStream inputStream) {
        delegate.data(key, filename, inputStream);
        return this;
    }

    @Override
    public Connection data(String key, String filename, InputStream inputStream, String contentType) {
        delegate.data(key, filename, inputStream, contentType);
        return this;
    }

    @Override
    public Connection data(Collection<KeyVal> data) {
        delegate.data(data);
        return this;
    }

    @Override
    public Connection data(Map<String, String> data) {
        delegate.data(data);
        return this;
    }

    @Override
    public Connection data(String... keyvals) {
        delegate.data(keyvals);
        return this;
    }

    @Override
    public KeyVal data(String key) {
        return delegate.data(key);
    }

    @Override
    public Connection requestBody(String body) {
        delegate.requestBody(body);
        return this;
    }

    @Override
    public Connection header(String name, String value) {
        delegate.header(name, value);
        return this;
    }

    @Override
    public Connection headers(Map<String, String> headers) {
        delegate.headers(headers);
        return this;
    }

    @Override
    public Connection cookie(String name, String value) {
        delegate.cookie(name, value);
        return this;
    }

    @Override
    public Connection cookies(Map<String, String> cookies) {
        delegate.cookies(cookies);
        return this;
    }

    @Override
    public Connection parser(Parser parser) {
        delegate.parser(parser);
        return this;
    }

    @Override
    public Connection postDataCharset(String charset) {
        delegate.postDataCharset(charset);
        return this;
    }

    @Override
    public Document get() throws IOException {
        return delegate.get();
    }

    @Override
    public Document post() throws IOException {
        return delegate.post();
    }

    @Override
    public Response execute() throws IOException {
        return delegate.execute();
    }

    @Override
    public Request request() {
        return delegate.request();
    }

    @Override
    public Connection request(Request request) {
        delegate.request(request);
        return this;
    }

    @Override
    public Response response() {
        return delegate.response();
    }

    @Override
    public Connection response(Response response) {
        delegate.response(response);
        return this;
    }
}
