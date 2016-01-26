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

import javax.annotation.Nullable;

import com.nec.congenio.ConfigException;
import com.nec.congenio.ConfigProperties;
import com.nec.congenio.impl.ConfigResource;
import com.nec.congenio.impl.PathContext;

public class LibPathContext {
	public static final String SCHEME = "lib";
	private static final Pattern LIB = Pattern.compile("^(\\w+):(.*)$");
	private static final Pattern LIB_DEF = Pattern.compile("^\\s*(\\w+)\\s*=\\s*(.*)$");
	private static final int NAME = 1;
	private static final int PATH = 2;
	private static final int DEF_NAME = 1;
	private static final int DEF_PATH = 2;
	private final Map<String, PathContext> global;
	private final Map<String, PathContext> effective =
			new HashMap<String, PathContext>();
	public LibPathContext() {
		global = new HashMap<String, PathContext>();
	}

	LibPathContext(Map<String, PathContext> global) {
		this.global = global;
		this.effective.putAll(global);
	}

	public LibPathContext contextAt(File dir) {
		LibPathContext libp = new LibPathContext(global);
		Map<String, PathContext> map = create(dir, this);
		for (Map.Entry<String, PathContext> e : map.entrySet()) {
			libp.setLocalPathContext(e.getKey(), e.getValue());
		}
		return libp;
	}

	public void setPathContext(String libName, PathContext lib) {
		global.put(libName, lib);
		effective.put(libName, lib);
	}
	public void setLocalPathContext(String libName, PathContext lib) {
		if (!effective.containsKey(libName)) {
			effective.put(libName, lib);
		}
	}

	@Nullable
	public PathContext getPathContext(String libName) {
		return effective.get(libName);
	}
	public ConfigResource getResource(PathExpression exp) {
		Matcher m = LIB.matcher(exp.getPathPart());
		if (m.matches()) {
			String libName = m.group(NAME);
			String path = m.group(PATH);
			PathContext ctxt = effective.get(libName);
			if (ctxt != null) {
				return ctxt.interpret(path).getResource();
			} else {
				throw new ConfigException("unknown lib: " + libName
						+ " not in " + effective.keySet());
			}
		} else {
			throw new ConfigException("malformed lib path: "
					+ exp.getPathPart());
		}
	}
	public static LibPathContext create(File baseDir) {
		Properties props = ConfigProperties.getProperties(baseDir);
		return create(baseDir, props);
	}

	public static LibPathContext create(File baseDir, Properties props) {
		String libdef = props.getProperty(ConfigProperties.PROP_LIBS);
		if (libdef != null) {
			return create(libdef, baseDir);
		} else {
			return new LibPathContext();
		}
	}

	public static LibPathContext create(Properties props) {
		String libdef = props.getProperty(ConfigProperties.PROP_LIBS);
		if (libdef != null) {
			return create(libdef, new File("."));
		} else {
			return new LibPathContext();
		}
	}

	public static LibPathContext create(String libPaths, File baseDir) {
		LibPathContext lib = new LibPathContext();
		Map<String, PathContext> map = create(libPaths, baseDir, lib);
		for (Map.Entry<String, PathContext> e : map.entrySet()) {
			lib.setPathContext(e.getKey(), e.getValue());
		}
		return lib;
	}

	private static Map<String, PathContext> create(File baseDir, LibPathContext lib) {
		Properties props = ConfigProperties.getProperties(baseDir);
		String libdef = props.getProperty(ConfigProperties.PROP_LIBS);
		if (libdef != null) {
			return create(libdef, baseDir, lib);
		} else {
			return new HashMap<String, PathContext>();
		}
	}

	private static Map<String, PathContext> create(String libPaths, File baseDir,
			LibPathContext lib) {
		Map<String, PathContext> map = new HashMap<String, PathContext>();
		for (String def : libPaths.split(";")) {
			def = def.trim();
			if (!def.isEmpty()) {
				Matcher m = LIB_DEF.matcher(def);
				if (m.matches()) {
					String libName = m.group(DEF_NAME);
					String path = m.group(DEF_PATH);
					File dir = toFile(baseDir, path);
					PathContext p = SearchPath.create(dir, lib);
					map.put(libName, p);
				} else {
					throw new ConfigException("malformed lib definition: "
				+ def);				
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
