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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.ValidationMessages;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class ReSharperFileParser {
    private static final Logger LOG = LoggerFactory.getLogger(ReSharperFileParser.class);
    private ValidationMessages messages ;
    

    public void setMessages(ValidationMessages messages) {
        this.messages = messages;
    }
    
    public  List<ReSharperRule> parseRules(Reader reader) {
        List<ReSharperRule> result = new ArrayList<ReSharperRule>();

        try {
            InputSource source = new InputSource(reader);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            NodeList nodes = (NodeList) xpath.evaluate("//IssueType",source, XPathConstants.NODESET);

            if (nodes == null || nodes.getLength() == 0)  {
                String logMsg = "No IssueType nodes found in profile file";
                logWarningMessage(logMsg);
            }
            else {

                int count = nodes.getLength();
                LOG.info("Found " + count + " IssueType nodes (rules)" );

                // For each rule we extract the elements
                for (int idxRule = 0; idxRule < count; idxRule++) {
                    Element ruleElement = (Element) nodes.item(idxRule);
                    ReSharperRule rule = new ReSharperRule();
                    String ruleId = ruleElement.getAttribute("Id");
                    rule.setId(ruleId);

                    String active = ruleElement.getAttribute("Enabled");
                    rule.setEnabled(active.toLowerCase().contains("true"));

                    String category = ruleElement.getAttribute("Category");
                    rule.setCategory(category);

                    String description = ruleElement.getAttribute("Description");
                    rule.setDescription(description);

                    String wikiLink = ruleElement.getAttribute("WikiUrl");
                    rule.setWikiLink(wikiLink);

                    String severity = ruleElement.getAttribute("Severity");
                    try {
                        rule.setSeverity(ReSharperRule.ReSharperSeverity.valueOf(severity));
                    } catch (Exception ex) {
                        rule.setSeverity(ReSharperRule.ReSharperSeverity.WARNING);
                        String logMsg = "exception while parsing resharper severity '" + severity +"': " + ex.getMessage();
                        logWarningMessage(logMsg);
                    }
                    result.add(rule);
                }
            }
        } catch (XPathExpressionException e) {
            String logMsg = "xpath exception while parsing resharper config file: " + e.getMessage();
            logWarningMessage(logMsg);

        } catch (Exception e) {
            String logMsg =       "Failed to read the profile to import: " + e.getMessage();
            logWarningMessage(logMsg);
        }
        return result;
    }
    
    private void logWarningMessage(String logMsg) {
        if (messages != null)
            messages.addErrorText(logMsg);
        else LOG.warn(logMsg);
        
    }
}
