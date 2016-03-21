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
