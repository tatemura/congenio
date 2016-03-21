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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nec.congenio.ConfigValue;

public abstract class IndexFilter implements Filter {
    private static final Pattern INCLUSIVE_RANGE_PATTERN =
            Pattern.compile("(\\d*)\\.\\.(\\d*)");
    private static final Pattern EXCLUSIVE_RANGE_PATTERN =
            Pattern.compile("(\\d*)\\.\\.\\.(\\d*)");

    /**
     * Creates an index-based filter.
     * It supports Ruby-like range expression (two
     * dots and three dots).
     * <ul>
     * <li> NUMBER
     * <li> MIN_NUMBER..MAX_NUMBER (max inclusive)
     * <li> MIN_NUMBER...MAX_NUMBER (max exclusive)
     * <li> EXP,EXP (disjunction)
     * </ul>
     * @param pattern index pattern
     * @return an index-based filter
     */
    public static IndexFilter create(String pattern) {
        List<IndexFilter> filters = new ArrayList<IndexFilter>();
        for (String p : pattern.split(",")) {
            filters.add(createSingle(p));
        }
        if (filters.size() == 1) {
            return filters.get(0);
        } else {
            return new OrFilter(filters);
        }
        
    }

    private static IndexFilter createSingle(String pattern) {
        Matcher match = INCLUSIVE_RANGE_PATTERN.matcher(pattern);
        if (match.matches()) {
            int min = toInt(match.group(1), 0);
            int max = toInt(match.group(2), Integer.MAX_VALUE - 1);
            return new RangeFilter(min, max + 1);
        }
        Matcher exMatch = EXCLUSIVE_RANGE_PATTERN.matcher(pattern);
        if (exMatch.matches()) {
            int min = toInt(exMatch.group(1), 0);
            int max = toInt(exMatch.group(2), Integer.MAX_VALUE);
            return new RangeFilter(min, max);
        }
        return new PointFilter(Integer.parseInt(pattern));
    }

    private static int toInt(String matchStr, int defaultValue) {
        if (matchStr == null || matchStr.isEmpty()) {
            return defaultValue;
        } else {
            return Integer.parseInt(matchStr);
        }
    }

    @Override
    public boolean output(ConfigValue value) {
        return true;
    }

    public static class PointFilter extends IndexFilter {
        private final int idx;

        public PointFilter(int idx) {
            this.idx = idx;
        }
        
        @Override
        public boolean output(int idx) {
            return this.idx < 0 || this.idx == idx;
        }

        @Override
        public int maxIndex() {
            if (idx < 0) {
                return Integer.MAX_VALUE;
            } else {
                return idx + 1;
            }
        }
        
    }

    public static class RangeFilter extends IndexFilter {
        private final int min;
        private final int max;

        public RangeFilter(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean output(int idx) {
            return idx >= min && idx < max;
        }

        @Override
        public int maxIndex() {
            return max;
        } 
    }

    static class OrFilter extends IndexFilter {
        private final List<IndexFilter> filters;

        OrFilter(List<IndexFilter> filters) {
            this.filters = filters;
        }

        @Override
        public boolean output(int idx) {
            for (Filter f : filters) {
                if (f.output(idx)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int maxIndex() {
            int max = 0;
            for (Filter f : filters) {
                int idx = f.maxIndex();
                if (idx > max) {
                    max = idx;
                }
            }
            return max;
        }        
    }
}