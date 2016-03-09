package com.nec.congenio.impl.path.sys;

import java.util.Map;

import com.nec.congenio.ConfigException;
import com.nec.congenio.ConfigValue;
import com.nec.congenio.impl.EvalContext;
import com.nec.congenio.impl.Eval;
import com.nec.congenio.value.PrimitiveValue;
import com.nec.congenio.value.xml.XmlValueBuilder;

public class EnvFuncs implements FuncModule {

    @Override
    public String getName() {
        return "env";
    }

    @Override
    public Eval<ConfigValue> create(String call, EvalContext ctxt) {
        if ("getenv".equals(call)) {
            return envs();
        } else if (call.startsWith("getenv/")) {
            String arg = call.substring("getenv/".length());
            return getEnv(arg.trim());
        }
        throw new ConfigException("unknown sys call (sys:" + this.getName() + "): " + call);
    }

    Eval<ConfigValue> envs() {
        return new Eval<ConfigValue>() {
            @Override
            public ConfigValue getValue() {
                Map<String, String> env = System.getenv();
                XmlValueBuilder builder = XmlValueBuilder.create("env");
                for (Map.Entry<String, String> e : env.entrySet()) {
                    builder.add(e.getKey(), e.getValue());
                }
                return builder.build();
            }
        };
    }

    Eval<ConfigValue> getEnv(final String name) {
        return new Eval<ConfigValue>() {
            @Override
            public ConfigValue getValue() {
                String value = System.getenv(name);
                if (value != null) {
                    return PrimitiveValue.valueOf(value);
                } else {
                    return PrimitiveValue.NULL;
                }
            }
        };
    }

}
