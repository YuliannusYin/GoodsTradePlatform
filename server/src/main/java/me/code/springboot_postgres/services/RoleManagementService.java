package me.code.springboot_postgres.services;

import me.code.springboot_postgres.dtos.requests.CreateRoleDTO;
import me.code.springboot_postgres.dtos.requests.UpdateRoleDTO;
import me.code.springboot_postgres.dtos.responses.success.Success;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.Permission;
import me.code.springboot_postgres.models.entities.Role;
import me.code.springboot_postgres.repositories.PermissionRepository;
import me.code.springboot_postgres.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleManagementService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public RoleManagementService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(String roleId) {
        return roleRepository.findById(roleId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Role not found with id: " + roleId));
    }

    public Role createRole(CreateRoleDTO dto) {
        if (roleRepository.existsByName(dto.name())) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Role with name '" + dto.name() + "' already exists");
        }

        Role role = new Role(dto.name(), dto.description());
        if (dto.permissionIds() != null && !dto.permissionIds().isEmpty()) {
            Set<Permission> permissions = loadPermissionsByIds(dto.permissionIds());
            role.setPermissions(permissions);
        }

        return roleRepository.save(role);
    }

    public Role updateRole(String roleId, UpdateRoleDTO dto) {
        Role role = getRoleById(roleId);

        if (dto.name() != null && !dto.name().equals(role.getName())) {
            if (roleRepository.existsByName(dto.name())) {
                throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Role with name '" + dto.name() + "' already exists");
            }
            role.setName(dto.name());
        }

        if (dto.description() != null) {
            role.setDescription(dto.description());
        }

        if (dto.permissionIds() != null) {
            Set<Permission> permissions = loadPermissionsByIds(dto.permissionIds());
            role.setPermissions(permissions);
        }

        return roleRepository.save(role);
    }

    public Success deleteRole(String roleId) {
        Role role = getRoleById(roleId);

        // Prevent deletion of built-in roles
        if (isBuiltInRole(role.getName())) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "Cannot delete built-in role: " + role.getName());
        }

        roleRepository.delete(role);
        return new Success(HttpStatus.OK, "Role deleted successfully");
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public List<Permission> getPermissionsByModule(String module) {
        return permissionRepository.findByModule(module);
    }

    private Set<Permission> loadPermissionsByIds(Set<String> permissionIds) {
        Set<Permission> permissions = new HashSet<>();
        for (String permId : permissionIds) {
            Permission perm = permissionRepository.findById(permId).orElseThrow(
                    () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Permission not found with id: " + permId));
            permissions.add(perm);
        }
        return permissions;
    }

    private boolean isBuiltInRole(String roleName) {
        return roleName.equals("SUPER_ADMIN") || roleName.equals("ADMIN") || roleName.equals("USER");
    }
}
