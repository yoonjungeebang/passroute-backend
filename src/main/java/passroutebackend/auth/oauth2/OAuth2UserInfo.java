package passroutebackend.auth.oauth2;

public interface OAuth2UserInfo {
    String getProviderId();
    String getEmail();
    String getName();
}
