package com.carrental.web.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ApplicationParameterForm {

    @NotBlank
    private String code;

    @NotBlank(message = "Description is required.")
    private String description;

    @NotBlank(message = "Value is required.")
    private String value;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
