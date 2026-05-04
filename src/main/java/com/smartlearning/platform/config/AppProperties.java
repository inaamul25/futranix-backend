package com.smartlearning.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Storage storage = new Storage();
    private Paytm paytm = new Paytm();
    private String frontendUrl;
    private PasswordReset passwordReset = new PasswordReset();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long expirationMs;
    }

    @Getter
    @Setter
    public static class Storage {
        private String root;
        private String publicBaseUrl;
        private List<String> allowedVideoTypes;
        private long maxVideoSizeBytes;
    }

    @Getter
    @Setter
    public static class PasswordReset {
        private long expiryMinutes;
    }

    @Getter
    @Setter
    public static class Paytm {
        private boolean enabled;
        private String mid;
        private String merchantKey;
        private String website;
        private String callbackUrl;
        private String initiateUrl;
        private String statusUrl;
    }
}
