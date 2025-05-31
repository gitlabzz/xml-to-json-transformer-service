package com.example.transformer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import java.io.IOException;

/**
 * Pretty printer that avoids spaces and root separators.
 */
public class CompactPrettyPrinter extends MinimalPrettyPrinter {
    public CompactPrettyPrinter() {
        this._rootValueSeparator = "";
    }

    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException {
        g.writeRaw(':');
    }

    @Override
    public void writeArrayValueSeparator(JsonGenerator g) throws IOException {
        g.writeRaw(',');
    }

    @Override
    public void writeRootValueSeparator(JsonGenerator g) throws IOException {
        // no root value separator
    }
}
