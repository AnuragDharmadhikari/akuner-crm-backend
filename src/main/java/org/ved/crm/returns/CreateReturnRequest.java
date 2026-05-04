package org.ved.crm.returns;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateReturnRequest(

        UUID chemistId,
        UUID stockistId,

        @NotNull
        LocalDate returnDate,

        @NotNull
        String reason,

        @NotNull
        @NotEmpty
        @Valid
        List<ReturnItemRequest> returnItems

) {


}
