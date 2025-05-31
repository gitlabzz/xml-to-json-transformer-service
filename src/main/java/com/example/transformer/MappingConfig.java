package com.example.transformer;

import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "mapping")
public class MappingConfig {
    private String attributePrefix = "@";
    private String textField = "#text";
    private boolean arraysForRepeatedSiblings = true;

    public String getAttributePrefix() {
        return attributePrefix;
    }

    public void setAttributePrefix(String attributePrefix) {
        this.attributePrefix = attributePrefix;
    }

    public String getTextField() {
        return textField;
    }

    public void setTextField(String textField) {
        this.textField = textField;
    }

    public boolean isArraysForRepeatedSiblings() {
        return arraysForRepeatedSiblings;
    }

    public void setArraysForRepeatedSiblings(boolean arraysForRepeatedSiblings) {
        this.arraysForRepeatedSiblings = arraysForRepeatedSiblings;
    }
}
