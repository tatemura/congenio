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

package com.nec.congenio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigProperties {

    public static final String PROPERTY_FILE_NAME = "congen.properties";
    private static final Pattern LIB_DEF =
            Pattern.compile("^\\s*(\\w+)\\s*=\\s*(.*)$");
    private static final int DEF_NAME = 1;
    private static final int DEF_PATH = 2;

    /**
     * Semicolon-separated pairs of lib path definitions. A lib path definition
     * is given in the form of "name=path". A path can start with "/" (for an
     * absolute path) or "~/" (for a path from the user's home). Otherwise, it
     * is defined relative to the current working directory. For example:
     * 
     * <pre>
     * "conf=../config;server=defs/server;client=~/defs/client;test=/tmp/test"
     * </pre>
     */
    public static final String PROP_LIBS = "congen.libs";
    public static final String LIBPATH_SEPARATOR = ";";

    public static final String LIBPATH_FILE_NAME = "congen-libs.properties";

    private ConfigProperties() {
    }

    static boolean loadProperties(Properties prop, File file) {
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                try {
                    prop.load(fis);
                    return true;
                } finally {
                    fis.close();
                }
            } catch (IOException ex) {
                throw new ConfigException(
                        "failed to read properties in " + file);
            }
        }
        return false;
    }

    /**
     * Gets lib-path definitions defined at the given directory.
     * @param dir the directory
     * @return a map from name to path.
     */
    public static Map<String, String> getLibDefs(File dir) {
        Properties props = libDefProperties(dir);
        if (!props.isEmpty()) {
            return toAbsolutes(props, dir);
        }
        String libdef = findLibDefStr(dir);
        if (libdef != null) {
            return parse(libdef, dir);
        }
        return new HashMap<String, String>();
    }

    public static Map<String, String> getLibDefs() {
        return getLibDefs(new File("."));
    }

    private static Properties libDefProperties(File dir) {
        Properties props = new Properties();
        File file = new File(dir, ConfigProperties.LIBPATH_FILE_NAME);
        loadProperties(props, file);
        return props;
    }

    private static String findLibDefStr(File dir) {
        Properties props = new Properties();
        File file = new File(dir, ConfigProperties.PROPERTY_FILE_NAME);
        loadProperties(props, file);
        return props.getProperty(ConfigProperties.PROP_LIBS);
    }

    /**
     * Converts a lib path definitions (in properties)
     * into a map of name and absolute paths.
     * @param paths a set of properties, each of which
     *        is a lib name and path.
     * @param baseDir the base directory for relative paths.
     * @return a map from names to absolute paths.
     */
    public static Map<String, String> toAbsolutes(Properties paths,
            File baseDir) {
        Map<String, String> map =
                new HashMap<String, String>();
        for (String name : paths.stringPropertyNames()) {
            map.put(name, toAbsolute(baseDir,
                    paths.getProperty(name)));
        }
        return map;
    }

    /**
     * Parses a lib path definition.
     * @param libDefStr a pattern of lib path definition
     * @return a map of names and paths
     */
    public static Map<String, String> parse(String libDefStr,
            File baseDir) {
        Map<String, String> map =
                new HashMap<String, String>();
        for (String def : libDefStr.split(ConfigProperties.LIBPATH_SEPARATOR)) {
            def = def.trim();
            if (!def.isEmpty()) {
                Matcher match = LIB_DEF.matcher(def);
                if (match.matches()) {
                    String libName = match.group(DEF_NAME);
                    String path = match.group(DEF_PATH);
                    map.put(libName, toAbsolute(baseDir, path));
                }
            }
        }
        return map;
    }

    private static final String HOME = "~/";

    /**
     * Converts a path to a file.
     *
     * <p>A path can start with '/' for an absolute path
     * and start with '~/' for a path relative to the user's
     * home directory. Otherwise, it is regarded as a relative
     * path.
     * @param baseDir the base directory used for
     *        a relative path.
     * @param path the path to be converted to a file.
     * @return a file for the path.
     */
    static String toAbsolute(File baseDir, String path) {
        if (path.startsWith("/")) {
            return path;
        } else if (path.startsWith(HOME)) {
            String home = System.getProperty("user.home");
            return new File(home + "/" + path.substring(HOME.length()))
                    .getAbsolutePath();
        }
        return new File(baseDir, path).getAbsolutePath();
    }

}
