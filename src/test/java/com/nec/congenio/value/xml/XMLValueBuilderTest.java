package com.nec.congenio.value.xml;

import static org.junit.Assert.*;

import java.util.Properties;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.junit.Test;

import com.nec.congenio.ConfigValue;
import com.nec.congenio.Type;
import com.nec.congenio.ValueBuilder;
import com.nec.congenio.Values;
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
	@Test
	public void testPropertyValue() {
		Properties prop = new Properties();
		prop.setProperty("key1", "value1");
		prop.setProperty("key2", "value2");
		ConfigValue v = XMLValueBuilder.create("test")
				.add("params", prop).build();

		Properties prop1 = v.getProperties("params");
		assertEquals(prop, prop1);
	}

	@Test
	public void testBeanObjects() {
		Properties prop = new Properties();
		prop.setProperty("key1", "value1");
		prop.setProperty("key2", "value2");
		TestEntity entity = new TestEntity();
		entity.setName("test");
		entity.setFlag(true);
		entity.setId(10);
		entity.setSub(new SubEntity("sub", 0.5));
		entity.setProps(prop);
		ConfigValue v = XMLValueBuilder.create("test")
				.add("t", entity).build();
		TestEntity entity1 = v.getObject("t", TestEntity.class);
		assertEquals(entity.getName(), entity1.getName());
		assertEquals(entity.getId(), entity1.getId());
		assertEquals(entity.getFlag(), entity1.getFlag());
		assertEquals(entity.getProps(), entity1.getProps());
		assertEquals(entity.getSub().getName(), entity1.getSub().getName());
	}
	@Test
	public void testBeanObjectCreate() {
		Properties prop = new Properties();
		prop.setProperty("key1", "value1");
		prop.setProperty("key2", "value2");
		TestEntity entity = new TestEntity();
		entity.setName("test");
		entity.setFlag(true);
		entity.setId(10);
		entity.setSub(new SubEntity("sub", 0.5));
		entity.setProps(prop);
		ConfigValue v = Values.create(entity);
		TestEntity entity1 = v.toObject(TestEntity.class);
		assertEquals(entity.getName(), entity1.getName());
		assertEquals(entity.getId(), entity1.getId());
		assertEquals(entity.getFlag(), entity1.getFlag());
		assertEquals(entity.getProps(), entity1.getProps());
		assertEquals(entity.getSub().getName(), entity1.getSub().getName());
	}

	public static class TestEntity {
		private String name;
		private int id;
		private boolean flag;
		private Properties props;
		private SubEntity sub;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public boolean getFlag() {
			return flag;
		}
		public void setFlag(boolean flag) {
			this.flag = flag;
		}
		public Properties getProps() {
			return props;
		}
		public void setProps(Properties props) {
			this.props = props;
		}
		public SubEntity getSub() {
			return sub;
		}
		public void setSub(SubEntity sub) {
			this.sub = sub;
		}
	}
	public static class SubEntity {
		private String name;
		private double value;
		public SubEntity() {
		}
		public SubEntity(String name, double value) {
			this.name = name;
			this.value = value;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public double getValue() {
			return value;
		}
		public void setValue(double value) {
			this.value = value;
		}
	}

}
