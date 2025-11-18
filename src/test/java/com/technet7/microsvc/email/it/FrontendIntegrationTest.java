package com.technet7.microsvc.email.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FrontendIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    public void rootReturnsIndexHtmlWhenPresent() {
        String url = "http://localhost:" + port + "/";
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);

        // If frontend build is present, we expect index.html (text/html) or at least HTML content
        assertEquals(200, resp.getStatusCodeValue());
        HttpHeaders headers = resp.getHeaders();
        MediaType ct = headers.getContentType();
        boolean isHtml = (ct != null && (MediaType.TEXT_HTML.includes(ct) || ct.getSubtype().contains("html")))
                        || (resp.getBody() != null && (resp.getBody().toLowerCase().contains("<html") || resp.getBody().toLowerCase().contains("<!doctype")));
        assertTrue(isHtml, "Expected HTML response for '/', got: " + ct);
    }

}
