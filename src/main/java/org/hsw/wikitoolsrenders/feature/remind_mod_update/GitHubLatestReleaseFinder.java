package org.hsw.wikitoolsrenders.feature.remind_mod_update;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class GitHubLatestReleaseFinder implements FindModVersion {

    private final String releasesQueryUrl;

    public GitHubLatestReleaseFinder(String releasesQueryUrl) {
        this.releasesQueryUrl = releasesQueryUrl;
    }

    public FindModVersionResult findLatestVersion() {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(releasesQueryUrl);
            request.addHeader("content-type", "application/json");

            HttpResponse result = httpClient.execute(request);
            String json = EntityUtils.toString(result.getEntity(), "UTF-8");

            Release release = new Gson().fromJson(json, Release.class);
            String latestVersionName = release.tag_name;

            if (release.tag_name == null) {
                return FindModVersionResult.failure("Latest Release Fetch/Parse Failure (" + result.getStatusLine() + ")");
            }

            return FindModVersionResult.success(latestVersionName);
        } catch (IOException ignored) {
            return FindModVersionResult.failure("Latest Release Fetch Failure");
        }
    }

    private static class Release {
        public String tag_name;
    }

}
