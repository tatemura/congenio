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
import java.util.Properties;

public final class ConfigProperties {

    public static final String PROPERTY_FILE_NAME = "congen.properties";

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

    public static Properties getProperties() {
        return getProperties(null);
    }

    /**
     * Gets config properties if it exists in the directory.
     * @param dir the directory from which the property
     *        file is found.
     * @return an empty properties if there is no
     *        config property file in the directory.
     */
    public static Properties getProperties(File dir) {
        Properties prop = new Properties();
        File file = new File(dir, ConfigProperties.PROPERTY_FILE_NAME);
        loadProperties(prop, file);
        return prop;
    }

    /**
     * Merges the config properties.
     * 
     * <p>Use this method instead of just overwriting Properties
     * (e.g. base.putAll(ext)) since merge must treat some
     * property values in a special manner (i.e., combining values
     * of the same property).
     * @param base the base properties
     * @param ext the properties that overrides the base.
     * @return the merged properties.
     */
    public static Properties merge(Properties base, Properties ext) {
        Properties prop = new Properties(base);
        prop.putAll(ext);
        /**
         * TODO refactor the following.
         */
        String libBase = base.getProperty(PROP_LIBS, "");
        String libExt = ext.getProperty(PROP_LIBS, "");
        if (!libExt.isEmpty()) {
            prop.setProperty(PROP_LIBS, libBase + ";" + libExt);
        }
        return prop;
    }
}
