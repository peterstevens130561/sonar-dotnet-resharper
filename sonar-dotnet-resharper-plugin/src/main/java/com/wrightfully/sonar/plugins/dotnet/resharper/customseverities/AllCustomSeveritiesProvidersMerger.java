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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;

import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperConstants;

public class AllCustomSeveritiesProvidersMerger {
    

    private static final Logger LOG = LoggerFactory.getLogger(AllCustomSeveritiesProvidersMerger.class);
    CustomSeverities[] customSeveritiesMergers = { new FileCustomSeverities(), new PropertyBasedCustomSeverities() };
    private Settings settings;
    private RulesProfile profile;
    
    public void merge() {
        for(CustomSeverities merger : customSeveritiesMergers ) {
            merger.setSettings(settings);
            merger.merge(profile);
        }
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public void setProfile(RulesProfile profile) {
        this.profile = profile;        
    }
    
    /* (non-Javadoc)
    * @see com.wrightfully.sonar.plugins.dotnet.resharper.customseverities.CustomSeverities#getProfileName()
    */
   public String getProfileName() {
           String profileName=ReSharperConstants.PROFILE_DEFAULT;
           String customName=settings.getString(ReSharperConstants.PROFILE_NAME);
           if(customName != null && customName.length()>0) {
               profileName = customName;
           } else {
               LOG.warn("No profile defined for resharper, using default");
           }
               
           LOG.debug("Using profile " + profileName);
           return profileName;
       }
    
}
