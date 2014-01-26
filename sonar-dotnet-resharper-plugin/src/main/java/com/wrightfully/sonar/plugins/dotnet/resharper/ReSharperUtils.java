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

import org.sonar.api.rules.RulePriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReSharperUtils {
	
    private static final Logger LOG = LoggerFactory.getLogger(ReSharperUtils.class);
	  //http://www.jetbrains.com/resharper/webhelp/Reference__Options__Code_Inspection__Inspection_Severity.html
    //http://www.jetbrains.com/resharper/webhelp/Code_Analysis__Code_Highlighting.html
    public enum ReSharperSeverity {
        ERROR,   //Errors have the highest priority of all - they prevent your code from compiling.
        WARNING,  // ReSharper provides you with warnings that do not prevent your code from compiling but may nevertheless represent serious coding inefficiencies.
        SUGGESTION,  //Code suggestions provide insights into code structure, drawing your attention to things that aren't necessarily bad or wrong, but probably useful to know.
        INFO, //See IssueType "InvocationIsSkipped" has undocumented "INFO" severity -- http://youtrack.jetbrains.com/issue/RSRP-390375
        HINT, //This is the lowest possible severity level. A hint simply brings your attention to a particular code detail and recommends a way of improvement.
        DO_NOT_SHOW
    }

    public static ReSharperSeverity getResharperSeverity(String severity) {
        ReSharperSeverity result;
    	try {
            result=ReSharperSeverity.valueOf(severity);
        } catch (Exception ex)
        {
            result=ReSharperSeverity.WARNING;
            LOG.warn("Invalid severity"  + severity);
        }
    	return result;
    }

    /**
     * translate the resharper severity into sonar priority
     */
    public static  RulePriority translateResharperPriorityIntoSonarPriority(ReSharperSeverity reSharperSeverity) {
        switch (reSharperSeverity) {
            case ERROR:
                return RulePriority.BLOCKER;
            case WARNING :
                return RulePriority.CRITICAL;
            case SUGGESTION:
                return RulePriority.MINOR;
            case HINT:
            case INFO:
            case DO_NOT_SHOW:
                return RulePriority.INFO ;
            default:
                return null;
        }
    }

    /**
     * translate sonar priority into resharper severity
     * @param priority
     * @return
     */
   public static  ReSharperSeverity translateSonarPriorityIntoResharperSeverity(RulePriority priority) {

        switch (priority) {
            case BLOCKER:
                return ReSharperSeverity.ERROR;
            case CRITICAL:
            case MAJOR:
                return ReSharperSeverity.WARNING;
            case MINOR:
                return ReSharperSeverity.SUGGESTION;
            case INFO:
                return ReSharperSeverity.HINT;
            default:
                return null;
        }

    }

}
