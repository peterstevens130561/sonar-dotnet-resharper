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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.utils.SonarException;

import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.CustomSeverities;
import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.IssueModel;

@Properties({
    @Property(key = ReSharperConstants.FAIL_ON_ISSUES_KEY,
    defaultValue = "CSharpError", name = "ReSharper issues to fail analysis on",
    description = "Add IssueType values from ReSharper's results file for issues that should result in the analysis failing, i.e. CSharpErrors",
            type = PropertyType.TEXT, global = true, project = false)
})
public class FailingIssuesVisitor {
    private static final Logger LOG = LoggerFactory.getLogger(FailingIssuesVisitor.class);
    
    private Collection<String> issuesToFailOn = new ArrayList<String>();
    private List<IssueModel> issues =new ArrayList<IssueModel>();
    
	public Boolean hasMatches() {
		return issues.size() > 0;
	}

	public void Visit(IssueModel issue) {
		if(issuesToFailOn.contains(issue.getId())) {
			issues.add(issue);
		}
	}
	public void setIssuesToFailOn(String issueTypes) {
		for(String issue : issueTypes.split(",")) {
			this.issuesToFailOn.add(issue);
		}
		
	}

	public void Check() {
		if(hasMatches()) {
			String msg = String.format("found %d issues that will cause the analysis to fail, please address first. Showing first %d issues\n",issues.size(),10);
			LOG.error(msg);
			throw new SonarException("Issues found that fail the analysis, please check the log");
		}
		
	}
	
}
