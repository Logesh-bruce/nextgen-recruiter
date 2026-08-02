package com.hireflow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Strongly-typed binding for all {@code hireflow.*} properties in application.yml.
 * Inject this bean instead of using {@code @Value} for structured config access.
 */
@Configuration
@ConfigurationProperties(prefix = "hireflow")
@Getter
@Setter
public class HireFlowProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Ai ai = new Ai();
    private Storage storage = new Storage();
    private Notifications notifications = new Notifications();
    private RateLimit rateLimit = new RateLimit();

    @Getter @Setter
    public static class Jwt {
        private String privateKey;
        private String publicKey;
        private long accessTokenExpirySeconds = 900;
        private int refreshTokenExpiryDays = 7;
    }

    @Getter @Setter
    public static class Cors {
        private String allowedOrigins;
    }

    @Getter @Setter
    public static class Ai {
        private String provider = "openai";
        private OpenAi openai = new OpenAi();
        private Gemini gemini = new Gemini();

        @Getter @Setter
        public static class OpenAi {
            private String apiKey;
            private String model = "gpt-4o-mini";
            private int maxTokens = 1500;
        }

        @Getter @Setter
        public static class Gemini {
            private String apiKey;
            private String model = "gemini-1.5-flash";
        }
    }

    @Getter @Setter
    public static class Storage {
        private String provider = "local";
        private S3 s3 = new S3();
        private Local local = new Local();

        @Getter @Setter
        public static class S3 {
            private String bucket;
            private String region;
        }

        @Getter @Setter
        public static class Local {
            private String uploadDir = "./uploads";
        }
    }

    @Getter @Setter
    public static class Notifications {
        private SendGrid sendgrid = new SendGrid();
        private Twilio twilio = new Twilio();

        @Getter @Setter
        public static class SendGrid {
            private String apiKey;
            private String fromEmail;
        }

        @Getter @Setter
        public static class Twilio {
            private String accountSid;
            private String authToken;
            private String fromNumber;
        }
    }

    @Getter @Setter
    public static class RateLimit {
        private int authRequestsPerMinute = 5;
        private int aiRequestsPerMinute = 20;
    }
}
