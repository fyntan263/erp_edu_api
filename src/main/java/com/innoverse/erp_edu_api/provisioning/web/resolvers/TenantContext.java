package com.innoverse.erp_edu_api.provisioning.web.resolvers;

import org.springframework.web.context.annotation.RequestScope;

@RequestScope
public class TenantContext {
    private static final ThreadLocal<String> CTX = new ThreadLocal<>();
    public static void set(String t)  {CTX.set(t);}
    public static String get()       {return CTX.get();}
    public static void clear()       {CTX.remove();}
}
