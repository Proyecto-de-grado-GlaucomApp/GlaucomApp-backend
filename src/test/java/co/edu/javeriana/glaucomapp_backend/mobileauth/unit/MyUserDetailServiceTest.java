package co.edu.javeriana.glaucomapp_backend.mobileauth.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;



import co.edu.javeriana.glaucomapp_backend.mobileauth.exposed.MyUserDetailService;
import co.edu.javeriana.glaucomapp_backend.mobileauth.exposed.MyUser;
import co.edu.javeriana.glaucomapp_backend.mobileauth.repository.MyUserRepository;

public class MyUserDetailServiceTest {
    
    @Mock
    private MyUserRepository userRepository;

    @InjectMocks
    private MyUserDetailService myUserDetailService;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoadUserByUsernameExist() {
        //Arrange
        MyUser user = new MyUser();
        user.setUsername("testuser");
        user.setPassword("test");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));


        //Act
        var userDetails = myUserDetailService.loadUserByUsername("testuser");

        //Assert
        assertEquals("testuser", userDetails.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    
    }

    @Test
    public void testLoadUserByUsernameNotExist() {
        //Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        //Act
        try {
            myUserDetailService.loadUserByUsername("testuser");
        } catch (Exception e) {
            //Assert
            assertEquals("testuser", e.getMessage());
        }
        verify(userRepository, times(1)).findByUsername("testuser");
    }

}