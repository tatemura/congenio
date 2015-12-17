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
package com.nec.congenio.value.xml;

import java.io.File;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.nec.congenio.ConfigValue;
import com.nec.congenio.ValueFormat;
import com.nec.congenio.xml.XML;

public class XMLValueFormat implements ValueFormat {

	@Override
	public ConfigValue parse(String data) {
		return create(XML.parse(data));
	}

	@Override
	public ConfigValue parse(URL url) {
		return create(XML.parse(url));
	}

	@Override
	public ConfigValue parse(File file) {
		return create(XML.parse(file));
	}
	
	@Override
	public ConfigValue parse(InputStream instr) {
		return create(XML.parse(instr));
	}

	private ConfigValue create(Document doc) {
		return XMLValue.create(doc.getDocumentElement());
	}

	@Override
    public void write(ConfigValue conf, Writer writer, boolean indent) {
		if (conf instanceof XMLValue) {
	    	Element e = ((XMLValue) conf).toXML();
	    	XML.write(e, writer, false, indent);
		} else {
			Element e = conf.toXML(conf.getName());
	    	XML.write(e, writer, false, indent);
		}
    }

}
