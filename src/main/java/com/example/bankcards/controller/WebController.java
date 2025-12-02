package com.example.bankcards.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    private Authentication getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication;
        }
        return null;
    }

    private boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    @GetMapping("/")
    public String index() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication != null) {
            if (hasRole(authentication, "ROLE_ADMIN")) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication != null) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication != null) {
            return "redirect:/";
        }
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin_dashboard";
    }
}