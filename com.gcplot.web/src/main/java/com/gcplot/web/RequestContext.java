package com.gcplot.web;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RequestContext {

    RequestContext response(Object response);

    RequestContext responseCode(int code);

    RequestContext logIn(LoginInfo info);

    RequestContext mimeType(String mimeType);

    RequestContext putResponseHeader(String header, String value);

    RequestContext write(String value);

    RequestContext writeLine(String value);

    RequestContext finish(String value);

    RequestContext finish();

    RequestContext clear();

    boolean isFinished();

    String getIp();

    String getUserAgent();

    Optional<LoginInfo> loginInfo();

    List<UploadedFile> files();

    HttpMethod method();

    String param(String key);

    String param(String key, String defaultValue);

    boolean hasParam(String key);

    String mimeType();

    String path();

    Map<String, List<String>> headers();

}
