package co.edu.javeriana.glaucomapp_backend.apikey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
    public void testFindByUserApiIdAndStatusIn() {
        List<ApiKeyStatus> statuses = Arrays.asList(ApiKeyStatus.ACTIVE, ApiKeyStatus.INACTIVE);
        Optional<ApiKey> optionalApiKey = Optional.of(apiKey);
        when(apiKeyRepository.findByUserApiIdAndStatusIn(1L, statuses)).thenReturn(optionalApiKey);

        Optional<ApiKey> result = apiKeyRepository.findByUserApiIdAndStatusIn(1L, statuses);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getUserApiId());
    }

    @Test
    public void testFindByUserApiIdAndStatus() {
        Optional<ApiKey> optionalApiKey = Optional.of(apiKey);
        when(apiKeyRepository.findByUserApiIdAndStatus(1L, ApiKeyStatus.ACTIVE)).thenReturn(optionalApiKey);

        Optional<ApiKey> result = apiKeyRepository.findByUserApiIdAndStatus(1L, ApiKeyStatus.ACTIVE);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getUserApiId());
        assertEquals(ApiKeyStatus.ACTIVE, result.get().getStatus());
    }

    @Test
    public void testFindByUserApiId() {
        Optional<ApiKey> optionalApiKey = Optional.of(apiKey);
        when(apiKeyRepository.findByUserApiId(1L)).thenReturn(optionalApiKey);

        Optional<ApiKey> result = apiKeyRepository.findByUserApiId(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getUserApiId());
    }
}
