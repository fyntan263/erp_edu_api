package com.innoverse.erp_edu_api.provisioning.web.resolvers;

import jakarta.servlet.http.HttpServletRequest;

public interface TenantResolver {
    String resolve(HttpServletRequest req);
}