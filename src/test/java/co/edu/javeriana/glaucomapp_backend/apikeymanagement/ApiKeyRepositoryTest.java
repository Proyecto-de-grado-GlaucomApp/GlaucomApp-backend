package co.edu.javeriana.glaucomapp_backend.apikeymanagement;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class ApiKeyRepositoryTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    private ApiKey apiKey;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        apiKey = new ApiKey();
        apiKey.setApiKey("testApiKey");
        apiKey.setStatus(ApiKeyStatus.ACTIVE);
        apiKey.setUserApiId(1L);
    }

    @Test
    public void testFindByApiKey() {
        Optional<ApiKey> optionalApiKey = Optional.of(apiKey);
        when(apiKeyRepository.findByApiKey("testApiKey")).thenReturn(optionalApiKey);

        Optional<ApiKey> result = apiKeyRepository.findByApiKey("testApiKey");
        assertTrue(result.isPresent());
        assertEquals("testApiKey", result.get().getApiKey());
    }

    @Test
    public void testFindByStatus() {
        List<ApiKey> apiKeys = Arrays.asList(apiKey);
        when(apiKeyRepository.findByStatus(ApiKeyStatus.ACTIVE)).thenReturn(apiKeys);

        List<ApiKey> result = apiKeyRepository.findByStatus(ApiKeyStatus.ACTIVE);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(ApiKeyStatus.ACTIVE, result.get(0).getStatus());
    }

    @Test
    public void testFindByUserApiId() {
        // Arrange for multiple methods: findByUserApiIdAndStatusIn, findByUserApiIdAndStatus, and findByUserApiId
        List<ApiKeyStatus> statuses = Arrays.asList(ApiKeyStatus.ACTIVE, ApiKeyStatus.INACTIVE);
        Optional<ApiKey> optionalApiKey = Optional.of(apiKey);
        
        when(apiKeyRepository.findByUserApiIdAndStatusIn(1L, statuses)).thenReturn(optionalApiKey);
        when(apiKeyRepository.findByUserApiIdAndStatus(1L, ApiKeyStatus.ACTIVE)).thenReturn(optionalApiKey);
        when(apiKeyRepository.findByUserApiId(1L)).thenReturn(optionalApiKey);

        // Test findByUserApiIdAndStatusIn
        Optional<ApiKey> result = apiKeyRepository.findByUserApiIdAndStatusIn(1L, statuses);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getUserApiId());

        // Test findByUserApiIdAndStatus
        result = apiKeyRepository.findByUserApiIdAndStatus(1L, ApiKeyStatus.ACTIVE);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getUserApiId());
        assertEquals(ApiKeyStatus.ACTIVE, result.get().getStatus());

        // Test findByUserApiId
        result = apiKeyRepository.findByUserApiId(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getUserApiId());
    }
}
