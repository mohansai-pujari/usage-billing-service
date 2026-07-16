package com.billing.web;

import com.billing.storage.UsageRepository;
import com.billing.web.dto.request.UsageRequest;
import com.billing.web.handler.ApiErrorMessages;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BillingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsageRepository usageRepository;

    @BeforeEach
    void clearRepository() {
        usageRepository.clear();
    }

    @Test
    void shouldReturnHealthStatus() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldRecordUsageAndReturnInvoice() throws Exception {
        persistUsage(usageRequest("user-1", "disk-1", "storage", "GB_HOUR", "50", "2026-01-10T10:00:00Z"));

        mockMvc.perform(get("/invoices/user-1")
                        .param("start", epoch("2026-01-01T00:00:00Z"))
                        .param("end", epoch("2026-02-01T00:00:00Z")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.total").value("USD 1.00"));
    }

    @Test
    void shouldReturnInvoiceInRequestedCurrency() throws Exception {
        persistUsage(usageRequest("user-1", "disk-1", "storage", "GB_HOUR", "50", "2026-01-10T10:00:00Z"));

        mockMvc.perform(get("/invoices/user-1")
                        .param("start", epoch("2026-01-01T00:00:00Z"))
                        .param("end", epoch("2026-02-01T00:00:00Z"))
                        .param("currency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.total").value("EUR 0.92"));
    }

    @Test
    void shouldListUsagesWithinPeriodWithoutFilters() throws Exception {
        persistUsage(usageRequest("user-1", "disk-1", "storage", "GB_HOUR", "10", "2026-01-10T10:00:00Z"));
        persistUsage(usageRequest("user-2", "cpu-1", "compute", "COMPUTE_HOUR", "15", "2026-01-12T10:00:00Z"));

        mockMvc.perform(get("/usages")
                        .param("start", epoch("2026-01-01T00:00:00Z"))
                        .param("end", epoch("2026-02-01T00:00:00Z")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldListUsagesFilteredByUserOnly() throws Exception {
        persistUsage(usageRequest("user-1", "disk-1", "storage", "GB_HOUR", "10", "2026-01-10T10:00:00Z"));
        persistUsage(usageRequest("user-2", "cpu-1", "compute", "COMPUTE_HOUR", "15", "2026-01-12T10:00:00Z"));

        mockMvc.perform(get("/usages")
                        .param("userId", "user-1")
                        .param("start", epoch("2026-01-01T00:00:00Z"))
                        .param("end", epoch("2026-02-01T00:00:00Z")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value("user-1"));
    }

    @Test
    void shouldListUsagesFilteredByServiceTypeOnly() throws Exception {
        persistUsage(usageRequest("user-1", "disk-1", "storage", "GB_HOUR", "10", "2026-01-10T10:00:00Z"));
        persistUsage(usageRequest("user-2", "cpu-1", "compute", "COMPUTE_HOUR", "15", "2026-01-12T10:00:00Z"));

        mockMvc.perform(get("/usages")
                        .param("serviceType", "storage")
                        .param("start", epoch("2026-01-01T00:00:00Z"))
                        .param("end", epoch("2026-02-01T00:00:00Z")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].serviceType").value("storage"));
    }

    @Test
    void shouldListUsagesFilteredByUserAndServiceForPeriod() throws Exception {
        persistUsage(usageRequest("user-1", "disk-1", "storage", "GB_HOUR", "10", "2026-01-10T10:00:00Z"));
        persistUsage(usageRequest("user-2", "cpu-1", "compute", "COMPUTE_HOUR", "15", "2026-01-12T10:00:00Z"));

        mockMvc.perform(get("/usages")
                        .param("userId", "user-1")
                        .param("serviceType", "storage")
                        .param("start", epoch("2026-01-01T00:00:00Z"))
                        .param("end", epoch("2026-02-01T00:00:00Z")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value("user-1"))
                .andExpect(jsonPath("$[0].serviceType").value("storage"));
    }

    @Test
    void shouldReturnGenericMessageWhenInvoiceNotFound() throws Exception {
        mockMvc.perform(get("/invoices/unknown-user")
                        .param("start", epoch("2026-01-01T00:00:00Z"))
                        .param("end", epoch("2026-02-01T00:00:00Z")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ApiErrorMessages.NOT_FOUND))
                .andExpect(jsonPath("$.message").value(not(containsString("unknown-user"))));
    }

    @Test
    void shouldReturnGenericMessageForInvalidBillingPeriod() throws Exception {
        mockMvc.perform(get("/invoices/user-1").param("start", "100").param("end", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ApiErrorMessages.INVALID_REQUEST))
                .andExpect(jsonPath("$.message").value(not(containsString("start"))));
    }

    @Test
    void shouldReturnGenericMessageForMissingRequiredParameter() throws Exception {
        mockMvc.perform(get("/usages").param("start", epoch("2026-01-01T00:00:00Z")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ApiErrorMessages.MISSING_PARAMETER));
    }

    @Test
    void shouldReturnGenericMessageForUnknownServiceOnUsageRecord() throws Exception {
        mockMvc.perform(post("/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usageRequest(
                                "user-1", "disk-1", "unknown", "GB_HOUR", "10", "2026-01-10T10:00:00Z"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ApiErrorMessages.NOT_FOUND));
    }

    private void persistUsage(UsageRequest request) throws Exception {
        mockMvc.perform(post("/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(containsString("recorded")));
    }

    private static UsageRequest usageRequest(
            String userId,
            String resourceId,
            String serviceType,
            String unit,
            String quantity,
            String timestamp) {
        return new UsageRequest(
                userId,
                resourceId,
                serviceType,
                unit,
                new BigDecimal(quantity),
                Instant.parse(timestamp));
    }

    private static String epoch(String instant) {
        return String.valueOf(Instant.parse(instant).toEpochMilli());
    }
}
