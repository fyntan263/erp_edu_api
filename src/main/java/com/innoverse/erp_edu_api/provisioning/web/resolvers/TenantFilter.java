package com.innoverse.erp_edu_api.provisioning.web.resolvers;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


public class TenantFilter extends OncePerRequestFilter {
    private final TenantResolver resolver;

    public TenantFilter(TenantResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        try {
            TenantContext.set(resolver.resolve(req));
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();
        }
    }
}
