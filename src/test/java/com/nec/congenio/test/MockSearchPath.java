package com.nec.congenio.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.nec.congenio.impl.ConfigResource;
import com.nec.congenio.impl.SearchPath;
import com.nec.congenio.xml.XML;

public class MockSearchPath extends SearchPath {
	private final Map<String, Element> repo =
			new HashMap<String, Element>();

	public static MockSearchPath create(Element root) {
		MockSearchPath path = new MockSearchPath();
		for (Element e : XML.getElements(root)) {
			String name = XML.getAttribute("name", e, e.getTagName());
			path.set(name, e);
		}
		return path;
	}

	public MockSearchPath() {
	}
	public void set(String name, Element src) {
		repo.put(name, src);
	}

	@Override
	public ConfigResource findResource(String name) {
		Element e = repo.get(name);
		if (e != null) {
			return new MockResource(this, name, e);
		}
		return null;
	}
	@Override
	protected void addDescription(List<String> desc) {
		desc.add("map:" + repo.keySet().toString());
	}

	static class MockResource extends ConfigResource {
		private final SearchPath path;
		private final String name;
		private final Element src;
		public MockResource(SearchPath path, String name,
				Element src) {
			this.path = path;
			this.name = name;
			this.src = src;
		}
		@Override
		public Element createElement() {
			return (Element) src.cloneNode(true);
		}

		@Override
		public SearchPath searchPath() {
			return path;
		}

		@Override
		public String getURI() {
			return name;
		}
		
	}

}
