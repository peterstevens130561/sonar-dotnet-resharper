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





import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.dotnet.api.DotNetConfiguration;
import org.sonar.plugins.dotnet.api.microsoft.VisualStudioProject;
import org.sonar.plugins.dotnet.api.microsoft.VisualStudioSolution;

import com.wrightfully.sonar.dotnet.tools.resharper.ReSharperCommandBuilder;
import com.wrightfully.sonar.dotnet.tools.resharper.ReSharperException;
import com.wrightfully.sonar.dotnet.tools.resharper.ReSharperRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.sonar.api.utils.command.Command;

public class ResSharperRunnerTest {

    private static final String INSPECT_PATH="C:/Program Files/JetBrains";
    private VisualStudioSolution vsSolution ;
    private VisualStudioProject vsProject ;
    private File solutionFile;
    private ReSharperRunner runner ;
    
    @Before
    public void arrange() throws ReSharperException {
        vsSolution = mock(VisualStudioSolution.class);
        vsProject = mock(VisualStudioProject.class); 
        runner = ReSharperRunner.create(INSPECT_PATH);
        solutionFile = new File("solution.sln");
        when(vsSolution.getSolutionFile()).thenReturn(solutionFile);
    }
    
    @Test
    public void ReSharperRunner_CreateBasicInvocations_CommandLineHasSolutionAtEnd() throws ReSharperException {
      
        //Act
        ReSharperCommandBuilder builder = runner.createCommandBuilder(vsSolution, vsProject,null);
        builder.setReportFile(new File("john"));

        //Assert
        Command command=builder.toCommand();
        String commandLine=command.toCommandLine();
        
        String expectedSolutionPath = solutionFile.getAbsolutePath().replaceAll("/", "\\\\");       
        assertTrue(commandLine.endsWith(" " + expectedSolutionPath));
    }
    
    @Test
    public void ReSharperRunner_CreateInvocationWithoutInspectCodeProperties_CommandLineHasNoProperties() throws ReSharperException {
      
        //Act
        ReSharperCommandBuilder builder = runner.createCommandBuilder(vsSolution, vsProject,null);
        builder.setReportFile(new File("john"));

        //Assert
        Command command=builder.toCommand();
        String commandLine=command.toCommandLine();
        
        Assert.assertNotNull(commandLine);
        Assert.assertFalse(commandLine.contains(" /properties"));
    }
    @Test
    public void ReSharperRunner_CreateInvocationWithInspectCodeProperties_CommandLineHasPropertiesBeforeSolution() throws ReSharperException {

       
        //Act
        ReSharperCommandBuilder builder = runner.createCommandBuilder(vsSolution, vsProject,null);
        builder.setReportFile(new File("john"));
        builder.addArgument("/properties:","VisualStudioSolution=12.0");

        //Assert
        Command command=builder.toCommand();
        String commandLine=command.toCommandLine();
        
        String expectedSolutionPath = solutionFile.getAbsolutePath().replaceAll("/", "\\\\");       
        Assert.assertTrue(commandLine.endsWith(expectedSolutionPath));
        
        String expectedPath=INSPECT_PATH.replaceAll("/", "\\\\"); 
        Assert.assertTrue(commandLine.startsWith(expectedPath));
        
        Assert.assertNotNull(commandLine);
        Assert.assertTrue(commandLine.contains("/properties:VisualStudioSolution=12.0"));
        

    }
    
}
