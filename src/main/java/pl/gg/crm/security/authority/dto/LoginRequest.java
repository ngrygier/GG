package pl.gg.crm.security.authority.dto;

public record LoginRequest(
        String username,
        String password
) {}