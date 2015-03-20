package com.wrightfully.sonar.plugins.dotnet.resharper;

import java.io.File;

public class DateCacheValidator implements CacheValidator {
	private File cache;
	private File dotSettings;
	public void setCache(File cache) {
		this.cache=cache;
	}
	public void setDotSettings(File dotSettings) {
		this.dotSettings=dotSettings;
	}
	public boolean validate() {
		return false;
	}

}
