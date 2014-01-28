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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

import org.mockito.Matchers;

import static org.junit.Assert.*;

import java.io.Reader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Any;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.utils.ValidationMessages;

import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.ReSharperProfileImporter;
import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.ReSharperSonarWayProfile;
import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.ReSharperSonarWayProfileCSharp;


public class CustomSeveritiesIntegrationTest {

    private Settings settingsMock;
    
    @Before
    public void before() {
        settingsMock = mock(Settings.class);
    }
    
    @Test
    public void createProfileTest_NothingDefinedShouldWork() {
        RuleFinder finder = mock(RuleFinder.class);
        when(finder.find((RuleQuery) org.mockito.Matchers.anyObject())).thenReturn(null);
        ReSharperProfileImporter.CSharpRegularReSharperProfileImporter profileImporter = mock(ReSharperProfileImporter.CSharpRegularReSharperProfileImporter.class);
        RulesProfile profile = new RulesProfile();
        when(profileImporter.importProfile(any(Reader.class),any(ValidationMessages.class))).thenReturn(profile);
        ReSharperSonarWayProfile profileCSharp = new ReSharperSonarWayProfileCSharp(profileImporter, settingsMock);
        profileCSharp.createProfile(null);
        
        
    }

}
