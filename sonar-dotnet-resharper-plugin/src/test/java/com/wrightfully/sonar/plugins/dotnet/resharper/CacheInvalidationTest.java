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

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.sonar.api.config.Settings;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
public class CacheInvalidationTest {

	DateCacheValidator classUnderTest = new DateCacheValidator();
	File cache = mock(File.class);
	File dotSettings = mock(File.class);
	@Test
	public void cacheOlderThenSettings_Invalidate() throws ParseException {
		givenCache("mycache");
		givenDotSettings("mysettings");
		givenCacheCreationDate("2014-jan-1");
		givenSettingsDate("3-jan-2014");
		boolean valid=classUnderTest.validate();
		assertFalse(valid);
	}
	private void givenDotSettings(String string) {
		classUnderTest.setDotSettings(dotSettings);
	}
	private void givenSettingsDate(String string) {

	}
	private void givenCache(String string) {
		classUnderTest.setCache(cache);
		when(cache.getName()).thenReturn(string);
	}
	
	
	private void givenCacheCreationDate(String string) throws ParseException {
		Date date = new SimpleDateFormat("yyyy-MMM-dd").parse(string);
	}
	
	
	
}
