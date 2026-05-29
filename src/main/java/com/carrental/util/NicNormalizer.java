package com.carrental.util;

import java.util.Locale;

public final class NicNormalizer {

    private NicNormalizer() {
    }

    public static String normalize(String nic) {
        if (nic == null) {
            return "";
        }
        return nic.replaceAll("\\s+", "").toUpperCase(Locale.ENGLISH);
    }
}
