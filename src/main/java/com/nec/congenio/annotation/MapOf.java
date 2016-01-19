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
package com.nec.congenio.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the class is populated with
 * Map&lt;String,T> where T is the value specified by
 * this annotation. 
 * <ul>
 * <li> When set at the type (class), an instance can be
 * generated from ConfigValue. The class must have a constructor with
 * Map&lt;String,T> in order to get the converted data.
 * <li> TODO When set at the method, a ConfigValue can be generated
 * from an instance of this class. The annotated method
 * must return Map&lt;String,T&gt; and must not have arguments.
 * </ul>
 * @author tatemura
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MapOf {
	Class<?> value();
}
