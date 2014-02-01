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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.rules.RulePriority;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.wrightfully.sonar.dotnet.tools.resharper.ReSharperException;
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperConfiguration;
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperConstants;
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperUtils;
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperUtils.ReSharperSeverity;

public abstract class BaseCustomSeverities implements CustomSeverities {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyBasedCustomSeverities.class);
    private ReSharperConfiguration configuration ;
    CustomSeveritiesMap severities = new CustomSeveritiesMap();
    
    
    /* (non-Javadoc)
    * @see com.wrightfully.sonar.plugins.dotnet.resharper.customseverities.CustomSeverities#getProfileName()
    */
   public String getProfileName() {
           String profileName=ReSharperConstants.PROFILE_DEFAULT;
           String customName=getConfiguration().getString(ReSharperConstants.PROFILE_NAME);
           if(customName != null && customName.length()>0) {
               profileName = customName;
           } else {
               LOG.warn("No profile defined for resharper, using default");
           }
               
           LOG.debug("Using profile " + profileName);
           return profileName;
       }
    public ReSharperConfiguration getConfiguration() {
        return configuration;
    }
    public void setConfiguration(ReSharperConfiguration configuration) {
        this.configuration = configuration;
    }
    
    protected void addCustomSeverity(Node node){
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
        String[] values=value.split("[/=]");
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

}
