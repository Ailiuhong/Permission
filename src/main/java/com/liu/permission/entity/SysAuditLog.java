package com.liu.permission.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_audit_log")
public class SysAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String operation;
    private String method;
    private String requestUri;
    private String requestMethod;
    private String params;
    private String ip;
    /** 1成功 0失败 */
    private Integer status;
    private String errorMsg;
    private Long costTime;
    private LocalDateTime createTime;
}
