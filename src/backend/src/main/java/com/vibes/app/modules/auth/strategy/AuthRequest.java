package com.vibes.app.modules.auth.strategy;

public class AuthRequest {
    private AuthRequestType type;
    private String email;
    private String password;
    private String provider;
    private String code;

    public AuthRequest() {}

    public AuthRequest(AuthRequestType type, String email, String password, String provider, String code) {
        this.type = type;
        this.email = email;
        this.password = password;
        this.provider = provider;
        this.code = code;
    }

    public static AuthRequest credential(String email, String password) {
        return new AuthRequest(AuthRequestType.CREDENTIAL, email, password, null, null);
    }

    public static AuthRequest oauth(String provider, String code) {
        return new AuthRequest(AuthRequestType.OAUTH, null, null, provider, code);
    }

    public AuthRequestType getType() { return type; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getProvider() { return provider; }
    public String getCode() { return code; }
}
