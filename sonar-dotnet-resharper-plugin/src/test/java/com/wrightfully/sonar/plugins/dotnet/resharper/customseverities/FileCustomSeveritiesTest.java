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

import static org.mockito.Mockito.when;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sonar.api.config.Settings;
import org.sonar.test.TestUtils;
import org.xml.sax.InputSource;

import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperConstants;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Settings.class)
public class FileCustomSeveritiesTest {
    
    private Settings settings ;
    private FileCustomSeverities customSeverities ;
   
    @Before
    public void before() {
        customSeverities = new FileCustomSeveritiesStub() ;
        settings = PowerMockito.mock(Settings.class);
        customSeverities.setSettings(settings);      
    }

    @Test
    public void FileDoesNotExist_ExpectNoInputSource() {
        InputSource inputSource =customSeverities.createInputSource("rabarber901234");
        Assert.assertNull(inputSource);
        
    }
    
    @Test
    public void FileExists_ExpectInputSource() {
        File testFile=TestUtils.getResource("CustomSeverities/DotSettings.xml");
        String path=testFile.getAbsolutePath();
        InputSource inputSource =customSeverities.createInputSource(path);
        Assert.assertNotNull(inputSource);       
    }
    
    /**
     * Tests the famous BOM in Windows
     */
    @Test
    public void FileWithBOMAnd2EntriesExists_ExpectMapWith2Entries() {
        File testFile=TestUtils.getResource("CustomSeverities/DotSettingsWithBOM.xml");
        String path=testFile.getAbsolutePath();
        InputSource inputSource =customSeverities.createInputSource(path);
        CustomSeveritiesMap map=customSeverities.parseCustomSeverities(inputSource);
        Assert.assertEquals(2, map.size());     
    }
    

    private void setPropertyValue(String value) {
        when(settings.getString(ReSharperConstants.CUSTOM_SEVERITIES_PATH)).thenReturn(value);
    }
    private class FileCustomSeveritiesStub extends FileCustomSeverities {
        public InputSource createInputSource(String fileName) {
            return super.createInputSource(fileName);
        }
    }
}
