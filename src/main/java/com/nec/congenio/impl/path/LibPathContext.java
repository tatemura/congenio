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

import javax.annotation.Nullable;

import com.nec.congenio.ConfigException;
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
	private final Map<String, PathContext> libs =
			new HashMap<String, PathContext>();
	public LibPathContext() {
	}
	public LibPathContext(LibPathContext base) {
		libs.putAll(base.libs);
	}

	public void setPath(String libName, File dir) {
		setPathContext(libName, SearchPath.create(dir));
	}

	public void setPathContext(String libName, PathContext lib) {
		libs.put(libName, lib);
	}
	@Nullable
	public PathContext getPathContext(String libName) {
		return libs.get(libName);
	}
	public ConfigResource getResource(PathExpression exp) {
		Matcher m = LIB.matcher(exp.getPathPart());
		if (m.matches()) {
			String libName = m.group(NAME);
			String path = m.group(PATH);
			PathContext ctxt = libs.get(libName);
			if (ctxt != null) {
				return ctxt.interpret(path).getResource();
			} else {
				throw new ConfigException("unknown lib: " + libName
						+ " not in " + libs.keySet());
			}
		} else {
			throw new ConfigException("malformed lib path: "
					+ exp.getPathPart());
		}
	}
	public static LibPathContext create(String libPaths) {
		return create(libPaths, new File("."));
	}
	public static LibPathContext create(String libPaths, File baseDir) {
		LibPathContext lib = new LibPathContext();
		for (String def : libPaths.split(";")) {
			def = def.trim();
			if (!def.isEmpty()) {
				Matcher m = LIB_DEF.matcher(def);
				if (m.matches()) {
					String libName = m.group(DEF_NAME);
					String path = m.group(DEF_PATH);
					File dir = toFile(baseDir, path);
					lib.setPath(libName, dir);
				} else {
					throw new ConfigException("malformed lib definition: "
				+ def);				
				}
			}
		}
		return lib;
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
