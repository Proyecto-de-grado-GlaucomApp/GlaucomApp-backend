package co.edu.javeriana.glaucomapp_backend.mobileauth.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.UUID;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import co.edu.javeriana.glaucomapp_backend.mobileauth.exposed.MyUser;
import co.edu.javeriana.glaucomapp_backend.mobileauth.model.LogInForm;
import co.edu.javeriana.glaucomapp_backend.mobileauth.repository.DoctorEventService;
import co.edu.javeriana.glaucomapp_backend.mobileauth.repository.MyUserRepository;
import co.edu.javeriana.glaucomapp_backend.mobileauth.service.impl.AuthServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;


public class AuthServiceImplTest {

    @Mock
    private MyUserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private DoctorEventService doctorEventService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegister() {
        MyUser user = new MyUser();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setName("Test User");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(MyUser.class))).thenReturn(user);

        MyUser registeredUser = authServiceImpl.register(user);

        assertNotNull(registeredUser);
        assertEquals("testuser", registeredUser.getUsername());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testLogin() {
        LogInForm loginForm = new LogInForm("testuser", "password");
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        MyUser user = new MyUser();
        user.setUsername("testuser");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(MyUser.class))).thenReturn("jwtToken");

        authServiceImpl.login(loginForm, response);

        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    public void testRefreshToken() {
        String token = "oldToken";
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(jwtUtil.refreshToken(anyString())).thenReturn("newToken");

        authServiceImpl.refreshToken(token, response);

        verify(response, times(2)).addCookie(any(Cookie.class)); // Permite 2 invocaciones
    }

    @Test
    public void testLogout() {
        String authHeader = "Bearer jwtToken";
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(jwtUtil.extractIdFromToken(anyString())).thenReturn("userId");
        when(jwtUtil.validateToken(anyString())).thenReturn(true);

        authServiceImpl.logout(authHeader, response);

        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    public void testCloseAccount() {
        String token = "Bearer jwtToken";
        HttpServletResponse response = mock(HttpServletResponse.class);
        UUID userId = UUID.randomUUID();

        // Simulando el comportamiento de jwtUtil
        when(jwtUtil.extractIdFromToken(anyString())).thenReturn(userId.toString());
        when(jwtUtil.validateToken(anyString())).thenReturn(true); // Asegúrate de que el token sea considerado válido

        // Agregar simulación para el servicio que elimina al paciente y el usuario
        doNothing().when(doctorEventService).deletePatient(userId);
        doNothing().when(userRepository).deleteById(userId);

        authServiceImpl.closeAccount(token, response);

        verify(doctorEventService, times(1)).deletePatient(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    public void testValidateUserFields() throws Exception {
        MyUser user = new MyUser();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setName("Test User");

        invokePrivateMethod("validateUserFields", user);
    }

    @Test
    public void testCheckUsernameAvailability() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        invokePrivateMethod("checkUsernameAvailability", "testuser");
    }

    @Test
    public void testAuthenticateUser() throws Exception {
        LogInForm loginForm = new LogInForm("testuser", "password");
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        Authentication result = (Authentication) invokePrivateMethod("authenticateUser", loginForm);
        assertNotNull(result);
    }

    @Test
    public void testFindUserByUsername() throws Exception {
        MyUser user = new MyUser();
        user.setUsername("testuser");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        MyUser result = (MyUser) invokePrivateMethod("findUserByUsername", "testuser");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    public void testValidateAuthToken() throws Exception {
        String authHeader = "Bearer jwtToken";

        when(jwtUtil.extractIdFromToken(anyString())).thenReturn("userId");
        when(jwtUtil.validateToken(anyString())).thenReturn(true);

        invokePrivateMethod("validateAuthToken", authHeader);
    }

    @Test
    public void testIsNullOrEmpty() throws Exception {
        assertTrue((Boolean) invokePrivateMethod("isNullOrEmpty", (Object) null));
        assertTrue((Boolean) invokePrivateMethod("isNullOrEmpty", ""));
        assertFalse((Boolean) invokePrivateMethod("isNullOrEmpty", "test"));
    }

    @Test
    public void testInvalidateJwtCookie() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);

        invokePrivateMeth("invalidateJwtCookie", response);

        // Verifica que se haya llamado a addCookie una vez
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    private Object invokePrivateMethod(String methodName, Object... args) throws Exception {
        Method method = AuthServiceImpl.class.getDeclaredMethod(methodName, getParameterTypes(args));
        method.setAccessible(true); // Hacemos el método accesible
        return method.invoke(authServiceImpl, args);
    }

    @Test
    public void testSetJwtCookie() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        String token = "jwtToken";

        invokePrivate("setJwtCookie", response, token);

        // Verifica que se haya llamado a addCookie una vez
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    private Class<?>[] getParameterTypes(Object... args) {
        if (args == null) {
            return new Class<?>[0]; // Retornar un arreglo vacío si args es null
        }
        return Arrays.stream(args)
                .map(arg -> arg == null ? String.class : arg.getClass()) // Cambiar a String.class si arg es null
                .toArray(Class<?>[]::new);
    }

    private Object invokePrivateMeth(String methodName, Object... args) throws Exception {
        Method method = AuthServiceImpl.class.getDeclaredMethod(methodName, getParameterType(args));
        method.setAccessible(true); // Hacemos el método accesible
        return method.invoke(authServiceImpl, args);
    }

    private Class<?>[] getParameterType(Object... args) {
        return new Class<?>[] {
                HttpServletResponse.class // Solo un argumento de tipo HttpServletResponse
        };
    }

    private Object invokePrivate(String methodName, Object... args) throws Exception {
        Method method = AuthServiceImpl.class.getDeclaredMethod(methodName, getParameterT(args));
        method.setAccessible(true); // Hacemos el método accesible
        return method.invoke(authServiceImpl, args);
    }
    
    private Class<?>[] getParameterT(Object... args) {
        return new Class<?>[]{
            HttpServletResponse.class, // Tipo del primer argumento
            String.class // Tipo del segundo argumento
        };
    }

}