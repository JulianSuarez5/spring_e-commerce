package ppi.e_commerce.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Paypal paypal = new Paypal();
    private Seed seed = new Seed();
    private Upload upload = new Upload();
    private Admin admin = new Admin();

    // Subgrupo: JWT
    public static class Jwt {
        private String secret;
        private long expiration;

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }

        public long getExpiration() { return expiration; }
        public void setExpiration(long expiration) { this.expiration = expiration; }
    }

    // Subgrupo: PayPal
    public static class Paypal {
        private String clientId;         // app.paypal.client-id
        private String clientSecret;     // app.paypal.client-secret
        private String mode;
        private String baseUrl;          // app.paypal.base-url
        private String businessEmail;    // app.paypal.business-email

        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }

        public String getClientSecret() { return clientSecret; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public String getBusinessEmail() { return businessEmail; }
        public void setBusinessEmail(String businessEmail) { this.businessEmail = businessEmail; }
    }

    // Subgrupo: Seed
    public static class Seed {
        private boolean createSampleData;
        private boolean createDefaultAdmin;

        public boolean isCreateSampleData() { return createSampleData; }
        public void setCreateSampleData(boolean createSampleData) { this.createSampleData = createSampleData; }

        public boolean isCreateDefaultAdmin() { return createDefaultAdmin; }
        public void setCreateDefaultAdmin(boolean createDefaultAdmin) { this.createDefaultAdmin = createDefaultAdmin; }
    }

    // Subgrupo: Upload
    public static class Upload {
        private String dir;

        public String getDir() { return dir; }
        public void setDir(String dir) { this.dir = dir; }
    }

    // Subgrupo: Admin
    public static class Admin {
        private Registration registration = new Registration();

        public Registration getRegistration() { return registration; }
        public void setRegistration(Registration registration) { this.registration = registration; }

        public static class Registration {
            private String token;

            public String getToken() { return token; }
            public void setToken(String token) { this.token = token; }
        }
    }

    // Getters y Setters principales
    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }

    public Paypal getPaypal() { return paypal; }
    public void setPaypal(Paypal paypal) { this.paypal = paypal; }

    public Seed getSeed() { return seed; }
    public void setSeed(Seed seed) { this.seed = seed; }

    public Upload getUpload() { return upload; }
    public void setUpload(Upload upload) { this.upload = upload; }

    public Admin getAdmin() { return admin; }
    public void setAdmin(Admin admin) { this.admin = admin; }
}