package io.github.organizationApp.security;

import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class User {
    public static String getUserId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        KeycloakAuthenticationToken principal = (KeycloakAuthenticationToken) request.getUserPrincipal();
        final String userId = principal.getAccount().getKeycloakSecurityContext().getToken().getSubject();

        return userId;
    }
}
