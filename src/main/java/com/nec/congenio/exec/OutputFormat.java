/*******************************************************************************
 * Copyright 2015, 2016 Junichi Tatemura
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.nec.congenio.exec;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.Nullable;

import com.nec.congenio.ConfigValue;
import com.nec.congenio.Values;
import com.nec.congenio.json.JsonValueUtil;

public enum OutputFormat implements ValueOutputFormatter {
    XML(new XmlFormat()),
    XML_INDENT(new XmlFormat(true)),
    XML_NO_INDENT(new XmlFormat(false)),
    JSON(new JsonFormat()),
    JSON_INDENT(new JsonFormat(true)),
    JSON_NO_INDENT(new JsonFormat(false)),
    PROPERTIES(new PropertiesFormat());

    private final ValueOutputFormatter formatter;

    private OutputFormat(ValueOutputFormatter formatter) {
        this.formatter = formatter;
    }

    /**
     * Finds the output format for the given name.
     * @param name the name of output format.
     * @return an output format if it is found. null otherwise.
     */
    @Nullable
    public static OutputFormat find(String name) {
        String normalized = name.trim()
                .toUpperCase().replaceAll("-", "_");
        try {
            return OutputFormat.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public String fileSuffix() {
        return formatter.fileSuffix();
    }

    @Override
    public void write(ConfigValue value, Writer writer) throws IOException {
        formatter.write(value, writer);
    }

    @Override
    public void beginValue(int idx, Writer writer) throws IOException {
        formatter.beginValue(idx, writer);
    }

    public static class XmlFormat implements ValueOutputFormatter {
        private boolean indent = true;

        public XmlFormat() {
        }

        public XmlFormat(boolean indent) {
            this.indent = indent;
        }

        public void setIndent(boolean indent) {
            this.indent = indent;
        }

        @Override
        public String fileSuffix() {
            return ".xml";
        }

        @Override
        public void write(ConfigValue value, Writer writer) throws IOException {
            Values.write(value, writer, indent);
            if (!indent) {
                writer.write('\n');
            }
        }

        @Override
        public void beginValue(int idx, Writer writer) throws IOException {
            writer.write("<!-- " + idx + " -->\n");
        }
    }

    public static class JsonFormat implements ValueOutputFormatter {
        private boolean indent;

        public JsonFormat() {
            this.indent = true;
        }

        public JsonFormat(boolean indent) {
            this.indent = indent;
        }

        @Override
        public String fileSuffix() {
            return ".json";
        }

        @Override
        public void write(ConfigValue value, Writer writer)
                throws IOException {
            if (indent) {
                writer.write(JsonValueUtil.toString(value.toJson()));
                writer.write('\n');
            } else {
                writer.write(value.toJson().toString());
                writer.write('\n');
            }
        }

        @Override
        public void beginValue(int idx, Writer writer) throws IOException {
        }
    }

    public static class PropertiesFormat implements ValueOutputFormatter {

        @Override
        public String fileSuffix() {
            return ".properties";
        }

        @Override
        public void write(ConfigValue value, Writer writer) throws IOException {
            value.toProperties().store(writer, "");
        }

        @Override
        public void beginValue(int idx, Writer writer) throws IOException {
            writer.write("# " + idx + "\n");
        }
        
    }
}
