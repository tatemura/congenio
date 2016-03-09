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

package com.nec.congenio.impl;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.json.JsonObject;

import org.w3c.dom.Element;

import com.nec.congenio.ConfigException;
import com.nec.congenio.ConfigValue;
import com.nec.congenio.impl.path.ResourceFinder;
import com.nec.congenio.json.JsonValueUtil;
import com.nec.congenio.json.JsonXml;
import com.nec.congenio.xml.Xml;

public abstract class ConfigResource {
    public static ConfigResource create(ResourceFinder path, File file) {
        return new FileConfigResource(path, file);
    }

    public static ConfigResource create(ResourceFinder path, URL url) {
        return new UrlConfigResource(path, url);
    }

    public static ConfigResource create(ResourceFinder path,
            String uri, Eval<ConfigValue> value) {
        return new ConfigValueResource(path, uri, value);
    }

    /**
     * Gets the content of the resource as an XML element
     * 
     * @return the root element of the content.
     */
    public abstract Element createElement();

    /**
     * Gets the resource finder (associated with this resource) that interprets
     * a reference from this resource to another.
     */
    public abstract ResourceFinder getFinder();

    /**
     * Gets the URI of the content (e.g. URL or file path).
     */
    public abstract String getUri();

    static class UrlConfigResource extends ConfigResource {
        private final URL url;
        private final ResourceFinder path;

        public UrlConfigResource(ResourceFinder path, URL url) {
            this.url = url;
            this.path = path;
        }

        @Override
        public Element createElement() {
            // TODO JSON files
            return Xml.parse(url).getDocumentElement();
        }

        @Override
        public ResourceFinder getFinder() {
            return path;
        }

        @Override
        public String getUri() {
            try {
                return url.toURI().toString();
            } catch (URISyntaxException ex) {
                throw new ConfigException("invalid URL:" + url, ex);
            }
        }

    }

    static class FileConfigResource extends ConfigResource {
        private final File file;
        private final ResourceFinder path;

        public FileConfigResource(ResourceFinder path, File file) {
            this.file = file;
            this.path = path;
        }

        @Override
        public Element createElement() {
            if (isJsonFile()) {
                JsonObject json = JsonValueUtil.parseObject(file);
                return JsonXml.toXml(json);
            }
            return Xml.parse(file).getDocumentElement();
        }

        @Override
        public ResourceFinder getFinder() {
            return path;
        }

        @Override
        public String getUri() {
            return file.toURI().toString();
        }

        boolean isJsonFile() {
            return file.getPath().endsWith(".json");
        }

        public File getFile() {
            return file;
        }
    }

    static class ConfigValueResource extends ConfigResource {
        private final ResourceFinder path;
        private final String uri;
        private final Eval<ConfigValue> value;

        public ConfigValueResource(ResourceFinder path,
                String uri, Eval<ConfigValue> value) {
            this.path = path;
            this.uri = uri;
            this.value = value;
        }

        @Override
        public Element createElement() {
            return value.getValue().toXml("v");
        }

        @Override
        public ResourceFinder getFinder() {
            return path;
        }

        @Override
        public String getUri() {
            return uri;
        }
    }
}
