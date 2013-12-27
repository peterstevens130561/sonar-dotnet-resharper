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
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.utils.ValidationMessages;

import com.wrightfully.sonar.dotnet.tools.resharper.ReSharperException;
import com.wrightfully.sonar.plugins.dotnet.resharper.CustomSeverities;
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperConfiguration;
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperConstants;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

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
        String profileName=getProfileName();
        profile.setName(profileName);
        mergeCustomSeveritiesIntoProfile(profile);
        return profile;
    }

    private void mergeCustomSeveritiesIntoProfile(RulesProfile profile) {
        List<ActiveRule> rules = profile.getActiveRules();
        ReSharperConfiguration config = new ReSharperConfiguration(settings);
        CustomSeverities customSeverities = new CustomSeverities(config);
        customSeverities.parse();
        if (rules == null) return;
        for (ActiveRule activeRule : rules) {
            customSeverities.assignCustomSeverity(activeRule);
        }
    }


    private String getProfileName() {
    	String profileName=ReSharperConstants.PROFILE_DEFVALUE;
    	Map <String,String> properties= settings.getProperties() ;
    	LOG.info(" found properties" + properties.size());
    	String customName=settings.getString(ReSharperConstants.PROFILE_NAME);
    	if(customName != null && customName.length()>0) {
    		profileName = customName;
    	} else {
    		LOG.warn("No profile defined for resharper, using default");
    	}
    		
    	LOG.info("Using profile " + profileName);
    	return profileName;
    }

}