package com.nec.congenio.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.junit.Test;

public class JsonValueUtilTest {

	@Test
	public void parseTest() {
		String jsonText = new StringBuilder()
		.append("{")
		.append("\"intval\": 10,")
		.append("\"txtval\": \"10\",")
		.append("\"intarry\": [0,1,2]")
		.append("}   ")
		.toString();
		JsonValue v = JsonValueUtil.parse(jsonText);
		assertEquals(ValueType.OBJECT, v.getValueType());
		JsonObject obj = (JsonObject) v;
		assertEquals(10, obj.getInt("intval"));
		assertNotNull(obj.getJsonArray("intarry"));
	}
}
