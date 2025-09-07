package org.hsw.wikitoolsrenders.features.remind_mod_update;

import org.hsw.wikitoolsrenders.feature.remind_mod_update.FindModVersion;

public class LatestReleaseFinderStub implements FindModVersion {
    private final FindModVersionResult result;

    public LatestReleaseFinderStub(FindModVersionResult result) {
        this.result = result;
    }

    @Override
    public FindModVersionResult findLatestVersion() {
        return result;
    }
}
