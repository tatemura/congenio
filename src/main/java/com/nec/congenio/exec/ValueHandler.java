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
import com.nec.congenio.ConfigValue;

public interface ValueHandler {

    /**
     * Inspects the config description (before
     * unfolding) for initialization.
     * @param cdl the config description to be handled
     * @throws Exception thrown for any issue that
     *         needs the handling to stop.
     */
    void init(ConfigDescription cdl) throws Exception;

    /**
     * Handles one of the resolved (unfolded) documents.
     * @param idx the index of the document.
     * @param value the value of the document.
     * @throws Exception thrown for any issue that
     *         needs the handling to stop.
     */
    void value(int idx, ConfigValue value) throws Exception;

    /**
     * Finishes the value handling.
     * @throws Exception for any issue.
     */
    void close() throws Exception;
}