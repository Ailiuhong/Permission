package com.liu.permission.dto;

import java.util.List;

public record AssignPermissionsRequest(List<Long> permissionIds) {
}
