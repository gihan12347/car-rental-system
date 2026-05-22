package com.carrental.web;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

public final class RedirectUtil {

    private RedirectUtil() {
    }

    /**
     * Redirect to Referer when it points to this application; otherwise to a safe fallback path (with context).
     */
    public static String redirectToReferer(HttpServletRequest request, String fallbackPath) {
        String ref = request.getHeader("Referer");
        String context = request.getContextPath();
        if (context == null) {
            context = "";
        }
        if (ref == null || ref.isEmpty()) {
            return "redirect:" + context + fallbackPath;
        }
        try {
            URI uri = new URI(ref);
            String host = request.getServerName();
            if (uri.getHost() == null || !host.equalsIgnoreCase(uri.getHost())) {
                return "redirect:" + context + fallbackPath;
            }
            String refPath = uri.getPath();
            if (refPath == null || refPath.isEmpty()) {
                return "redirect:" + context + fallbackPath;
            }
            if (context.isEmpty()) {
                if (refPath.isEmpty() || !refPath.startsWith("/") || refPath.contains("..")) {
                    return "redirect:" + context + fallbackPath;
                }
            } else if (!refPath.startsWith(context)) {
                return "redirect:" + context + fallbackPath;
            }
            return "redirect:" + ref;
        } catch (URISyntaxException e) {
            return "redirect:" + context + fallbackPath;
        }
    }
}
