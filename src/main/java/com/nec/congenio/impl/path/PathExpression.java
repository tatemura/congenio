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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nec.congenio.ConfigException;

/**
 * A path expression used for a pointer of
 * inheritance
 * <pre>
 *  (SCHEME ':')? PATH ('#' DOC_PATH)?
 * </pre>
 * @author tatemura
 *
 */
public class PathExpression {
	private static final Pattern EXP = Pattern.compile("^\\s*((\\w+):)?([^#]*)(#(.*))?\\s*$");
	private static final int SCHEME = 2;
	private static final int PATH = 3;
	private static final int DOC = 5;
	public static PathExpression parse(String name) {
		Matcher m = EXP.matcher(name);
		if (m.matches()) {
			String scheme = m.group(SCHEME);
			if (scheme == null) {
				scheme = "";
			}
			String path = m.group(PATH);
			if (path == null) {
				path = "";
			}
			String doc = m.group(DOC);
			if (doc == null) {
				doc = "";
			}
			return new PathExpression(scheme, path, doc);
		} else {
			throw new ConfigException("malformed path expression: " + name);
		}
	}
	private final String scheme;
	private final String path;
	private final String docPath;

	public PathExpression(String scheme,
			String rootName, String docPath) {
		this.scheme = scheme;
		this.path = rootName;
		this.docPath = docPath;
	}

	public String getScheme() {
		return scheme;
	}
	public String getPathPart() {
		return path;
	}
	public String getDocPath() {
		return docPath;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (!scheme.isEmpty()) {
			sb.append(scheme).append(":");
		}
		sb.append(path);
		if (!docPath.isEmpty()) {
			sb.append("#").append(docPath);
		}
		return sb.toString();
	}

}
