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

/**
 * Resource pointer optionally associated  with
 * a document path. Whereas a resource pointer points
 * to the root of a config document, a conf path can
 * refer to a sub element of the document.
 * @author tatemura
 *
 */
public class ConfigPath implements ResourcePointer {
	private final String docPath;
	private final ResourcePointer rp;

	public ConfigPath(ResourcePointer rp, String docPath) {
		this.rp = rp;
		this.docPath = docPath;
	}
	public boolean hasDocPath() {
		return !docPath.isEmpty();
	}
	public String getDocPath() {
		return docPath;
	}
	/**
	 * Gets the unique ID of the resource (which
	 * can be used as a key for caching the content).
	 * @return a string that represents the URI.
	 */
	public String getResourceURI() {
		return rp.getResource().getURI();
	}
	@Override
	public ConfigResource getResource() {
		return rp.getResource();
	}
}