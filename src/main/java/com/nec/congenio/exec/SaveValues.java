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

package com.nec.congenio.exec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import com.nec.congenio.ConfigDescription;
import com.nec.congenio.ConfigValue;

public class SaveValues implements ValueHandler {
    private static final int LEN = 8;

    private final File dir;
    private final File outDir;
    private final ValueOutputFormatter format;

    /**
     * Creates SaveValues instance.
     * @param dir the directory where the result is saved
     * @param format output format for the values
     */
    public SaveValues(File dir, ValueOutputFormatter format) {
        this.dir = dir;
        this.outDir = new File(dir, "out");
        outDir.mkdirs();
        this.format = format;
    }

    @Override
    public void init(ConfigDescription cdl) throws Exception {
       File file = new File(dir, "snapshot.xml");
       OutputStreamWriter writer = new OutputStreamWriter(
               new FileOutputStream(file));
       try {
           cdl.write(writer);
       } finally {
           writer.close();
       }
    }

    protected File fileFor(int idx) {
        String name = Integer.toString(idx);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LEN - name.length(); i++) {
            sb.append('0');
        }
        sb.append(name).append(format.fileSuffix());
        return new File(outDir, sb.toString());
    }

    @Override
    public void value(int idx, ConfigValue value) throws Exception {
        OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(fileFor(idx)));
        try {
           format.write(value, writer);
        } finally {
            writer.close();
        }
    }

    @Override
    public void close() throws Exception {
    }
}