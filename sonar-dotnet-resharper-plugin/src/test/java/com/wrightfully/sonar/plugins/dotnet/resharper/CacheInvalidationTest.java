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
