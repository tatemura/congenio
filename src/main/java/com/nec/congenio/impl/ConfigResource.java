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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import javax.json.JsonObject;

import org.w3c.dom.Element;

import com.nec.congenio.ConfigException;
import com.nec.congenio.ConfigValue;
import com.nec.congenio.Values;
import com.nec.congenio.impl.path.ResourceFinder;
import com.nec.congenio.impl.path.SearchPath;
import com.nec.congenio.json.JsonValueUtil;
import com.nec.congenio.json.JsonXml;
import com.nec.congenio.xml.Xml;

public abstract class ConfigResource {
    public static final String JSON_SUFFIX = ".json";
    public static final String PROPERTY_SUFFIX = ".properties";

    /**
     * Creates a resource in a class path.
     * @param cls a class that is used to define class paths.
     * @param path a search path.
     * @param props properties to configure a resource.
     * @return a config resource.
     */
    public static ConfigResource create(Class<?> cls,
            String path, Properties props) {
        URL url = cls.getResource(path);
        if (url == null) {
            url = cls.getResource(path + ".xml");
        }
        if (url == null) {
            throw new ConfigException("resource not found: " + path);
        }
        String prefix = new File(path).getParent();
        if (prefix == null) {
            prefix = "";
        }
        return create(SearchPath.create(cls, prefix, props), url);
    }

    /**
     * Creates a resource from a file.
     * @param file the file used as a resource.
     * @param props properties to configure resource.
     * @return a config resource.
     */
    public static ConfigResource create(File file, Properties props) {
        /**
         * NOTE: file.getParentFile() can be null (= ".")
         */
        File dir = file.getParentFile();

        return create(SearchPath.create(dir, props), file);
    }

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
            } else if (isPropertyFile()) {
                return getPropertyXml();
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
            return file.getPath().endsWith(JSON_SUFFIX);
        }

        boolean isPropertyFile() {
            return file.getPath().endsWith(PROPERTY_SUFFIX);
        }

        private Element getPropertyXml() {
            Properties props = new Properties();
            try {
                props.load(new FileInputStream(file));
            } catch (FileNotFoundException ex) {
                throw new ConfigException(
                        "file not found: " + file);
            } catch (IOException ex) {
                throw new ConfigException(
                      "failed to read properties: " + file, ex);
            }
            return Values.valueOf(props).toXml("properties");
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
