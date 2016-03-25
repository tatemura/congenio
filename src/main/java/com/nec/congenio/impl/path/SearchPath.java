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

package com.nec.congenio.impl.path;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nullable;

import com.nec.congenio.ConfigDescription;
import com.nec.congenio.ConfigException;
import com.nec.congenio.impl.ConfigResource;
import com.nec.congenio.impl.EvalContext;

public abstract class SearchPath implements ResourceFinder {
    private final LibPath libp;
    private final SysPath sysp = new SysPath();

    public SearchPath(LibPath libp) {
        this.libp = libp;
    }

    protected LibPath libPath() {
        return libp;
    }

    @Override
    public ConfigResource getResource(PathExpression exp, EvalContext ctxt) {
        String scheme = exp.getScheme();
        if (LibPath.SCHEME.equals(scheme)) {
            return libp.getResource(exp, ctxt);
        } else if (SysPath.SCHEME.equals(scheme)) {
            return sysp.getResource(exp, ctxt);
        } else if (!scheme.isEmpty()) {
            throw new ConfigException("unsupported path scheme: " + scheme);
        }
        String name = exp.getPathPart();
        ConfigResource res = findResource(name);
        if (res != null) {
            return res;
        }
        String desc = getDescription();
        throw new ConfigException(
                "Not found: " + name
                + "(." + Arrays.toString(ConfigDescription.SUFFIXES)
                + ") @ " + desc);
    }

    public static ResourceFinder create(Class<?> cls, String prefix,
            Map<String, String> libDefs) {
        return new ResourceSearchPath(cls, prefix,
                LibPath.create(libDefs));
    }

    public static ResourceFinder create(File dir, Map<String, String> libDefs) {
        return new FileSearchPath(dir, LibPath.create(libDefs));
    }

    public static ResourceFinder create(File dir, LibPath libp) {
        return new FileSearchPath(dir, libp);
    }

    @Nullable
    public abstract ConfigResource findResource(String name);

    public abstract String getDescription();


    public static class ResourceSearchPath extends SearchPath {
        private final Class<?> cls;
        private final String prefix;

        ResourceSearchPath(Class<?> cls, String prefix, LibPath libp) {
            super(libp);
            this.cls = cls;
            this.prefix = prefix;
        }

        @Override
        public ConfigResource findResource(String name) {
            String path = prefix + name;
            URL url = cls.getResource(path);
            if (url == null) {
                url = cls.getResource(path + ".xml");
            }
            if (url != null) {
                return ConfigResource.create(this, url);
            } else {
                return null;
            }
        }

        @Override
        public String getDescription() {
            return "resource:" + cls.getName() + "@'" + prefix + "'";
        }

    }

    public static class FileSearchPath extends SearchPath {
        /**
         * Used to create File(parent, name). i.e., null means '.'
         */
        @Nullable
        private final File parent;

        FileSearchPath(@Nullable File path, LibPath libp) {
            super(libp);
            this.parent = path;
        }

        protected ResourceFinder contextOf(File dir) {
            return new FileSearchPath(dir, libPath().libPathAt(dir));
        }

        @Override
        public ConfigResource findResource(String name) {
            File file = getFile(name);
            if (file != null) {
                return ConfigResource.create(
                        contextOf(file.getParentFile()), file);
            } else {
                return null;
            }
        }

        @Override
        public String getDescription() {
            return toString();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("file:");
            if (parent != null) {
                sb.append(parent.getAbsolutePath());
            } else {
                sb.append('.');
            }
            return sb.toString();
        }

        protected File getFile(String name) {
            File file = new File(parent, name);
            if (file.isFile()) {
                return file;
            }
            for (String sfx : ConfigDescription.SUFFIXES) {
                file = new File(parent, name + "." + sfx);
                if (file.isFile()) {
                    return file;
                }
            }
            return null;
        }
    }

}
