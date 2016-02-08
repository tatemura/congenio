package com.nec.congenio.impl;

import java.util.ArrayList;
import java.util.List;

import com.nec.congenio.ConfigException;
import com.nec.congenio.impl.path.ResourceFinder;
import com.nec.congenio.impl.path.PathExpression;

public class EvalContext {
	private ResourceFinder pc;
	private PathExpression currentPath;
	private ConfigResource currentResource;
	private List<ConfigResource> resourceStack;

	public static EvalContext create(ConfigResource resource) {
		return new EvalContext(resource);
	}
	public EvalContext(ConfigResource resource) {
		this.pc = resource.getFinder();
		this.resourceStack = new ArrayList<ConfigResource>();
		this.currentResource = resource;
		this.resourceStack.add(resource);		
	}

	public EvalContext() {
		pc = new ResourceFinder() {

			@Override
			public ConfigResource getResource(PathExpression expr, EvalContext ctxt) {
				throw new ConfigException(
						"extension path is not allowed: " + expr);
			}
			
		};
		this.resourceStack = new ArrayList<ConfigResource>();
	}
	public EvalContext(PathExpression path,
			ConfigResource resource,
			List<ConfigResource> resourceStack) {
		this.pc = resource.getFinder();
		this.currentPath = path;
		this.resourceStack = new ArrayList<ConfigResource>();
		this.currentResource = resource;
		this.resourceStack.add(resource);
		this.resourceStack.addAll(resourceStack);
	}

	private PathExpression parse(String pathExpr) {
		try {
			return PathExpression.parse(pathExpr);
		} catch (ConfigException e) {
			printResourceTrace();
			throw e;
		}
		
	}

	public EvalContext of(String pathExpr) {
		PathExpression expr = parse(pathExpr);
		try {
			ConfigResource resource =
					pc.getResource(expr, this);
			return new EvalContext(
					expr, resource, resourceStack);
		} catch (ConfigException e) {
			printResourceTrace();
			throw e;
		}
	}

	public ConfigResource getCurrentResource() {
		return currentResource;
	}

	public void printResourceTrace() {
		for (ConfigResource r : resourceStack) {
			System.err.println(r.getURI());
		}
	}
	public boolean hasDocPath() {
		return currentPath != null
				&& !currentPath.getDocPath().isEmpty();
	}
	public String getDocPath() {
		return currentPath.getDocPath();
	}
}
