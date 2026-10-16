package com.liu.permission.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UserRequest(Long id,
                          @NotBlank(message = "账号不能为空") String username,
                          String password,
                          @NotBlank(message = "姓名不能为空") String nickname,
                          Long orgId,
                          Integer status,
                          List<Long> roleIds) {
}
