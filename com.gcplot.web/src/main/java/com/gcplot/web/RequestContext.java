package com.gcplot.web;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RequestContext {

    void response(Object response);

    void responseCode(int code);

    void logIn(LoginInfo info);

    void mimeType(String mimeType);

    void putResponseHeader(String header, String value);

    void write(String value);

    void writeLine(String value);

    void finish(String value);

    void finish();

    String getIp();

    String getUserAgent();

    Optional<LoginInfo> loginInfo();

    List<UploadedFile> files();

    HttpMethod method();

    String param(String key);

    boolean hasParam(String key);

    String mimeType();

    String query();

    Map<String, List<String>> headers();

}
