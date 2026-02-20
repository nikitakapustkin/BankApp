package org.nikitakapustkin.adapters.in.web.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.adapters.in.web.dto.mapper.TransactionWebMapper;
import org.nikitakapustkin.adapters.in.web.dto.response.CommonErrorResponses;
import org.nikitakapustkin.application.ports.in.queries.GetTransactionsQuery;
import org.nikitakapustkin.bank.contracts.dto.response.TransactionResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CommonErrorResponses
@RestController
@RequestMapping("/transactions")
@Tag(name = "Transaction Controller", description = "API for retrieving transaction details")
@RequiredArgsConstructor
public class TransactionController {

  private final GetTransactionsQuery getTransactionsQuery;
  private final TransactionWebMapper transactionMapper;

  @GetMapping
  @Transactional(readOnly = true)
  public ResponseEntity<List<TransactionResponseDto>> getTransactions(
      @RequestParam(required = false, name = "type") String type,
      @RequestParam(required = false, name = "accountId") UUID accountId) {

    var transactions = getTransactionsQuery.getTransactions(type, accountId);
    var response = transactions.stream().map(transactionMapper::toResponse).toList();

    return ResponseEntity.ok(response);
  }
}
