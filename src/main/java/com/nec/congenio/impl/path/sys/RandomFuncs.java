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

import java.util.Random;

import org.w3c.dom.Element;

import com.nec.congenio.ConfigException;
import com.nec.congenio.impl.ValueFunc;
import com.nec.congenio.value.PrimitiveValue;

public class RandomFuncs implements FuncModule {

	@Override
	public String getName() {
		return "random";
	}

	@Override
	public ValueFunc<Element> create(String call) {
		if ("longValue".equals(call)) {
			return longValue();
		} else if ("intValue".equals(call)) {
			return intValue();
		}
		throw new ConfigException("unknown sys call (sys:"
				+ this.getName() + "): " + call);
	}
	ValueFunc<Element> longValue() {
		return new ValueFunc<Element>() {
			@Override
			public Element getValue() {
				long value = new Random().nextLong();
				return PrimitiveValue.valueOf(value).toXML("v");
			}
		};
	}
	ValueFunc<Element> intValue() {
		return new ValueFunc<Element>() {
			@Override
			public Element getValue() {
				int value = new Random().nextInt();
				return PrimitiveValue.valueOf(value).toXML("v");
			}
		};
	}
}