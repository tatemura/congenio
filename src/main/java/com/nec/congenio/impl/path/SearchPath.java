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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nullable;

import com.nec.congenio.ConfigDescription;
import com.nec.congenio.ConfigException;
import com.nec.congenio.impl.ConfigResource;
import com.nec.congenio.impl.EvalContext;

public abstract class SearchPath implements ResourceFinder {
	public static SearchPath create(Properties props) {
		LibPath libs = LibPath.create(props);
		return new NoPath(libs);
	}

	public static ResourceFinder create(Class<?> cls, String prefix) {
		return new ResourceSearchPath(cls, prefix, new LibPath());
	}

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
			throw new ConfigException("unsupported path scheme: "
					+ scheme);
		}
		String name = exp.getPathPart();
		ConfigResource res = findResource(name);
		if (res != null) {
			return res;
		}
		List<String> descs = getDescription();
		String desc = descs.size() == 1 ? descs.get(0) : descs.toString();
        throw new ConfigException("Not found: "
    	        + name + "(."
    	        + Arrays.toString(ConfigDescription.SUFFIXES)
    	        + ") @ " + desc);
	}

	public static ConfigResource toResource(File file, Properties props) {
		/**
		 * NOTE: file.getParentFile() can be null
		 * (= ".")
		 */
		File dir = file.getParentFile();
		LibPath libs = LibPath.create(dir, props);

		return ConfigResource.create(new FileSearchPath(dir,
						libs), file);
	}

	public ConfigResource toResource(Class<?> cls, String path) {
		URL url = cls.getResource(path);
		if (url == null) {
			url = cls.getResource(path + ".xml");
		}
		if (url == null) {
			throw new ConfigException("resource not found: " + path);
		}
		String prefix = new File(path).getParent();
		if (prefix == null) {
			prefix = "";
		}
		return ConfigResource.create(
				new ResourceSearchPath(cls, prefix, libp), url);
	}

	public static ResourceFinder create(File dir, String libdef) {
		File baseDir = dir.isDirectory() ? dir : dir.getParentFile();
		LibPath libp = LibPath.create(libdef, baseDir);
		return new FileSearchPath(baseDir, libp);
		
	}
	public static ResourceFinder create(File dir, LibPath libp) {
		File baseDir = dir.isDirectory() ? dir : dir.getParentFile();
		return new FileSearchPath(baseDir, libp);
	}

	public static ResourceFinder create(File dir) {
		return create(dir, new LibPath());
	}

	@Nullable
	public abstract ConfigResource findResource(String name);


	public List<String> getDescription() {
		List<String> desc = new ArrayList<String>();
		addDescription(desc);
		return desc;
	}

	protected abstract void addDescription(List<String> desc);


	public static class NoPath extends SearchPath {

		public NoPath(LibPath libp) {
			super(libp);
		}

		@Override
		public ConfigResource findResource(String name) {
			return null;
		}

		@Override
		protected void addDescription(List<String> desc) {
		}
	}
	public static class ResourceSearchPath extends SearchPath {
		private final Class<?> cls;
		private final String prefix;

		public ResourceSearchPath(Class<?> cls, String prefix,
				LibPath libp) {
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
		protected void addDescription(List<String> desc) {
			desc.add("resource:" + cls.getName()
					+ "@'" + prefix + "'");
		}

	}

	public static class FileSearchPath extends SearchPath {
		/**
		 * Used to create File(parent, name).
		 * i.e., null means '.'
		 */
		@Nullable
		private final File parent;

		public FileSearchPath(@Nullable File path, LibPath libp) {
			super(libp);
			this.parent = path;
		}


		protected ResourceFinder contextOf(File dir) {
			return new FileSearchPath(dir,
					libPath().libPathAt(dir));
		}
		@Override
		public ConfigResource findResource(String name) {
			File file = getFile(name);
			if (file != null) {
				return ConfigResource.create(contextOf(file.getParentFile()), file);
			} else {
				return null;
			}
		}
		@Override
		protected void addDescription(List<String> desc) {
			StringBuilder sb = new StringBuilder();
			sb.append("file:[");
			if (parent != null) {
				sb.append(parent.getAbsolutePath());
			} else {
				sb.append('.');
			}
			sb.append("]");
			desc.add(sb.toString());
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("file:[");
			if (parent != null) {
				sb.append(parent.getAbsolutePath());
			} else {
				sb.append('.');
			}
			sb.append("]");
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
