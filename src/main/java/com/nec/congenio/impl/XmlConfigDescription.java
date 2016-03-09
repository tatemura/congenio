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
import com.nec.congenio.value.xml.XmlValue;
import com.nec.congenio.xml.Xml;

public class XmlConfigDescription extends ConfigDescription {
    private final Element root;

    public XmlConfigDescription(Element elem) {
        super();
        this.root = elem;
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
        Xml.write(root, writer, false, indent);
    }

    @Override
    @Nullable
    public String get(String path) {
        Element out = Xml.getSingleElement(path, root, false);
        if (out != null) {
            return ExpXml.stringValue(new XmlConfigDescription(out)
                    .resolveReferences());
        }
        return null;
    }

    @Override
    public ConfigValue resolve() {
        Element elem = resolveReferences();
        return XmlValue.create(ExpXml.evaluate(elem));
    }

    public Element resolveReferences() {
        return RefXml.resolve(root);
    }

    @Override
    public Iterable<ConfigValue> evaluate() {
        final Iterable<Element> unfold = ForLoop.unfold(root);
        return new Iterable<ConfigValue>() {
            @Override
            public Iterator<ConfigValue> iterator() {
                return new ResolveIterator(unfold.iterator());
            }
        };
    }

    /**
     * Gets a name of an element that is used for inheritance.
     * the element in the extending document will override
     * the element of the same name in the base document.
     * @param elem the element whose name is found.
     * @return the name of the element.
     */
    public static String nameOf(Element elem) {
        String name = Xml.getAttribute("name", elem, null);
        if (name != null) {
            return elem.getTagName() + " @ " + name;
        } else {
            return elem.getTagName();
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
            Element unfoldedElem = unfolded.next();
            return new XmlConfigDescription(unfoldedElem).resolve();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
