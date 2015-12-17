/*******************************************************************************
 *   Copyright 2015 Junichi Tatemura
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
package com.nec.congenio;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nullable;
import javax.json.JsonValue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public interface ConfigValue {
	/**
	 * Gets the name of a named object of a list.
	 * For XML it returns "name"
	 * attribute or the tag name.
	 * For JSON, it is equivalent to
	 * find("name") or an empty String.
	 * <p>
	 * A list of named objects is used (instead
	 * of a map) when the order of objects
	 * is important. For XML, it is just a
	 * sequence of elements (with optional "name"
	 * attribute). For JSON, it is
	 * an array of object where each object has
	 * "name" attribute.
	 */
	String getName();

	/**
	 * Gets a value with the name and interprets
	 * it as an int value.
	 * @param name
	 * @return get(name).numberValue().intValue().
	 */
	int getInt(String name);

	int getInt(String name, int defaultValue);

	@Nullable
	Integer findInt(String name);

	long getLong(String name);

	long getLong(String name, long defaultValue);

	double getDouble(String name);

	double getDouble(String name, double defaultValue);

	/**
	 * Gets a Boolean value (default is "false").
	 * @param name
	 * @return getBoolean(name, false).
	 */
	boolean getBoolean(String name);

	boolean getBoolean(String name, boolean defaultValue);

	String get(String name);

	String get(String name, String defaultValue);

	@Nullable
	String find(String name);

	List<String> getList(String name);

	ConfigValue getValue(String name);

	@Nullable
	ConfigValue findValue(String name);

	boolean hasValue(String name);
	/**
	 * Gets the value with the name and interprets
	 * it as a list.
	 * @param name
	 * @return empty if no such name. Otherwise,
	 * it is equivalent to getValue(name).toValueList().
	 */
	List<ConfigValue> getValueList(String name);

	/**
	 * Gets the value with the name and interprets
	 * it as a Map.
	 * @param name
	 * @return an empty map if no value found
	 * with the given name. Otherwise, it is equivalent
	 * to getValue(name).toValueMap().
	 */
	Map<String, ConfigValue> getValueMap(String name);
	

	<T> T getObject(String name, Class<T> objectClass);
	@Nullable
	<T> T findObject(String name, Class<T> objectClass);

	<T> T getObject(String name, Class<T> objectClass, T defaultObject);

	/**
	 * Interprets the value as a map.
	 * @return an empty value if there is no
	 * name-value pair found in the content.
	 */
	Map<String, ConfigValue> toValueMap();

	/**
	 * Interprets the value as a list.
	 * (XML: all the elements under the element)
	 * (TODO JSON: If the value is an array that contains
	 * objects, return it. If the value is null, return
	 * an empty list otherwise, return an array that only
	 * contains the current value).
	 */
	List<ConfigValue> toValueList();

	/**
	 * Interprets the value as a string.
	 * @return null if the value is null.
	 */
	@Nullable
	String stringValue();

	/**
	 * Interprets the value as a number.
	 * @return null if the value is null.
	 */
	@Nullable
	BigDecimal numberValue();

	/**
	 * Interprets the value as a Boolean.
	 * @return false if the value is null.
	 */
	boolean booleanValue();
	/**
	 * Interprets the value as an integer.
	 * @param defaultValue
	 * @return equivalent to (numberValue() != null ?
	 * numberValue().intValue() : defaultValue).
	 */
	int intValue(int defaultValue);

	/**
	 * Interprets the value as a Properties.
	 * @return an empty properties if the
	 * value is null.
	 */
	Properties toProperties();

	/**
	 * Gets the value with the name and
	 * interpret it as Properties.
	 * @param name
	 * @return an empty Properties if there
	 * is no value associated with the name.
	 * Otherwise getValue(name).toProperties().
	 */
	Properties getProperties(String name);

	/**
	 * Interprets the value as a Json value.
	 * @return JsonValue
	 */
	JsonValue toJson();

	/**
	 * Gets the value with the name and
	 * interprets it as a Json value.
	 * @param name
	 * @return JsonValue.NULL if the name is
	 * not found. getValue(name).toJson() otherwise.
	 */
	JsonValue getJson(String name);
	/**
	 * Finds the value with the name and,
	 * if found, interprets it as a JsonValue
	 * @param name
	 * @return null if the name is not found
	 * else getValue(name).toJson()
	 */
	@Nullable
	JsonValue findJson(String name);

	/**
	 * Creates an XML form of the value.
	 * @param doc the document to which
	 * the element will belong.
	 * @param name the tag name of the element
	 * to be created.
	 * @return a W3C DOM3 element that
	 * holds the configuration value
	 */
	Element toXML(Document doc, String name);

	/**
	 * Creates an XML form of the value.
	 * @param name the tag name of the element
	 * to be created.
	 * @return a W3C DOM3 element that
	 * holds the configuration value
	 */
	Element toXML(String name);

	/**
	 * Finds a value with the name and
	 * interprets it as an XML element.
	 * @param name
	 * @return null if find(name) returns
	 * null. find(name).toXML(name) otherwise.
	 */
	@Nullable
	Element findXML(String name);

	Type getType();
}
