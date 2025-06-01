package com.example.transformer;


public class MappingConfig {
    private String attributePrefix = "@";
    private String textField = "#text";
    private boolean arraysForRepeatedSiblings = true;
    /**
     * Whether to wrap the root element name in the produced JSON output. When
     * {@code true} (default), the top level JSON object contains a single field
     * named after the XML root element. If set to {@code false}, the children of
     * the XML root element are written directly into the top level JSON object.
     */
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

    /**
     * Alias for {@link #isWrapRoot()} to improve readability when using the
     * builder API.
     */
    public boolean isWrapRootElement() {
        return wrapRoot;
    }

    /**
     * Alias for {@link #setWrapRoot(boolean)}. Allows configuring the behaviour
     * using a more descriptive name.
     */
    public void setWrapRootElement(boolean wrapRoot) {
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
