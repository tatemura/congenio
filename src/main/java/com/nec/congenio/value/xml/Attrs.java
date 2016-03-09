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

package com.nec.congenio.value.xml;

import java.util.Map;

import org.w3c.dom.Element;

import com.nec.congenio.xml.Xml;

/**
 * XML attributes for a CDGL value.
 *
 */
public final class Attrs {

    private Attrs() {
    }

    public static final String TYPE = "type";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    /**
     * Attributes used to represent a ConfigValue.
     */
    public static final String[] VALUE_ATTRS = { TYPE, NAME, VALUE, };

    /**
     * Gets the attributes that are defined by the users,
     * i.e., that are not reserved by the configuration
     * generation language.
     * @param elem the element where attributes are found.
     * @return a map of attribute name and value.
     */
    public static Map<String, String> userAttrs(Element elem) {
        Map<String, String> attrs = Xml.getAttributes(elem);
        for (String a : Attrs.VALUE_ATTRS) {
            attrs.remove(a);
        }
        return attrs;
    }
}
