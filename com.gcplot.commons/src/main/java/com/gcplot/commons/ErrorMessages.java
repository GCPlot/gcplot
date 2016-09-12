package com.gcplot.commons;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

public class ErrorMessages {
    public static final long UNDEFINED_ERROR = 0x1;

    public static final long NOT_AUTHORISED = 0x123;
    public static final long ACCOUNT_NOT_CONFIRMED = 0x124;
    public static final long REQUEST_FILTERED = 0x125;
    public static final long WRONG_CREDENTIALS = 0x126;
    public static final long INTERNAL_ERROR = 0x1F4;
    public static final long NOT_UNIQUE_FIELDS = 0x127;
    public static final long USER_IS_BLOCKED = 0x128;
    public static final long NOT_FOUND = 0x194;
    public static final long SAME_PASSWORD = 0x195;

    public static final long RESOURCE_NOT_FOUND_RESPONSE = 0x197;

    public static final long UNKNOWN_GC_ANALYSE = 0x201;
    public static final long UNKNOWN_JVM_ID = 0x202;

    public static final long INVALID_REQUEST_PARAM = 0x301;

    public static String buildJson(long code) {
        ObjectNode node = new ObjectNode(JSON_NODE_FACTORY);
        node.put("error", code);
        node.put("message", errorMessages.get(code));
        return node.toString();
    }

    public static String buildJson(long code, String message) {
        ObjectNode node = new ObjectNode(JSON_NODE_FACTORY);
        node.put("error", code);
        node.put("message", message);
        return node.toString();
    }

    private static final TLongObjectMap<String> errorMessages = new TLongObjectHashMap<>();

    static {
        errorMessages.put(NOT_AUTHORISED, "Not authorised in the system.");
        errorMessages.put(ACCOUNT_NOT_CONFIRMED, "Account needs to be confirmed first.");
        errorMessages.put(REQUEST_FILTERED, "Request filtered.");
        errorMessages.put(WRONG_CREDENTIALS, "Wrong credentials, try again.");
        errorMessages.put(INTERNAL_ERROR, "Internal error.");
        errorMessages.put(NOT_UNIQUE_FIELDS, "One of the fields are already presented.");
        errorMessages.put(USER_IS_BLOCKED, "User is blocked.");
        errorMessages.put(NOT_FOUND, "The requested path wasn't found.");
        errorMessages.put(SAME_PASSWORD, "Old and new passwords should be different.");
        errorMessages.put(RESOURCE_NOT_FOUND_RESPONSE, "Requested resource was not found in the system.");
    }

    private static final JsonNodeFactory JSON_NODE_FACTORY = JsonNodeFactory.instance;
}
