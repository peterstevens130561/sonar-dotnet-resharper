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
import static org.mockito.Mockito.mock;

import org.xml.sax.InputSource;

import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sonar.api.config.Settings;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.profiles.RulesProfile;

import junit.framework.Assert;

import com.wrightfully.sonar.dotnet.tools.resharper.ReSharperException;
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperConfiguration;
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperConstants;
import com.wrightfully.sonar.plugins.dotnet.resharper.customseverities.CustomSeveritiesMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Settings.class)
public class CustomSeveritiesTest {
	
	final static String header= "<wpf:ResourceDictionary xml:space=\"preserve\" " +
			"xmlns:x=\"http://schemas.microsoft.com/winfx/2006/xaml\" " +
			"xmlns:s=\"clr-namespace:System;assembly=mscorlib\" " +
			"xmlns:ss=\"urn:shemas-jetbrains-com:settings-storage-xaml\" " +
			"xmlns:wpf=\"http://schemas.microsoft.com/winfx/2006/xaml/presentation\">";
	
	final static String footer="</wpf:ResourceDictionary>";
	
	private Settings settingsMock;
	private CustomSeveritiesStub customSeverities;

	@Before
	public void beforeTest() {
        settingsMock = PowerMockito.mock(Settings.class);
	    customSeverities = new CustomSeveritiesStub() ;
	    customSeverities.setSettings(settingsMock);
	    
	}
	/**
	 * give the parser a null string, should be ok
	 * @throws ReSharperException 
	 */
	@Test
	public void EmptySeveritiesListShouldResultInEmptyMap() throws ReSharperException {

		CustomSeveritiesMap map = testParse("");
		Assert.assertEquals(0, map.size());
	}

	/**
	 * @param object 
	 * 
	 */
	private void setPropertyValue(String value) {
		when(settingsMock.getString(ReSharperConstants.CUSTOM_SEVERITIES_DEFINITON)).thenReturn(value);
	}
	
	/**
	 * Feed the parser garbage, should be fine too
	 */
	@Test
	public void GarbageShouldResultInEmptyMap() {
		CustomSeveritiesMap map = testParse("garbage");
		Assert.assertEquals(0, map.size());		
	}
	
	/**
	 * Feed it invalid xml, should be ok too
	 * @throws ReSharperException 
	 */
	@Test
	public void InvalidXmlhouldBeOkToo() throws ReSharperException {
		String emptyListWithInvalidXmlAtTheEnd = header + "</wpf>";
        CustomSeveritiesMap map = testParse(emptyListWithInvalidXmlAtTheEnd);
		Assert.assertEquals(0, map.size());		
	}
	
	/**
	 * Feed valid xml, no further content, should be ok
	 * @throws ReSharperException 
	 */
	@Test
	public void ValidXmlWithNoContentShouldBeOkToo() throws ReSharperException {
		String emptyList = header + footer;
		setPropertyValue(emptyList);
        CustomSeveritiesMap map = testParse(emptyList);
		Assert.assertEquals(0, map.size());		
	}
	
	/**
	 * Feed one custom rule
	 * @throws ReSharperException 
	 */
	@Test
	public void OneCustomSeverity() throws ReSharperException {
		String customSeverity = "<s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=AssignNullToNotNullAttribute/@EntryIndexedValue\">ERROR</s:String>";
		String customList = header + customSeverity + footer;
        CustomSeveritiesMap map = testParse(customList);
		Assert.assertEquals(1, map.size());				
	}
	
	/**
	 * Duplicate custom rule
	 */
	@Test
	public void DuplicateCustomRuleShouldHaveOnlyOney()throws ReSharperException {
		String customList=createCustomSeverityDefinition();
        CustomSeveritiesMap map = testParse(customList);
		Assert.assertEquals(1, map.size());				
	}

    private String createCustomSeverityDefinition() {
        String customSeverity = "<s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=AssignNullToNotNullAttribute/@EntryIndexedValue\">ERROR</s:String>";
		return  header + customSeverity + customSeverity + footer;
    }
	
	/**
	 * Lots of assumptions are made on the rules, check that an invalid key is survived
	 */
	@Test
	public void InvalidCustomRuleWithWeirdKeyShouldBeIgnored() throws ReSharperException{
		String invalidSeverity = "<s:String x:Key=\"InvalidKey\">ERROR</s:String>";
		String customList = header + invalidSeverity + footer;
        CustomSeveritiesMap map = testParse(customList);
		Assert.assertEquals(0, map.size());				
	}
	
	   /**
     * Lots of assumptions are made on the rules, check that an invalid key is survived
     */
    @Test
    public void ParseCustomSeverities_TooManyPartsInKeyShouldBeIgnored() throws ReSharperException{
        String invalidSeverity = "<s:String x:Key=\"/Default/ExtraPart/AnotherPart/CodeInspection/Highlighting/InspectionSeverities/=AssignNullToNotNullAttribute/@EntryIndexedValue\">ERROR</s:String>";
        String customList = header + invalidSeverity + footer;
        CustomSeveritiesMap map = testParse(customList);
        Assert.assertEquals(0, map.size());             
    }
	
	@Test
	public void NoCustomSeveritiesDefinedCheckThatActiveRuleIsNotChanged() throws ReSharperException{
		String customSeverity = "<s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=AssignNullToNotNullAttribute/@EntryIndexedValue\">ERROR</s:String>";
		String customList = header + customSeverity + footer;

		Rule rule = new Rule();
		rule.setSeverity(null);
		rule.setKey("bozo");
		rule.setSeverity(RulePriority.INFO);
		ActiveRule activeRule = new ActiveRule(null,rule,null);

        CustomSeveritiesMap map = testParse(customList);
		customSeverities.assignCustomSeverity(activeRule);
		Assert.assertEquals(RulePriority.INFO, activeRule.getSeverity());
	}
	
	@Test
	public void CustomSeveritiesDefinedCheckThatActiveRuleIsChanged() throws ReSharperException {
		String customSeverity = "<s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=AssignNullToNotNullAttribute/@EntryIndexedValue\">ERROR</s:String>";
		String customList = header + customSeverity + footer;

		Rule rule = createRule();
		ActiveRule activeRule = new ActiveRule(null,rule,null);

        CustomSeveritiesMap map = testParse(customList);
		customSeverities.assignCustomSeverity(activeRule);
		Assert.assertEquals(RulePriority.BLOCKER, activeRule.getSeverity());
	}


	
	/**
	 * With no active rules, any attempt to merge could result in havoc
	 *
	 */
	@Test
	public void EmptyProfileShouldReturnWithoutException() {
	    RulesProfile profileMock = mock(RulesProfile.class);
	    when(profileMock.getActiveRules()).thenReturn(null);
	    customSeverities.mergeCustomSeverities(profileMock);
	    Assert.assertTrue(true);
	}
	
	/**
	 * With a
	 */
	@Test
	public void ProfileShouldBeChangedOnMatch() {
        customSeverities.setSource(createCustomSeverityDefinition());

        List<ActiveRule> rules = new ArrayList<ActiveRule>();
        ActiveRule activeRule=createActiveRule();
        rules.add(activeRule);
        RulesProfile profileMock = mock(RulesProfile.class);
        when(profileMock.getActiveRules()).thenReturn(rules);
    
        customSeverities.mergeCustomSeverities(profileMock);
        Assert.assertEquals(RulePriority.BLOCKER, activeRule.getSeverity());
	}
	

	private ActiveRule createActiveRule() {
	    ActiveRule activeRule = new ActiveRule();
	    activeRule.setRule(createRule());
	    return activeRule;

	}
    private Rule createRule() {
        Rule rule = new Rule();
        rule.setSeverity(null);
        rule.setKey("AssignNullToNotNullAttribute");
        rule.setSeverity(RulePriority.INFO);
        return rule;
    }
    
    public CustomSeveritiesMap testParse(String value) {
        customSeverities.setSource(value);
        InputSource inputSource = customSeverities.createInputSource();
        return customSeverities.parseCustomSeverities(inputSource);
    }
    
    private class CustomSeveritiesStub extends BaseCustomSeverities {

        private String source ;
        
        public void setSource(String source) {
            this.source = source;
        }
        @Override
        InputSource createInputSource() {
            StringReader reader = new StringReader(source);
            return new InputSource(reader);
        }
        
        public CustomSeveritiesMap parseCustomSeverities(InputSource inputSource) {
            return super.parseCustomSeverities(inputSource);
        }
        

        
    }
} 