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

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.stubbing.Answer;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.dotnet.api.DotNetResourceBridge;
import org.sonar.plugins.dotnet.api.microsoft.MicrosoftWindowsEnvironment;
import org.sonar.plugins.dotnet.api.microsoft.VisualStudioProject;
import org.sonar.plugins.dotnet.api.microsoft.VisualStudioSolution;
import org.sonar.plugins.dotnet.api.utils.ResourceHelper;
import org.sonar.test.TestUtils;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.StatusPrinter;

import org.slf4j.ILoggerFactory;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;

@RunWith(MockitoJUnitRunner.class)
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
    File resultFile;
    FailingIssuesVisitorListener failingIssuesListener;
    
    // inspiration taken from http://bloodredsun.com/2010/12/09/checking-logging-in-unit-tests/
    
    @Mock
    private Appender<LoggingEvent> mockAppender;
    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    @Before
    public void init() throws Exception {

        _context = mock(SensorContext.class);

        _resourcesBridge = mock(DotNetResourceBridge.class);
        when(_resourcesBridge.getLanguageKey()).thenReturn("cs");

        resultFile = TestUtils.getResource(RESULT_FILE);
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

        ConfigureState(true, false, true);
        failingIssuesListener = new FailingIssuesVisitorListener();
        _parser.addObserver(failingIssuesListener);
    }
	

    private void appendLogger() {
    	 LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger log = loggerContext.getLogger(FailingIssuesVisitorListener.class);
        mockAppender = mock(Appender.class);
        log.addAppender(mockAppender);
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
        failingIssuesListener.setIssueTypesToFailOn("CSharpErrors");
        _parser.parse(resultFile);
        int errors=failingIssuesListener.getErrorCount();
        Assert.assertEquals("expect no errors", errors,0);
	}
	
	/**
	 * Give the listener issues that do fail. Verify that the exception is thrown as well as
	 * the generated logs
	 */
	@Test
	public void GetVisitedWithIssuesThatDoFailTheAnalysisCheckDump() {
		appendLogger();
		parseIssuesThatFail();    
        checkDumpIsLogged();
        int errors=failingIssuesListener.getErrorCount();
        Assert.assertEquals("expect five errors in example.application", 5,errors);
        
	}


	/**
	 * Give the listener issues that do fail. Verify that the exception is thrown as well as
	 * the generated logs
	 */
	@Test
	public void GetVisitedWithIssuesThatDoFailTheAnalysis() {
		parseIssuesThatFail();
        int errors=failingIssuesListener.getErrorCount();
        Assert.assertEquals("expect five errors in example.application", 5,errors);
        
	}

	/**
	 * Test the default situation
	 */
	@Test
	public void GetVisitedWithIssuesAndNoRulesSpecified() {
        failingIssuesListener.setIssueTypesToFailOn("");
        _parser.parse(resultFile);
        int errors=failingIssuesListener.getErrorCount();
        Assert.assertEquals("expect no errors in example.application", 0,errors);
        
	}
	
	/**
	 * Test with a null string, you nevre know
	 */
	@Test
	public void GetVisitedWithIssuesAndNullRulesSpecified() {
        failingIssuesListener.setIssueTypesToFailOn(null);
        _parser.parse(resultFile);
        int errors=failingIssuesListener.getErrorCount();
        Assert.assertEquals("expect no errors in example.application", 0,errors);
        
	}
	
	/**
	 * Specify two issuetypes to fail on
	 */
	@Test
	public void GetVisitedWithTwoIssueTypesThatDoFailTheAnalysis() {
        failingIssuesListener.setIssueTypesToFailOn("UnusedParameter.Local,ClassNeverInstantiated.Global");
        try {
        	_parser.parse(resultFile);
        } catch ( SonarException e ) {
        	// should get an exception here, as we get errors
        }
        int errors=failingIssuesListener.getErrorCount();
        Assert.assertEquals("expect two errors in example.application", 2,errors);
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

		private void parseIssuesThatFail() {
			Boolean seenException=false;
	        failingIssuesListener.setIssueTypesToFailOn("SuggestUseVarKeywordEvident");
	        try {
	        	_parser.parse(resultFile);
	        } catch ( SonarException e ) {
	        	seenException=true;
	        }
	        Assert.assertTrue("Expected SonarException",seenException);
		}

		private void checkDumpIsLogged() {
			verify(mockAppender,times(2)).doAppend(captorLoggingEvent.capture());
	        List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
	        LoggingEvent dump = loggingEvents.get(1);
	        String lines[]=dump.getMessage().split("\n");
	        Assert.assertEquals(5,lines.length);
		}
		
}
