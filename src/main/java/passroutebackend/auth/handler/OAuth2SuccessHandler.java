package passroutebackend.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import passroutebackend.auth.oauth2.CustomOAuth2User;
import passroutebackend.global.jwt.JwtTokenProvider;
import passroutebackend.global.property.OAuth2Properties;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2Properties oAuth2Properties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.generateAccessToken(oAuth2User.getUserId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(oAuth2User.getUserId());

        String fragment = "accessToken=" + accessToken + "&refreshToken=" + refreshToken;
        String redirectUri = UriComponentsBuilder
                .fromUriString(oAuth2Properties.getRedirectUri())
                .fragment(fragment)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}
