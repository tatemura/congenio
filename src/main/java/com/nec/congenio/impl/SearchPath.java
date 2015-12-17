/*******************************************************************************
 *   Copyright 2015 Junichi Tatemura
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
package com.nec.congenio.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.nec.congenio.ConfigDescription;
import com.nec.congenio.ConfigException;

public abstract class SearchPath {
	public static SearchPath create() {
		File conf = new File(".cdglpath");
		if (conf.isFile()) {
			return new FileSearchPath(readPaths(conf));
		}

		return new NoSearchPath();
	}
	public static SearchPath none() {
		return new NoSearchPath();
	}
	public static SearchPath create(Class<?> cls, String prefix) {
		return new ResourceSearchPath(cls, prefix, new NoSearchPath());
	}

	private static List<File> readPaths(File conf) {
		List<File> paths = new ArrayList<File>();
		InputStream istr = null;
		BufferedReader r = null;
		try {
			istr = new FileInputStream(conf);
			InputStreamReader isr = new InputStreamReader(istr, Charset.forName("UTF-8"));
			r = new BufferedReader(isr);
			String line;
			while ((line = r.readLine()) != null) {
				paths.add(new File(line.trim()));
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			/**
			 * ignore
			 * TODO WARN
			 */
		} finally {
			if (istr != null) {
				try {
					istr.close();
				} catch (IOException e) {
				}
			}
			if (r != null) {
				try {
					r.close();
				} catch (IOException e) {
				}
			}
		}
		return paths;
	}
	public SearchPath() {
	}

	public ConfigPath getPath(String pathExpr) {
		return new ConfigPath(this, pathExpr);
	}
	public ConfigResource toResource(File file) {
		/**
		 * NOTE: file.getParentFile() can be null
		 * (= ".")
		 */
		List<File> paths = Arrays.asList(file.getParentFile());
		return ConfigResource.create(new FileSearchPath(paths, this), file);
	}

	@Nullable
	public abstract ConfigResource findResource(String name);

	public ConfigResource getResource(String name) {
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

	public List<String> getDescription() {
		List<String> desc = new ArrayList<String>();
		addDescription(desc);
		return desc;
	}

	protected abstract void addDescription(List<String> desc);

	public static class NoSearchPath extends SearchPath {

		public NoSearchPath() {
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
		private final SearchPath base;
		private final String prefix;
		public ResourceSearchPath(Class<?> cls, String prefix, SearchPath base) {
			this.cls = cls;
			this.base = base;
			this.prefix = prefix;
		}
		@Override
		public ConfigResource findResource(String name) {
			URL url = cls.getResource(prefix + name);
			if (url != null) {
				return ConfigResource.create(this, url);
			} else {
				return base.findResource(name);
			}
		}
		@Override
		protected void addDescription(List<String> desc) {
			desc.add("resource:" + cls.getName()
					+ "@'" + prefix + "'");
			base.addDescription(desc);
		}

	}

	public static class FileSearchPath extends SearchPath {
		/**
		 * NOTE it may contain null element ('.')
		 */
		private final List<File> paths;
		private final SearchPath base;
		public FileSearchPath(List<File> paths) {
			this(paths, new NoSearchPath());
		}
		public FileSearchPath(List<File> paths, SearchPath base) {
			this.paths = paths;
			this.base = base;
		}
		public ConfigResource toResource(File file) {
			return ConfigResource.create(push(file.getParentFile()), file);
		}
		protected SearchPath push(File dir) {
			List<File> newpath = new ArrayList<File>(
					paths.size() + 1);
			newpath.add(dir);
			newpath.addAll(paths);
			return new FileSearchPath(newpath, base);
		}
		@Override
		public ConfigResource findResource(String name) {
			File file = getFile(name);
			if (file != null) {
				return ConfigResource.create(push(file.getParentFile()), file);
			} else {
				return base.findResource(name);
			}
		}
		@Override
		protected void addDescription(List<String> desc) {
			StringBuilder sb = new StringBuilder();
			sb.append("file:[");
			for (int i = 0; i < paths.size(); i++) {
				if (i > 0) {
					sb.append(", ");
				}
				File f = paths.get(i);
				if (f != null) {
					sb.append(f.getAbsolutePath());
				} else {
					sb.append('.');
				}
			}
			sb.append("]");
			desc.add(sb.toString());
			base.addDescription(desc);
		}


		protected File getFile(String name) {
			for (File base : paths) {
	            File file = new File(base, name);
	            if (file.isFile()) {
	            	return file;
	            }
	            for (String sfx : ConfigDescription.SUFFIXES) {
	            	file = new File(base, name + "." + sfx);
	            	if (file.isFile()) {
	            		return file;
	            	}
	            }
	        	file = new File(base, name + ".json");
	        	if (file.isFile()) {
	        		return file;
	        	}
			}
			return null;
		}
	}
}
