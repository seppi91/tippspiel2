package com.github.zemke.tippspiel2.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// Is written in Java to retain IDEA support for binding application.properties to this class.

@Component
@ConfigurationProperties("tippspiel2.football-data")
public class FootballDataProperties {

    /**
     * The auth token to authenticate toward the external API.
     */
    private String apiToken;

    /**
     * The name of the header to put the {@link com.github.zemke.tippspiel2.core.properties.FootballDataProperties#apiToken} in.
     */
    private String apiTokenHeader = "X-Auth-Token";

    /**
     * Header name whose value indicates the seconds until the request count is reset.
     */
    private String secondsTillResetHeader = "X-RequestCounter-Reset";

    /**
     * Header name whose value indicates the requests left until the counter resets.
     */
    private String requestsTillResetHeader = "X-Requests-Available";

    /**
     * The endpoint to run requests against i.e. {@code https://www.football-data.org/v1/}.
     */
    private String endpoint;

    /**
     * Interval in milliseconds to poll the endpoint with in case the headers {@link #secondsTillResetHeader} or {@link #requestsTillResetHeader} are missing.
     */
    private Integer fallbackPollingInterval = 2500;

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getApiTokenHeader() {
        return apiTokenHeader;
    }

    public void setApiTokenHeader(String apiTokenHeader) {
        this.apiTokenHeader = apiTokenHeader;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getSecondsTillResetHeader() {
        return secondsTillResetHeader;
    }

    public void setSecondsTillResetHeader(String secondsTillResetHeader) {
        this.secondsTillResetHeader = secondsTillResetHeader;
    }

    public String getRequestsTillResetHeader() {
        return requestsTillResetHeader;
    }

    public void setRequestsTillResetHeader(String requestsTillResetHeader) {
        this.requestsTillResetHeader = requestsTillResetHeader;
    }

    public Integer getFallbackPollingInterval() {
        return fallbackPollingInterval;
    }

    public void setFallbackPollingInterval(Integer fallbackPollingInterval) {
        this.fallbackPollingInterval = fallbackPollingInterval;
    }
}
