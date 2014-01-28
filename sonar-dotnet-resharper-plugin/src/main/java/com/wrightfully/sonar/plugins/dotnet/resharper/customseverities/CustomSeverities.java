package com.wrightfully.sonar.plugins.dotnet.resharper.customseverities;

import org.sonar.api.profiles.RulesProfile;

public interface CustomSeverities {

    public abstract void mergeCustomSeverities(RulesProfile profile);

    public abstract String getProfileName();

}