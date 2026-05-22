package com.carrental.web;

public final class SearchQuery {

    private SearchQuery() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim();
    }

    public static boolean isPresent(String raw) {
        return !normalize(raw).isEmpty();
    }
}
