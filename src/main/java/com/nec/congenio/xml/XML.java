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



public final class XML {
	static final XPath XPATH = XPathFactory.newInstance().newXPath();

	private XML() {
		// not instantiated
	}
	public static Document parse(String input) {
		return parse(new ByteArrayInputStream(input.getBytes()));
	}

	public static Document parse(InputStream instr) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
            return builder.parse(instr);
        } catch (ParserConfigurationException e) {
			throw new InvalidXMLException(
			  "parse failed due to configuration exception", e);
        } catch (SAXException e) {
			throw new InvalidXMLException(
			  "parse failed due to SAX exception", e);
		} catch (IOException e) {
			throw new InvalidXMLException(
			  "parse failed due to IO exception", e);
		}
	}

    public static Document parse(URL resource) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            if (builder == null) {
            	throw new InvalidXMLException("document builder not created");
            }
            if (resource == null) {
            	throw new InvalidXMLException("resource URL is null");
            }
            return builder.parse(resource.toString());
        } catch (ParserConfigurationException e) {
			throw new InvalidXMLException(
			  "parse failed due to configuration exception", e);
        } catch (SAXException e) {
			throw new InvalidXMLException(
			  "parse failed due to SAX exception", e);
		} catch (IOException e) {
			throw new InvalidXMLException(
			  "parse failed due to IO exception", e);
		}
    }

    /**
     * Parses an XML text file as a W3C DOM document.
     * @param file
     * @return parsed Document
     */
    public static Document parse(File file) {
        try {
            // Parse the XML as a W3C document.
            DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            return builder.parse(file);
        } catch (ParserConfigurationException e) {
			throw new InvalidXMLException(
			   "parse failed due to configuration exception", e);
        } catch (SAXException e) {
			throw new InvalidXMLException(
			   "parse failed due to SAX exception", e);
        } catch (IOException e) {
			throw new InvalidXMLException(
			   "parse failed due to IO exception", e);
        }
    }
    /**
     * Creates a new W3C DOM XML document
     * @return a new document.
     */
	public static Document createDocument() {
		try {
			DocumentBuilderFactory factory =
			    DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.newDocument();
		} catch (FactoryConfigurationError e) {
			throw new InvalidXMLException(
			    "failed to create a document", e);
		} catch (ParserConfigurationException e) {
			throw new InvalidXMLException(
			    "failed to create a document", e);
		}
	}

	// XML DOM utilities
	public static Map<String, String> getAttributes(Node n) {
	    Map<String, String> result = new HashMap<String, String>();
        NamedNodeMap map = n.getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
            Attr attr = (Attr) map.item(i);
            result.put(attr.getName(), attr.getValue());
        }
	    return result;
	}

	public static String getAttribute(String name, Node n) {
		if (n.getNodeType() == Node.ELEMENT_NODE) {
			String value = ((Element) n).getAttribute(name);
			if (value == null || value.length() == 0) {
				throw new InvalidXMLException(
				        "attribute not found: @" + name);
			}
			return value;
		} else {
			throw new InvalidXMLException("not Element");
		}
	}
	public static String getAttribute(String name,
	        Node n, String defaultValue) {
		if (n.getNodeType() == Node.ELEMENT_NODE) {
			String value = ((Element) n).getAttribute(name);
			if (value == null || value.length() == 0) {
				return defaultValue;
			}
			return value;
		} else {
			throw new InvalidXMLException("not Element");
		}
	}
	public static int getAttributeInt(String name,
	        Node n, int defaultValue) {
		if (n.getNodeType() == Node.ELEMENT_NODE) {
			String value = ((Element) n).getAttribute(name);
			if (value == null || value.length() == 0) {
				return defaultValue;
			}
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new InvalidXMLException(
				   "bad number format:@" + name + "=" + value);
			}
		} else {
			throw new InvalidXMLException("not Element");
		}
	}

	@Nullable
	public static Element getSingleElement(String path, Node n) {
		return getSingleElement(path, n, true);
	}
	/**
	 * Gets an element specified with an XPath
	 * @param path
	 * @param n
	 * @param mandate
	 * @return null if no element matches and mandate is false
	 */
	@Nullable
	public static Element getSingleElement(String path,
	        Node n, boolean mandate) {
		try {
			Node node = (Node) XPATH.evaluate(path, n,
			        XPathConstants.NODE);
			if (node == null) {
				if (mandate) {
					throw new InvalidXMLException(
					        "no element matched:" + path);
				} else {
					return null;
				}
			} else if (node.getNodeType() == Node.ELEMENT_NODE) {
				return (Element) node;
			} else {
				throw new InvalidXMLException("not Element");
			}
		} catch (XPathExpressionException e) {
			throw new InvalidXMLException("xpath error", e);
		}

	}

	public static List<Element> getElements(String path, Node n) {
		try {
			NodeList nlist = (NodeList) XPATH.evaluate(
			        path, n, XPathConstants.NODESET);
			List<Element> elements =
			    new ArrayList<Element>(nlist.getLength());
			for (int i = 0; i < nlist.getLength(); i++) {
				Node node = nlist.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					elements.add((Element) node);
				} else {
					throw new InvalidXMLException(
					        "not Element");
				}
			}
			return elements;
		} catch (XPathExpressionException e) {
			throw new InvalidXMLException("xpath error", e);
		}

	}
	public static List<Element> getElements(Node n) {
			NodeList nlist = n.getChildNodes();
			List<Element> elements = new ArrayList<Element>();
			for (int i = 0; i < nlist.getLength(); i++) {
				Node node = nlist.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					elements.add((Element) node);
				}
			}
			return elements;

	}
	public static List<Node> getChildren(Node n) {
	    NodeList nlist = n.getChildNodes();
        List<Node> nodes = new ArrayList<Node>();
        for (int i = 0; i < nlist.getLength(); i++) {
            Node node = nlist.item(i);
            nodes.add(node);
        }
        return nodes;
	}

	public static Element getFirstElement(Node n) {
		return getFirstElement(n, true);
	}

	@Nullable
	public static Element getFirstElementIfExists(Node n) {
		return getFirstElement(n, false);
	}


	@Nullable
	public static Element getFirstElement(Node n, boolean mandate) {
		NodeList nlist = n.getChildNodes();
		for (int i = 0; i < nlist.getLength(); i++) {
			Node child = nlist.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				return (Element) child;
			}
		}
		if (mandate) {
			throw new InvalidXMLException("no child element found");
		}
		return null;
	}

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
	public static void write(Node node,
			Writer writer, boolean xmldecl, boolean indent) {
		try {
			TransformerFactory factory =
					TransformerFactory.newInstance();
			Transformer trans = factory.newTransformer();
			if (!xmldecl) {
				trans.setOutputProperty("omit-xml-declaration",
						"yes");
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

		} catch (TransformerConfigurationException e) {
			throw new ConfigException(
					"cannot transform XML node",
					e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new ConfigException(
					"cannot transform XML node",
					e);
		} catch (TransformerException e) {
			throw new ConfigException(
					"cannot transform XML node",
					e);
		}
	}

}
