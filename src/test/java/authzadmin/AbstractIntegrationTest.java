package authzadmin;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.test.context.jdbc.SqlConfig.ErrorMode.FAIL_ON_ERROR;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed.sql"},
  config = @SqlConfig(errorMode = FAIL_ON_ERROR, transactionMode = ISOLATED))
public class AbstractIntegrationTest {

  protected TestRestTemplate restTemplate = new TestRestTemplate();

  @Value("${local.server.port}")
  protected int port;

  @Autowired
  protected ClientRegistrationService clientRegistrationService;

  protected MultiValueMap<String, String> formMap(String params, Object... placeholders) {
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    String s = String.format(params, placeholders);
    Arrays.asList(s.split("&")).forEach(paramPair -> addToMap(map, paramPair));
    return map;
  }

  protected int numberOfClients() {
    return numberOfRegistrations(false);
  }

  protected int numberOfResourceServers() {
    return numberOfRegistrations(true);
  }

  protected ClientDetails findByClientId(String clientId) {
    return clientRegistrationService.listClientDetails()
      .stream().filter(details -> details.getClientId().equals(clientId)).collect(toList()).get(0);
  }

  private int numberOfRegistrations(boolean resourceServer) {
    Map<Boolean, List<ClientDetails>> details = clientRegistrationService.listClientDetails()
      .stream()
      .collect(partitioningBy(client -> client.getAuthorities().stream().map(GrantedAuthority::getAuthority)
        .collect(toList()).contains("ROLE_TOKEN_CHECKER"))
      );
    return details.get(resourceServer).size();
  }

  private void addToMap(MultiValueMap<String, String> map, String paramPair) {
    String[] param = paramPair.split("=");
    map.add(param[0], param[1]);
  }

}
