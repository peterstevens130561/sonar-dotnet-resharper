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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.dotnet.api.DotNetConfiguration;
import org.sonar.plugins.dotnet.api.microsoft.VisualStudioProject;
import org.sonar.plugins.dotnet.api.microsoft.VisualStudioSolution;

import com.wrightfully.sonar.dotnet.tools.resharper.ReSharperCommandBuilder;
import com.wrightfully.sonar.dotnet.tools.resharper.ReSharperException;
import com.wrightfully.sonar.dotnet.tools.resharper.ReSharperRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.sonar.api.utils.command.Command;

public class ResSharperRunnerTest {

    private static final String INSPECT_PATH="C:/Program Files/JetBrains";
    private VisualStudioSolution vsSolution ;
    private VisualStudioProject vsProject ;
    private DotNetConfiguration configuration;
    
    @Before
    public void setup() {
        vsSolution = mock(VisualStudioSolution.class);
        vsProject = mock(VisualStudioProject.class); 
        configuration = mock(DotNetConfiguration.class);
    }
    @Test
    public void ReSharperRunner_CreateInspectCodeArguments_ShouldMatch() throws ReSharperException {
        //Setup
        ReSharperRunner runner = ReSharperRunner.create(INSPECT_PATH);
        when(vsSolution.getSolutionFile()).thenReturn(new File("solution.sln"));
        //Arrange
        ReSharperCommandBuilder builder = runner.createCommandBuilder(vsSolution, vsProject);
        builder.setReportFile(new File("john"));
        builder.addArgument("/properties:","VisualStudioSolution=12.0");
        int timeout = configuration.getInt(ReSharperConstants.TIMEOUT_MINUTES_KEY);
        Command command=builder.toCommand();
        //Verify
        String commandLine=command.toCommandLine();
        
        Assert.assertNotNull(commandLine);
        Assert.assertTrue(commandLine.contains("/properties:VisualStudioSolution=12.0"));
    }
    
}
