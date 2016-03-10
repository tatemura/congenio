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
import java.util.Properties;

import org.w3c.dom.Element;

import com.nec.congenio.ConfigDescription;
import com.nec.congenio.ConfigException;

public class ConfigFactory {
    private final Properties props;

    public ConfigFactory(Properties props) {
        this.props = props;
    }

    /**
     * Creates a config description from a given file.
     * @param file a file that contains config description.
     * @return a created description.
     */
    public XmlConfigDescription create(File file) {
        if (file.exists()) {
            return create(ConfigResource.create(file, props));
        } else {
            throw new ConfigException("file not found: " + file.getAbsolutePath());
        }
    }

    public XmlConfigDescription create(Class<?> cls, String resourcePath) {
        return create(ConfigResource.create(cls, resourcePath, props));
    }

    /**
     * Creates a config description from a given file with a base
     * configuration.
     * @param file a file that contains a config description.
     * @param base a base configuration that is extended by the
     *        given description in the file.
     * @return an extended description.
     */
    public XmlConfigDescription create(File file, ConfigDescription base) {
        if (file.exists()) {
            return create(ConfigResource.create(file, props),
                    (XmlConfigDescription) base);
        } else {
            throw new ConfigException("file not found: " + file.getAbsolutePath());
        }
    }

    private XmlConfigDescription create(ConfigResource resource) {
        Element elem = resource.createElement();
        ExtendXml.resolve(elem, resource);
        return new XmlConfigDescription(elem);
    }

    private XmlConfigDescription create(ConfigResource resource,
            XmlConfigDescription base) {
        Element elem = resource.createElement();
        ExtendXml.resolve(elem, base.getRoot(), resource);
        return new XmlConfigDescription(elem);
    }

}