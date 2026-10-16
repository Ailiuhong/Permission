package com.liu.permission.controller;

import com.liu.permission.annotation.Audit;
import com.liu.permission.common.Result;
import com.liu.permission.dto.OrgRequest;
import com.liu.permission.entity.SysOrg;
import com.liu.permission.service.OrgService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/system/org")
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;

    /** 组织树是用户的公共基础数据，登录即可读取 */
    @GetMapping("/tree")
    public Result<List<SysOrg>> tree() {
        return Result.ok(orgService.tree());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:org:add')")
    @Audit(module = "组织架构", value = "新增组织")
    public Result<Long> create(@Valid @RequestBody OrgRequest request) {
        return Result.ok(orgService.create(request));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:org:edit')")
    @Audit(module = "组织架构", value = "修改组织")
    public Result<Void> update(@Valid @RequestBody OrgRequest request) {
        orgService.update(request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:org:delete')")
    @Audit(module = "组织架构", value = "删除组织")
    public Result<Void> delete(@PathVariable Long id) {
        orgService.delete(id);
        return Result.ok();
    }
}
