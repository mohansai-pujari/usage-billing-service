package com.billing.web;

import com.billing.storage.UsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
class BulkUploadProdIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsageRepository usageRepository;

    @BeforeEach
    void clearRepository() {
        usageRepository.clear();
    }

    @Test
    void shouldHideBulkUploadInProdProfile() throws Exception {
        mockMvc.perform(post("/usage/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }
}
