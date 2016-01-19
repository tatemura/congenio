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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.w3c.dom.Element;

import com.nec.congenio.ConfigException;
import com.nec.congenio.impl.ConfigPath;
import com.nec.congenio.impl.ConfigResource;
import com.nec.congenio.impl.PathContext;
import com.nec.congenio.impl.ResourcePointer;
import com.nec.congenio.impl.XMLValueUtil;
import com.nec.congenio.impl.path.LibPathContext;
import com.nec.congenio.impl.path.PathExpression;
import com.nec.congenio.xml.XML;

public class MockPathContext implements PathContext {
	private final Map<String, Element> repo =
			new HashMap<String, Element>();
	private final LibPathContext libp = new LibPathContext();

	public static MockPathContext create(@Nullable Element root) {
		MockPathContext path = new MockPathContext();
		if (root != null) {
			for (Element e : XML.getElements(root)) {
				String name = XML.getAttribute("name", e, e.getTagName());
				path.set(name, e);
			}
		}
		return path;
	}

	public MockPathContext() {
	}

	public void set(String name, Element src) {
		repo.put(name, src);
	}
	public void setLib(String name, PathContext ctxt) {
		libp.setPathContext(name, ctxt);
	}

	@Override
	public ConfigPath interpret(String pathExpr) {
		final PathExpression exp = PathExpression.parse(pathExpr);
		return new ConfigPath(new ResourcePointer() {
			@Override
			public ConfigResource getResource() {
				return get(exp);
			}
		},
		exp.getDocPath());
	}
	private ConfigResource get(PathExpression exp) {
		String scheme = exp.getScheme();
		if ("lib".equals(scheme)) {
			return libp.getResource(exp);
		} else if (!scheme.isEmpty()) {
			throw new ConfigException("scheme not supported: "
					+ exp.getScheme());
		}
		String name = exp.getPathPart();
		Element e = repo.get(name);
		if (e != null) {
			return new MockResource(this, 
					name + "@" + XMLValueUtil.path(e), e);
		}
		throw new ConfigException("not found: " + name);
	}

}
