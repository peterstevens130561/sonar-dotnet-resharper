/*
 * .NET tools :: ReSharper Runner
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
package com.wrightfully.sonar.dotnet.tools.resharper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.sonar.plugins.dotnet.api.microsoft.VisualStudioProject;
import org.sonar.plugins.dotnet.api.microsoft.VisualStudioSolution;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
public class ReSharperCommandBuilderTest {

	private VisualStudioSolution solution = mock(VisualStudioSolution.class);
	private File solutionFile = new File("G:/solutions");
	
	private ReSharperCommandBuilder classUnderTest = initBuilder();

	@Test
	public void buildProfileNotSet_NotInCommandLine() throws ReSharperException {
		String command=classUnderTest.toCommand().toString();
		assertFalse(command,command.contains("/profile"));
	}
	
	@Test
	public void buildProfileSet_InCommandLine() throws ReSharperException {
		String profile="someprofile";
		classUnderTest.setProfile(profile);
		String command=classUnderTest.toCommand().toString();
		assertTrue(command,command.contains("/profile="+profile));
	}
	
	@Test
	public void buildProfileNull_InCommandLine() throws ReSharperException {
		classUnderTest.setProfile(null);
		String command=classUnderTest.toCommand().toString();
		assertFalse(command,command.contains("/profile"));
	}
	
	@Test
	public void cachesHomeNotSet_NotInCommandLine() throws ReSharperException {
		String command=classUnderTest.toCommand().toString();
		assertFalse(command,command.contains("/caches"));		
	}
	
	@Test
	public void cachesHomeNullSet_NotInCommandLine() throws ReSharperException {
		String command=classUnderTest.toCommand().toString();
		classUnderTest.setCachesHome(null);
		assertFalse(command,command.contains("/caches"));		
	}
	
	@Test
	public void cachesHomeSet_InCommandLine() throws ReSharperException {
		String caches="somecache";
		classUnderTest.setCachesHome(caches);
		String command=classUnderTest.toCommand().toString();
		assertTrue(command,command.contains("/caches-home="+caches));
	}

	@Test
	public void propertiesSet_InProperties() throws ReSharperException {
		String properties="funny\";some more;\"";
		classUnderTest.setProperties(properties);
		String command=classUnderTest.toCommand().toString();
		assertTrue(command,command.contains("/properties:" + properties));
	}
	
	@Test
	public void propertiesNotSet_NotInProperties() throws ReSharperException {
		classUnderTest.setProperties("");
		String command=classUnderTest.toCommand().toString();
		assertFalse(command,command.contains("/properties:"));
	}
	private ReSharperCommandBuilder initBuilder() {
		when(solution.getSolutionFile()).thenReturn(solutionFile);
		VisualStudioProject project = mock(VisualStudioProject.class);
		List<String> properties = new ArrayList<String>();
		ReSharperCommandBuilder classUnderTest = ReSharperCommandBuilder.createBuilder(solution, project, properties);
		classUnderTest.setExecutable(new File("C:/Program Files/somedummy"));
		classUnderTest.setReportFile(new File("C:/My Reports/report.xml"));
		return classUnderTest;
	}
}
