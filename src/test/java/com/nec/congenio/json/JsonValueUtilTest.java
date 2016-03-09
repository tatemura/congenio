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
                .append("}   ").toString();
 
        JsonValue val = JsonValueUtil.parse(jsonText);
        assertEquals(ValueType.OBJECT, val.getValueType());
        JsonObject obj = (JsonObject) val;
        assertEquals(10, obj.getInt("intval"));
        assertNotNull(obj.getJsonArray("intarry"));
    }
}
