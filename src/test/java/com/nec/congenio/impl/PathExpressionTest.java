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
package com.nec.congenio.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import com.nec.congenio.impl.path.PathExpression;

public class PathExpressionTest {

	@Test
	public void testRegularPath() {
		PathExpression exp = PathExpression.parse("test/test#aaa");
		assertEquals("", exp.getScheme());
		assertEquals("test/test", exp.getPathPart());
		assertEquals("aaa", exp.getDocPath());
		PathExpression exp1 = PathExpression.parse("test/test");
		assertEquals("", exp1.getScheme());
		assertEquals("test/test", exp1.getPathPart());
		assertEquals("", exp1.getDocPath());
	}

	@Test
	public void testLibPath() {
		PathExpression exp = PathExpression.parse("lib:test:xxx/yyy#aaa");
		assertEquals("lib", exp.getScheme());
		assertEquals("test:xxx/yyy", exp.getPathPart());
		assertEquals("aaa", exp.getDocPath());
	}
}
