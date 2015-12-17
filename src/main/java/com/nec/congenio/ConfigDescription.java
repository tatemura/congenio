/*******************************************************************************
 *   Copyright 2015 Junichi Tatemura
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
package com.nec.congenio;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

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
        return new ConfigFactory().create(file);
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
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("arg: filename");
            return;
        }
    	ConfigDescription cdl = ConfigDescription.create(new File(args[0]));
        if (EXTEND_ONLY.equals(System.getProperty(PROP_MODE))) {
        	OutputStreamWriter w = new OutputStreamWriter(System.out);
        	cdl.write(w, true);
        } else if ("json".equals(System.getProperty(PROP_OUT))) {
        	for (ConfigValue conf : cdl.evaluate()) {
        		JsonValue value = conf.toJson();
        		System.out.println(JsonValueUtil.toString(value));
        	}        	
        } else {
        	OutputStreamWriter w = new OutputStreamWriter(System.out);
        	int idx = 0;
        	for (ConfigValue conf : cdl.evaluate()) {
        		writeSeparator(idx, w);
        		Values.write(conf, w, true);
        		idx++;
        	}
        }
    }
    static void writeSeparator(int idx, Writer w) throws IOException {
    	if (idx > 0) {
        	w.write("<!-- -->\n");
    	}
    }
}
