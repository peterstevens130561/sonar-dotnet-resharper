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
package com.wrightfully.sonar.plugins.dotnet.resharper.profiles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.utils.ValidationMessages;

import com.wrightfully.sonar.plugins.dotnet.resharper.customseverities.CustomSeverities;
import com.wrightfully.sonar.plugins.dotnet.resharper.customseverities.PropertyBasedCustomSeverities;

import java.io.InputStreamReader;


public class ReSharperSonarWayProfile extends ProfileDefinition {
	
    private static final Logger LOG = LoggerFactory.getLogger(ReSharperSonarWayProfile.class);
    private ReSharperProfileImporter profileImporter;
    private String languageKey;
    private Settings settings;
    
    protected ReSharperSonarWayProfile(ReSharperProfileImporter profileImporter, String languageKey,Settings settings) {
        this.profileImporter = profileImporter;
        this.languageKey = languageKey;
        this.settings=settings;
    }

    public RulesProfile createProfile(ValidationMessages messages) {
        RulesProfile profile = profileImporter.importProfile(
                new InputStreamReader(getClass().getResourceAsStream("/com/wrightfully/sonar/plugins/dotnet/resharper/rules/DefaultRules.ReSharper")), messages);
        profile.setLanguage(languageKey);
        CustomSeverities customSeverities = new PropertyBasedCustomSeverities(settings);
        profile.setName(customSeverities.getProfileName());
        customSeverities.mergeCustomSeverities(profile);
        return profile;
    }

}