package com.reliaquest.api.service.client.error;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.controller.exceptions.EmployeeNotFoundException;
import com.reliaquest.api.controller.exceptions.TooManyRequestException;
import com.reliaquest.api.service.client.model.Response;
import java.io.IOException;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Slf4j
public class EmployeeErrorHandler implements RestClient.ResponseSpec.ErrorHandler {

    private final ObjectMapper objectMapper;

    /**
     * Only handle 429, and 404 to propagate the code
     * back to caller
     * anything else should produce http 500
     *
     * @return whether we should handler the error
     */
    public static Predicate<HttpStatusCode> statusPredicate() {
        return httpStatusCode -> {
            HttpStatus httpStatus = HttpStatus.valueOf(httpStatusCode.value());
            return switch (httpStatus) {
                case NOT_FOUND, TOO_MANY_REQUESTS -> true;
                default -> false;
            };
        };
    }

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {
        int httpCode = response.getStatusCode().value();
        log.info("Handling error for http status: {}", httpCode);

        if (httpCode == HttpStatus.TOO_MANY_REQUESTS.value()) {
            throw new TooManyRequestException();
        }

        if (httpCode == HttpStatus.NOT_FOUND.value()) {
            Response<?> returnedObject = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

            // no errors, cant rely on status text as is not code base is just string
            // also make sure we unmarshall the data correctly by making sure
            // status has text
            if (returnedObject.error() == null && returnedObject.status() != null) {
                throw new EmployeeNotFoundException();
            }
        }

        throw new IllegalStateException("Cannot handle http code: " + httpCode);
    }
}
