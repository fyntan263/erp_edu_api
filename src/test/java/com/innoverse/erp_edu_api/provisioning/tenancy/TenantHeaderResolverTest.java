//package com.innoverse.erp_edu_api.provisioning.tenancy;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import com.innoverse.erp_edu_api.provisioning.api.web.TenantHeaderResolver;
//import com.innoverse.erp_edu_api.provisioning.infrastructure.config.TenantProperties;
//import jakarta.servlet.http.HttpServletRequest;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import static org.mockito.Mockito.*;
//
//class TenantHeaderResolverTest {
//
//
//        @Mock
//        private HttpServletRequest request;
//
//        private TenantHeaderResolver resolver;
//        private TenantProperties tenantProperties;
//
//        @BeforeEach
//        void setUp() {
//            MockitoAnnotations.openMocks(this);
//            tenantProperties = new TenantProperties();
//            tenantProperties.setHeaderName("X-Tenant-ID");
//            tenantProperties.setDefaultTenant("public");
//            resolver = new TenantHeaderResolver(tenantProperties);
//        }
//
//        @Test
//        void testResolveWithValidHeader() {
//            when(request.getHeader("X-Tenant-ID")).thenReturn("tenant-123");
//
//            String result = resolver.resolve(request);
//
//            assertEquals("tenant-123", result);
//        }
//
//        @Test
//        void testResolveWithNullHeader() {
//            when(request.getHeader("X-Tenant-ID")).thenReturn(null);
//
//            String result = resolver.resolve(request);
//
//            assertEquals("public", result);
//        }
//
//        @Test
//        void testResolveWithEmptyHeader() {
//            when(request.getHeader("X-Tenant-ID")).thenReturn("");
//
//            String result = resolver.resolve(request);
//
//            assertEquals("public", result);
//        }
//
//        @Test
//        void testResolveWithWhitespaceHeader() {
//            when(request.getHeader("X-Tenant-ID")).thenReturn("   ");
//
//            String result = resolver.resolve(request);
//
//            assertEquals("public", result);
//        }
//
//        @Test
//        void testResolveWithCustomHeaderName() {
//            tenantProperties.setHeaderName("Custom-Tenant-Header");
//            when(request.getHeader("Custom-Tenant-Header")).thenReturn("custom-tenant");
//
//            String result = resolver.resolve(request);
//
//            assertEquals("custom-tenant", result);
//        }
//    }