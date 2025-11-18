package com.team_5_back_repository.project.global.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final ClientRegistrationRepository clientRegistrationRepository;

    private DefaultOAuth2AuthorizationRequestResolver createDefaultResolver() {
        return new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = createDefaultResolver().resolve(request);
        return customizeState(req, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = createDefaultResolver().resolve(request, clientRegistrationId);
        return customizeState(req, request);
    }

    private OAuth2AuthorizationRequest customizeState(OAuth2AuthorizationRequest req, HttpServletRequest request) {
        if (req == null) return null;

        String redirectUrl = request.getParameter("redirectUrl");
        if (redirectUrl == null) redirectUrl = "/";

        String originState = UUID.randomUUID().toString();

        String rawState = redirectUrl + "#" + originState;

        String encodedState = Base64.getUrlEncoder().encodeToString(rawState.getBytes(StandardCharsets.UTF_8));

        return OAuth2AuthorizationRequest.from(req)
                .state(encodedState)
                .build();
    }
}
