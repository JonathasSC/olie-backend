package com.olie.api.usecase.wearitem;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class WearItemDateRules {

    private WearItemDateRules() {
    }

    static void requireInstallationNotBeforePurchase(LocalDate purchaseDate, LocalDate installationDate) {
        if (installationDate != null && installationDate.isBefore(purchaseDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "installationDate must not be before purchaseDate");
        }
    }
}
