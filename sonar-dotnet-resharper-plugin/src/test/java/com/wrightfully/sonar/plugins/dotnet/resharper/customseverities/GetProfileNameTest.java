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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperConfiguration;
import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperConstants;

public class GetProfileNameTest {

    private ReSharperConfiguration settingsMock;

    @Before
    public void beforeTest() {
        settingsMock = mock(ReSharperConfiguration.class);
        
    }
    
    @Test
    public void getProfileName_ProfileNameDefined_ShouldBeDefault() {
        testGetProfileName(ReSharperConstants.PROFILE_DEFAULT,null);
    }
    
    @Test
    public void getProfileName_ProfileNameEmpty_ShouldBeDefault() {
            testGetProfileName(ReSharperConstants.PROFILE_DEFAULT,"");
        }
    
   @Test
    public void getProfileName_ProfileNameDefined_ShouldBeSame() {
       String definedProfileName="booh";
       testGetProfileName(definedProfileName,definedProfileName);
    }
   
    private void testGetProfileName(String expectedProfileName,String definedProfileName) {
        when(settingsMock.getString(ReSharperConstants.PROFILE_NAME)).thenReturn(definedProfileName);
        CustomSeverities customSeverities = new CustomSeverities(settingsMock);    
        String actualProfileName=customSeverities.getProfileName();
        Assert.assertEquals(expectedProfileName,actualProfileName);
    }

}
