package pl.gg.crm.security.authority.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {}
