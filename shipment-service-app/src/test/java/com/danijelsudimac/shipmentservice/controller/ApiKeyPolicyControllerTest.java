package com.danijelsudimac.shipmentservice.controller;

import com.danijelsudimac.shipmentservice.model.entity.ApiKeyPolicy;
import com.danijelsudimac.shipmentservice.service.ApiKeyConfigurationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiKeyPolicyController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApiKeyPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiKeyConfigurationService apiKeyConfigurationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return api key policy when client id exists")
    void shouldGetApiKeyPolicyByClientId() throws Exception {
        ApiKeyPolicy policy = new ApiKeyPolicy();
        policy.setId(1L);
        policy.setClientId(100L);

        when(apiKeyConfigurationService.getApiKeyPolicyByClientId(100L))
                .thenReturn(Optional.of(policy));

        mockMvc.perform(get("/api/apikey-policy/100"))
                .andExpect(status().isOk());

        verify(apiKeyConfigurationService)
                .getApiKeyPolicyByClientId(100L);
    }

    @Test
    @DisplayName("Should return 404 when api key policy is not found")
    void shouldReturnNotFoundWhenPolicyDoesNotExist() throws Exception {
        when(apiKeyConfigurationService.getApiKeyPolicyByClientId(100L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/apikey-policy/100"))
                .andExpect(status().isNotFound());

        verify(apiKeyConfigurationService)
                .getApiKeyPolicyByClientId(100L);
    }

    @Test
    @DisplayName("Should create api key policy")
    void shouldCreateApiKeyPolicy() throws Exception {
        ApiKeyPolicy request = new ApiKeyPolicy();
        request.setClientId(100L);

        ApiKeyPolicy created = new ApiKeyPolicy();
        created.setId(1L);
        created.setClientId(100L);

        when(apiKeyConfigurationService.createApiKeyPolicy(any(ApiKeyPolicy.class)))
                .thenReturn(created);

        mockMvc.perform(post("/api/apikey-policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(apiKeyConfigurationService)
                .createApiKeyPolicy(any(ApiKeyPolicy.class));
    }

    @Test
    @DisplayName("Should update api key policy")
    void shouldUpdateApiKeyPolicy() throws Exception {
        ApiKeyPolicy request = new ApiKeyPolicy();
        request.setClientId(100L);

        ApiKeyPolicy updated = new ApiKeyPolicy();
        updated.setId(1L);
        updated.setClientId(100L);

        when(apiKeyConfigurationService.updateApiKeyPolicy(eq(1L), any(ApiKeyPolicy.class)))
                .thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/apikey-policy/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(apiKeyConfigurationService)
                .updateApiKeyPolicy(eq(1L), any(ApiKeyPolicy.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non existing policy")
    void shouldReturnNotFoundWhenUpdatingNonExistingPolicy() throws Exception {
        ApiKeyPolicy request = new ApiKeyPolicy();

        when(apiKeyConfigurationService.updateApiKeyPolicy(eq(1L), any(ApiKeyPolicy.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/apikey-policy/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(apiKeyConfigurationService)
                .updateApiKeyPolicy(eq(1L), any(ApiKeyPolicy.class));
    }

    @Test
    @DisplayName("Should delete api key policy")
    void shouldDeleteApiKeyPolicy() throws Exception {
        when(apiKeyConfigurationService.deleteApiKeyPolicy(1L))
                .thenReturn(true);

        mockMvc.perform(delete("/api/apikey-policy/1"))
                .andExpect(status().isNoContent());

        verify(apiKeyConfigurationService)
                .deleteApiKeyPolicy(1L);
    }

    @Test
    @DisplayName("Should return 404 when deleting non existing policy")
    void shouldReturnNotFoundWhenDeletingNonExistingPolicy() throws Exception {
        when(apiKeyConfigurationService.deleteApiKeyPolicy(1L))
                .thenReturn(false);

        mockMvc.perform(delete("/api/apikey-policy/1"))
                .andExpect(status().isNotFound());

        verify(apiKeyConfigurationService)
                .deleteApiKeyPolicy(1L);
    }
}
