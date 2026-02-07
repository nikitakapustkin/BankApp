package org.nikitakapustkin.adapters.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.nikitakapustkin.bank.contracts.errors.ApiError;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiError.class),
                        examples = @ExampleObject(
                                name = "InvalidArgument",
                                value = """
                                        {
                                          "timestamp": "2025-12-21T12:34:56.789Z",
                                          "status": 400,
                                          "error": "INVALID_ARGUMENT",
                                          "message": "Amount must be positive",
                                          "path": "/accounts/123e4567-e89b-12d3-a456-426614174000/deposit"
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Not Found",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiError.class),
                        examples = @ExampleObject(
                                name = "NotFound",
                                value = """
                                        {
                                          "timestamp": "2025-12-21T12:34:56.789Z",
                                          "status": 404,
                                          "error": "NOT_FOUND",
                                          "message": "Account not found: 123e4567-e89b-12d3-a456-426614174000",
                                          "path": "/accounts/123e4567-e89b-12d3-a456-426614174000/balance"
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "409",
                description = "Conflict (business conflict or concurrent update)",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiError.class),
                        examples = {
                                @ExampleObject(
                                        name = "NotEnoughMoney",
                                        value = """
                                                {
                                                  "timestamp": "2025-12-21T12:34:56.789Z",
                                                  "status": 409,
                                                  "error": "NOT_ENOUGH_MONEY",
                                                  "message": "Not enough money on this account to proceed",
                                                  "path": "/accounts/123e4567-e89b-12d3-a456-426614174000/withdraw"
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "ConcurrentUpdate",
                                        value = """
                                                {
                                                  "timestamp": "2025-12-21T12:34:56.789Z",
                                                  "status": 409,
                                                  "error": "CONCURRENT_UPDATE",
                                                  "message": "Concurrent update detected. Please retry the request.",
                                                  "path": "/accounts/123e4567-e89b-12d3-a456-426614174000/transfer"
                                                }
                                                """
                                )
                        }
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiError.class),
                        examples = @ExampleObject(
                                name = "InternalError",
                                value = """
                                        {
                                          "timestamp": "2025-12-21T12:34:56.789Z",
                                          "status": 500,
                                          "error": "INTERNAL_ERROR",
                                          "message": "Internal server error",
                                          "path": "/users"
                                        }
                                        """
                        )
                )
        )
})
public @interface CommonErrorResponses {}
