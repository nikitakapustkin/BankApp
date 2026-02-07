package org.nikitakapustkin.adapters.in.web.dto.mapper;

import org.nikitakapustkin.bank.contracts.dto.response.TransactionResponseDto;
import org.nikitakapustkin.bank.contracts.enums.TransactionType;
import org.nikitakapustkin.domain.models.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionWebMapper {

    public TransactionResponseDto toResponse(Transaction t) {
        return new TransactionResponseDto(
                t.getId(),
                t.getAccountId(),
                toContractType(t.getTransactionType()),
                t.getAmount(),
                t.getCreatedAt()
        );
    }

    private static TransactionType toContractType(org.nikitakapustkin.domain.enums.TransactionType type) {
        if (type == null) {
            return null;
        }
        return TransactionType.valueOf(type.name());
    }
}
