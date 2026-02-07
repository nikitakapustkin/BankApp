package org.nikitakapustkin.security.contracts;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class BankOpenApiContractTest {

    @Test
    void bank_openapi_contains_endpoints_used_by_security() {
        Path specPath = Path.of("..", "bank-bootstrap", "src", "main", "resources", "openapi", "bank-openapi.yaml")
                .normalize()
                .toAbsolutePath();

        assertThat(Files.exists(specPath))
                .as("bank-openapi.yaml must exist at %s", specPath)
                .isTrue();

        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(specPath.toString(), null, null);
        OpenAPI openAPI = result.getOpenAPI();

        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getPaths()).isNotNull();

        assertThat(openAPI.getPaths().get("/users")).isNotNull();
        assertThat(openAPI.getPaths().get("/users").getGet()).isNotNull();
        assertThat(openAPI.getPaths().get("/users").getPost()).isNull();

        assertThat(openAPI.getPaths().get("/users/{userId}")).isNotNull();
        assertThat(openAPI.getPaths().get("/users/{userId}").getGet()).isNotNull();
        assertThat(openAPI.getPaths().get("/users/{userId}").getDelete()).isNotNull();

        assertThat(openAPI.getPaths().get("/users/friends")).isNotNull();
        assertThat(openAPI.getPaths().get("/users/friends").getPost()).isNotNull();
        assertThat(openAPI.getPaths().get("/users/friends").getDelete()).isNotNull();

        assertThat(openAPI.getPaths().get("/users/{userId}/accounts")).isNotNull();
        assertThat(openAPI.getPaths().get("/users/{userId}/accounts").getGet()).isNotNull();

        assertThat(openAPI.getPaths().get("/accounts")).isNotNull();
        assertThat(openAPI.getPaths().get("/accounts").getGet()).isNotNull();
        assertThat(openAPI.getPaths().get("/accounts").getPost()).isNotNull();
        assertThat(openAPI.getPaths().get("/accounts").getPost().getResponses()).containsKey("201");

        assertThat(openAPI.getPaths().get("/accounts/{accountId}")).isNotNull();
        assertThat(openAPI.getPaths().get("/accounts/{accountId}").getGet()).isNotNull();

        assertThat(openAPI.getPaths().get("/accounts/{accountId}/deposit")).isNotNull();
        assertThat(openAPI.getPaths().get("/accounts/{accountId}/deposit").getPost()).isNotNull();

        assertThat(openAPI.getPaths().get("/accounts/{accountId}/withdraw")).isNotNull();
        assertThat(openAPI.getPaths().get("/accounts/{accountId}/withdraw").getPost()).isNotNull();

        assertThat(openAPI.getPaths().get("/accounts/{fromAccountId}/transfer")).isNotNull();
        assertThat(openAPI.getPaths().get("/accounts/{fromAccountId}/transfer").getPost()).isNotNull();

        assertThat(openAPI.getPaths().get("/transactions")).isNotNull();
        assertThat(openAPI.getPaths().get("/transactions").getGet()).isNotNull();
    }
}
