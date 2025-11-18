package com.technet7.microsvc.email.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static resources from classpath:/static/ for requests that look like files
        // (contain an extension). We avoid the unsupported "/**/*.*" pattern with the
        // PathPattern parser by registering a small set of file-like patterns at a few
        // path depths. This keeps controller paths (which typically don't include a
        // dot extension) from being intercepted while covering common static asset
        // layouts like /js/app.js, /assets/img/logo.png, /css/vendor/main.css, etc.
        String[] fileLikePatterns = new String[] {
            "/*.*",
            "/*/*.*",
            "/*/*/*.*",
            "/*/*/*/*.*",
            "/*/*/*/*/*.*"
        };
        registry.addResourceHandler(fileLikePatterns)
            .addResourceLocations("classpath:/static/")
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {
                @Override
                protected Resource getResource(String resourcePath, Resource location) {
                    // resourcePath is the path relative to the handler (no leading '/').
                    if (resourcePath == null) return null;

                    try {
                        Resource requestedResource = location.createRelative(resourcePath);
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                    } catch (Exception e) {
                        // ignore and fall back to index
                    }
                    // For non-existing resources (and non-controller paths), serve index.html if present
                    Resource index = new ClassPathResource("static/index.html");
                    return index.exists() && index.isReadable() ? index : null;
                }
            });
    }

}
