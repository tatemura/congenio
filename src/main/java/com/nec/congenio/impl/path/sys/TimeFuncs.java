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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.nec.congenio.ConfigException;
import com.nec.congenio.ConfigValue;
import com.nec.congenio.impl.EvalContext;
import com.nec.congenio.impl.Eval;
import com.nec.congenio.value.PrimitiveValue;

public class TimeFuncs implements FuncModule {

    @Override
    public String getName() {
        return "time";
    }

    @Override
    public Eval<ConfigValue> create(String call, EvalContext ctxt) {
        if ("longValue".equals(call)) {
            return longValue();
        } else if ("dateTime".equals(call)) {
            return timeFunc("");
        } else if (call.startsWith("dateTime/")) {
            String arg = call.substring("dateTime/".length());
            return timeFunc(arg.trim());
        }
        throw new ConfigException("unknown sys call (sys:" + this.getName() + "): " + call);
    }

    Eval<ConfigValue> longValue() {
        return new Eval<ConfigValue>() {
            @Override
            public ConfigValue getValue() {
                long value = new Date().getTime();
                return PrimitiveValue.valueOf(value);
            }
        };
    }

    Eval<ConfigValue> timeFunc(String arg) {
        final DateFormat format = format(arg);
        return new Eval<ConfigValue>() {
            @Override
            public ConfigValue getValue() {
                String value = format.format(new Date());
                return PrimitiveValue.valueOf(value);
            }
        };
    }

    private DateFormat format(String arg) {
        if (arg.isEmpty()) {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
        } else {
            return new SimpleDateFormat(arg);
        }
    }

}