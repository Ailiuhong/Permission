package com.liu.permission.dto;

import java.util.List;

public record AssignRolesRequest(List<Long> roleIds) {
}
