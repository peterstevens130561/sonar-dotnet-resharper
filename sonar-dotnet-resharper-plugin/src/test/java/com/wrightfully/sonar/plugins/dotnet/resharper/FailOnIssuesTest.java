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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.utils.SonarException;


import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.IssueModel;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
public class FailOnIssuesTest {

	static final String ERROR_TYPE = "CSharpErrors";
	@Before
	public void before() {
		
	}
	
	
	@Test
	public void NonFailingIssue() {

		ReSharperConfiguration configMock = createConfigurationMock();
		
		IssueModel issue = createNonFailingIssue();
		
		FailingIssueListener failingIssueVisitor = parseIssue(
				configMock, issue);
		
		Assert.assertFalse(failingIssueVisitor.hasMatches());
	}
	
	@Test(expected=SonarException.class)
	public void ThrowExceptionOnFailingIssueInResult() {
		ReSharperConfiguration configMock = createConfigurationMock();

		IssueModel failingIssue = createFailingIssue();
		parseIssue(configMock, failingIssue);
		
		Assert.assertTrue("Should not get here",false);
	}


	@Test
	public void NoExceptionOnNoFailingIssues() {
		ReSharperConfiguration configMock = createConfigurationMock();
		
		IssueModel issue = createNonFailingIssue();
		
		FailingIssueListener failingIssueVisitor = parseIssue(
				configMock, issue);
		
		Assert.assertTrue("Should get here",true);
	}

	private IssueModel createNonFailingIssue() {
		IssueModel issue = new IssueModel();
		issue.setId(ERROR_TYPE + "a");
		issue.setLine("1");
		issue.setMessage("Compilation Error");
		return issue;
	}
	
	private ReSharperConfiguration createConfigurationMock() {
		ReSharperConfiguration configMock = mock(ReSharperConfiguration.class);
		when(configMock.getString(ReSharperConstants.FAIL_ON_ISSUES_KEY)).thenReturn(ERROR_TYPE);
		return configMock;
	}

	private IssueModel createFailingIssue() {
		IssueModel issue = new IssueModel();
		issue.setId(ERROR_TYPE);
		issue.setLine("1");
		issue.setMessage("Compilation Error");
		return issue;
	}
	
	private FailingIssueListener parseIssue(
			ReSharperConfiguration configMock, IssueModel issue) {
		FailingIssueListener failingIssueVisitor = new FailingIssueListener();
		failingIssueVisitor.parsingStart(configMock);
		failingIssueVisitor.parsedIssue(issue);
		failingIssueVisitor.parsingComplete();
		return failingIssueVisitor;
	}
}
