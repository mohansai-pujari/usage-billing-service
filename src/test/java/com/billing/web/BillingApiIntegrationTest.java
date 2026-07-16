package com.billing.web;

import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import com.billing.storage.UsageRepository;
import com.billing.support.TestTimestamps;
import com.billing.web.dto.request.BulkUsageRequest;
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
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
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
        persistUsage(usageRequest("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "50", TestTimestamps.JAN_10_2026_10_00));

        mockMvc.perform(get("/invoices/user-1")
                        .param("start", String.valueOf(TestTimestamps.PERIOD_START))
                        .param("end", String.valueOf(TestTimestamps.PERIOD_END)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.total").value("USD 1.00"));
    }

    @Test
    void shouldReturnInvoiceInRequestedCurrency() throws Exception {
        persistUsage(usageRequest("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "50", TestTimestamps.JAN_10_2026_10_00));

        mockMvc.perform(get("/invoices/user-1")
                        .param("start", String.valueOf(TestTimestamps.PERIOD_START))
                        .param("end", String.valueOf(TestTimestamps.PERIOD_END))
                        .param("currency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.total").value("EUR 0.92"));
    }

    @Test
    void shouldListUsagesWithinPeriodWithoutFilters() throws Exception {
        persistUsage(usageRequest("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "10", TestTimestamps.JAN_10_2026_10_00));
        persistUsage(usageRequest("user-2", "cpu-1", ServiceType.COMPUTE, UnitType.COMPUTE_HOUR, "15", TestTimestamps.JAN_12_2026_10_00));

        mockMvc.perform(get("/usages")
                        .param("start", String.valueOf(TestTimestamps.PERIOD_START))
                        .param("end", String.valueOf(TestTimestamps.PERIOD_END)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldListUsagesFilteredByUserOnly() throws Exception {
        persistUsage(usageRequest("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "10", TestTimestamps.JAN_10_2026_10_00));
        persistUsage(usageRequest("user-2", "cpu-1", ServiceType.COMPUTE, UnitType.COMPUTE_HOUR, "15", TestTimestamps.JAN_12_2026_10_00));

        mockMvc.perform(get("/usages")
                        .param("userId", "user-1")
                        .param("start", String.valueOf(TestTimestamps.PERIOD_START))
                        .param("end", String.valueOf(TestTimestamps.PERIOD_END)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].userId").value("user-1"));
    }

    @Test
    void shouldListUsagesFilteredByServiceTypeOnly() throws Exception {
        persistUsage(usageRequest("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "10", TestTimestamps.JAN_10_2026_10_00));
        persistUsage(usageRequest("user-2", "cpu-1", ServiceType.COMPUTE, UnitType.COMPUTE_HOUR, "15", TestTimestamps.JAN_12_2026_10_00));

        mockMvc.perform(get("/usages")
                        .param("serviceType", "STORAGE")
                        .param("start", String.valueOf(TestTimestamps.PERIOD_START))
                        .param("end", String.valueOf(TestTimestamps.PERIOD_END)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].serviceType").value("STORAGE"));
    }

    @Test
    void shouldListUsagesFilteredByUserAndServiceForPeriod() throws Exception {
        persistUsage(usageRequest("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "10", TestTimestamps.JAN_10_2026_10_00));
        persistUsage(usageRequest("user-2", "cpu-1", ServiceType.COMPUTE, UnitType.COMPUTE_HOUR, "15", TestTimestamps.JAN_12_2026_10_00));

        mockMvc.perform(get("/usages")
                        .param("userId", "user-1")
                        .param("serviceType", "STORAGE")
                        .param("start", String.valueOf(TestTimestamps.PERIOD_START))
                        .param("end", String.valueOf(TestTimestamps.PERIOD_END)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].userId").value("user-1"))
                .andExpect(jsonPath("$.content[0].serviceType").value("STORAGE"));
    }

    @Test
    void shouldPaginateUsageResults() throws Exception {
        persistUsage(usageRequest("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "10", TestTimestamps.JAN_10_2026_10_00));
        persistUsage(usageRequest("user-2", "cpu-1", ServiceType.COMPUTE, UnitType.COMPUTE_HOUR, "15", TestTimestamps.JAN_12_2026_10_00));
        persistUsage(usageRequest("user-3", "api-1", ServiceType.API, UnitType.API_CALL, "1000", TestTimestamps.JAN_15_2026_10_00));

        mockMvc.perform(get("/usages")
                        .param("start", String.valueOf(TestTimestamps.PERIOD_START))
                        .param("end", String.valueOf(TestTimestamps.PERIOD_END))
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldDeduplicateUsageEventsByEventId() throws Exception {
        mockMvc.perform(post("/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usageRequest(
                                "user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "10",
                                TestTimestamps.JAN_10_2026_10_00, "evt-dup-1"))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usageRequest(
                                "user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "99",
                                TestTimestamps.JAN_10_2026_10_00, "evt-dup-1"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/usages")
                        .param("start", String.valueOf(TestTimestamps.PERIOD_START))
                        .param("end", String.valueOf(TestTimestamps.PERIOD_END)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].quantity").value("10"));
    }

    @Test
    void shouldExposeActuatorHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldReturnInvoiceForAllUsersWithoutUserId() throws Exception {
        persistUsage(usageRequest("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "100", TestTimestamps.JAN_10_2026_10_00));
        persistUsage(usageRequest("user-2", "disk-2", ServiceType.STORAGE, UnitType.GB_HOUR, "50", TestTimestamps.JAN_25_2026_10_00));

        mockMvc.perform(get("/invoices")
                        .param("start", String.valueOf(TestTimestamps.PERIOD_START))
                        .param("end", String.valueOf(TestTimestamps.PERIOD_END)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(nullValue()))
                .andExpect(jsonPath("$.total").value("USD 3.00"))
                .andExpect(jsonPath("$.lineItems", hasSize(2)));
    }

    @Test
    void shouldReturnInvoiceFilteredByServiceTypeForUser() throws Exception {
        persistUsage(usageRequest("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "50", TestTimestamps.JAN_10_2026_10_00));
        persistUsage(usageRequest("user-1", "cpu-1", ServiceType.COMPUTE, UnitType.COMPUTE_HOUR, "150", TestTimestamps.JAN_15_2026_10_00));

        mockMvc.perform(get("/invoices/user-1")
                        .param("start", String.valueOf(TestTimestamps.PERIOD_START))
                        .param("end", String.valueOf(TestTimestamps.PERIOD_END))
                        .param("serviceType", "STORAGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.total").value("USD 1.00"))
                .andExpect(jsonPath("$.serviceSubtotals", hasSize(1)))
                .andExpect(jsonPath("$.serviceSubtotals[0].serviceType").value("STORAGE"));
    }

    @Test
    void shouldReturnInvoiceFilteredByServiceTypeAcrossAllUsers() throws Exception {
        persistUsage(usageRequest("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "100", TestTimestamps.JAN_10_2026_10_00));
        persistUsage(usageRequest("user-2", "cpu-1", ServiceType.COMPUTE, UnitType.COMPUTE_HOUR, "150", TestTimestamps.JAN_12_2026_10_00));

        mockMvc.perform(get("/invoices")
                        .param("start", String.valueOf(TestTimestamps.PERIOD_START))
                        .param("end", String.valueOf(TestTimestamps.PERIOD_END))
                        .param("serviceType", "STORAGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value("USD 2.00"))
                .andExpect(jsonPath("$.serviceSubtotals", hasSize(1)))
                .andExpect(jsonPath("$.serviceSubtotals[0].serviceType").value("STORAGE"));
    }

    @Test
    void shouldReturnGenericMessageWhenInvoiceNotFound() throws Exception {
        mockMvc.perform(get("/invoices/unknown-user")
                        .param("start", String.valueOf(TestTimestamps.PERIOD_START))
                        .param("end", String.valueOf(TestTimestamps.PERIOD_END)))
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
        mockMvc.perform(get("/usages").param("start", String.valueOf(TestTimestamps.PERIOD_START)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ApiErrorMessages.MISSING_PARAMETER));
    }

    @Test
    void shouldBulkLoadUsageFromClasspathFixtureWhenPayloadEmpty() throws Exception {
        mockMvc.perform(post("/usage/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accepted").value(17))
                .andExpect(jsonPath("$.skippedDuplicates").value(0))
                .andExpect(jsonPath("$.totalProcessed").value(17))
                .andExpect(jsonPath("$.source").value("test-data/usage-events.json"));

        mockMvc.perform(get("/invoices/user-1")
                        .param("start", String.valueOf(TestTimestamps.PERIOD_START))
                        .param("end", String.valueOf(TestTimestamps.PERIOD_END)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.serviceSubtotals", hasSize(3)))
                .andExpect(jsonPath("$.total").value("USD 672.80"));
    }

    @Test
    void shouldBulkLoadUsageFromExplicitPayload() throws Exception {
        BulkUsageRequest request = new BulkUsageRequest(List.of(
                usageRequest("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "50", TestTimestamps.JAN_10_2026_10_00)));

        mockMvc.perform(post("/usage/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accepted").value(1))
                .andExpect(jsonPath("$.source").value("request-payload"));
    }

    @Test
    void shouldSkipDuplicateEventIdsOnBulkReplay() throws Exception {
        mockMvc.perform(post("/usage/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accepted").value(17));

        mockMvc.perform(post("/usage/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accepted").value(0))
                .andExpect(jsonPath("$.skippedDuplicates").value(17));
    }

    @Test
    void shouldReturnMessageWhenServiceTypeAndUnitDoNotMatchOnUsageRecord() throws Exception {
        mockMvc.perform(post("/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "user-1",
                                  "resourceId": "cpu-1",
                                  "serviceType": "COMPUTE",
                                  "unit": "GB_HOUR",
                                  "quantity": 10,
                                  "timestamp": %d
                                }
                                """.formatted(TestTimestamps.JAN_10_2026_10_00)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ApiErrorMessages.SERVICE_TYPE_UNIT_MISMATCH));
    }

    @Test
    void shouldReturnGenericMessageForUnknownServiceOnUsageRecord() throws Exception {
        mockMvc.perform(post("/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "user-1",
                                  "resourceId": "disk-1",
                                  "serviceType": "unknown",
                                  "unit": "GB_HOUR",
                                  "quantity": 10,
                                  "timestamp": %d
                                }
                                """.formatted(TestTimestamps.JAN_10_2026_10_00)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ApiErrorMessages.MALFORMED_BODY));
    }

    @Test
    void shouldRejectInvalidServiceTypeOnInvoiceQuery() throws Exception {
        mockMvc.perform(get("/invoices/user-1")
                        .param("start", String.valueOf(TestTimestamps.PERIOD_START))
                        .param("end", String.valueOf(TestTimestamps.PERIOD_END))
                        .param("serviceType", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ApiErrorMessages.INVALID_PARAMETER));
    }

    @Test
    void shouldRejectInvalidServiceTypeOnUsageQuery() throws Exception {
        mockMvc.perform(get("/usages")
                        .param("start", String.valueOf(TestTimestamps.PERIOD_START))
                        .param("end", String.valueOf(TestTimestamps.PERIOD_END))
                        .param("serviceType", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ApiErrorMessages.INVALID_PARAMETER));
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
            ServiceType serviceType,
            UnitType unit,
            String quantity,
            long timestamp) {
        return usageRequest(userId, resourceId, serviceType, unit, quantity, timestamp, null);
    }

    private static UsageRequest usageRequest(
            String userId,
            String resourceId,
            ServiceType serviceType,
            UnitType unit,
            String quantity,
            long timestamp,
            String eventId) {
        return new UsageRequest(
                userId,
                resourceId,
                serviceType,
                unit,
                new BigDecimal(quantity),
                timestamp,
                eventId);
    }
}
