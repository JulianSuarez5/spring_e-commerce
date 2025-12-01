package ppi.e_commerce.Dto;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private String username;
    private String role;
    private Integer userId;
    private String email;
    private String name;

    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, String username, String role, 
                       Integer userId, String email, String name) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    // Getters and Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
