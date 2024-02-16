package com.exampleJPA2.JPA2demo.repository;

import com.exampleJPA2.JPA2demo.models.ERole;
import com.exampleJPA2.JPA2demo.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);

}
