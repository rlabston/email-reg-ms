package com.technet7.microsvc.email.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for MindsDB query requests
 */
public class MindsDBQueryRequest {
    
    @NotBlank(message = "Query cannot be blank")
    private String query;

    public MindsDBQueryRequest() {
    }

    public MindsDBQueryRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
