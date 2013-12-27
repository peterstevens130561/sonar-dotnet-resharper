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
package com.wrightfully.sonar.plugins.dotnet.resharper;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.config.Settings;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;

import junit.framework.Assert;

import com.wrightfully.sonar.dotnet.tools.resharper.ReSharperException;

@RunWith(MockitoJUnitRunner.class)
public class CustomSeveritiesTest {
	
	final static String header= "<wpf:ResourceDictionary xml:space=\"preserve\" " +
			"xmlns:x=\"http://schemas.microsoft.com/winfx/2006/xaml\" " +
			"xmlns:s=\"clr-namespace:System;assembly=mscorlib\" " +
			"xmlns:ss=\"urn:shemas-jetbrains-com:settings-storage-xaml\" " +
			"xmlns:wpf=\"http://schemas.microsoft.com/winfx/2006/xaml/presentation\">";
	
	final static String footer="</wpf:ResourceDictionary>";
	
	ReSharperConfiguration settingsMock;

	
	/**
	 * give the parser a null string, should be ok
	 * @throws ReSharperException 
	 */
	@Test
	public void EmptySeveritiesListShouldResultInEmptyMap() throws ReSharperException {
		setPropertyValue(null);
		CustomSeverities customSeverities = new CustomSeverities(settingsMock) ;
		CustomSeveritiesMap map = customSeverities.parse();
		Assert.assertEquals(0, map.size());
	}

	/**
	 * @param object 
	 * 
	 */
	private void setPropertyValue(String value) {
		settingsMock = mock(ReSharperConfiguration.class);
		when(settingsMock.getString(ReSharperConstants.CUSTOM_SEVERITIES_PROP_KEY)).thenReturn(value);
	}
	
	/**
	 * Feed the parser garbage, should be fine too
	 */
	@Test
	public void GarbageShouldResultInEmptyMap() {
		setPropertyValue("garbage");
		CustomSeverities customSeverities = new CustomSeverities(settingsMock) ;
		CustomSeveritiesMap map = customSeverities.parse();
		Assert.assertEquals(0, map.size());		
	}
	
	/**
	 * Feed it invalid xml, should be ok too
	 * @throws ReSharperException 
	 */
	@Test
	public void InvalidXmlhouldBeOkToo() throws ReSharperException {
		String emptyListWithInvalidXmlAtTheEnd = header + "</wpf>";
		setPropertyValue(emptyListWithInvalidXmlAtTheEnd);
		CustomSeverities customSeverities = new CustomSeverities(settingsMock) ;
		CustomSeveritiesMap map = customSeverities.parse();
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
		CustomSeverities customSeverities = new CustomSeverities(settingsMock) ;
		CustomSeveritiesMap map = customSeverities.parse();
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
		setPropertyValue(customList);
		CustomSeverities customSeverities = new CustomSeverities(settingsMock) ;
		CustomSeveritiesMap map = customSeverities.parse();
		Assert.assertEquals(1, map.size());				
	}
	
	/**
	 * Duplicate custom rule
	 */
	@Test
	public void DuplicateCustomRuleShouldHaveOnlyOney()throws ReSharperException {
		String customSeverity = "<s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=AssignNullToNotNullAttribute/@EntryIndexedValue\">ERROR</s:String>";
		String customList = header + customSeverity + customSeverity + footer;
		setPropertyValue(customList);
		CustomSeverities customSeverities = new CustomSeverities(settingsMock) ;
		CustomSeveritiesMap map = customSeverities.parse();
		Assert.assertEquals(1, map.size());				
	}
	
	/**
	 * Lots of assumptions are made on the rules, check that an invalid key is survived
	 */
	@Test
	public void InvalidCustomRuleWithWeirdKeyShouldBeIgnored() throws ReSharperException{
		String invalidSeverity = "<s:String x:Key=\"InvalidKey\">ERROR</s:String>";
		String customList = header + invalidSeverity + footer;
		setPropertyValue(customList);
		CustomSeverities customSeverities = new CustomSeverities(settingsMock) ;
		CustomSeveritiesMap map = customSeverities.parse();
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
		setPropertyValue(customList);
		CustomSeverities customSeverities = new CustomSeverities(settingsMock) ;
		customSeverities.parse();
		customSeverities.assignCustomSeverity(activeRule);
		Assert.assertEquals(RulePriority.INFO, activeRule.getSeverity());
	}
	
	@Test
	public void CustomSeveritiesDefinedCheckThatActiveRuleIsChanged() throws ReSharperException {
		String customSeverity = "<s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=AssignNullToNotNullAttribute/@EntryIndexedValue\">ERROR</s:String>";
		String customList = header + customSeverity + footer;
		Rule rule = new Rule();
		rule.setSeverity(null);
		rule.setKey("AssignNullToNotNullAttribute");
		rule.setSeverity(RulePriority.INFO);
		ActiveRule activeRule = new ActiveRule(null,rule,null);
		setPropertyValue(customList);
		CustomSeverities customSeverities = new CustomSeverities(settingsMock) ;
		customSeverities.parse();
		customSeverities.assignCustomSeverity(activeRule);
		Assert.assertEquals(RulePriority.BLOCKER, activeRule.getSeverity());
	}
}