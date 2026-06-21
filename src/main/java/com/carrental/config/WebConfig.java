package com.carrental.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.cars.dir:uploads/cars}")
    private String carsUploadDir;

    @Value("${app.upload.employees.dir:uploads/employees}")
    private String employeesUploadDir;

    @Value("${app.upload.profiles.dir:uploads/profiles}")
    private String profilesUploadDir;

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
                    response.setHeader("Vary", "Cookie");
                }
                return true;
            }

            private boolean shouldPreventBrowserCache(String uri) {
                if (uri.startsWith("/css/")
                        || uri.startsWith("/images/")
                        || uri.startsWith("/js/")
                        || uri.startsWith("/uploads/cars/")
                        || uri.startsWith("/uploads/employees/")
                        || uri.startsWith("/uploads/profiles/")
                        || uri.startsWith("/icons/")
                        || uri.startsWith("/media/")
                        || "/favicon.ico".equals(uri)
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
        registry.addResourceHandler("/uploads/cars/**")
                .addResourceLocations(toResourceLocation(carsUploadDir));
        registry.addResourceHandler("/uploads/employees/**")
                .addResourceLocations(toResourceLocation(employeesUploadDir));
        registry.addResourceHandler("/uploads/profiles/**")
                .addResourceLocations(toResourceLocation(profilesUploadDir));
    }

    private String toResourceLocation(String uploadDir) {
        Path root = Paths.get(uploadDir);
        root = root.isAbsolute()
                ? root.normalize()
                : root.toAbsolutePath().normalize();
        String location = root.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        return location;
    }
}
