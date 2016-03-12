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

package com.nec.congenio.impl.path;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nec.congenio.ConfigException;
import com.nec.congenio.ConfigProperties;
import com.nec.congenio.impl.ConfigResource;
import com.nec.congenio.impl.EvalContext;

/**
 * A resource finder that interprets a path expression
 * whose scheme is "lib". A path expression is
 * in the following format:
 * <pre>
 *  'lib' ':' LIB_NAME ':' PATH ('#' DOC_PATH)?
 * </pre>
 * The LibPath resource finder identifies the location
 * of LIB_NAME from which the remaining path
 * expression (i.e., PATH ('#' DOC_PATH)?) is
 * resolved as a regular path.
 * @author tatemura
 *
 */
public class LibPath implements ResourceFinder {
    public static final String SCHEME = "lib";
    private static final Pattern LIB =
            Pattern.compile("^(\\w+):(.*)$");
    private static final int NAME = 1;
    private static final int PATH = 2;
    private final Map<String, ResourceFinder> global;
    private final Map<String, ResourceFinder> effective =
            new HashMap<String, ResourceFinder>();

    public LibPath() {
        global = new HashMap<String, ResourceFinder>();
    }

    LibPath(Map<String, ResourceFinder> global) {
        this.global = global;
        this.effective.putAll(global);
    }

    /**
     * Creates a derived LibPath resource finder for a
     * specified directory. Local lib path definitions
     * defined at the directory will be effective.
     * @param dir the directory where the finder is used.
     * @return a new LibPath finder for the directory
     */
    public LibPath libPathAt(File dir) {
        LibPath libp = new LibPath(global);
        Map<String, String> defs = ConfigProperties.getLibDefs(dir);
        for (Map.Entry<String, String> e : defs.entrySet()) {
            ResourceFinder finder =
                    SearchPath.create(new File(e.getValue()), this);
            libp.setLocalPath(e.getKey(), finder);
        }
        return libp;
    }

    /**
     * Sets a global mapping from the lib name to a resource finder.
     * The global mapping will be inherited to a new LibPath resource finder
     * that is generated from this LibPath finder.
     * @param libName the lib name
     * @param lib a resource finder associated with the name.
     */
    public void setGlobalPathContext(String libName, ResourceFinder lib) {
        global.put(libName, lib);
        effective.put(libName, lib);
    }

    /**
     * Sets a local mapping from the lib name to a resource finder.
     * @param libName the lib name.
     * @param lib a resource finder associated with the name.
     */
    public void setLocalPath(String libName, ResourceFinder lib) {
        if (!effective.containsKey(libName)) {
            effective.put(libName, lib);
        }
    }

    @Override
    public ConfigResource getResource(PathExpression exp, EvalContext ctxt) {
        Matcher match = LIB.matcher(exp.getPathPart());
        if (match.matches()) {
            String libName = match.group(NAME);
            String path = match.group(PATH);
            ResourceFinder finder = effective.get(libName);
            if (finder != null) {
                return finder.getResource(PathExpression.parse(path), ctxt);
            } else {
                throw new ConfigException(
                        "unknown lib: " + libName
                        + " not in " + effective.keySet());
            }
        } else {
            throw new ConfigException(
                    "malformed lib path: " + exp.getPathPart());
        }
    }

    public static LibPath create(File baseDir) {
        return create(ConfigProperties.getLibDefs(baseDir));
    }

    /**
     * Creates a LibPath resource finder based on a map
     * of lib-path definitions.
     * @param libDefs a map of name and path
     * @return a LibPath resource finder.
     */
    public static LibPath create(Map<String, String> libDefs) {
        LibPath lib = new LibPath();
        for (Map.Entry<String, String> e : libDefs.entrySet()) {
            File dir = new File(e.getValue());
            ResourceFinder finder = SearchPath.create(dir, lib);
            lib.setGlobalPathContext(e.getKey(), finder);
        }
        return lib;
    }

}
