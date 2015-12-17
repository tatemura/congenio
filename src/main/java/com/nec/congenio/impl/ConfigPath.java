/*******************************************************************************
 *   Copyright 2015 Junichi Tatemura
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
package com.nec.congenio.impl;

public class ConfigPath {
	private final SearchPath path;
	private final String name;
	private final String rootName;
	private final String docPath;
	public ConfigPath(SearchPath path, String name) {
		this.path = path;
		this.name = name;
		String[] parts = name.split("#");
		if (parts.length == 2) {
			rootName = parts[0];
			docPath = parts[1];
		} else {
			rootName = name;
			docPath = "";
		}
	}
	public String getName() {
		return name;
	}
	public boolean hasDocPath() {
		return !docPath.isEmpty();
	}
	public String getDocPath() {
		return docPath;
	}
	public String getRootName() {
		return rootName;
	}
	public String getResourceURI() {
		return path.getResource(rootName).getURI();
	}
	public ConfigResource getResource() {
		return path.getResource(rootName);
	}
}