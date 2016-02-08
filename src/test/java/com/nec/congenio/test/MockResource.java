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
package com.nec.congenio.test;

import org.w3c.dom.Element;

import com.nec.congenio.impl.ConfigResource;
import com.nec.congenio.impl.path.ResourceFinder;

public class MockResource extends ConfigResource {
	private final ResourceFinder path;
	private final String uri;
	private final Element src;
	public MockResource(ResourceFinder path, String uri,
			Element src) {
		this.path = path;
		this.uri = uri;
		this.src = src;
	}
	@Override
	public Element createElement() {
		return (Element) src.cloneNode(true);
	}

	@Override
	public ResourceFinder getFinder() {
		return path;
	}

	@Override
	public String getURI() {
		return uri;
	}
	
}