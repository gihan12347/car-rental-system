package com.carrental.config;

import com.carrental.storage.CarImageStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CarImageStorageService carImageStorageService;

    public WebConfig(CarImageStorageService carImageStorageService) {
        this.carImageStorageService = carImageStorageService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                String uri = request.getRequestURI();
                if (uri != null && shouldPreventBrowserCache(uri)) {
                    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
                    response.setHeader("Pragma", "no-cache");
                    response.setDateHeader("Expires", 0);
                }
                return true;
            }

            private boolean shouldPreventBrowserCache(String uri) {
                if (uri.startsWith("/css/")
                        || uri.startsWith("/images/")
                        || uri.startsWith("/js/")
                        || uri.startsWith("/uploads/cars/")
                        || uri.startsWith("/icons/")
                        || "/sw.js".equals(uri)
                        || "/site.webmanifest".equals(uri)) {
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = carImageStorageService.getUploadRoot().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/uploads/cars/**").addResourceLocations(location);
    }
}
