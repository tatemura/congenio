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
        throw new ConfigException("unknown sys call (sys:" + this.getName() + "): " + call);
    }

    Eval<ConfigValue> here(EvalContext ctxt, final String path) {
        String uri = ctxt.getCurrentResource().getUri();
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
        } catch (URISyntaxException ex) {
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
