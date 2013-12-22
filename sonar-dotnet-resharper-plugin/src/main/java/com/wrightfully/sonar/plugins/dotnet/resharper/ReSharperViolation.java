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

import java.io.File;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.Violation;
import org.sonar.api.resources.Project;
import org.sonar.plugins.dotnet.api.microsoft.VisualStudioProject;

public class ReSharperViolation {
    private static final Logger LOG = LoggerFactory.getLogger(ReSharperViolation.class);
	private Project project;
	private VisualStudioProject vsProject;
    private SensorContext context;
    private Violation violation ;



		public ReSharperViolation(SensorContext context,Project project,VisualStudioProject vsProject) {
    		this.context = context;
    		this.project = project;
    		this.vsProject = vsProject;
    	}
    	
    	public void  createFileOrProjectViolation(SMInputCursor violationsCursor, Rule currentRule, File sourceFile)
    			throws XMLStreamException {
    		try {
    			violation=createViolationAgainstFile(violationsCursor, currentRule, sourceFile);
    		} catch (Exception ex){
    		    LOG.warn("Violation could not be saved against file, associating to VS project instead: " + sourceFile.getPath());
    		    violation=createViolationAgainstProject(violationsCursor, currentRule, sourceFile);
    		}
            context.saveViolation(violation);
    	}
	    private Violation createViolationAgainstFile(SMInputCursor violationsCursor, Rule currentRule, File sourceFile) throws Exception {
	        final org.sonar.api.resources.File sonarFile = org.sonar.api.resources.File.fromIOFile(sourceFile, project);

	        Violation violation = Violation.create(currentRule, sonarFile);

	        String message = violationsCursor.getAttrValue("Message");

	        String lineNumber = violationsCursor.getAttrValue("Line");
	        if (lineNumber != null) {
	            violation.setLineId(Integer.parseInt(lineNumber));

	            if (!vsProject.contains(sourceFile))
	            {
	                message += " (for file " + sonarFile.getName();
	                if (lineNumber != null) {
	                    message += " line " + lineNumber;
	                }
	                message +=  ")";

	            }
	        }

	        violation.setMessage(message.trim());
	        return violation;
	    }

	    private Violation createViolationAgainstProject(SMInputCursor violationsCursor, Rule currentRule, File sourceFile) throws XMLStreamException {
	        Violation violation = Violation.create(currentRule, project);
	        String lineNumber = violationsCursor.getAttrValue("Line");

	        String message = violationsCursor.getAttrValue("Message");

	        message += " (for file " + sourceFile.getName();
	        if (lineNumber != null) {
	            message += " line " + lineNumber;
	        }
	        message +=  ")";

	        violation.setMessage(message.trim());
	        return violation;
	    }
	    
	    public Violation getViolation() {
			return violation;
		}

		public void setViolation(Violation violation) {
			this.violation = violation;
		}

}
