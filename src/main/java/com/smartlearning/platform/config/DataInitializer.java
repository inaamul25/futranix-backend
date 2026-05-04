package com.smartlearning.platform.config;

import com.smartlearning.platform.entity.Role;
import com.smartlearning.platform.entity.enums.RoleType;
import com.smartlearning.platform.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        Arrays.stream(RoleType.values()).forEach(roleType ->
                roleRepository.findByName(roleType).orElseGet(() -> roleRepository.save(new Role(roleType)))
        );
    }
}
