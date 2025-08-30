package com.innoverse.erp_edu_api.provisioning.api.web;

import jakarta.servlet.http.HttpServletRequest;

public interface TenantResolver {
    String resolve(HttpServletRequest req);
}