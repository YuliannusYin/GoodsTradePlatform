package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.requests.CreateRoleDTO;
import me.code.springboot_postgres.dtos.requests.UpdateRoleDTO;
import me.code.springboot_postgres.dtos.responses.success.Success;
import me.code.springboot_postgres.models.entities.Permission;
import me.code.springboot_postgres.models.entities.Role;
import me.code.springboot_postgres.services.RoleManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin/roles")
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

    @Autowired
    public RoleManagementController(RoleManagementService roleManagementService) {
        this.roleManagementService = roleManagementService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleManagementService.getAllRoles());
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<Role> getRoleById(@PathVariable String roleId) {
        return ResponseEntity.ok(roleManagementService.getRoleById(roleId));
    }

    @PostMapping("/add")
    public ResponseEntity<Role> createRole(@RequestBody CreateRoleDTO dto) {
        return ResponseEntity.ok(roleManagementService.createRole(dto));
    }

    @PutMapping("/edit/{roleId}")
    public ResponseEntity<Role> updateRole(@PathVariable String roleId, @RequestBody UpdateRoleDTO dto) {
        return ResponseEntity.ok(roleManagementService.updateRole(roleId, dto));
    }

    @DeleteMapping("/delete/{roleId}")
    public ResponseEntity<Success> deleteRole(@PathVariable String roleId) {
        return roleManagementService.deleteRole(roleId).toResponseEntity();
    }

    @GetMapping("/permissions/all")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(roleManagementService.getAllPermissions());
    }

    @GetMapping("/permissions/module/{module}")
    public ResponseEntity<List<Permission>> getPermissionsByModule(@PathVariable String module) {
        return ResponseEntity.ok(roleManagementService.getPermissionsByModule(module));
    }
}
