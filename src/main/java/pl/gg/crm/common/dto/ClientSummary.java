package pl.gg.crm.common.dto;

import pl.gg.crm.entity.Client;

public record ClientSummary(
        Integer id,
        String displayName,
        String firstName,
        String lastName,
        String companyName,
        String email,
        String phoneNumber
) {

    public static ClientSummary from(Client client) {
        return new ClientSummary(
                client.getId(),
                displayName(client),
                client.getFirstName(),
                client.getLastName(),
                client.getCompanyName(),
                client.getEmail(),
                client.getPhoneNumber());
    }

    private static String displayName(Client client) {
        if (client.getCompanyName() != null && !client.getCompanyName().isBlank()) {
            return client.getCompanyName();
        }
        String first = client.getFirstName() == null ? "" : client.getFirstName();
        String last = client.getLastName() == null ? "" : client.getLastName();
        return (first + " " + last).trim();
    }
}
