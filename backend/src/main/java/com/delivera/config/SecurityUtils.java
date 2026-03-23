package com.delivera.config;

import com.delivera.exception.CompanyContextException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityUtils {

    public UUID getCurrentCompanyId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getDetails() instanceof UUID companyId)) {
            throw new CompanyContextException();
        }
        return companyId;
    }

    public String getCurrentEmail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? (String) auth.getPrincipal() : null;
    }
}
