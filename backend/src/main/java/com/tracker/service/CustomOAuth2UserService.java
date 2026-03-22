package com.tracker.service;

import com.tracker.model.User;
import com.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest)
            throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest);

        String email      = oidcUser.getAttribute("email");
        String name       = oidcUser.getAttribute("name");
        String imageUrl   = oidcUser.getAttribute("picture");
        String providerId = oidcUser.getAttribute("sub");

        log.info("Google login: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElse(User.builder().email(email).build());

        user.setName(name);
        user.setImageUrl(imageUrl);
        user.setProviderId(providerId);
        userRepository.save(user);

        log.info("User saved: id={}", user.getId());
        return oidcUser;
    }
}