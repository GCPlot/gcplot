package com.gcplot.web;

import io.vertx.core.json.JsonObject;

import java.io.Closeable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Dispatcher<Route> extends Closeable {

    Dispatcher<Route> preHandle(Consumer<RequestContext> handler);

    Dispatcher<Route> postHandle(Consumer<RequestContext> handler);

    Dispatcher<Route> get(Route route, Consumer<RequestContext> handler);

    Dispatcher<Route> head(Route route, Consumer<RequestContext> handler);

    Dispatcher<Route> delete(Route route, Consumer<RequestContext> handler);

    <Payload> Dispatcher<Route> post(Route route, Class<? extends Payload> payloadType,
                                     BiConsumer<Payload, RequestContext> handler);

    Dispatcher<Route> postJson(Route route, BiConsumer<JsonObject, RequestContext> handler);

    Dispatcher<Route> post(Route route, Consumer<RequestContext> handler);

    Dispatcher<Route> post(Route route, BiConsumer<byte[], RequestContext> handler);

    Dispatcher<Route> put(Route route, Consumer<RequestContext> handler);

    <Payload> Dispatcher<Route> put(Route route, Class<? extends Payload> payloadType,
                                    BiConsumer<Payload, RequestContext> handler);

    Dispatcher<Route> putJson(Route route, BiConsumer<JsonObject, RequestContext> handler);

    Dispatcher<Route> put(Route route, BiConsumer<byte[], RequestContext> handler);


    Dispatcher<Route> blocking();

    Dispatcher<Route> noAuth();

    Dispatcher<Route> requireAuth();

    Dispatcher<Route> requireConfirmed();

    Dispatcher<Route> filter(Predicate<RequestContext> filter, String message);

    Dispatcher<Route> mimeTypes(String... mimeTypes);

    Dispatcher<Route> exceptionHandler(BiConsumer<Throwable, RequestContext> handler);

}
