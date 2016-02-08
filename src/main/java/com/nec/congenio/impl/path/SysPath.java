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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nec.congenio.ConfigException;
import com.nec.congenio.ConfigValue;
import com.nec.congenio.impl.ConfigResource;
import com.nec.congenio.impl.EvalContext;
import com.nec.congenio.impl.Eval;
import com.nec.congenio.impl.path.sys.EnvFuncs;
import com.nec.congenio.impl.path.sys.FuncModule;
import com.nec.congenio.impl.path.sys.PathFuncs;
import com.nec.congenio.impl.path.sys.RandomFuncs;
import com.nec.congenio.impl.path.sys.TimeFuncs;

public class SysPath implements ResourceFinder {
	public static final String SCHEME = "sys";
	private static final Pattern SYS = Pattern.compile("^(\\w+):(.*)$");
	private static final int MODULE = 1;
	private static final int CALL = 2;
	private static final Map<String, FuncModule> DEFAULT_MODULES =
			new HashMap<String, FuncModule>();
	private static void modules(FuncModule... modules) {
		for (FuncModule m : modules) {
			DEFAULT_MODULES.put(m.getName(), m);
		}
	}
	static {
		modules(new TimeFuncs(),
				new RandomFuncs(),
				new EnvFuncs(),
				new PathFuncs()
				);
	}
	private static final ResourceFinder NO_PATH = new ResourceFinder() {
		@Override
		public ConfigResource getResource(PathExpression pathExpr, EvalContext ctxt) {
			throw new ConfigException("invalid context to interpret a path");
		}
	};

	private Map<String, FuncModule> modules =
			new HashMap<String, FuncModule>(DEFAULT_MODULES);

	@Override
	public ConfigResource getResource(PathExpression exp, EvalContext ctxt) {
		if (!SCHEME.equals(exp.getScheme())) {
			throw new ConfigException("not a sys path: " + exp);
		}
		Matcher m = SYS.matcher(exp.getPathPart());
		if (m.matches()) {
			String moduleName = m.group(MODULE);
			String call = m.group(CALL);
			FuncModule module = getModule(moduleName);
			if (module == null) {
				throw new ConfigException("unknown sys module: " + moduleName);
			}
			Eval<ConfigValue> val = module.create(call, ctxt);
			return ConfigResource.create(NO_PATH, exp.toString(), val);
		}
		throw new ConfigException("malformed sys path: "
				+ exp.getPathPart());
	}
	private FuncModule getModule(String name) {
		return modules.get(name);
	}
	public void addModule(FuncModule module) {
		modules.put(module.getName(), module);
	}
}
