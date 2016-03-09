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
import java.util.Properties;
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
    private static final Pattern LIB_DEF =
            Pattern.compile("^\\s*(\\w+)\\s*=\\s*(.*)$");
    private static final int NAME = 1;
    private static final int PATH = 2;
    private static final int DEF_NAME = 1;
    private static final int DEF_PATH = 2;
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

    public LibPath libPathAt(File dir) {
        LibPath libp = new LibPath(global);
        Map<String, ResourceFinder> map = create(dir, this);
        for (Map.Entry<String, ResourceFinder> e : map.entrySet()) {
            libp.setLocalPath(e.getKey(), e.getValue());
        }
        return libp;
    }

    public void setPathContext(String libName, ResourceFinder lib) {
        global.put(libName, lib);
        effective.put(libName, lib);
    }

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
        Properties props = ConfigProperties.getProperties(baseDir);
        return create(baseDir, props);
    }

    /**
     * Creates a LibPath resource finder based on a property set.
     * @param baseDir the base directory that is used when
     *        a search path is relative.
     * @param props the property set to be used to create
     *        a resource finder.
     * @return a LibPath resource finder.
     */
    public static LibPath create(File baseDir, Properties props) {
        String libdef = props.getProperty(ConfigProperties.PROP_LIBS);
        if (libdef != null) {
            return create(libdef, baseDir);
        } else {
            return new LibPath();
        }
    }

    /**
     * Creates a LibPath resource finder based on a property set.
     * The current directory is used as the base directory.
     * @param props the property set to be used to
     *        create a resource finder.
     * @return a LibPath resource finder.
     */
    public static LibPath create(Properties props) {
        return create(new File("."), props);
    }

    /**
     * Creates a LibPath resource finder based on
     * search paths and the base directory.
     * @param libDefs a semicolon-separated list of path definitions.
     * @param baseDir the base Directory from which a relative
     *        path is defined.
     * @return the generated LibPath resource finder.
     */
    public static LibPath create(String libDefs, File baseDir) {
        LibPath lib = new LibPath();
        Map<String, ResourceFinder> map = create(libDefs, baseDir, lib);
        for (Map.Entry<String, ResourceFinder> e : map.entrySet()) {
            lib.setPathContext(e.getKey(), e.getValue());
        }
        return lib;
    }

    private static Map<String, ResourceFinder> create(File baseDir, LibPath lib) {
        Properties props = ConfigProperties.getProperties(baseDir);
        String libdef = props.getProperty(ConfigProperties.PROP_LIBS);
        if (libdef != null) {
            return create(libdef, baseDir, lib);
        } else {
            return new HashMap<String, ResourceFinder>();
        }
    }

    private static Map<String, ResourceFinder> create(
            String libDefs, File baseDir, LibPath lib) {
        Map<String, ResourceFinder> map =
                new HashMap<String, ResourceFinder>();
        for (String def : libDefs.split(";")) {
            def = def.trim();
            if (!def.isEmpty()) {
                Matcher match = LIB_DEF.matcher(def);
                if (match.matches()) {
                    String libName = match.group(DEF_NAME);
                    String path = match.group(DEF_PATH);
                    File dir = toFile(baseDir, path);
                    ResourceFinder finder = SearchPath.create(dir, lib);
                    map.put(libName, finder);
                } else {
                    throw new ConfigException(
                            "malformed lib definition: " + def);
                }
            }
        }
        return map;
    }

    private static final String HOME = "~/";

    static File toFile(File baseDir, String path) {
        if (path.startsWith("/")) {
            return new File(path);
        } else if (path.startsWith(HOME)) {
            String home = System.getProperty("user.home");
            return new File(home + "/" + path.substring(HOME.length()));
        }
        return new File(baseDir, path);
    }
}
