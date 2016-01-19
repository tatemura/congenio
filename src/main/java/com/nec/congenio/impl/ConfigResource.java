/*******************************************************************************
 * Copyright 2015 Junichi Tatemura
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
import com.nec.congenio.json.JsonValueUtil;
import com.nec.congenio.json.JsonXML;
import com.nec.congenio.xml.XML;

public abstract class ConfigResource {
	public static ConfigResource create(PathContext path, File file) {
		return new FileConfigResource(path, file);
	}
	public static ConfigResource create(PathContext path, URL url) {
		return new URLConfigResource(path, url);
	}
	/**
	 * Gets the content of the resource as an
	 * XML element
	 * @return the root element of
	 * the content.
	 */
	public abstract Element createElement();

	/**
	 * Gets the context (associated with this resource)
	 * that interprets a reference from this resource
	 * to another.
	 */
	public abstract PathContext pathContext();

	/**
	 * Gets the URI of the content (e.g. URL or file path).
	 */
	public abstract String getURI();

	static class URLConfigResource extends ConfigResource {
		private final URL url;
		private final PathContext path;
		public URLConfigResource(PathContext path, URL url) {
			this.url = url;
			this.path = path;
		}
		@Override
		public Element createElement() {
			// TODO JSON files
			return XML.parse(url).getDocumentElement();
		}

		@Override
		public PathContext pathContext() {
			return path;
		}

		@Override
		public String getURI() {
			try {
				return url.toURI().toString();
			} catch (URISyntaxException e) {
				throw new ConfigException("invalid URL:" + url,
						e);
			}
		}
		
	}
	static class FileConfigResource extends ConfigResource {
		private final File file;
		private final PathContext path;
		public FileConfigResource(PathContext path, File file) {
			this.file = file;
			this.path = path;
		}
		public Element createElement() {
			if (isJsonFile()) {
		    	JsonObject json = JsonValueUtil.parseObject(file);
				return JsonXML.toXML(json);
			}
			return XML.parse(file).getDocumentElement();
		}
		public PathContext pathContext() {
			return path;
		}
		@Override
		public String getURI() {
			return file.toURI().toString();
		}
	    boolean isJsonFile() {
	    	return file.getPath().endsWith(".json");
	    }
	}
}
