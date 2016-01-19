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
package com.nec.congenio;

public final class PropertyNames {

	/**
	 * Semicolon-separated pairs of lib path definitions.
	 * A lib path definition is given in the form of "name=path".
	 * A path can start with "/" (for an absolute path) or "~/"
	 * (for a path from the user's home). Otherwise, it is defined
	 * relative to the current working directory.
	 * For example:
	 * <pre>
	 * "conf=../config;server=defs/server;client=~/defs/client;test=/tmp/test"
	 * </pre>
	 */
	public static final String PROP_LIBS = "congen.libs";

	private PropertyNames() {
	}

}
