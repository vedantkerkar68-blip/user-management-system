package com.demo.controller;

import com.demo.security.UserDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/whoami")
    public DebugInfo whoami(@AuthenticationPrincipal UserDetailsImpl user) {
        return new DebugInfo(
            user.getUsername(),
            user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
        );
    }

    public record DebugInfo(String username, Collection<String> authorities) {}
}