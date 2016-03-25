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

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import com.nec.congenio.ConfigValue;

public class ClassPathDocTest {

    @Test
    public void test() {
        XmlConfigDescription conf =
                new ConfigFactory()
                .create(ClassPathDocTest.class, "data");
        ConfigValue val = conf.resolve();
        assertEquals(1, val.getInt("a"));
        assertEquals(2, val.getInt("b"));
    }

    @Test
    public void testExtend() {
        XmlConfigDescription conf =
                new ConfigFactory()
                .create(ClassPathDocTest.class, "data1");
        ConfigValue val = conf.resolve();
        assertEquals(3, val.getInt("a"));
        assertEquals(2, val.getInt("b"));
    }

    @Test
    public void test1() {
        File file = new File(".");
        File abs = new File(file.getAbsolutePath()).getParentFile();
        System.err.println(new File(file, "test").getAbsolutePath());
        System.err.println(new File(abs, "test").getAbsolutePath());
        System.err.println(abs.getParent());
    }
}
