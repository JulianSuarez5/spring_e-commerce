package ppi.e_commerce.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    
    private Jwt jwt = new Jwt();
    private Paypal paypal = new Paypal();
    
    public static class Jwt {
        private String secret = "mySecretKey123456789012345678901234567890";
        private long expiration = 86400000; // 24 horas
        
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        
        public long getExpiration() { return expiration; }
        public void setExpiration(long expiration) { this.expiration = expiration; }
    }
    
    public static class Paypal {
        private String clientId = "AWQzV1dqMn_FBTgi0omvUgPEXy-QSIaX9jl8x5QaCahO4ppt5AQAL7gwO4KMyhk42XXFhrqPT0-FRCG1";
        private String clientSecret = "EKa-ZK15yhqJK97uKAw4-6flzEHvYkqPZvaghfARonrK6pO7dISqXHLEjjVxwIRcBfSTo5sWWzL6Wc4H";
        private String mode = "sandbox";
        private String baseUrl = "https://api-m.sandbox.paypal.com";
        private String businessEmail = "sb-yhrmo44286547@business.example.com";
        
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
    
    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }
    
    public Paypal getPaypal() { return paypal; }
    public void setPaypal(Paypal paypal) { this.paypal = paypal; }
}