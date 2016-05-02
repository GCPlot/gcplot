package com.gcplot.web.vertx;

import com.gcplot.accounts.Account;
import com.gcplot.accounts.AccountRepository;
import com.gcplot.commons.serialization.JsonSerializer;
import com.gcplot.web.*;
import io.vertx.ext.web.RoutingContext;

import java.util.*;
import java.util.stream.Collectors;

public class VertxRequestContext implements RequestContext {

    @Override
    public void response(Object response) {
        context.response().end(JsonSerializer.serialize(response));
    }

    @Override
    public void responseCode(int code) {
        context.response().setStatusCode(code);
    }

    @Override
    public void logIn(LoginInfo info) {
        context.response().putHeader(Constants.AUTH_TOKEN_HEADER, info.token());
    }

    @Override
    public void mimeType(String mimeType) {
        context.response().putHeader("Content-Type", mimeType);
    }

    @Override
    public void putResponseHeader(String header, String value) {
        context.response().putHeader(header, value);
    }

    @Override
    public void write(String value) {
        context.response().write(value);
    }

    @Override
    public void writeLine(String value) {
        context.response().write(value + System.lineSeparator());
    }

    @Override
    public void finish(String value) {
        context.response().end(value);
    }

    @Override
    public void finish() {
        context.response().end();
    }

    @Override
    public String getIp() {
        return context.request().remoteAddress().host();
    }

    @Override
    public String getUserAgent() {
        return context.request().getHeader("User-Agent");
    }

    @Override
    public Optional<LoginInfo> loginInfo() {
        String token = context.request().getParam("token");
        if (token == null) {
            token = context.request().getHeader(Constants.AUTH_TOKEN_HEADER);
        }
        if (token == null) {
            return Optional.empty();
        }
        Optional<Account> account = accountRepository.account(token);
        if (account.isPresent()) {
            return Optional.of(new LoginInfo(token, account.get()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<UploadedFile> files() {
        return context.fileUploads().stream()
                .map(fu -> new UploadedFileImpl(fu.fileName(), fu.uploadedFileName()))
                .collect(Collectors.toList());
    }

    @Override
    public HttpMethod method() {
        return mapHttpMethod(context.request().method());
    }

    @Override
    public String param(String key) {
        return context.request().getParam(key);
    }

    @Override
    public String mimeType() {
        return context.request().getHeader("Content-Type");
    }

    @Override
    public String query() {
        return context.request().query();
    }

    @Override
    public Map<String, List<String>> headers() {
        Map<String, List<String>> result = new HashMap<>();
        context.request().headers().iterator().forEachRemaining(e ->
                result.computeIfAbsent(e.getKey(), k -> new LinkedList<>()).add(e.getValue()));
        return result;
    }

    public VertxRequestContext reset(RoutingContext context) {
        this.context = context;
        return this;
    }

    public VertxRequestContext setAccountRepository(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        return this;
    }

    protected HttpMethod mapHttpMethod(io.vertx.core.http.HttpMethod httpMethod) {
        switch (httpMethod) {
            case GET: return HttpMethod.GET;
            case POST: return HttpMethod.POST;
            case HEAD: return HttpMethod.HEAD;
            case OPTIONS: throw new RuntimeException("OPTIONS is not supported!");
            case PUT: return HttpMethod.PUT;
            case DELETE: return HttpMethod.DELETE;
            case TRACE: return HttpMethod.GET;
            case CONNECT: throw new RuntimeException("CONNECT is not supported!");
            case PATCH: return HttpMethod.PATCH;
            default: throw new RuntimeException("Unsupported http method: " + httpMethod);
        }
    }

    protected RoutingContext context;
    protected AccountRepository accountRepository;

    protected static class UploadedFileImpl implements UploadedFile {

        private final String originalName;
        @Override
        public String originalName() {
            return originalName;
        }

        private final String fileName;
        @Override
        public String fileName() {
            return fileName;
        }

        public UploadedFileImpl(String originalName, String fileName) {
            this.originalName = originalName;
            this.fileName = fileName;
        }
    }
}
