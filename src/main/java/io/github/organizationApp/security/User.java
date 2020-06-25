package io.github.organizationApp.security;

import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class User {
    private static final Logger logger = LoggerFactory.getLogger(User.class);

    public static String getUserId() {

        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

            KeycloakAuthenticationToken principal = (KeycloakAuthenticationToken) request.getUserPrincipal();
            final String userId = principal.getAccount().getKeycloakSecurityContext().getToken().getSubject();

            return userId;
        } catch (NullPointerException e) {
            logger.error("userId not found! ");
            throw new NullPointerException("The client has not been recognized");
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
            throw new IllegalStateException("an error occurred while capturing a request");
        }
    }
}
