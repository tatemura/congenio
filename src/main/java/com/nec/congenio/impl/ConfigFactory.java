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
import com.nec.congenio.impl.path.SearchPath;

public class ConfigFactory {
	private final Properties props;
    public ConfigFactory(Properties props) {
    	this.props = props;
    }

    public XMLConfigDescription create(File file) {
    	if (file.exists()) {
        	return create(SearchPath.toResource(file, props));
    	} else {
    		throw new ConfigException("file not found: "
    				+ file.getAbsolutePath());
    	}
    }
    public XMLConfigDescription create(Class<?> cls, String resourcePath) {
    	return create(SearchPath.create(props).toResource(cls, resourcePath));
    }
    public XMLConfigDescription create(File file, ConfigDescription base) {
    	if (file.exists()) {
        	return create(SearchPath.toResource(file, props),
        			(XMLConfigDescription) base);
    	} else {
    		throw new ConfigException("file not found: "
    				+ file.getAbsolutePath());
    	}
    }

    private XMLConfigDescription create(ConfigResource resource) {
    	Element e = resource.createElement();
    	ExtendXML.resolve(e, resource);
    	return new XMLConfigDescription(e);
    }
    private XMLConfigDescription create(ConfigResource resource,
    		XMLConfigDescription base) {
    	Element e = resource.createElement();
    	ExtendXML.resolve(e, base.getRoot(),
    			resource);
    	return new XMLConfigDescription(e);
    }

}