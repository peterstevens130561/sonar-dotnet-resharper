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


import org.sonar.api.*;
import org.sonar.api.config.Settings;

import java.util.ArrayList;
import java.util.List;


/**
 * Creates ReSharper rule repositories for every language supported by ReSharper.
 */
@Properties({
        @Property(key = ReSharperConstants.CUSTOM_RULES_PROP_KEY,
                defaultValue = "", name = "ReSharper custom rules",
                description = "Add &lt;IssueType&gt; values from ReSharper's results file for issues that are not built-in to the plugin's rules. A restart is required to take affect.",
                type = PropertyType.TEXT, global = true, project = false)
})
public class ReSharperRuleRepositoryProvider extends ExtensionProvider implements ServerExtension {

    private Settings settings;

    public ReSharperRuleRepositoryProvider(Settings settings) {
        this.settings = settings;
    }

    @Override
    public Object provide() {
        List<ReSharperRuleRepository> extensions = new ArrayList<ReSharperRuleRepository>();

        for (String languageKey : ReSharperConstants.SUPPORTED_LANGUAGES) {
            // every repository key should be "resharper-<language_key>"
            String repoKey = ReSharperConstants.REPOSITORY_KEY + "-" + languageKey;
            extensions.add(new ReSharperRuleRepository(repoKey, languageKey, settings));
        }

        return extensions;
    }

}
