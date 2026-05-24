package passroutebackend.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.auth.oauth2.CustomOAuth2User;
import passroutebackend.auth.oauth2.OAuth2UserInfo;
import passroutebackend.auth.oauth2.OAuth2UserInfoFactory;
import passroutebackend.user.entity.AuthProvider;
import passroutebackend.user.entity.User;
import passroutebackend.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, oAuth2User.getAttributes());

        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_found"), "소셜 계정에서 이메일 정보를 가져올 수 없습니다.");
        }

        User user = userRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
                .orElseGet(() -> registerNewUser(userInfo, provider));

        return new CustomOAuth2User(oAuth2User, user.getId());
    }

    private User registerNewUser(OAuth2UserInfo userInfo, AuthProvider provider) {
        userRepository.findByEmail(userInfo.getEmail()).ifPresent(existing -> {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_already_exists"),
                    "이미 " + existing.getProvider().name() + " 방식으로 가입된 이메일입니다.");
        });

        User user = User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .provider(provider)
                .providerId(userInfo.getProviderId())
                .emailVerified(true)
                .build();

        return userRepository.save(user);
    }
}
