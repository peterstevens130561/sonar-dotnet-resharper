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
package com.wrightfully.sonar.plugins.dotnet.resharper.customseverities;


import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.test.TestUtils;

import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperConstants;
import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.ReSharperProfileImporter;
import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.ReSharperSonarWayProfile;
import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.ReSharperSonarWayProfileCSharp;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Settings.class,ValidationMessages.class})
public class CustomSeveritiesIntegrationTest {

    private Settings settingsMock;
    private ValidationMessages messagesMock;
    
    @Before
    public void beforeTest() {
        settingsMock = PowerMockito.mock(Settings.class);
        messagesMock = PowerMockito.mock(ValidationMessages.class);
    }

    
    @Test
    public void createProfileTest_ProfileSetNoCustomSeverities_ProfileShouldBeNamedAccordingly() {
        //Given a profile set to booh, and no defined custom severities
        String profileName="booh";
        ReSharperProfileImporter.CSharpRegularReSharperProfileImporter profileImporter = createProfileMocksFocussedOnProfileGetName();
        setSetting(ReSharperConstants.PROFILE_NAME,profileName);
        ReSharperSonarWayProfile profileCSharp = new ReSharperSonarWayProfileCSharp(profileImporter, settingsMock);
        //When creating the profile
        RulesProfile actualProfile=profileCSharp.createProfile(messagesMock);
        
        //Then the profile should have name booh, and no messages should be logged
        assertEquals(profileName,actualProfile.getName());   
        assertNoMessages();
    }


    @Test
    public void createProfileTest_ProfileNotSetNoCustomSeverityes_ProfileShouldBeDefault() {
        ReSharperProfileImporter.CSharpRegularReSharperProfileImporter profileImporter = createProfileMocksFocussedOnProfileGetName();

        ReSharperSonarWayProfile profileCSharp = new ReSharperSonarWayProfileCSharp(profileImporter, settingsMock);
        //When creating the profile
        RulesProfile actualProfile=profileCSharp.createProfile(messagesMock);
        
        //Then the profile should have name booh, and no messages should be logged
        assertEquals(ReSharperConstants.PROFILE_DEFAULT,actualProfile.getName());
        assertNoMessages();       
    }
    
    private ReSharperProfileImporter.CSharpRegularReSharperProfileImporter createProfileMocksFocussedOnProfileGetName() {
        RuleFinder finder = mock(RuleFinder.class);
        when(finder.find((RuleQuery) org.mockito.Matchers.anyObject())).thenReturn(null);
        ReSharperProfileImporter.CSharpRegularReSharperProfileImporter profileImporter = mock(ReSharperProfileImporter.CSharpRegularReSharperProfileImporter.class);
        @SuppressWarnings("deprecation")
        RulesProfile profile = new RulesProfile();
        when(profileImporter.importProfile(any(Reader.class),any(ValidationMessages.class))).thenReturn(profile);
        return profileImporter;
    }

  
    @Test
    public void createProfileTest_NoCustomSeverities_ShouldHaveDefaultRules() {
        ReSharperSonarWayProfile profileCSharp = prepareProfiler();
        //When creating the profile
        RulesProfile actualProfile=profileCSharp.createProfile(messagesMock);
        
        //Then the profile should have name booh, and no messages should be logged, and there should be 650 rules
        //unless more rules were added
        int activeRuleSize=actualProfile.getActiveRules().size();
        assertEquals(650,activeRuleSize);
        assertNoMessages();       
    }
    
  
    @Test
    public void createProfileTest_CustomSeverities_ShouldHaveSeverityChanges() throws IOException {
        //Given a profile set to booh, and no defined custom severities, and assuming all rules are present
        ReSharperSonarWayProfile profileCSharp = prepareProfiler();

        //When creating the profile without and with the custom severities
        RulesProfile defaultProfile=profileCSharp.createProfile(messagesMock);
        setCustomSeveritiesProperty("DotSettings.xml");    
        RulesProfile customProfile=profileCSharp.createProfile(messagesMock);
        
        //Then there should be differences
        int severityDifferences = getSeverityDifferencesCount(defaultProfile,customProfile);
        assertEquals(72,severityDifferences);
    }

    @Test
    public void createProfileTest_2CustomSeveritiesFromFile_ShouldHaveS2everityChanges() throws IOException {
        //Given a profile set to booh, and no defined custom severities, and assuming all rules are present
        ReSharperSonarWayProfile profileCSharp = prepareProfiler();

        //When creating the profile without and with the custom severities
        RulesProfile defaultProfile=profileCSharp.createProfile(messagesMock);
        setFileProperty("DotSettingsWithBOM.xml");    
        RulesProfile customProfile=profileCSharp.createProfile(messagesMock);
        
        //Then there should be differences
        int severityDifferences = getSeverityDifferencesCount(defaultProfile,customProfile);
        assertEquals(2,severityDifferences);
        assertEquals(RulePriority.INFO,getRulePriority(customProfile,"AssignNullToNotNullAttribute"));
        assertEquals(RulePriority.INFO,getRulePriority(customProfile,"BaseMemberHasParams"));
    }
    
    private ReSharperSonarWayProfile prepareProfiler() {
        RuleFinder ruleFinder = new SimpleRuleFinder();
        ReSharperProfileImporter.CSharpRegularReSharperProfileImporter profileImporter = new ReSharperProfileImporter.CSharpRegularReSharperProfileImporter(ruleFinder);

        ReSharperSonarWayProfile profileCSharp = new ReSharperSonarWayProfileCSharp(profileImporter, settingsMock);
        return profileCSharp;
    }   
    
    private RulePriority getRulePriority(RulesProfile profile,String key) {
        List<ActiveRule> rules = profile.getActiveRules();
        for(ActiveRule rule : rules) {
            if(rule.getRuleKey().equals(key)) {
                return rule.getSeverity();
            }
        }
        return null;     
    }
    private int getSeverityDifferencesCount(RulesProfile defaultProfile,
            RulesProfile customProfile) {

        List<ActiveRule> defaultRules=defaultProfile.getActiveRules();
        List<ActiveRule> customRules=customProfile.getActiveRules();
        int differences=0;
        
        for(int i=0;i<defaultRules.size();i++) {
            RulePriority defaultPriority =defaultRules.get(i).getSeverity();
            RulePriority customPriority = customRules.get(i).getSeverity();
            if(!defaultPriority.equals(customPriority)) {
                differences +=1;
                
            }
        }
        return differences;
        
    }

    private void setCustomSeveritiesProperty(String file) throws IOException {
        Reader reader = new StringReader(TestUtils.getResourceContent("/CustomSeverities/" + file ));
        String myString = IOUtils.toString(reader);
        setSetting(ReSharperConstants.CUSTOM_SEVERITIES_DEFINITON,myString);
    }
    
    private void setFileProperty(String file) throws IOException {
        File testFile=TestUtils.getResource("CustomSeverities/" + file);
        String path=testFile.getAbsolutePath();
        setSetting(ReSharperConstants.CUSTOM_SEVERITIES_PATH,path);
    }

    private void assertNoMessages() {
        verify(messagesMock,never()).addErrorText(anyString());
        verify(messagesMock,never()).addWarningText(anyString());
        verify(messagesMock,never()).addInfoText(anyString());
    }
    
    /***
     * add setting to the mock
     * @param key of the property
     * @param value of the property
     */
    private void setSetting(String key,String value) {
        when(settingsMock.getString(key)).thenReturn(value);
    }
    
    private class SimpleRuleFinder implements RuleFinder {

        public Rule findById(int ruleId) {
            throw new NotImplementedException();
        }

        public Rule findByKey(String repositoryKey, String key) {
            throw new NotImplementedException();
        }

        /***
         * As all rules are added in the repository part, we can just return a rule
         */
        public Rule find(RuleQuery query) {
            @SuppressWarnings("deprecation")
            Rule rule = new Rule() ;
            rule.setKey(query.getKey());
            return rule;
        }
        public Collection<Rule> findAll(RuleQuery query) {
            throw new NotImplementedException();
        }
        
    }
    
    

}
