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

import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.IssueModel;
import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.IssueListener;


/**
 * @author stevpet
 * Listens to parsed issues, and if any of these match a set of issues on which the analysis should fail, 
 * then at the end of the parsing a list of the issues found, and a SonarException is thrown to halt the analysis
 */
public class FailingIssueListener implements IssueListener {
    private static final Logger LOG = LoggerFactory.getLogger(FailingIssueListener.class);
    
    private Collection<String> issuesToFailOn = new ArrayList<String>();
    private List<IssueModel> issues =new ArrayList<IssueModel>();
    

	/** 
	 * @see com.wrightfully.sonar.plugins.dotnet.resharper.profiles.IssueListener#parsedIssue(com.wrightfully.sonar.plugins.dotnet.resharper.profiles.IssueModel)
	 */
	public void parsedIssue(IssueModel issue) {
		if( issue == null) {
			return ;
		}
		if(issuesToFailOn.contains(issue.getId())) {
			issues.add(issue);
		}
	}
	
	/**
	 * Provide comma seperated list of issueTypes on which the analysis should fail
	 * @param issueTypes
	 */
	public void setIssueTypesToFailOn(String issueTypes) {
		if(issueTypes == null) {
			return;
		}
		for(String issue : issueTypes.split(",")) {
			this.issuesToFailOn.add(issue);
		}
	}

	
	/**
	 * @return at least one issue found
	 */
	public Boolean hasMatches() {
		return issues.size() > 0;
	}
	/**
	 * @return number of issues found
	 */
	public int getErrorCount() {
		return issues.size();
	}

	/* (non-Javadoc)
	 * @see com.wrightfully.sonar.plugins.dotnet.resharper.profiles.IssueListener#parsingComplete()
	 */
	public void parsingComplete() {
		if(hasMatches()) {
			String msg = String.format("found %d issues that will cause the analysis to fail, please address first.\n",issues.size(),10);
			LOG.error(msg);
			String formattedIssues=formatIssues();
			LOG.error(formattedIssues);
			throw new SonarException("Issues found that fail the analysis, please check the log");
		}
	}

	/* (non-Javadoc)
	 * @see com.wrightfully.sonar.plugins.dotnet.resharper.profiles.IssueListener#parsingStart(com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperConfiguration)
	 */
	public void parsingStart(ReSharperConfiguration configuration) {
		String issueTypes=configuration.getString(ReSharperConstants.FAIL_ON_ISSUES_KEY);
		LOG.debug(ReSharperConstants.FAIL_ON_ISSUES_KEY,issueTypes);
        setIssueTypesToFailOn(issueTypes);
	}
	
	private String formatIssues() {
		StringBuilder formattedIssues = new StringBuilder();
		for(IssueModel issue: issues) {
			String formattedIssue = formatIssue(issue);
			formattedIssues.append(formattedIssue);
		}
		return formattedIssues.toString();
	}
	private String formatIssue(IssueModel issue) {
		String msg = String.format("%s\t%s\t%s\t%s\n",issue.getFile(),issue.getLine(),issue.getId(),issue.getMessage());
		return msg;
	}

}
