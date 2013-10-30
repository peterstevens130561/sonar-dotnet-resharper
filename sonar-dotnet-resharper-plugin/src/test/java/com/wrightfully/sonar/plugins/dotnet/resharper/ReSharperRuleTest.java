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

import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.ReSharperRule;
import com.wrightfully.sonar.plugins.dotnet.resharper.profiles.ReSharperRule.ReSharperSeverity;
import org.junit.Test;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;

import static org.fest.assertions.Assertions.assertThat;

public class ReSharperRuleTest {

    @Test
    public void testReSharperRuleAllFields() throws Exception {

        ReSharperRule reSharperRule = new ReSharperRule();
        reSharperRule.setId("MyId");
        reSharperRule.setEnabled(true);
        reSharperRule.setCategory("This is my category");
        reSharperRule.setDescription("A rather boring description");
        reSharperRule.setWikiLink("http://foo.bar/baz/bin");
        reSharperRule.setSeverity(ReSharperSeverity.HINT);

        Rule sonarRule = reSharperRule.toSonarRule();

        assertThat(sonarRule.getKey()).isEqualTo("MyId");
        assertThat(sonarRule.getName()).isEqualTo("MyId");
        assertThat(sonarRule.getSeverity()).isEqualTo(RulePriority.INFO);
        assertThat(sonarRule.getConfigKey()).isEqualTo("ReSharperInspectCode#MyId");
        assertThat(sonarRule.getDescription()).isEqualTo("A rather boring description<br />http://foo.bar/baz/bin<br />(Category: This is my category)");
    }


    @Test
    public void testReSharperRuleNoCategory() throws Exception {

        ReSharperRule reSharperRule = new ReSharperRule();
        reSharperRule.setId("MyId");
        reSharperRule.setEnabled(true);
        reSharperRule.setDescription("A rather boring description");
        reSharperRule.setWikiLink("http://foo.bar/baz/bin");
        reSharperRule.setSeverity(ReSharperSeverity.HINT);

        Rule sonarRule = reSharperRule.toSonarRule();

        assertThat(sonarRule.getKey()).isEqualTo("MyId");
        assertThat(sonarRule.getName()).isEqualTo("MyId");
        assertThat(sonarRule.getSeverity()).isEqualTo(RulePriority.INFO);
        assertThat(sonarRule.getConfigKey()).isEqualTo("ReSharperInspectCode#MyId");
        assertThat(sonarRule.getDescription()).isEqualTo("A rather boring description<br />http://foo.bar/baz/bin");
    }

    @Test
    public void testReSharperRuleNoWiki() throws Exception {

        ReSharperRule reSharperRule = new ReSharperRule();
        reSharperRule.setId("MyId");
        reSharperRule.setEnabled(true);
        reSharperRule.setCategory("This is my category");
        reSharperRule.setDescription("A rather boring description");
        reSharperRule.setSeverity(ReSharperSeverity.HINT);

        Rule sonarRule = reSharperRule.toSonarRule();

        assertThat(sonarRule.getKey()).isEqualTo("MyId");
        assertThat(sonarRule.getName()).isEqualTo("MyId");
        assertThat(sonarRule.getSeverity()).isEqualTo(RulePriority.INFO);
        assertThat(sonarRule.getConfigKey()).isEqualTo("ReSharperInspectCode#MyId");
        assertThat(sonarRule.getDescription()).isEqualTo("A rather boring description<br />(Category: This is my category)");
    }

    @Test
    public void testReSharperRuleNoCategoryNoWiki() throws Exception {

        ReSharperRule reSharperRule = new ReSharperRule();
        reSharperRule.setId("MyId");
        reSharperRule.setEnabled(true);
        reSharperRule.setDescription("A rather boring description");
        reSharperRule.setSeverity(ReSharperSeverity.HINT);

        Rule sonarRule = reSharperRule.toSonarRule();

        assertThat(sonarRule.getKey()).isEqualTo("MyId");
        assertThat(sonarRule.getName()).isEqualTo("MyId");
        assertThat(sonarRule.getSeverity()).isEqualTo(RulePriority.INFO);
        assertThat(sonarRule.getConfigKey()).isEqualTo("ReSharperInspectCode#MyId");
        assertThat(sonarRule.getDescription()).isEqualTo("A rather boring description");
    }



    @Test
    public void testReSharperRuleReSharperSeverityToSonarPriority() throws Exception {

        /*
            ReSharperSeverity.ERROR => RulePriority.BLOCKER;
            ReSharperSeverity.WARNING => RulePriority.CRITICAL;
            ReSharperSeverity.SUGGESTION => RulePriority.MINOR;
            ReSharperSeverity.HINT => RulePriority.INFO ;
            ReSharperSeverity.DO_NOT_SHOW => RulePriority.INFO ;
        */


        ReSharperRule reSharperRule = new ReSharperRule();

        reSharperRule.setSeverity(ReSharperSeverity.ERROR);
        assertThat(reSharperRule.getSonarPriority()).isEqualTo(RulePriority.BLOCKER);


        reSharperRule.setSeverity(ReSharperSeverity.WARNING);
        assertThat(reSharperRule.getSonarPriority()).isEqualTo(RulePriority.CRITICAL);


        reSharperRule.setSeverity(ReSharperSeverity.SUGGESTION);
        assertThat(reSharperRule.getSonarPriority()).isEqualTo(RulePriority.MINOR);


        reSharperRule.setSeverity(ReSharperSeverity.HINT);
        assertThat(reSharperRule.getSonarPriority()).isEqualTo(RulePriority.INFO);

        reSharperRule.setSeverity(ReSharperSeverity.DO_NOT_SHOW);
        assertThat(reSharperRule.getSonarPriority()).isEqualTo(RulePriority.INFO);
    }

    @Test
    public void testReSharperRuleSonarPriorityToReSharperSeverity() throws Exception {

        /*
            RulePriority.BLOCKER => ReSharperSeverity.ERROR;
            RulePriority.CRITICAL => ReSharperSeverity.WARNING ;
            RulePriority.MAJOR => ReSharperSeverity.WARNING ;
            RulePriority.MINOR => ReSharperSeverity.SUGGESTION;
            RulePriority.INFO => ReSharperSeverity.HINT;
        */


        ReSharperRule reSharperRule = new ReSharperRule();

        reSharperRule.setSonarPriority(RulePriority.BLOCKER);
        assertThat(reSharperRule.getSeverity()).isEqualTo(ReSharperSeverity.ERROR);


        reSharperRule.setSonarPriority(RulePriority.CRITICAL);
        assertThat(reSharperRule.getSeverity()).isEqualTo(ReSharperSeverity.WARNING);


        reSharperRule.setSonarPriority(RulePriority.MAJOR);
        assertThat(reSharperRule.getSeverity()).isEqualTo(ReSharperSeverity.WARNING);


        reSharperRule.setSonarPriority(RulePriority.MINOR);
        assertThat(reSharperRule.getSeverity()).isEqualTo(ReSharperSeverity.SUGGESTION);


        reSharperRule.setSonarPriority(RulePriority.INFO);
        assertThat(reSharperRule.getSeverity()).isEqualTo(ReSharperSeverity.HINT);
    }


    @Test
    public void testReSharperRuleCreateFromActiveRule() throws Exception {

        Rule sonarRule = Rule.create(ReSharperConstants.REPOSITORY_KEY+"-cs", "MyId", "MyId")
                .setDescription("A rather boring description<br />http://foo.bar/baz/bin<br />(Category: This is my category)")
                .setConfigKey("ReSharperInspectCode#MyId")
                .setSeverity(RulePriority.INFO);


        RulesProfile profile = RulesProfile.create("Sonar way", "cs");
        ActiveRule activeRule = profile.activateRule(sonarRule, RulePriority.INFO);

        ReSharperRule reSharperRule = ReSharperRule.createFromActiveRule(activeRule);
        assertThat(reSharperRule.getId()).isEqualTo("MyId");
        assertThat(reSharperRule.isEnabled()).isEqualTo(true);
        assertThat(reSharperRule.getDescription()).isEqualTo("A rather boring description<br />http://foo.bar/baz/bin<br />(Category: This is my category)");
        assertThat(reSharperRule.getSeverity()).isEqualTo(ReSharperSeverity.HINT);

    }


}
