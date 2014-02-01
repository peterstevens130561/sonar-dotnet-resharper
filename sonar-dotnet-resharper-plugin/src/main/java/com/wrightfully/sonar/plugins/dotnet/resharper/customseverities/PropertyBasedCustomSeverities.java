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
package com.wrightfully.sonar.plugins.dotnet.resharper.customseverities;



import java.io.StringReader;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.RulePriority;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.wrightfully.sonar.dotnet.tools.resharper.ReSharperException;
import com.wrightfully.sonar.plugins.dotnet.resharper.EmptyNodeList;
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperConfiguration;
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperConstants;
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperUtils;
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperUtils.ReSharperSeverity;


public class PropertyBasedCustomSeverities extends BaseCustomSeverities {

    
    private static final Logger LOG = LoggerFactory.getLogger(PropertyBasedCustomSeverities.class);
    
	
    public PropertyBasedCustomSeverities() {
    }
	

	/* (non-Javadoc)
     * @see com.wrightfully.sonar.plugins.dotnet.resharper.customseverities.CustomSeverities#mergeCustomSeverities(org.sonar.api.profiles.RulesProfile)
     */
	public void mergeCustomSeverities(RulesProfile profile) {
        List<ActiveRule> rules = profile.getActiveRules();
        if (rules == null) {
            return;
        }
        
        parseCustomSeverities();
        for (ActiveRule activeRule : rules) {
            assignCustomSeverity(activeRule);
        }		
	}


	/**
	 * Get a map indexed by rulekey, and severity as attribute
	 * @return
	 * @throws XPathExpressionException
	 * @throws ReSharperException 
	 */
	public CustomSeveritiesMap parseCustomSeverities() {
    	String propertyValue=getConfiguration().getString(ReSharperConstants.CUSTOM_SEVERITIES_DEFINITON);
    	if(StringUtils.isNotEmpty(propertyValue)) {
			NodeList nodes=getStringNodes(propertyValue);
			for(int nodeIndex=0;nodeIndex < nodes.getLength();nodeIndex++) {
				Node node = nodes.item(nodeIndex);
				addCustomSeverity(node);
			}
    	}
		return severities;
	}



	
	/**
	 * Get the String nodes through the reader
	 * @return list of string nodes
	 */
	private NodeList getStringNodes(String propertyValue) {
        XPath xpath = createXPathForInspectCode();
        NodeList nodes= new EmptyNodeList();
		try {
	       	StringReader reader = new StringReader(propertyValue);
			InputSource source = new InputSource(reader);
			nodes = (NodeList) xpath.evaluate("//s:String",source, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			// There are two cases that can cause this error
			//1: invalid expression, which can't happen
			//2: invalid source, which can happen with an empty string
			LOG.debug("XPATH error (ignoring",e);
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

	/**
	 * Given a rule in the profile, check if it is in the custom severities, and if so, take its changed severity
	 * 
	 * @param activeRule - the rule that will be changed
	 */
    public void assignCustomSeverity(ActiveRule activeRule) {
        if (severities == null) {
        	return;
        }
        
        String ruleKey = activeRule.getRuleKey();
        if (severities.containsKey(ruleKey)) {
            RulePriority newPriority = severities.get(ruleKey);
            activeRule.setSeverity(newPriority);
            LOG.debug("overriding priority for" + ruleKey + " with " + newPriority);
        }
    }

    public CustomSeveritiesMap getSeverities() {
        return severities;
    }


    public void setSettings(Settings settings) {
            setConfiguration(new ReSharperConfiguration(settings));
        }
    }

