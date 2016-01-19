/*******************************************************************************
 * Copyright 2015 Junichi Tatemura
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
package com.nec.congenio.value;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

import com.nec.congenio.ConfigValue;
import com.nec.congenio.Values;

public class GetObjectTest {


	@Test
	public void testCreateObject() {
		ConfigValue value = Values.builder("test")
				.add("t",
						Values.builder("test")
						.add("num", 10)
						.add("id", "1000")
						.add("value", 1.52)
						.add("url", "http://test.org/"))
				.build();
		TestEntity entity = value.getObject("t", TestEntity.class);
		assertEquals(10, entity.getNum());
		assertEquals(1000, entity.getId());
		assertEquals(1.52, entity.getValue(), 0.0001);
		assertEquals("http://test.org/", entity.getUrl());
	}
	@Test
	public void testCreateObjectWithMissingValue() {
		ConfigValue value = Values.builder("test")
				.add("t",
						Values.builder("test")
						.add("id", "1000")
						.add("value", 1.52))
				.build();
		TestEntity entity = value.getObject("t", TestEntity.class);
		assertEquals(0, entity.getNum());
		assertEquals(1000, entity.getId());
		assertEquals(1.52, entity.getValue(), 0.0001);
		assertNull(entity.getUrl());
	}

	@Test
	public void testCreateNestedValue() {
		ConfigValue value = Values.builder("test")
				.add("entity", entity(1000))
				.add("name", "testparent")
				.build();
		TestParent par = value.toObject(TestParent.class);
		assertEquals("testparent", par.getName());
		TestEntity entity = par.getEntity();
		assertEquals(10, entity.getNum());
		assertEquals(1000, entity.getId());
		assertEquals(1.52, entity.getValue(), 0.0001);
		assertEquals("http://test.org/", entity.getUrl());
	}
	@Test
	public void testCreateArrayValue() {
		ConfigValue[] vals = {
				entity(1),
				entity(2),
				entity(3)
		};
		ConfigValue value = Values.builder("test")
				.add("entities", vals)
				.add("name", "testparent")
				.build();
		TestGroup grp = value.toObject(TestGroup.class);
		TestEntity[] entities = grp.getEntities();
		assertEquals(vals.length, entities.length);
	}

	@Test
	public void testCreateWithProperties() {
		Properties prop = new Properties();
		prop.setProperty("k1", "v1");
		prop.setProperty("k2", "v2");
		ConfigValue value = Values.builder("test")
				.add("name", "name1")
				.add("params", prop).build();
		TestProp p = value.toObject(TestProp.class);
		assertEquals(prop, p.getParams());
	}
	ConfigValue entity(long id) {
		return Values.builder("test")
				.add("num", 10)
				.add("id", id)
				.add("value", 1.52)
				.add("url", "http://test.org/").build();
	}
	public static final class TestParent {
		private TestEntity entity;
		private String name;
		public TestEntity getEntity() {
			return entity;
		}
		public void setEntity(TestEntity entity) {
			this.entity = entity;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}
	public static final class TestGroup {
		private TestEntity[] entities;
		private String name;
		public TestEntity[] getEntities() {
			return entities;
		}
		public void setEntities(TestEntity[] entities) {
			this.entities = entities;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}

	public static final class TestEntity {
		private int num;
		private long id;
		private double value;
		private String url;
		public int getNum() {
			return num;
		}
		public void setNum(int num) {
			this.num = num;
		}
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public double getValue() {
			return value;
		}
		public void setValue(double value) {
			this.value = value;
		}
	}
	public static final class TestProp {
		private Properties params;
		private String name;
		public Properties getParams() {
			return params;
		}
		public void setParams(Properties params) {
			this.params = params;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}

}
