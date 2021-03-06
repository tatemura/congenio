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

import java.util.Arrays;

import javax.annotation.Nullable;

import org.w3c.dom.Element;

import com.nec.congenio.ConfigDescription;
import com.nec.congenio.xml.Xml;

/**
 * A class that encapsulates a path expression for extension.
 * <pre>
 *   extends="b;m1;m2;..;m3"
 * </pre>
 * 
 * @author tatemura
 *
 */
public class ExtendPath {
    public static final String EXTEND_HERE_REFERENCE = ".";

    private final String path;
    private final String[] mixins;

    /**
     * Creates a path for extension.
     * @param path the string value of "extends".
     */
    public ExtendPath(String path) {
        String[] parts = path.split(";");
        this.path = parts[0];
        this.mixins = Arrays.copyOfRange(parts, 1, parts.length);
    }

    /**
     * Finds a extend path at the given element.
     * @param elem the element from which a path is found.
     * @return null if there is no extend path associated
     *         with the given element.
     */
    @Nullable
    public static ExtendPath find(Element elem) {
        String protoPath = Xml.getAttribute(
                ConfigDescription.ATTR_EXTENDS, elem, null);
        if (protoPath != null) {
            return new ExtendPath(protoPath);
        } else {
            return null;
        }
    }

    public static void remove(Element elem) {
        elem.removeAttribute(ConfigDescription.ATTR_EXTENDS);
    }

    public String getPath() {
        return path;
    }

    public boolean hasMixins() {
        return mixins.length > 0;
    }

    public String[] getMixins() {
        return mixins;
    }

    public boolean isDeepExtendPoint() {
        return EXTEND_HERE_REFERENCE.equals(path);
    }

}
