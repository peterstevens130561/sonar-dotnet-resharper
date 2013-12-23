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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.dotnet.api.DotNetResourceBridge;
import org.sonar.plugins.dotnet.api.microsoft.MicrosoftWindowsEnvironment;
import org.sonar.plugins.dotnet.api.microsoft.VisualStudioProject;
import org.sonar.plugins.dotnet.api.microsoft.VisualStudioSolution;
import org.sonar.plugins.dotnet.api.utils.ResourceHelper;
import org.sonar.test.TestUtils;

import com.google.common.collect.Lists;
import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.IssueVisitor;

public class FailingIssuesVisitorTest {

    private SensorContext _context;
    private DotNetResourceBridge _resourcesBridge;
    private ReSharperResultParser _parser;
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

    final static String RESULT_FILE = "/solution/Example/resharper-results-example_sln.xml";
    @Before
    public void init() throws Exception {
        _context = mock(SensorContext.class);

        _resourcesBridge = mock(DotNetResourceBridge.class);
        when(_resourcesBridge.getLanguageKey()).thenReturn("cs");

        File resultFile = TestUtils.getResource(RESULT_FILE);
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
	
    private void ConfigureState(boolean isSupported, boolean isExcluded, boolean propertyIncludeAllFiles) {

        ReSharperConfiguration configuration = mock(ReSharperConfiguration.class);
        when(configuration.getBoolean(ReSharperConstants.INCLUDE_ALL_FILES)).thenReturn(propertyIncludeAllFiles);

        when(_context.isExcluded(any(Resource.class))).thenReturn(isExcluded);

        when(_vsProject.contains(any(File.class))).thenReturn(isSupported);

        _parser = new ReSharperResultParser(_env, _project, _context, newRuleFinder(), configuration);

    }
    
	@Test
	public void GetVisitedWithIssuesThatDoNotFailTheAnalysis() {
        ConfigureState(true, false, true);

        File resultFile = TestUtils.getResource("/solution/Example/resharper-results-example_sln.xml");
        FailingIssuesVisitor failingIssuesVisitor = new FailingIssuesVisitor();
        failingIssuesVisitor.setIssuesToFailOn("CSharpErrors");
        _parser.addVisitor(failingIssuesVisitor);
        _parser.parse(resultFile);
        int errors=failingIssuesVisitor.getErrorCount();
        Assert.assertEquals("expect no errors", errors,0);
	}
	
	@Test
	public void GetVisitedWithIssuesThatDoFailTheAnalysis() {
        ConfigureState(true, false, true);

        File resultFile = TestUtils.getResource("/solution/Example/resharper-results-example_sln.xml");
        FailingIssuesVisitor failingIssuesVisitor = new FailingIssuesVisitor();
        failingIssuesVisitor.setIssuesToFailOn("SuggestUseVarKeywordEvident");
        _parser.addVisitor(failingIssuesVisitor);
        _parser.parse(resultFile);
        int errors=failingIssuesVisitor.getErrorCount();
        Assert.assertEquals("expect five errors in example.application", 5,errors);
        
	}
	
	@Test
	public void GetVisitedWithTwoIssueTypesThatDoFailTheAnalysis() {
        ConfigureState(true, false, true);

        File resultFile = TestUtils.getResource("/solution/Example/resharper-results-example_sln.xml");
        FailingIssuesVisitor failingIssuesVisitor = new FailingIssuesVisitor();
        failingIssuesVisitor.setIssuesToFailOn("UnusedParameter.Local,ClassNeverInstantiated.Global");
        _parser.addVisitor(failingIssuesVisitor);
        _parser.parse(resultFile);
        int errors=failingIssuesVisitor.getErrorCount();
        Assert.assertEquals("expect five errors in example.application", 5,errors);
        
	}
	
	   private RuleFinder newRuleFinder() {
	        RuleFinder ruleFinder = mock(RuleFinder.class);
	        when(ruleFinder.find((RuleQuery) anyObject())).thenAnswer(new Answer<Rule>() {

	            public Rule answer(InvocationOnMock iom) throws Throwable {
	                RuleQuery query = (RuleQuery) iom.getArguments()[0];

	                String ruleKey = query.getConfigKey();

	                if (ruleKey.equals("ReSharperInspectCode#RedundantUsingDirective")) {
	                    return _rudRule;
	                } else if (ruleKey.equals("ReSharperInspectCode#ClassNeverInstantiated.Global")) {
	                    return _cnigRule;
	                } else if (ruleKey.equals("ReSharperInspectCode#UnusedParameter.Local"))   {
	                    return _uplRule;
	                } else if (ruleKey.equals("ReSharperInspectCode#SuggestUseVarKeywordEvident")){
	                    return _suvkeRule;
	                } else if (ruleKey.equals("ReSharperInspectCode#SuggestUseVarKeywordEverywhere")){
	                    return _suvke2Rule;
	                } else if (ruleKey.equals("ReSharperInspectCode#RedundantToStringCall")){
	                    return _rtscRule;
	                } else if (ruleKey.equals("ReSharperInspectCode#UnknownRule1")) {
	                    return null;
	                } else if (ruleKey.equals("ReSharperInspectCode#UnknownRule2")) {
	                    return null;
	                } else if (ruleKey.equals("ReSharperInspectCode#UnknownRule3")) {
	                    return null;
	                } else if (ruleKey.equals("ReSharperInspectCode#Sonar.UnknownIssueType")) {
	                    return _missingRule;
	                } else {
	                    Rule fakeRule = Rule.create(ReSharperConstants.REPOSITORY_KEY, query.getKey(), "Fake rule")
	                            .setConfigKey(query.getConfigKey());
	                    return fakeRule;
	                }

	            }
	        });
	        return ruleFinder;
	    }

}
