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
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sonar.api.Extension;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
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
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperUtils;
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperUtils.ReSharperSeverity;

/**
 * Creates ReSharper rule repositories for every language supported by ReSharper. The class attempts to
 * contain all of its needs.
 */
@Properties({
    @Property(key = CustomSeverities.PROPERTY_KEY,
    defaultValue = "", name = "ReSharper custom severities",
    description = "Add &lt;IssueType&gt; vales from ReSharper's custom definitions A restart is required to take affect.",
            type = PropertyType.TEXT, global = true, project = false),
    @Property(key= CustomSeverities.PROFILE_NAME,defaultValue = "Sonar Way", name = "Profile",
    description = "Profile to which rules will be saved on restart, if profile does not exist",type=PropertyType.TEXT,global=true,project=false)
})
public class CustomSeverities implements Extension {
	static final String PROPERTY_KEY = "sonar.resharper.customSeverities.definition";
	static final String PROFILE_NAME = "sonar.resharper.profile";
    static final String PROFILE_DEFVALUE="Sonar Way";
    
    private static final Logger LOG = LoggerFactory.getLogger(CustomSeverities.class);
    
    private ReSharperConfiguration configuration ;
    CustomSeveritiesMap severities = new CustomSeveritiesMap();
	
    public CustomSeverities(ReSharperConfiguration settingsMock) {
    	this.configuration = settingsMock;
    }
	
	public CustomSeverities(Settings settings) {
		configuration = new ReSharperConfiguration(settings);
	}
	
	public void mergeCustomSeverities(RulesProfile profile) {
        List<ActiveRule> rules = profile.getActiveRules();
        if (rules == null) return;
        
        parseCustomSeverities();
        for (ActiveRule activeRule : rules) {
            assignCustomSeverity(activeRule);
        }		
	}

	public void setProfileName(RulesProfile profile) {
        String profileName=getProfileName();
        profile.setName(profileName);
	}
	
	 private String getProfileName() {
	    	String profileName=PROFILE_DEFVALUE;
	    	String customName=configuration.getString(PROFILE_NAME);
	    	if(customName != null && customName.length()>0) {
	    		profileName = customName;
	    	} else {
	    		LOG.warn("No profile defined for resharper, using default");
	    	}
	    		
	    	LOG.debug("Using profile " + profileName);
	    	return profileName;
	    }
	/**
	 * Get a map indexed by rulekey, and severity as attribute
	 * @return
	 * @throws XPathExpressionException
	 * @throws ReSharperException 
	 */
	public CustomSeveritiesMap parseCustomSeverities() {
    	String propertyValue=configuration.getString(PROPERTY_KEY);
    	if(StringUtils.isNotEmpty(propertyValue)) {
			NodeList nodes=getStringNodes(propertyValue);
			for(int nodeIndex=0;nodeIndex < nodes.getLength();nodeIndex++) {
				Node node = nodes.item(nodeIndex);
				addCustomSeverity(node);
			}
    	}
		return severities;
	}



	private void addCustomSeverity(Node node){
		try {
			tryAddCustomSeverity(node);
		} catch(ReSharperException e) {
			LOG.error("Failed to add CustomSeverity on Node " + node + "\nmessage:" + e.getMessage(),e);
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
        return values[values.length-2];
	}
	
	private RulePriority getRulePriority(Node node) {
        String severityText= node.getTextContent();
        ReSharperSeverity reSharperSeverity = ReSharperUtils.getResharperSeverity(severityText);
        return ReSharperUtils.translateResharperPriorityIntoSonarPriority(reSharperSeverity);
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

}