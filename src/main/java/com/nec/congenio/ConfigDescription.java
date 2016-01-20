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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import javax.annotation.Nullable;
import javax.json.JsonValue;

import com.nec.congenio.impl.ConfigFactory;
import com.nec.congenio.json.JsonValueUtil;


public abstract class ConfigDescription {
	public static final String[] SUFFIXES = {
		"xml", "cdl"
	};
	public static final String ATTR_EXTENDS = "extends";
	public static final String ATTR_REF = "ref";

	public static ConfigValue resolve(File file) {
		return create(file).resolve();
	}
	public static Iterable<ConfigValue> evaluate(File file) {
		return create(file).evaluate();
	}
    public static ConfigDescription create(File file) {
    	try {
			return create(file, congenProperties());
		} catch (IOException e) {
			throw new ConfigException(
					"IOException reading properties", e);
		}
    }	
    public static ConfigDescription create(File file, Properties props) {
        return new ConfigFactory(props).create(file);
    }

    public abstract String getName();

    /**
     * Writes the extended document without indentation.
     * @param writer
     */
    public void write(Writer writer) {
		write(writer, false);
	}

    /**
     * Writes the extended document.
     * @param writer
     * @param indent true to use indentation.
     */
	public abstract void write(Writer writer, boolean indent);

	/**
	 * Evaluates the document without foreach unfolding,
	 * resulting in a single value.
	 * @return The result value.
	 */
    public abstract ConfigValue resolve();
    /**
     * Fully evaluates the document: (1) foreach unfolding,
     * (2) reference resolution, and (3) value expression evaluation.
     * @return a sequence of the result values.
     */
    public abstract Iterable<ConfigValue> evaluate();

    /**
     * Evaluates a part of the document specified with the
     * name, without foreach unfolding.
     * @param name
     * @return equivalent to resolve().find(name).
     */
    @Nullable
    public abstract String get(String name);

    public static final String EXTEND_ONLY = "extend-only";
    public static final String PROP_MODE = "cdl.mode";
    public static final String PROP_OUT = "cdl.output";
    public static final String PROP_IDX = "cdl.doc.idx";
    public static final String PROP_PATH = "cdl.doc.path";

    public static final String PROP_FILE = "congen.properties";
    public static final String CONGEN_DIR = ".congen";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("arg: filename");
            return;
        }
        Properties prop = congenProperties();
    	ConfigDescription cdl = ConfigDescription.create(new File(args[0]),
    			prop);
        if (EXTEND_ONLY.equals(System.getProperty(PROP_MODE))) {
        	OutputStreamWriter w = new OutputStreamWriter(System.out);
        	cdl.write(w, true);
        } else if ("json".equals(System.getProperty(PROP_OUT))) {
        	Filter f = new Filter(System.getProperty(PROP_IDX));
        	Projection p = new Projection(System.getProperty(PROP_PATH));
        	int idx = 0;
        	for (ConfigValue conf : cdl.evaluate()) {
        		if (f.output(idx)) {
            		JsonValue value = p.project(conf).toJson();
            		System.out.println(JsonValueUtil.toString(value));
        		}
        		idx++;
        		if (idx > f.maxIndex()) {
        			break;
        		}
        	}        	
        } else {
        	Filter f = new Filter(System.getProperty(PROP_IDX));
        	Projection p = new Projection(System.getProperty(PROP_PATH));
        	OutputStreamWriter w = new OutputStreamWriter(System.out);
        	int idx = 0;
        	for (ConfigValue conf : cdl.evaluate()) {
        		if (f.output(idx)) {
            		writeSeparator(idx, w);
            		Values.write(p.project(conf), w, true);
        		}
        		idx++;
        		if (idx > f.maxIndex()) {
        			break;
        		}
        	}
        }
    }
    static Properties congenProperties() throws IOException {
    	Properties sys = System.getProperties();
		Properties prop = new Properties();
		for (String f : new String[] {
				PROP_FILE,
				sys.getProperty("user.home") + "/"
						+ CONGEN_DIR + "/" + PROP_FILE
				}) {
			if (loadProperties(prop, new File(f))) {
				break;
			}
		}
        prop.putAll(sys);
        return prop;
    }
    static boolean loadProperties(Properties prop, File file) throws IOException {
		if  (file.exists()) {
			FileInputStream fis = new FileInputStream(file);
			try {
				prop.load(fis);
				return true;
			} finally {
				fis.close();
			}
		}
		return false;
    }
    static void writeSeparator(int idx, Writer w) throws IOException {
    	if (idx > 0) {
        	w.write("<!-- "
        			+ idx + " -->\n");
    	}
    }
    static class Projection {
    	String[] path;
    	Projection(String pattern) {
    		if (pattern == null) {
    			path = new String[0];
    		} else {
//    			path = pattern.split("\\.");
    			path = pattern.split("/");
    		}
    	}
    	public ConfigValue project(ConfigValue v) {
    		for (String p : path) {
    			v = v.getValue(p);
    		}
    		return v;
    	}
    }
    static class Filter {
    	int idx;
    	Filter(String pattern) {
    		if (pattern != null) {
    			idx = Integer.parseInt(pattern);
    		} else {
    			idx = -1;
    		}
    	}
    	public boolean output(int idx) {
    		return this.idx < 0 || this.idx == idx;
    	}
    	public int maxIndex() {
    		if (idx < 0) {
    			return Integer.MAX_VALUE;
    		} else {
    			return idx;
    		}
    	}
    }
}
