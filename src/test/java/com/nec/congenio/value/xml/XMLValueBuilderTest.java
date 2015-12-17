package com.nec.congenio.value.xml;

import static org.junit.Assert.*;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.junit.Test;

import com.nec.congenio.ConfigValue;
import com.nec.congenio.Type;
import com.nec.congenio.ValueBuilder;
import com.nec.congenio.value.xml.XMLValueBuilder;

public class XMLValueBuilderTest {


	@Test
	public void testBuildJsonValue() {
		JsonValue value = XMLValueBuilder.create("test").add("int1", 1)
				.add("txt2", "2").add("double3", 3.0).build().toJson();
		assertTrue(value instanceof JsonObject);
		JsonObject obj = (JsonObject) value;
		JsonValue vInt1 = obj.get("int1");
		assertEquals(ValueType.NUMBER, vInt1.getValueType());
		assertTrue(vInt1 instanceof JsonNumber);
		assertEquals(1, ((JsonNumber) vInt1).intValueExact());
		JsonValue vTxt2 = obj.get("txt2");
		assertEquals(ValueType.STRING, vTxt2.getValueType());
		assertTrue(vTxt2 instanceof JsonString);
	}

	@Test
	public void testBoolean() {
		ConfigValue v = XMLValueBuilder.create("test")
				.add("boolT", true)
				.add("boolF", false)
				.add("long1", 1L).build();
		assertEquals(Type.OBJECT, v.getType());
		assertEquals(Type.BOOL, v.getValue("boolT").getType());
		assertEquals(Type.BOOL, v.getValue("boolF").getType());
		assertTrue(v.getBoolean("boolT"));
		assertTrue(v.getValue("boolT").booleanValue());
		assertFalse(v.getBoolean("boolF"));
	}
	@Test
	public void testBooleanJsonValue() {
		ConfigValue v = XMLValueBuilder.create("test")
				.add("boolT", true)
				.add("boolF", false)
				.add("long1", 1L).build();
		JsonObject jvalue = (JsonObject) v.toJson();
		assertEquals(ValueType.TRUE, jvalue.get("boolT").getValueType());
		assertEquals(ValueType.FALSE, jvalue.get("boolF").getValueType());
	}

	@Test
	public void testNestedValue() {
		ValueBuilder parent = XMLValueBuilder.create("test").add("xxx", "yyy");
		ValueBuilder child = XMLValueBuilder.create("test").add("int1", 1)
				.add("txt2", "2").add("double3", 3.0);
		ConfigValue v = parent.add("child", child).build();

		assertEquals(Type.OBJECT, v.getType());
		assertEquals(Type.STRING, v.getValue("xxx").getType());
		assertEquals(Type.OBJECT, v.getValue("child").getType());
		assertEquals(Type.NUMBER, v.getValue("child").getValue("double3").getType());

		JsonValue value = v.getValue("child").toJson();
		assertTrue(value instanceof JsonObject);
		JsonObject obj = (JsonObject) value;
		JsonValue vInt1 = obj.get("int1");
		assertEquals(ValueType.NUMBER, vInt1.getValueType());
	}

}
