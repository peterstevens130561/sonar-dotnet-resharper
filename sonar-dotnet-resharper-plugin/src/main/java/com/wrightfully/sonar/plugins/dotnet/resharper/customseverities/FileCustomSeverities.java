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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import com.wrightfully.sonar.plugins.dotnet.resharper.ReSharperConstants;


public class FileCustomSeverities extends BaseCustomSeverities {

    private static final Logger LOG = LoggerFactory.getLogger(FileCustomSeverities.class);

    @Override
    InputSource createInputSource() {
        String propertyValue=getConfiguration().getString(ReSharperConstants.CUSTOM_SEVERITIES_PATH);
        if(StringUtils.isEmpty(propertyValue)) {
            return null ;
        }
        return getInputSource(propertyValue);
    }

    private InputSource getInputSource(String path) {
        try {
            File file = new File(path);
            FileReader fileReader = new FileReader(file);
            return new InputSource(fileReader);           
        } catch (FileNotFoundException e) {
            LOG.error("could not open " + path + "defined in " + ReSharperConstants.CUSTOM_SEVERITIES_PATH + " reason:", e);
        }
        return null;
    }
}
