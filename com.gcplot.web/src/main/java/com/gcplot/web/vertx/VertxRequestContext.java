package com.gcplot.web.vertx;

import com.gcplot.commons.exceptions.Exceptions;
import com.gcplot.commons.serialization.JsonSerializer;
import com.gcplot.messages.Wrapper;
import com.gcplot.model.account.Account;
import com.gcplot.repository.AccountRepository;
import com.gcplot.web.*;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VertxRequestContext implements RequestContext {
    protected static final String APPLICATION_JSON = "application/json; charset=utf-8";
    protected AccountRepository accountRepository;
    protected StringBuilder sb = new StringBuilder();
    protected String mimeType = APPLICATION_JSON;
    protected RoutingContext context;
    protected LoginInfo loginInfo;

    @Override
    public RequestContext response(Object response) {
        if (!isChunked()) {
            return finish(JsonSerializer.serialize(new Wrapper(response)));
        } else {
            return write(JsonSerializer.serialize(response));
        }
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
        if (isChunked()) {
            context.response().write(value);
        } else {
            sb.append(value);
        }
        return this;
    }

    @Override
    public RequestContext writeLine(String value) {
        return write(value + System.lineSeparator());
    }

    @Override
    public RequestContext finish(String value) {
        if (Strings.isNullOrEmpty(mimeType)) {
            context.response().putHeader("Content-Type", mimeType);
        }
        if (!isChunked()) {
            sb.append(value);
            byte[] response;
            try {
                response = sb.toString().getBytes("UTF-8");
            } catch (Throwable t) {
                response = new byte[0];
            }
            context.response().putHeader("Content-Length", response.length + "");
            context.response().end(Buffer.buffer(response));
        } else {
            if (Strings.isNullOrEmpty(value)) {
                context.response().end();
            } else {
                context.response().end(value);
            }
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
    public RequestContext setChunked(boolean chunked) {
        context.response().setChunked(chunked);
        return this;
    }

    @Override
    public RequestContext redirect(String url) {
        context.response().setStatusCode(301).putHeader("Location", url).end();
        return this;
    }

    @Override
    public boolean isChunked() {
        return context.response().isChunked();
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
        if (loginInfo == null) {
            String token = context.request().getParam("token");
            if (token == null) {
                token = context.request().getHeader(Constants.AUTH_TOKEN_HEADER);
            }
            if (token == null) {
                return Optional.empty();
            }
            Optional<Account> account = accountRepository.account(token);
            if (account.isPresent()) {
                loginInfo = new LoginInfo(token, account.get());
            }
        }
        return Optional.ofNullable(loginInfo);
    }

    @Override
    public List<UploadedFile> files() {
        return context.fileUploads().stream()
                .map(fu -> new UploadedFileImpl(context.vertx(), context.request(), fu))
                .collect(Collectors.toList());
    }

    @Override
    public HttpMethod method() {
        return mapHttpMethod(context.request().method());
    }

    @Override
    public String param(String key) {
        Preconditions.checkNotNull(key);
        return context.request().getParam(key);
    }

    @Override
    public String param(String key, String defaultValue) {
        String param = param(key);
        return Strings.isNullOrEmpty(param) ? defaultValue : param;
    }

    @Override
    public boolean hasParam(String key) {
        return param(key, null) != null;
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
        this.loginInfo = null;
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("context", context)
                .add("userAgent", getUserAgent())
                .add("method", method())
                .add("mimeType", mimeType())
                .add("path", path())
                .add("headers", headers())
                .add("getIp", getIp())
                .toString();
    }

    protected static class UploadedFileImpl implements UploadedFile {
        private final FileUpload fu;
        private final HttpServerRequest req;
        private final Vertx vertx;

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

        private final String contentType;
        @Override
        public String contentType() {
            return contentType;
        }

        private final long size;
        @Override
        public long size() {
            return size;
        }

        private File file;
        @Override
        public File file() {
            if (file == null) {
                req.pause();
                File f;
                try {
                    f = Files.createTempFile("upload-", ".log").toFile();
                } catch (IOException e) {
                    throw Exceptions.runtime(e);
                }
                final OpenOptions inOpts = new OpenOptions();
                inOpts.setDeleteOnClose(true);
                AsyncFile af = vertx.fileSystem().openBlocking(fu.uploadedFileName(), inOpts);
                AsyncFile to = vertx.fileSystem().openBlocking(f.getAbsolutePath(), new OpenOptions());
                Pump pump = Pump.pump(af, to);
                CountDownLatch finished = new CountDownLatch(1);
                af.endHandler(v -> {
                    CountDownLatch closed = new CountDownLatch(1);
                    af.close();
                    to.close(a -> closed.countDown());
                    try {
                        closed.await(1, TimeUnit.SECONDS);
                    } catch (InterruptedException ignored) {
                    }
                    finished.countDown();
                });
                pump.start();
                try {
                    finished.await();
                } catch (InterruptedException e) {
                    throw Exceptions.runtime(e);
                }
                req.resume();
                file = f;
            }
            return file;
        }

        public UploadedFileImpl(Vertx vertx, HttpServerRequest req, FileUpload fu) {
            this.fu = fu;
            this.req = req;
            this.vertx = vertx;
            this.originalName = fu.fileName();
            this.fileName = fu.uploadedFileName();
            this.contentType = fu.contentType();
            this.size = fu.size();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UploadedFileImpl that = (UploadedFileImpl) o;

            if (size != that.size) return false;
            if (originalName != null ? !originalName.equals(that.originalName) : that.originalName != null)
                return false;
            if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) return false;
            return contentType != null ? contentType.equals(that.contentType) : that.contentType == null;

        }

        @Override
        public int hashCode() {
            int result = originalName != null ? originalName.hashCode() : 0;
            result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
            result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
            result = 31 * result + (int) (size ^ (size >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("originalName", originalName)
                    .add("fileName", fileName)
                    .add("contentType", contentType)
                    .add("size", size)
                    .toString();
        }
    }
}
