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


    @Override
    protected InputSource createInputSource() {
        String propertyValue=getConfiguration().getString(ReSharperConstants.CUSTOM_SEVERITIES_DEFINITON);
        InputSource source = null;
        if(StringUtils.isNotEmpty(propertyValue)) {
            StringReader reader = new StringReader(propertyValue);
            source = new InputSource(reader);
        }
        return source;
    }
	
}

