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

package com.nec.congenio.value.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import com.nec.congenio.value.xml.XmlValueBuilder;

public class XmlValueBuilderTest {

    @Test
    public void testBuildJsonValue() {
        JsonValue value = XmlValueBuilder.create("test")
                .add("int1", 1)
                .add("txt2", "2")
                .add("double3", 3.0)
                .build()
                .toJson();
        assertTrue(value instanceof JsonObject);
        JsonObject obj = (JsonObject) value;
        JsonValue valInt1 = obj.get("int1");
        assertEquals(ValueType.NUMBER, valInt1.getValueType());
        assertTrue(valInt1 instanceof JsonNumber);
        assertEquals(1, ((JsonNumber) valInt1).intValueExact());
        JsonValue valTxt2 = obj.get("txt2");
        assertEquals(ValueType.STRING, valTxt2.getValueType());
        assertTrue(valTxt2 instanceof JsonString);
    }

    @Test
    public void testBoolean() {
        ConfigValue confVal = XmlValueBuilder.create("test")
                .add("boolT", true)
                .add("boolF", false)
                .add("long1", 1L)
                .build();
        assertEquals(Type.OBJECT, confVal.getType());
        assertEquals(Type.BOOL, confVal.getValue("boolT").getType());
        assertEquals(Type.BOOL, confVal.getValue("boolF").getType());
        assertTrue(confVal.getBoolean("boolT"));
        assertTrue(confVal.getValue("boolT").booleanValue());
        assertFalse(confVal.getBoolean("boolF"));
    }

    @Test
    public void testBooleanJsonValue() {
        ConfigValue confVal = XmlValueBuilder.create("test")
                .add("boolT", true)
                .add("boolF", false)
                .add("long1", 1L)
                .build();
        JsonObject jvalue = (JsonObject) confVal.toJson();
        assertEquals(ValueType.TRUE, jvalue.get("boolT").getValueType());
        assertEquals(ValueType.FALSE, jvalue.get("boolF").getValueType());
    }

    @Test
    public void testNestedValue() {
        ValueBuilder parent = XmlValueBuilder.create("test")
                .add("xxx", "yyy");
        ValueBuilder child = XmlValueBuilder.create("test")
                .add("int1", 1)
                .add("txt2", "2")
                .add("double3", 3.0);
        ConfigValue confVal = parent.add("child", child).build();

        assertEquals(Type.OBJECT, confVal.getType());
        assertEquals(Type.STRING, confVal.getValue("xxx").getType());
        assertEquals(Type.OBJECT, confVal.getValue("child").getType());
        assertEquals(Type.NUMBER,
                confVal.getValue("child")
                .getValue("double3")
                .getType());

        JsonValue value = confVal.getValue("child").toJson();
        assertTrue(value instanceof JsonObject);
        JsonObject obj = (JsonObject) value;
        JsonValue valInt1 = obj.get("int1");
        assertEquals(ValueType.NUMBER, valInt1.getValueType());
    }

    @Test
    public void testPropertyValue() {
        Properties prop = new Properties();
        prop.setProperty("key1", "value1");
        prop.setProperty("key2", "value2");
        ConfigValue val = XmlValueBuilder.create("test")
                .add("params", prop).build();

        Properties prop1 = val.getProperties("params");
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
        ConfigValue val = XmlValueBuilder.create("test")
                .add("t", entity).build();
        TestEntity entity1 = val.getObject("t", TestEntity.class);
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
        ConfigValue val = Values.create(entity);
        TestEntity entity1 = val.toObject(TestEntity.class);
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
