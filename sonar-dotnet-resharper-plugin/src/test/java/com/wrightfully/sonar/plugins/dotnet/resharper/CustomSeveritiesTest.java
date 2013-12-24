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

import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.CustomSeverities;
import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.CustomSeveritiesMap;

public class CustomSeveritiesTest {
	
	final static String header= "<wpf:ResourceDictionary xml:space=\"preserve\" " +
			"xmlns:x=\"http://schemas.microsoft.com/winfx/2006/xaml\" " +
			"xmlns:s=\"clr-namespace:System;assembly=mscorlib\" " +
			"xmlns:ss=\"urn:shemas-jetbrains-com:settings-storage-xaml\" " +
			"xmlns:wpf=\"http://schemas.microsoft.com/winfx/2006/xaml/presentation\">";
	
	final static String footer="</wpf:ResourceDictionary>";
	@Before
	public void before() {
		
	}
	
	/**
	 * give the parser a null string, should be ok
	 */
	@Test
	public void EmptySeveritiesListShouldResultInEmptyMap() {
		CustomSeverities customSeverities = new CustomSeverities() ;
		CustomSeveritiesMap map = customSeverities.parseString(null);
		Assert.assertEquals(0, map.size());
	}
	
	/**
	 * Feed the parser garbage, should be fine too
	 */
	@Test
	public void GarbageShouldResultInEmptyMap() {
		CustomSeverities customSeverities = new CustomSeverities() ;
		CustomSeveritiesMap map = customSeverities.parseString("garbage");
		Assert.assertEquals(0, map.size());		
	}
	
	/**
	 * Feed it invalid xml, should be ok too
	 */
	@Test
	public void InvalidXmlhouldBeOkToo() {
		CustomSeverities customSeverities = new CustomSeverities() ;
		String emptyListWithInvalidXmlAtTheEnd = header + "</wpf>";
		CustomSeveritiesMap map = customSeverities.parseString(emptyListWithInvalidXmlAtTheEnd);
		Assert.assertEquals(0, map.size());		
	}
	
	/**
	 * Feed valid xml, no further content, should be ok
	 */
	@Test
	public void ValidXmlWithNoContentShouldBeOkToo() {
		CustomSeverities customSeverities = new CustomSeverities() ;
		String emptyList = header + footer;
		CustomSeveritiesMap map = customSeverities.parseString(emptyList);
		Assert.assertEquals(0, map.size());		
	}
	
	/**
	 * Feed one custom rule
	 */
	@Test
	public void OneCustomSeverity() {
		String customSeverity = "<s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=AssignNullToNotNullAttribute/@EntryIndexedValue\">ERROR</s:String>";
		String customList = header + customSeverity + footer;
		CustomSeverities customSeverities = new CustomSeverities() ;
		CustomSeveritiesMap map = customSeverities.parseString(customList);
		Assert.assertEquals(1, map.size());				
	}
}
