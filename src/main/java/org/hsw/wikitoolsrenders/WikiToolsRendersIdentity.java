package org.hsw.wikitoolsrenders;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WikiToolsRendersIdentity {

    public static final String MODID = "${GRADLE_MOD_ID}";
    public static final String VERSION = "${GRADLE_MOD_VERSION}";

    public static final String REPOSITORY_URL =
            "https://github.com/skyblock-wiki/wikitools-renders-1.8.9/releases/latest";
    public static final String RELEASES_QUERY_URL =
            "https://api.github.com/repos/skyblock-wiki/wikitools-renders-1.8.9/releases/latest";

    public static Logger getLogger() {
        return LogManager.getLogger(MODID);
    }

}
