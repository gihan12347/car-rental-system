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

    /** Strip spaces and dashes for registration / plate lookups (e.g. ABC1234 matches ABC-1234). */
    public static String normalizePlate(String raw) {
        String normalized = normalize(raw);
        if (normalized.isEmpty()) {
            return "";
        }
        return normalized.replace("-", "").replace(" ", "").toLowerCase(java.util.Locale.ROOT);
    }

    public static boolean isPresent(String raw) {
        return !normalize(raw).isEmpty();
    }
}
