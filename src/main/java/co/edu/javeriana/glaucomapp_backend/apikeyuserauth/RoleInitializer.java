package co.edu.javeriana.glaucomapp_backend.apikeyuserauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class RoleInitializer {

    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    public void init() {
        createRoleIfNotExists(RoleEnum.ADMIN);
        createRoleIfNotExists(RoleEnum.USER);
    }

    private void createRoleIfNotExists(RoleEnum roleName) {
        // Utiliza Optional para manejar la posibilidad de que el rol no exista
        if (roleRepository.findByRole(roleName) == null) {
            Role role = new Role();
            role.setRole(roleName); // Asignar el enum directamente
            roleRepository.save(role);
        }
    }
}
