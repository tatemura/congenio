package com.nec.congenio.impl.path.sys;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import com.nec.congenio.ConfigException;
import com.nec.congenio.ConfigValue;
import com.nec.congenio.impl.Eval;
import com.nec.congenio.impl.EvalContext;
import com.nec.congenio.value.PrimitiveValue;

public class PathFuncs implements FuncModule {

	@Override
	public String getName() {
		return "path";
	}

	@Override
	public Eval<ConfigValue> create(String call, EvalContext ctxt) {
		if ("here".equals(call)) {
			return here(ctxt, "");
		} else if (call.startsWith("here/")) {
			String arg = call.substring("here/".length());
			return here(ctxt, arg.trim());
		} else if ("pwd".equals(call)) {
			return pwd("");
		} else if (call.startsWith("pwd/")) {
			String arg = call.substring("pwd/".length());
			return pwd(arg.trim());
		}
		throw new ConfigException("unknown sys call (sys:"
				+ this.getName() + "): " + call);
	}

	Eval<ConfigValue> here(EvalContext ctxt, final String path) {
		String uri = ctxt.getCurrentResource().getURI();
		try {
			final File file = new File(new URI(uri));
			return new Eval<ConfigValue>() {

				@Override
				public ConfigValue getValue() {
					String dir = file.getParent();
					if (dir == null) {
						return PrimitiveValue.NULL;
					}
					if (!path.isEmpty()) {
						dir = new File(dir, path).getAbsolutePath();
					}
					return PrimitiveValue.valueOf(dir);
				}
			};
		} catch (URISyntaxException e) {
			throw new ConfigException("not a file :" + uri);
		}
	}
	Eval<ConfigValue> pwd(final String path) {
		return new Eval<ConfigValue>() {
			@Override
			public ConfigValue getValue() {
				String dir = System.getProperty("user.dir");
				if (dir == null) {
					return PrimitiveValue.NULL;
				}
				if (!path.isEmpty()) {
					dir = new File(dir, path).getAbsolutePath();
				}
				return PrimitiveValue.valueOf(dir);
			}
		};
	}
}
