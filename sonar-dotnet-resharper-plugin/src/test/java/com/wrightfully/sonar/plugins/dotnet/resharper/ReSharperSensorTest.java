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
import org.sonar.plugins.dotnet.api.microsoft.MicrosoftWindowsEnvironment;
import org.sonar.plugins.dotnet.api.microsoft.VisualStudioProject;
import org.sonar.plugins.dotnet.api.microsoft.VisualStudioSolution;

import com.wrightfully.sonar.dotnet.tools.resharper.ReSharperCommandBuilder;
import com.wrightfully.sonar.dotnet.tools.resharper.ReSharperException;
import com.wrightfully.sonar.dotnet.tools.resharper.ReSharperRunner;
import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.ReSharperProfileExporter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.utils.command.Command;

public class ReSharperSensorTest {

    private static final String INSPECT_PATH = "C:/Program Files/JetBrains";
    private VisualStudioSolution vsSolution;
    private VisualStudioProject vsProject;
    private File solutionFile;
    private ReSharperRunner runner;

    @Before
    public void arrange() throws ReSharperException {
        vsSolution = mock(VisualStudioSolution.class);
        vsProject = mock(VisualStudioProject.class);
        runner = ReSharperRunner.create(INSPECT_PATH);
        solutionFile = new File("solution.sln");
        when(vsSolution.getSolutionFile()).thenReturn(solutionFile);
    }

    private class SimpleSensor extends ReSharperSensor {

        protected SimpleSensor(ProjectFileSystem fileSystem,
                RulesProfile rulesProfile,
                ReSharperProfileExporter profileExporter,
                ReSharperResultParser resharperResultParser,
                DotNetConfiguration configuration,
                MicrosoftWindowsEnvironment microsoftWindowsEnvironment) {
            super(fileSystem, rulesProfile, profileExporter,
                    resharperResultParser, configuration,
                    microsoftWindowsEnvironment);
            // TODO Auto-generated constructor stub
        }

        {

        }

        @Override
        public String[] getSupportedLanguages() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
