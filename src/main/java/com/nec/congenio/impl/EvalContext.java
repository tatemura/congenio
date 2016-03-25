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

import org.w3c.dom.Element;

import com.nec.congenio.ConfigException;
import com.nec.congenio.impl.path.ResourceFinder;
import com.nec.congenio.value.xml.XmlValueFormat;
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
    private final List<StackElement> resourceStack;

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
        this.resourceStack = new ArrayList<StackElement>();
        this.currentResource = resource;
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
        this.resourceStack = new ArrayList<StackElement>();
    }

    protected EvalContext(PathExpression path,
            ConfigResource resource, List<StackElement> resourceStack) {
        this.finder = resource.getFinder();
        this.currentPath = path;
        this.resourceStack = resourceStack;
        this.currentResource = resource;
    }

    private PathExpression parse(String pathExpr, Element at) {
        try {
            return PathExpression.parse(pathExpr);
        } catch (ConfigException ex) {
            printResourceTrace();
            throw new ConfigEvalException(
                    "failed to parse path: " + pathExpr
                    + " at " + XmlValueFormat.path(at), this, ex);
        }

    }

    /**
     * Gets the (next) context to evaluate the
     * document referred to with the given path
     * expression that is found in the current context.
     * @param pathExpr the path expression that refers to
     *        another document.
     * @param at the element there the path is placed.
     * @return the evaluate context to evaluate
     *         the referred document.
     */
    public EvalContext of(String pathExpr, Element at) {
        PathExpression expr = parse(pathExpr, at);
        try {
            ConfigResource resource = finder.getResource(expr, this);
            return new EvalContext(expr, resource, newStack(at));
        } catch (ConfigException ex) {
            String msg = "failed to find resource: " + pathExpr
                    + " at " + XmlValueFormat.path(at);
            throw  new ConfigEvalException(msg, this, ex);
        }
    }

    private List<StackElement> newStack(Element elem) {
        List<StackElement> stack = new ArrayList<StackElement>();
        stack.add(new StackElement(currentResource, elem));
        stack.addAll(resourceStack);
        return stack;
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
        out.print("    at ");
        out.println(currentResource.getUri());
        for (StackElement s : resourceStack) {
            out.print("    at ");
            out.println(s.toString());
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

    public static class StackElement {
        private final ConfigResource resource;
        private final Element element;

        public StackElement(ConfigResource resource, Element element) {
            this.resource = resource;
            this.element = element;
        }

        public Element getElement() {
            return element;
        }

        public ConfigResource getResource() {
            return resource;
        }

        @Override
        public String toString() {
            return resource.getUri()
                    + "#"
                    + XmlValueFormat.path(element)
                    // replace '/' with '#'
                    .substring(1);
        }
    }
}
