/*
 * Sonar .NET Plugin :: ReSharper
 * Copyright (C) 2013 John M. Wright
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.wrightfully.sonar.plugins.dotnet.resharper.profiles;


import java.io.Reader;

import java.io.StringReader;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.RulePriority;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.wrightfully.sonar.dotnet.tools.resharper.ReSharperException;
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperConstants;
import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.ReSharperUtils.ReSharperSeverity;

/**
 * Creates ReSharper rule repositories for every language supported by ReSharper.
 */
@Properties({
        @Property(key = ReSharperConstants.CUSTOM_SEVERITIES_PROP_KEY,
        defaultValue = "", name = "ReSharper custom severities",
        description = "Add &lt;IssueType&gt; values from ReSharper's results file for issues that are not built-in to the plugin's rules. A restart is required to take affect.",
                type = PropertyType.TEXT, global = true, project = false)
})
public class CustomSeverities {

    private static final Logger LOG = LoggerFactory.getLogger(CustomSeverities.class);
    
    private Reader reader;
    CustomSeveritiesMap severities = new CustomSeveritiesMap();
	
	public CustomSeverities setReader(Reader reader) {
		this.reader = reader;
		return this;
	}
	
	
	/**
	 * Get a map indexed by rulekey, and severity as attribute
	 * @return
	 * @throws XPathExpressionException
	 * @throws ReSharperException 
	 */
	public CustomSeveritiesMap parse() throws ReSharperException {
		severities = new CustomSeveritiesMap();
		NodeList nodes=getStringNodes();
		for(int nodeIndex=0;nodeIndex < nodes.getLength();nodeIndex++) {
			Node node = nodes.item(nodeIndex);
			addCustomSeverity(node);
		}
		return severities;
	}

    /**
     * parse the given string, which is a xml document such as the resharper dotsettings file.
     * If there is an error the error is logged, and an empty map is returned.
     * @param customSeverities
     * @return CustomSeveritiesMap
     */
    public CustomSeveritiesMap parseString(String customSeverities) {
        severities = new CustomSeveritiesMap();
        if (StringUtils.isNotEmpty(customSeverities)) {
            setReader(new StringReader(customSeverities));
            try {
                severities = parse();
            } catch (ReSharperException e) {
                LOG.error("Could not get custom severities, error during parsing (ignoring) " + e.getMessage());
            }
        }
        return severities;
    }

	private void addCustomSeverity(Node node){
		try {
			tryAddCustomSeverity(node);
		} catch(ReSharperException e) {
			LOG.error("Failed to add CustomSeverity on Node " + node + "\nmessage:" + e.getMessage());
		}
	}
 	private void tryAddCustomSeverity(Node node) throws ReSharperException  {
		String key = getKey(node);
		RulePriority priority= getRulePriority(node);
		if (severities.containsKey(key)) {
			LOG.warn("duplicate entry for " + key);
		} else {
			severities.put(key, priority);
		}
	}
	
	private String getKey(Node node) throws ReSharperException  {
        NamedNodeMap attributeMap=node.getAttributes();
        Node keyAttribute=attributeMap.getNamedItem("x:Key");
        String value=keyAttribute.getNodeValue();
        String values[]=value.split("[/=]");
        if(values.length !=8 && values.length !=9) {
        	throw new ReSharperException("Invalid key, does not contain 8 or 9 segments seperated by / " + value + 
        			"\ncontains " + values.length + " elements" );
        }
        String key=values[values.length-2];
		return key;
	}
	
	private RulePriority getRulePriority(Node node) {
        String severityText= node.getTextContent();
        ReSharperSeverity reSharperSeverity = ReSharperUtils.getResharperSeverity(severityText);
        RulePriority sonarPriority=ReSharperUtils.translateResharperPriorityIntoSonarPriority(reSharperSeverity);
        return sonarPriority;
	}
	/**
	 * Get the String nodes through the reader
	 * @return list of string nodes
	 * @throws ReSharperException 
	 */
	private NodeList getStringNodes() throws ReSharperException  {
        XPath xpath = createXPathForInspectCode();
        NodeList nodes;
		try {
			InputSource source = new InputSource(reader);
			nodes = (NodeList) xpath.evaluate("//s:String",source, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			LOG.error(e.getMessage());
			throw new ReSharperException(e.getMessage());
		}
		return nodes;
	}


	/**
	 * create xpath and assign the namespace resolver for InspectCode namespace
	 * @return xpath
	 */
	private XPath createXPathForInspectCode() {
		XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        NamespaceContext inspectCodeNamespaceResolver = new InspectCodeNamespaceResolver();
        xpath.setNamespaceContext(inspectCodeNamespaceResolver);

		return xpath;
	}

    public void assignCustomSeverity(ActiveRule activeRule) {
        String ruleKey = activeRule.getRuleKey();
        if (severities == null) return;

        if (severities.containsKey(ruleKey)) {
            RulePriority newPriority = severities.get(ruleKey);
            activeRule.setSeverity(newPriority);
            LOG.debug("overriding priority for" + ruleKey + " with " + newPriority);
        }
    }

    public CustomSeveritiesMap getSeverities() {
        return severities;
    }
}
