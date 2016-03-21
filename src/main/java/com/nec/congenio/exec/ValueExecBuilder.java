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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import com.nec.congenio.ConfigDescription;
import com.nec.congenio.ConfigValue;

public class ValueExecBuilder {
    private final ConfigDescription cdl;
    private List<Filter> filters = new ArrayList<Filter>();
    private Projection proj = new IdentityProjection();
    private ValueHandler handler;

    public ValueExecBuilder(ConfigDescription cdl) {
        this.cdl = cdl;
    }

    /**
     * Adds a filter that selects values by index.
     * @param pattern index pattern
     * @return the builder itself for method chaining
     */
    public ValueExecBuilder filterIndex(String pattern) {
        if (pattern != null && !pattern.isEmpty()) {
            filters.add(IndexFilter.create(pattern));
        }
        return this;
    }

    /**
     * Sets a path that extracts an output value
     * from each generated value.
     * @param pattern a document path
     * @return the builder itself for method chaining
     */
    public ValueExecBuilder path(String pattern) {
        if (pattern != null && !pattern.isEmpty()) {
            proj = new PathProjection(pattern);
        }
        return this;
    }

    public ValueExecBuilder save(File dir, OutputFormat format) {
        return handler(new SaveValues(dir, format));
    }

    public ValueExecBuilder print(OutputFormat format) {
        return handler(new Printout(format));
    }

    public ValueExecBuilder handler(ValueHandler handler) {
        this.handler = handler;
        return this;
    }

    private Filter createFilter() {
        if (filters.isEmpty()) {
            return new NoFilter();
        } else if (filters.size() == 1) {
            return filters.get(0);
        } else {
            return new AndFilter(filters);
        }
    }

    public Runnable build() {
        return new ValueHandlerExecutor(
                cdl, handler, createFilter(), proj);
    }

    static class Printout implements ValueHandler {
        private OutputStreamWriter writer = new OutputStreamWriter(System.out);
        private final ValueOutputFormatter format;

        public Printout(ValueOutputFormatter format) {
            this.format = format;
        }

        @Override
        public void init(ConfigDescription cdl) throws Exception {
            // nothing
        }

        @Override
        public void value(int idx, ConfigValue value) throws IOException {
            format.beginValue(idx, writer);                
            format.write(value, writer);
        }

        @Override
        public void close() throws Exception {
            writer.flush();
        }
    }

    static class AndFilter implements Filter {
        private final List<Filter> filters;

        public AndFilter(List<Filter> filters) {
            this.filters = filters;
        }

        @Override
        public boolean output(int idx) {
            for (Filter f : filters) {
                if (!f.output(idx)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean output(ConfigValue value) {
            for (Filter f : filters) {
                if (!f.output(value)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int maxIndex() {
            int min = Integer.MAX_VALUE;
            for (Filter f : filters) {
                int idx = f.maxIndex();
                if (idx < min) {
                    min = idx;
                }
            }
            return min;
        }
        
    }

    static class NoFilter implements Filter  {

        @Override
        public boolean output(int idx) {
            return true;
        }

        @Override
        public boolean output(ConfigValue value) {
            return true;
        }

        @Override
        public int maxIndex() {
            return Integer.MAX_VALUE;
        }
    }

    static class IdentityProjection implements Projection {

        @Override
        public ConfigValue project(ConfigValue val) {
            return val;
        }
        
    }

}