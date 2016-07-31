package com.gcplot.web.vertx;

import com.gcplot.commons.serialization.JsonSerializer;
import com.gcplot.messages.Wrapper;
import com.gcplot.model.account.Account;
import com.gcplot.repository.AccountRepository;
import com.gcplot.web.*;
import com.google.common.base.Strings;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

public class VertxRequestContext implements RequestContext {

    @Override
    public RequestContext response(Object response) {
        return finish(JsonSerializer.serialize(new Wrapper(response)));
    }

    @Override
    public RequestContext responseCode(int code) {
        context.response().setStatusCode(code);
        return this;
    }

    @Override
    public RequestContext logIn(LoginInfo info) {
        context.response().putHeader(Constants.AUTH_TOKEN_HEADER, info.token());
        return this;
    }

    @Override
    public RequestContext mimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    @Override
    public RequestContext putResponseHeader(String header, String value) {
        context.response().putHeader(header, value);
        return this;
    }

    @Override
    public RequestContext write(String value) {
        sb.append(value);
        return this;
    }

    @Override
    public RequestContext writeLine(String value) {
        return write(value + System.lineSeparator());
    }

    @Override
    public RequestContext finish(String value) {
        sb.append(value);
        byte[] response = new byte[0];
        try {
            response = sb.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }
        context.response().putHeader("Content-Length", response.length + "");
        context.response().end(Buffer.buffer(response));
        if (Strings.isNullOrEmpty(mimeType)) {
            context.response().putHeader("Content-Type", mimeType);
        }
        return this;
    }

    @Override
    public RequestContext finish() {
        return finish("");
    }

    @Override
    public RequestContext clear() {
        sb = new StringBuilder();
        return this;
    }

    @Override
    public boolean isFinished() {
        return context.response().ended();
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
    public boolean hasParam(String key) {
        return param(key) != null;
    }

    @Override
    public String mimeType() {
        return context.request().getHeader("Content-Type");
    }

    @Override
    public String path() {
        return context.request().path();
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
        this.clear();
        mimeType = APPLICATION_JSON;
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
    protected StringBuilder sb = new StringBuilder();
    protected String mimeType = APPLICATION_JSON;

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

    protected static final String APPLICATION_JSON = "application/json; charset=utf-8";
}
