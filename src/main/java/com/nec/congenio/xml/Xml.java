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

package com.nec.congenio.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nec.congenio.ConfigException;

public final class Xml {
    static final XPath XPATH = XPathFactory.newInstance().newXPath();

    private Xml() {
        // not instantiated
    }

    /**
     * Parses an XML document from the given string.
     * @param input a string expression of an XML document.
     * @return a parsed XML document.
     * @throws InvalidXmlException if parsing failed.
     */
    public static Document parse(String input) {
        return parse(new ByteArrayInputStream(input.getBytes()));
    }

    /**
     * Parses an XML document in the given input stream.
     * @param instr an input stream from which a document
     *        is read. The input stream is NOT closed by
     *        this method.
     * @return the parsed XML document.
     * @throws InvalidXmlException if parsing failed.
     */
    public static Document parse(InputStream instr) {
        try {
            DocumentBuilder builder =
                    DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            return builder.parse(instr);
        } catch (ParserConfigurationException ex) {
            throw new InvalidXmlException(
                    "parse failed due to configuration exception", ex);
        } catch (SAXException ex) {
            throw new InvalidXmlException(
                    "parse failed due to SAX exception", ex);
        } catch (IOException ex) {
            throw new InvalidXmlException(
                    "parse failed due to IO exception", ex);
        }
    }

    /**
     * Parses an XML document from the resource specified
     * with a URL.
     * @param resource the URL of the XML document.
     * @return the parsed XML document.
     * @throws InvalidXmlException if parsing failed.
     */
    public static Document parse(URL resource) {
        try {
            DocumentBuilder builder =
                    DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            if (builder == null) {
                throw new InvalidXmlException("document builder not created");
            }
            if (resource == null) {
                throw new InvalidXmlException("resource URL is null");
            }
            return builder.parse(resource.toString());
        } catch (ParserConfigurationException ex) {
            throw new InvalidXmlException(
                    "parse failed due to configuration exception", ex);
        } catch (SAXException ex) {
            throw new InvalidXmlException(
                    "parse failed due to SAX exception", ex);
        } catch (IOException ex) {
            throw new InvalidXmlException(
                    "parse failed due to IO exception", ex);
        }
    }

    /**
     * Parses an XML text file as a W3C DOM document.
     * 
     * @param file the file that contains an XML document.
     * @return parsed Document
     * @throws InvalidXmlException if parsing failed.
     */
    public static Document parse(File file) {
        try {
            DocumentBuilder builder =
                    DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            return builder.parse(file);
        } catch (ParserConfigurationException ex) {
            throw new InvalidXmlException(
                    "parse failed due to configuration exception", ex);
        } catch (SAXException ex) {
            throw new InvalidXmlException(
                    "parse failed due to SAX exception", ex);
        } catch (IOException ex) {
            throw new InvalidXmlException(
                    "parse failed due to IO exception", ex);
        }
    }

    /**
     * Creates a new W3C DOM XML document
     * 
     * @return a new document.
     * @throws InvalidXmlException if document creation failed
     *         (due to configuration errors).
     */
    public static Document createDocument() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.newDocument();
        } catch (FactoryConfigurationError ex) {
            throw new InvalidXmlException(
                    "failed to create a document", ex);
        } catch (ParserConfigurationException ex) {
            throw new InvalidXmlException(
                    "failed to create a document", ex);
        }
    }

    // XML DOM utilities

    /**
     * Gets a map of attributes associated with the given node.
     * @param node the node (element) from which attributes
     *        are found.
     * @return a map of attribute name-value pairs.
     *         An empty map if there is no attribute at the node.
     */
    public static Map<String, String> getAttributes(Node node) {
        Map<String, String> result = new HashMap<String, String>();
        NamedNodeMap map = node.getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
            Attr attr = (Attr) map.item(i);
            result.put(attr.getName(), attr.getValue());
        }
        return result;
    }

    /**
     * Gets the value of an attribute at the given node.
     * @param name the name of the attribute.
     * @param node the node that has the attribute
     *        to be got.
     * @return the value of the attribute.
     * @throws InvalidXmlException if the node is not
     *         an element or if it has no such attribute.
     */
    public static String getAttribute(String name, Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            String value = ((Element) node).getAttribute(name);
            if (value == null || value.length() == 0) {
                throw new InvalidXmlException("attribute not found: @" + name);
            }
            return value;
        } else {
            throw new InvalidXmlException("not Element");
        }
    }

    /**
     * Gets the value of an attribute at the given node
     * if exists.
     * @param name the name of the attribute.
     * @param node the node that has the attribute
     *        to be got.
     * @param defaultValue the default value which
     *        is returned when the attribute is not found.
     * @return the value of the attribute.
     * @throws InvalidXmlException if the node is not
     *         an element.
     */
    public static String getAttribute(String name,
            Node node, String defaultValue) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            String value = ((Element) node).getAttribute(name);
            if (value == null || value.length() == 0) {
                return defaultValue;
            }
            return value;
        } else {
            throw new InvalidXmlException("not Element");
        }
    }

    /**
     * Gets the integer value of the attribute at the
     * given node.
     * @param name the name of the attribute.
     * @param node the node that has the attribute.
     * @param defaultValue  a default value which is returned
     *        when the attribute is not found.
     * @return an integer value of the attribute.
     * @throws InvalidXmlException if the node is not an
     *        element or if the attribute value is not a valid integer.
     */
    public static int getAttributeInt(String name,
            Node node, int defaultValue) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            String value = ((Element) node).getAttribute(name);
            if (value == null || value.length() == 0) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                throw new InvalidXmlException(
                        "bad number format:@" + name + "=" + value);
            }
        } else {
            throw new InvalidXmlException("not Element");
        }
    }

    @Nullable
    public static Element getSingleElement(String path, Node node) {
        return getSingleElement(path, node, true);
    }

    /**
     * Gets an element specified with an XPath
     * 
     * @param path an XPath that refers to the single element.
     * @param node the node to be searched.
     * @param mandate true if the match is mandate.
     * @return null if no element matches and mandate is false
     */
    @Nullable
    public static Element getSingleElement(String path,
            Node node, boolean mandate) {
        try {
            Node result = (Node) XPATH.evaluate(
                    path, node, XPathConstants.NODE);
            if (result == null) {
                if (mandate) {
                    throw new InvalidXmlException("no element matched:" + path);
                } else {
                    return null;
                }
            } else if (result.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) result;
            } else {
                throw new InvalidXmlException("not Element");
            }
        } catch (XPathExpressionException ex) {
            throw new InvalidXmlException("xpath error", ex);
        }

    }

    /**
     * Gets a list of elements that matches with the path
     * expression.
     * @param path the path expression use to find elements.
     * @param node the node where the path is applied.
     * @return an empty list when no match is found.
     */
    public static List<Element> getElements(String path, Node node) {
        try {
            NodeList nlist = (NodeList) XPATH.evaluate(
                    path, node, XPathConstants.NODESET);
            List<Element> elements =
                    new ArrayList<Element>(nlist.getLength());
            for (int i = 0; i < nlist.getLength(); i++) {
                Node matched = nlist.item(i);
                if (matched.getNodeType() == Node.ELEMENT_NODE) {
                    elements.add((Element) matched);
                } else {
                    throw new InvalidXmlException("not Element");
                }
            }
            return elements;
        } catch (XPathExpressionException ex) {
            throw new InvalidXmlException("xpath error", ex);
        }

    }

    /**
     * Gets the (immediate) child elements of the given node.
     * @param node the node from where child elements are found.
     * @return an empty list when the node has no element.
     */
    public static List<Element> getElements(Node node) {
        NodeList nlist = node.getChildNodes();
        List<Element> elements = new ArrayList<Element>();
        for (int i = 0; i < nlist.getLength(); i++) {
            Node child = nlist.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) child);
            }
        }
        return elements;

    }

    /**
     * Gets the (immediate) child nodes of the given node.
     * @param node the node from where children are found.
     * @return an empty list when the node has no child.
     */
    public static List<Node> getChildren(Node node) {
        NodeList nlist = node.getChildNodes();
        List<Node> nodes = new ArrayList<Node>();
        for (int i = 0; i < nlist.getLength(); i++) {
            Node child = nlist.item(i);
            nodes.add(child);
        }
        return nodes;
    }

    public static Element getFirstElement(Node node) {
        return getFirstElement(node, true);
    }

    /**
     * Gets the first child element of the given node.
     * @param node the node whose first child is retrieved.
     * @param mandate true to indicate the first child must
     *        exist.
     * @return null if mandate is set false and there is no
     *         child of the given node.
     * @throws InvalidXmlException if mandate is set true and
     *         there is no child of the given node.
     */
    @Nullable
    public static Element getFirstElement(Node node, boolean mandate) {
        NodeList nlist = node.getChildNodes();
        for (int i = 0; i < nlist.getLength(); i++) {
            Node child = nlist.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) child;
            }
        }
        if (mandate) {
            throw new InvalidXmlException("no child element found");
        }
        return null;
    }

    @Nullable
    public static Element getFirstElementIfExists(Node node) {
        return getFirstElement(node, false);
    }

    /**
     * Converts an XML node to its text representation
     * with indentation.
     * @param node the node to be converted.
     * @return the text representation of the node.
     */
    public static String toString(Node node) {
        StringWriter writer = new StringWriter();
        write(node, writer, false, true);
        return writer.toString();
    }

    public static void write(Document doc, Writer writer) {
        write(doc.getDocumentElement(), writer, true, true);
    }

    public static void write(Node node, Writer writer) {
        write(node, writer, false, false);
    }

    /**
     * Writes a text representation of an XML node into
     * a writer.
     * @param node the node to be written.
     * @param writer the writer used to write.
     * @param xmldecl true to place XML declaration at
     *        the beginning.
     * @param indent true to indent.
     */
    public static void write(Node node, Writer writer,
            boolean xmldecl, boolean indent) {
        try {
            TransformerFactory factory =
                    TransformerFactory.newInstance();
            Transformer trans = factory.newTransformer();
            if (!xmldecl) {
                trans.setOutputProperty("omit-xml-declaration", "yes");
            }
            if (indent) {
                trans.setOutputProperty(OutputKeys.INDENT, "yes");
                trans.setOutputProperty(
                        "{http://xml.apache.org/xslt}indent-amount", "2");
            }
            DOMSource src = new DOMSource();
            src.setNode(node);
            StreamResult dst = new StreamResult();
            dst.setWriter(writer);
            trans.transform(src, dst);

        } catch (TransformerConfigurationException ex) {
            throw new ConfigException("cannot transform XML node", ex);
        } catch (TransformerFactoryConfigurationError ex) {
            throw new ConfigException("cannot transform XML node", ex);
        } catch (TransformerException ex) {
            throw new ConfigException("cannot transform XML node", ex);
        }
    }

}
