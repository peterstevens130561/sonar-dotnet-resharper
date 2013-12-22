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


import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.Issue;

/**
 * @author stevpet
 *
 */
public class FailOnIssuesTest {

	static final String ERROR_TYPE = "CSharpErrors";
	@Before
	public void before() {
		
	}
	
	@Test
	public void ThrowExceptionOnFailingIssueInResult() {
		FailingIssuesVisitor failingIssueVisitor = new FailingIssuesVisitor();
		failingIssueVisitor.setIssuesToFailOn(ERROR_TYPE);
		Issue issue = new Issue();
		issue.setId(ERROR_TYPE);
		issue.setLine("1");
		issue.setMessage("Compilation Error");
		
		failingIssueVisitor.Visit(issue);
		Assert.assertTrue(failingIssueVisitor.hasMatches());
	}
	
	@Test
	public void NoExceptionOnIssueInResult() {
		FailingIssuesVisitor failingIssueVisitor = new FailingIssuesVisitor();
		failingIssueVisitor.setIssuesToFailOn(ERROR_TYPE);
		Issue issue = new Issue();
		issue.setId(ERROR_TYPE + "a");
		issue.setLine("1");
		issue.setMessage("Compilation Error");
		
		Assert.assertFalse(failingIssueVisitor.hasMatches());
	}
}
