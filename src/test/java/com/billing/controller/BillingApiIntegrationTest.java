package com.billing.controller;

import com.billing.dto.request.UsageRequest;
import com.billing.enums.ServiceType;
import com.billing.enums.UnitType;
import com.billing.repository.usage.InMemoryUsageRepository;
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
    private InMemoryUsageRepository usageRepository;

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
        UsageRequest request = createUsageRequest("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "50", "2026-01-10T10:00:00Z");

        mockMvc.perform(post("/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(containsString("recorded")));

        mockMvc.perform(get("/invoices/user-1")
                        .param("start", String.valueOf(Instant.parse("2026-01-01T00:00:00Z").toEpochMilli()))
                        .param("end", String.valueOf(Instant.parse("2026-02-01T00:00:00Z").toEpochMilli())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.total").value("USD 1.00"));
    }

    @Test
    void shouldListUsagesWithinPeriodWithoutFilters() throws Exception {
        persistUsage(createUsageRequest("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "10", "2026-01-10T10:00:00Z"));
        persistUsage(createUsageRequest("user-2", "cpu-1", ServiceType.COMPUTE, UnitType.COMPUTE_HOUR, "15", "2026-01-12T10:00:00Z"));

        mockMvc.perform(get("/usages")
                        .param("start", String.valueOf(Instant.parse("2026-01-01T00:00:00Z").toEpochMilli()))
                        .param("end", String.valueOf(Instant.parse("2026-02-01T00:00:00Z").toEpochMilli())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldListUsagesFilteredByUserOnly() throws Exception {
        persistUsage(createUsageRequest("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "10", "2026-01-10T10:00:00Z"));
        persistUsage(createUsageRequest("user-2", "cpu-1", ServiceType.COMPUTE, UnitType.COMPUTE_HOUR, "15", "2026-01-12T10:00:00Z"));

        mockMvc.perform(get("/usages")
                        .param("userId", "user-1")
                        .param("start", String.valueOf(Instant.parse("2026-01-01T00:00:00Z").toEpochMilli()))
                        .param("end", String.valueOf(Instant.parse("2026-02-01T00:00:00Z").toEpochMilli())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value("user-1"));
    }

    @Test
    void shouldListUsagesFilteredByServiceTypeOnly() throws Exception {
        persistUsage(createUsageRequest("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "10", "2026-01-10T10:00:00Z"));
        persistUsage(createUsageRequest("user-2", "cpu-1", ServiceType.COMPUTE, UnitType.COMPUTE_HOUR, "15", "2026-01-12T10:00:00Z"));

        mockMvc.perform(get("/usages")
                        .param("serviceType", "STORAGE")
                        .param("start", String.valueOf(Instant.parse("2026-01-01T00:00:00Z").toEpochMilli()))
                        .param("end", String.valueOf(Instant.parse("2026-02-01T00:00:00Z").toEpochMilli())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].serviceType").value("STORAGE"));
    }

    @Test
    void shouldListUsagesFilteredByUserAndServiceForPeriod() throws Exception {
        persistUsage(createUsageRequest("user-1", "disk-1", ServiceType.STORAGE, UnitType.GB_HOUR, "10", "2026-01-10T10:00:00Z"));
        persistUsage(createUsageRequest("user-2", "cpu-1", ServiceType.COMPUTE, UnitType.COMPUTE_HOUR, "15", "2026-01-12T10:00:00Z"));

        mockMvc.perform(get("/usages")
                        .param("userId", "user-1")
                        .param("serviceType", "STORAGE")
                        .param("start", String.valueOf(Instant.parse("2026-01-01T00:00:00Z").toEpochMilli()))
                        .param("end", String.valueOf(Instant.parse("2026-02-01T00:00:00Z").toEpochMilli())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value("user-1"))
                .andExpect(jsonPath("$[0].serviceType").value("STORAGE"));
    }

    private void persistUsage(UsageRequest request) throws Exception {
        mockMvc.perform(post("/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private UsageRequest createUsageRequest(String userId,
                                             String resourceId,
                                             ServiceType serviceType,
                                             UnitType unitType,
                                             String quantity,
                                             String timestamp) {
        UsageRequest request = new UsageRequest();
        request.setUserId(userId);
        request.setResourceId(resourceId);
        request.setServiceType(serviceType);
        request.setUnitType(unitType);
        request.setQuantity(new BigDecimal(quantity));
        request.setTimestamp(Instant.parse(timestamp));
        return request;
    }
}
