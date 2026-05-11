package passroutebackend.auth.oauth2;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String name;
    private final Long userId;

    public CustomOAuth2User(OAuth2User delegate, Long userId) {
        this.attributes = delegate.getAttributes();
        this.authorities = delegate.getAuthorities();
        this.name = delegate.getName();
        this.userId = userId;
    }
}
