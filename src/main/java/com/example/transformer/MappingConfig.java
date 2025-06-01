package com.example.transformer;

import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "mapping")
public class MappingConfig {
    private String attributePrefix = "@";
    private String textField = "#text";
    private boolean arraysForRepeatedSiblings = true;
    private boolean wrapRoot = true;
    private boolean prettyPrint = false;
    private boolean preserveNamespaces = true;
    private boolean escapeNonAscii = false;

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

    public boolean isWrapRoot() {
        return wrapRoot;
    }

    public void setWrapRoot(boolean wrapRoot) {
        this.wrapRoot = wrapRoot;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public boolean isPreserveNamespaces() {
        return preserveNamespaces;
    }

    public void setPreserveNamespaces(boolean preserveNamespaces) {
        this.preserveNamespaces = preserveNamespaces;
    }

    public boolean isEscapeNonAscii() {
        return escapeNonAscii;
    }

    public void setEscapeNonAscii(boolean escapeNonAscii) {
        this.escapeNonAscii = escapeNonAscii;
    }
}
