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

import com.nec.congenio.impl.ConfigResource;
import com.nec.congenio.impl.EvalContext;

public interface ResourceFinder {

	/**
	 * Retrieves a config resource specified
	 * with the given path expression.
	 * @param pathExpr the path expression of
	 * the configuration generation language.
	 * @param ctxt evaluation context that represents
	 * where this path expression is specified.
	 * @return a config resource.
	 */
	ConfigResource getResource(PathExpression expr, EvalContext ctxt);

}