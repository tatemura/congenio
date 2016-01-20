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

import java.io.Writer;
import java.util.Iterator;

import javax.annotation.Nullable;

import org.w3c.dom.Element;

import com.nec.congenio.ConfigDescription;
import com.nec.congenio.ConfigValue;
import com.nec.congenio.value.xml.XMLValue;
import com.nec.congenio.xml.XML;

public class XMLConfigDescription extends ConfigDescription {
    private final Element root;

	public XMLConfigDescription(Element e) {
		super();
		this.root = e;
	}
    public Element getRoot() {
    	return root;
    }
    @Override
    public String getName() {
    	return root.getTagName();
    }

    @Override
	public void write(Writer writer, boolean indent) {
		XML.write(root, writer, false, indent);
	}

    @Override
    @Nullable
    public String get(String path) {
		Element out = XML.getSingleElement(path,
				root, false);
		if (out != null) {
			return ExpXML.stringValue(new XMLConfigDescription(out)
			.resolveReferences());
		}
		return null;
    }

    @Override
    public ConfigValue resolve() {
    	Element e = resolveReferences();
    	return XMLValue.create(ExpXML.evaluate(e));
    }

    public Element resolveReferences() {
        return RefXML.resolve(root);
    }
    @Override
    public Iterable<ConfigValue> evaluate() {
		final Iterable<Element> unfold =
				ForLoop.unfold(root);
		return new Iterable<ConfigValue>() {
			@Override
			public Iterator<ConfigValue> iterator() {
				return new ResolveIterator(unfold.iterator());
			}
		};
    }
	public static String nameOf(Element e) {
	    String name = XML.getAttribute("name", e, null);
	    if (name != null) {
	        return e.getTagName() + " @ " + name;
	    } else {
	        return e.getTagName();
	    }
	}
	static class ResolveIterator implements Iterator<ConfigValue> {
		private final Iterator<Element> unfolded;
		ResolveIterator(Iterator<Element> unfolded) {
			this.unfolded = unfolded;
		}
		@Override
		public boolean hasNext() {
			return unfolded.hasNext();
		}

		@Override
		public ConfigValue next() {
			Element u = unfolded.next();
			return new XMLConfigDescription(u).resolve();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
