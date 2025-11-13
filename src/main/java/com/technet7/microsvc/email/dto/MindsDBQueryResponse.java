package com.technet7.microsvc.email.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO for MindsDB query responses
 */
public class MindsDBQueryResponse {
    
    private List<String> column_names;
    private List<List<Object>> data;
    private String type;
    private Map<String, Object> context;

    public MindsDBQueryResponse() {
    }

    public List<String> getColumn_names() {
        return column_names;
    }

    public void setColumn_names(List<String> column_names) {
        this.column_names = column_names;
    }

    public List<List<Object>> getData() {
        return data;
    }

    public void setData(List<List<Object>> data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }
}
