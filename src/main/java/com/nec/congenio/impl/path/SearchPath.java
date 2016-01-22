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
import com.nec.congenio.PropertyNames;
import com.nec.congenio.impl.ConfigPath;
import com.nec.congenio.impl.ConfigResource;
import com.nec.congenio.impl.PathContext;
import com.nec.congenio.impl.ResourcePointer;

public abstract class SearchPath implements PathContext {
	public static SearchPath create(Properties props) {
		String libdef = props.getProperty(PropertyNames.PROP_LIBS);
		if (libdef != null) {
			LibPathContext libs = LibPathContext.create(libdef);
			return new FileSearchPath(new ArrayList<File>(), libs);
		}
		return new NoSearchPath();
	}

	public static PathContext create(Class<?> cls, String prefix) {
		return new ResourceSearchPath(cls, prefix, new NoSearchPath());
	}

	private final LibPathContext libp;
	private final SysPath sysp = new SysPath();
	public SearchPath() {
		libp = new LibPathContext();
	}
	public SearchPath(LibPathContext libp) {
		this.libp = libp;
	}

	protected LibPathContext libPath() {
		return libp;
	}
	public ConfigPath interpret(String pathExpr) {
		PathExpression exp = PathExpression.parse(pathExpr);
		ResourcePointer rp = new ResourcePointerImpl(this, exp);
		return new ConfigPath(rp, exp.getDocPath());
	}

	public ConfigResource toResource(File file) {
		/**
		 * NOTE: file.getParentFile() can be null
		 * (= ".")
		 */
		List<File> paths = Arrays.asList(file.getParentFile());
		return ConfigResource.create(new FileSearchPath(paths, libp, this), file);
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
				new ResourceSearchPath(cls, prefix, this), url);
	}

	public static PathContext create(File dir, String libdef) {
		File baseDir = dir.isDirectory() ? dir : dir.getParentFile();
		LibPathContext libp = LibPathContext.create(libdef, baseDir);
		return new FileSearchPath(Arrays.asList(baseDir), libp);
		
	}
	public static PathContext create(File dir, LibPathContext libp) {
		if (dir.isDirectory()) {
			return new FileSearchPath(Arrays.asList(dir), libp);
		} else {
			return new FileSearchPath(Arrays.asList(dir.getParentFile()), libp);
		}
	}
	public static PathContext create(File dir) {
		return create(dir, new LibPathContext());
	}

	@Nullable
	public abstract ConfigResource findResource(String name);

	private ConfigResource getResource(PathExpression exp) {
		String scheme = exp.getScheme();
		if (LibPathContext.SCHEME.equals(scheme)) {
			return libp.getResource(exp);
		} else if (SysPath.SCHEME.equals(scheme)) {
			return sysp.getResource(exp);
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
			String path = prefix + name;
			URL url = cls.getResource(path);
			if (url == null) {
				url = cls.getResource(path + ".xml");
			}
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
		public FileSearchPath(List<File> paths, LibPathContext libp) {
			this(paths, libp, new NoSearchPath());
		}

		public FileSearchPath(List<File> paths, SearchPath base) {
			this.paths = paths;
			this.base = base;
		}
		public FileSearchPath(List<File> paths, LibPathContext libp,
				SearchPath base) {
			super(libp);
			this.paths = paths;
			this.base = base;
		}
		public ConfigResource toResource(File file) {
			return ConfigResource.create(push(file.getParentFile()), file);
		}
		protected PathContext push(File dir) {
			List<File> newpath = new ArrayList<File>(
					paths.size() + 1);
			newpath.add(dir);
			newpath.addAll(paths);
			return new FileSearchPath(newpath, this.libPath(), base);
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
	static class ResourcePointerImpl implements ResourcePointer {
		private final SearchPath sp;
		private final PathExpression exp;
		public ResourcePointerImpl(SearchPath sp, PathExpression exp) {
			this.sp = sp;
			this.exp = exp;
		}
		@Override
		public ConfigResource getResource() {
			return sp.getResource(exp);
		}
	}
}
