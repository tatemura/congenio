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

import com.nec.congenio.ConfigDescription;
import com.nec.congenio.ConfigException;
import com.nec.congenio.ConfigValue;

public class ValueHandlerExecutor implements Runnable {
    private final ValueHandler handler;
    private final Filter filter;
    private final Projection proj;
    private final ConfigDescription cdl;

    /**
     * Instantiates an executor with a value handler.
     * @param cdl the document to be executed
     * @param handler the handler that handles values.
     * @param filter filter to select the values.
     * @param proj projection applied to the values.
     */
    public ValueHandlerExecutor(ConfigDescription cdl,
            ValueHandler handler,
            Filter filter, Projection proj) {
        this.cdl = cdl;
        this.handler = handler;
        this.filter = filter;
        this.proj = proj;
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (Exception ex) {
            throw new ConfigException(
                "exception during document execution", ex);
        }
    }

    /**
     * Executes the document with the value handler.
     * @throws Exception if any exception thrown by the handler.
     */
    public void execute() throws Exception {
        try {
            handler.init(cdl);
            int idx = 0;
            for (ConfigValue conf : cdl.evaluate()) {
                /**
                 * TODO support iteration without evaluation
                 * and then split filtering into two stage
                 * (pre and post evaluation)
                 */
                if (filter.output(idx) && filter.output(conf)) {
                    handler.value(idx, proj.project(conf));
                }
                idx++;
                if (idx >= filter.maxIndex()) {
                    break;
                }
            }
        } finally {
            handler.close();
        }
    }
}