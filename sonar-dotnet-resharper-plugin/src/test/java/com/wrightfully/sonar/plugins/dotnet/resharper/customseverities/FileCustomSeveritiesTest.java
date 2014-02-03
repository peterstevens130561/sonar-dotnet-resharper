
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
    public void NoPropertySet_NoInputSource() {
        setPropertyValue(null);
        InputSource inputSource =customSeverities.createInputSource();
        Assert.assertNull(inputSource);
    }
    
    @Test
    public void FileDoesNotExist_NoInputSource() {
        setPropertyValue("rabarber901234");
        InputSource inputSource =customSeverities.createInputSource();
        Assert.assertNull(inputSource);
        
    }
    
    @Test
    public void FileExists_InputSource() {
        File testFile=TestUtils.getResource("CustomSeverities/DotSettings.xml");
        String path=testFile.getAbsolutePath();
        setPropertyValue(path);
        InputSource inputSource =customSeverities.createInputSource();
        Assert.assertNotNull(inputSource);       
    }
    
    private void setPropertyValue(String value) {
        when(settings.getString(ReSharperConstants.CUSTOM_SEVERITIES_PATH)).thenReturn(value);
    }
    private class FileCustomSeveritiesStub extends FileCustomSeverities {
        public InputSource createInputSource(String FileName) {
            return super.createInputSource();
        }
    }
}
