package com.example.bookstore.service;

import com.example.bookstore.dto.request.PermissionRequest;
import com.example.bookstore.dto.response.PermissionResponse;
import com.example.bookstore.mapper.PermissionMapper;
import com.example.bookstore.model.Permission;
import com.example.bookstore.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionResponse create(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAll() {
        var permissions = permissionRepository.findAll();
        // Dùng Stream để lặp qua List và biến biến Entity thành Response
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    public void delete(String permission) {
        permissionRepository.deleteById(permission);
    }
}
