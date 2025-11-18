package com.technet7.microsvc.email.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DevWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static frontend build files from the frontend project's dist folder when present.
        // Useful in dev: build the frontend (npm run build) and the backend will serve files from
        // ../web-frontend/dist/web-frontend/
        String distPath = "file:../web-frontend/dist/web-frontend/";
        registry.addResourceHandler("/**")
                .addResourceLocations(distPath)
                .setCachePeriod(0);
    }

}
