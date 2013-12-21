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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.staxmate.in.SMInputCursor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.dotnet.api.DotNetResourceBridge;
import org.sonar.plugins.dotnet.api.microsoft.MicrosoftWindowsEnvironment;
import org.sonar.plugins.dotnet.api.microsoft.VisualStudioProject;
import org.sonar.plugins.dotnet.api.microsoft.VisualStudioSolution;
import org.sonar.plugins.dotnet.api.utils.ResourceHelper;
import org.sonar.test.TestUtils;

import com.google.common.collect.Lists;
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperViolation;
import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.ReSharperRule;
import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.ReSharperRule.ReSharperSeverity;

public class ReSharperViolationBuilderTest {

    private SensorContext _context;
    private DotNetResourceBridge _resourcesBridge;
    private Project _project;
    VisualStudioProject _vsProject;
    MicrosoftWindowsEnvironment _env;

    private Rule _rudRule;
    private Rule _cnigRule;
    private Rule _uplRule;
    private Rule _suvkeRule;
    private Rule _suvke2Rule;
    private Rule _rtscRule;
    private Rule _missingRule;
    @Before
    public void init() throws Exception {
        _context = mock(SensorContext.class);

        _resourcesBridge = mock(DotNetResourceBridge.class);
        when(_resourcesBridge.getLanguageKey()).thenReturn("cs");

        File resultFile = TestUtils.getResource("/solution/Example/resharper-results-example_sln.xml");
        final File soltionFolder = new File(resultFile.getParent());
        if (!soltionFolder.exists())
            throw new Exception("Solution folder path does not exist: " + soltionFolder.getPath());
        String sourcePath = soltionFolder.getCanonicalPath();

        _project = mock(Project.class);
        ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);
        when(fileSystem.getSourceDirs()).thenReturn(Lists.newArrayList(new File(sourcePath)));
        when(fileSystem.getSourceCharset()).thenReturn(Charset.forName("UTF-8"));
        when(_project.getFileSystem()).thenReturn(fileSystem);
        when(_project.getLanguageKey()).thenReturn("cs");
        when(_project.getName()).thenReturn("Example.Application");

        when(fileSystem.resolvePath(anyString())).thenAnswer(new Answer<File>(){
            public File answer(InvocationOnMock invocation) throws Throwable {
                String param = (String)invocation.getArguments()[0];
                return new File(soltionFolder + "/" + param);
            }
        });

        _env = mock(MicrosoftWindowsEnvironment.class);
        _vsProject = mock(VisualStudioProject.class);
        VisualStudioSolution solution = mock(VisualStudioSolution.class);

        when(_env.getCurrentSolution()).thenReturn(solution);
        when(_vsProject.getName()).thenReturn("Example.Application");
        when(_vsProject.contains(any(File.class))).thenReturn(true);
        when(solution.getProjectFromSonarProject(any(Project.class))).thenReturn(_vsProject);
        when(solution.getProject(any(File.class))).thenReturn(_vsProject);
        when(solution.getSolutionDir()).thenReturn(soltionFolder);

        ResourceHelper resourceHelper = mock(ResourceHelper.class);
        when(resourceHelper.isResourceInProject(any(Resource.class), any(Project.class))).thenReturn(true);

        _rudRule = Rule.create(ReSharperConstants.REPOSITORY_KEY, "RedundantUsingDirective", "RedundantUsingDirective")
                .setConfigKey("ReSharperInspectCode#RedundantUsingDirective");

        _cnigRule= Rule.create(ReSharperConstants.REPOSITORY_KEY, "ClassNeverInstantiated.Global", "ClassNeverInstantiated.Global")
                .setConfigKey("ReSharperInspectCode#ClassNeverInstantiated.Global");

        _uplRule = Rule.create(ReSharperConstants.REPOSITORY_KEY, "UnusedParameter.Local", "UnusedParameter.Local")
                .setConfigKey("ReSharperInspectCode#UnusedParameter.Local");

        _suvkeRule= Rule.create(ReSharperConstants.REPOSITORY_KEY, "SuggestUseVarKeywordEvident", "SuggestUseVarKeywordEvident")
                .setConfigKey("ReSharperInspectCode#SuggestUseVarKeywordEvident");

        _suvke2Rule= Rule.create(ReSharperConstants.REPOSITORY_KEY, "SuggestUseVarKeywordEverywhere", "SuggestUseVarKeywordEverywhere")
                .setConfigKey("ReSharperInspectCode#SuggestUseVarKeywordEverywhere");

        _rtscRule= Rule.create(ReSharperConstants.REPOSITORY_KEY, "RedundantToStringCall", "RedundantToStringCall")
                .setConfigKey("ReSharperInspectCode#RedundantToStringCall");

        _missingRule= Rule.create(ReSharperConstants.REPOSITORY_KEY, "Sonar.UnknownIssueType", "Sonar.UnknownIssueType")
                .setConfigKey("ReSharperInspectCode#Sonar.UnknownIssueType");

    }

    @Test
    public void CreateViolationForSourceFileInProject() throws Exception {
    	ReSharperViolation violationBuilder = new ReSharperViolation(_context,_project,_vsProject);
    	SMInputCursor violationsCursor = mock(SMInputCursor.class);
    	String message="booh";
    	String line="1234";
    	when(violationsCursor.getAttrValue("Line")).thenReturn(line);
    	when(violationsCursor.getAttrValue("Message")).thenReturn(message);

		Rule currentRule = _rudRule;
		File sourceFile = mock(File.class);
		List<File> files=_project.getFileSystem().getSourceDirs();
		when(sourceFile.getAbsolutePath()).thenReturn(files.get(0) + "/humbug.cs");
		
		when(_vsProject.contains(any(File.class))).thenReturn(true);
		
		Violation violation=violationBuilder.createViolationAgainstFile(violationsCursor, currentRule, sourceFile);
		Assert.assertEquals(line,violation.getLineId().toString());
		Assert.assertEquals(message,violation.getMessage());
    }
    
    @Test
    public void CreateViolationForSourceFileNotInProject() throws Exception {
    	ReSharperViolation violationBuilder = new ReSharperViolation(_context,_project,_vsProject);
    	SMInputCursor violationsCursor = mock(SMInputCursor.class);
    	String message="booh";
    	String line="1234";
    	when(violationsCursor.getAttrValue("Line")).thenReturn(line);
    	when(violationsCursor.getAttrValue("Message")).thenReturn(message);

		Rule currentRule = _rudRule;
		File sourceFile = mock(File.class);
		List<File> files=_project.getFileSystem().getSourceDirs();
		when(sourceFile.getAbsolutePath()).thenReturn(files.get(0) + "/humbug.cs");
		
		when(_vsProject.contains(any(File.class))).thenReturn(false);
		
		Violation violation=violationBuilder.createViolationAgainstFile(violationsCursor, currentRule, sourceFile);
		Assert.assertEquals(line,violation.getLineId().toString());
		String expected=message + " (for file humbug.cs line " + line + ")";
		Assert.assertEquals(expected,violation.getMessage());
    }


    
}
