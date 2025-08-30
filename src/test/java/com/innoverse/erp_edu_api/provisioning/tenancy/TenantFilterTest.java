//package com.innoverse.erp_edu_api.provisioning.tenancy;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import com.innoverse.erp_edu_api.provisioning.api.web.TenantContext;
//import com.innoverse.erp_edu_api.provisioning.api.web.TenantFilter;
//import com.innoverse.erp_edu_api.provisioning.api.web.TenantResolver;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import static org.mockito.Mockito.*;
//
//class TenantFilterTest {
//
//    @Mock
//    private TenantResolver tenantResolver;
//
//    @Mock
//    private HttpServletRequest request;
//
//    @Mock
//    private HttpServletResponse response;
//
//    @Mock
//    private FilterChain filterChain;
//
//    @InjectMocks
//    private TenantFilter tenantFilter;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testTenantFilterSetsAndClearsContext() throws Exception {
//        when(tenantResolver.resolve(request)).thenReturn("test-tenant");
//
//        tenantFilter.doFilterInternal(request, response, filterChain);
//
//        // Verify context was set during filter execution
//        verify(filterChain).doFilter(request, response);
//
//        // Verify context is cleared after execution
//        assertNull(TenantContext.get());
//    }
//
//    @Test
//    void testTenantFilterWithNullTenant() throws Exception {
//        when(tenantResolver.resolve(request)).thenReturn(null);
//
//        tenantFilter.doFilterInternal(request, response, filterChain);
//
//        verify(filterChain).doFilter(request, response);
//        assertNull(TenantContext.get());
//    }
//
//    @Test
//    void testTenantFilterWithEmptyTenant() throws Exception {
//        when(tenantResolver.resolve(request)).thenReturn("");
//
//        tenantFilter.doFilterInternal(request, response, filterChain);
//
//        verify(filterChain).doFilter(request, response);
//        assertNull(TenantContext.get());
//    }
//}