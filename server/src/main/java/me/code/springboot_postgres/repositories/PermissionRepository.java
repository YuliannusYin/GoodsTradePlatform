package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, String> {
    Optional<Permission> findByName(String name);
    boolean existsByName(String name);
    List<Permission> findByModule(String module);
}
