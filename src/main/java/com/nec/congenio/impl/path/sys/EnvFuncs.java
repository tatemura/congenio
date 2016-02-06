package com.nec.congenio.impl.path.sys;

import java.util.Map;

import org.w3c.dom.Element;

import com.nec.congenio.ConfigException;
import com.nec.congenio.impl.ValueFunc;
import com.nec.congenio.value.PrimitiveValue;
import com.nec.congenio.value.xml.XMLValueBuilder;

public class EnvFuncs implements FuncModule {

	@Override
	public String getName() {
		return "env";
	}

	@Override
	public ValueFunc<Element> create(String call) {
		if ("getenv".equals(call)) {
			return envs();
		} else if (call.startsWith("getenv/")) {
			String arg = call.substring("getenv/".length());
			return getEnv(arg.trim());
		}
		throw new ConfigException("unknown sys call (sys:"
				+ this.getName() + "): " + call);
	}

	ValueFunc<Element> envs() {
		return new ValueFunc<Element>() {
			@Override
			public Element getValue() {
				Map<String, String> env = System.getenv();
				XMLValueBuilder b = XMLValueBuilder.create("env");
				for (Map.Entry<String, String> e : env.entrySet()) {
					b.add(e.getKey(), e.getValue());
				}
				return b.build().toXML();
			}
		};
	}
	ValueFunc<Element> getEnv(final String name) {
		return new ValueFunc<Element>() {
			@Override
			public Element getValue() {
				String value = System.getenv(name);
				if (value != null) {
					return PrimitiveValue.valueOf(value).toXML("v");
				} else {
					return PrimitiveValue.NULL.toXML("v");
				}
			}
		};
	}

}
