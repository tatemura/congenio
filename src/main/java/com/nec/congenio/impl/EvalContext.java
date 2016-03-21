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

package com.nec.congenio.impl;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.nec.congenio.ConfigException;
import com.nec.congenio.impl.path.ResourceFinder;
import com.nec.congenio.impl.path.PathExpression;

/**
 * A class that encapsulates the context of a
 * document that is being evaluated.
 * @author tatemura
 *
 */
public class EvalContext {
    private final ResourceFinder finder;
    private PathExpression currentPath;
    private ConfigResource currentResource;
    private final List<ConfigResource> resourceStack;

    public static EvalContext create(ConfigResource resource) {
        return new EvalContext(resource);
    }

    /**
     * Creates an initial evaluation context starting
     * from the given resource.
     * @param resource the resource that corresponds
     *        to the current (top-level) document.
     */
    public EvalContext(ConfigResource resource) {
        this.finder = resource.getFinder();
        this.resourceStack = new ArrayList<ConfigResource>();
        this.currentResource = resource;
        this.resourceStack.add(resource);
    }

    /**
     * Creates a closed evaluation context that disallows
     * extension references to external documents.
     */
    public EvalContext() {
        this.finder = new ResourceFinder() {

            @Override
            public ConfigResource getResource(
                    PathExpression expr, EvalContext ctxt) {
                throw new ConfigException(
                        "extension path is not allowed: " + expr);
            }

        };
        this.resourceStack = new ArrayList<ConfigResource>();
    }

    protected EvalContext(PathExpression path,
            ConfigResource resource, List<ConfigResource> resourceStack) {
        this.finder = resource.getFinder();
        this.currentPath = path;
        this.resourceStack = new ArrayList<ConfigResource>();
        this.currentResource = resource;
        this.resourceStack.add(resource);
        this.resourceStack.addAll(resourceStack);
    }

    private PathExpression parse(String pathExpr) {
        try {
            return PathExpression.parse(pathExpr);
        } catch (ConfigException ex) {
            printResourceTrace();
            throw ex;
        }

    }

    /**
     * Gets the (next) context to evaluate the
     * document referred to with the given path
     * expression that is found in the current context.
     * @param pathExpr the path expression that refers to
     *        another document.
     * @return the evaluate context to evaluate
     *         the referred document.
     */
    public EvalContext of(String pathExpr) {
        PathExpression expr = parse(pathExpr);
        try {
            ConfigResource resource = finder.getResource(expr, this);
            return new EvalContext(expr, resource, resourceStack);
        } catch (ConfigException ex) {
            printResourceTrace();
            throw ex;
        }
    }

    public ConfigResource getCurrentResource() {
        return currentResource;
    }

    public void printResourceTrace() {
        printResourceTrace(System.err);
    }

    /**
     * Prints the stack of evaluation contexts
     * (each of which corresponds to a document).
     * @param out the stream to which the stack
     *        is printed out.
     */
    public void printResourceTrace(PrintStream out) {
        for (ConfigResource r : resourceStack) {
            out.println(r.getUri());
        }
    }

    public boolean hasDocPath() {
        return currentPath != null && !currentPath.getDocPath().isEmpty();
    }

    /**
     * Gets a doc path of the current path.
     * @return an empty string if there is no
     *         doc path.
     */
    public String getDocPath() {
        if (currentPath == null) {
            return "";
        }
        return currentPath.getDocPath();
    }
}
