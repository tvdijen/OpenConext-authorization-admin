package authzadmin;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.stream.Collectors;

import static authzadmin.WebApplication.CLIENT_CREDENTIALS;
import static authzadmin.WebApplication.ROLE_TOKEN_CHECKER;

public class OauthClientDetails extends BaseClientDetails {

  /*
   * Spring logic for auto approving all scopes
   */
  public static final String AUTO_APPROVE_SCOPE = "true";
  private static final String DEFAULT_AUTHORIZED_GRANT_TYPES = "authorization_code,refresh_token,implicit";

  public OauthClientDetails(OauthSettings oauthSettings) {
    super(
      oauthSettings.getConsumerKey(),
      null,
      CollectionUtils.isEmpty(oauthSettings.getScopes()) ? null : StringUtils.collectionToCommaDelimitedString(oauthSettings.getScopes().stream().map(Scope::getValue).collect(Collectors.toList())),
      oauthSettings.isClientCredentialsAllowed() ? DEFAULT_AUTHORIZED_GRANT_TYPES + "," + CLIENT_CREDENTIALS : DEFAULT_AUTHORIZED_GRANT_TYPES,
      null,
      oauthSettings.getCallbackUrl()
    );
    setClientSecret(oauthSettings.getSecret());
    if (oauthSettings.isAutoApprove()) {
      setAutoApproveScopes(Collections.singletonList(AUTO_APPROVE_SCOPE));
    }
    if (oauthSettings.isResourceServer()) {
      setAuthorities(AuthorityUtils.createAuthorityList(ROLE_TOKEN_CHECKER));
      setAuthorizedGrantTypes(Collections.emptyList());
    }
  }
}
