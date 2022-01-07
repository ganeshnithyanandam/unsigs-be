package gimbalabs.unsigsbe;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gimbalabs.unsigsbe.model.AssetTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@ExtendWith({MockitoExtension.class, RestDocumentationExtension.class, SpringExtension.class})
public class BlockfrostAdapterTest extends UnsigsBeApplicationTests {

    @Autowired
    BlockfrostAdapter blockfrostAdapter;

    @Autowired
    AppConfig appConfig;

    private WebClient webClient;

    @BeforeEach
    public void setup(RestDocumentationContextProvider restDocumentation) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer ->
                        configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper)))
                .build();
        webClient = WebClient.builder()
                .baseUrl("https://cardano-testnet.blockfrost.io/api/v0")
                .defaultHeader("project_id",appConfig.getTestnetApiKey())
                .defaultHeader("Accept", "application/json")
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

    @Test
    public void whenGetAssetTransaction_thenReturnsAssetTransactionForCountPageAndOrder() throws Exception {

        String assetString = "1e82bbd44f7bd555a8bcc829bd4f27056e86412fbb549efdbf78f42d756e7369673030303137";

        String responseString = webClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder.path("/assets/{asset}/transactions"
                        .replaceFirst("\\{asset\\}", assetString))
                                .queryParam("order","desc")
                                .build()
                )
                .retrieve().bodyToMono(String.class).block();

        assertNotNull(responseString);
    }

    @Test
    public void whenGetAssetTransactionTopItem_thenReturnsLatestAssetTransaction() throws Exception {

        String assetString = "1e82bbd44f7bd555a8bcc829bd4f27056e86412fbb549efdbf78f42d756e7369673030303137";
        // 1e82bbd44f7bd555a8bcc829bd4f27056e86412fbb549efdbf78f42d.unsig00017

        AssetTransaction[] assetTransactionsArr = webClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder.path("/assets/{asset}/transactions"
                        .replaceFirst("\\{asset\\}", assetString))
                                .queryParam("order","desc")
                                .queryParam("page","1")
                                .queryParam("count","1")
                                .build()
                )
                .retrieve().bodyToMono(AssetTransaction[].class).block();

        assertNotNull(assetTransactionsArr);
        assertEquals(1, assetTransactionsArr.length);
        AssetTransaction at = assetTransactionsArr[0];
        assertNotNull(at.getTxHash());
        assertNotNull(at.getTxIndex());
        assertNotNull(at.getBlockHeight());
        assertNotNull(at.getBlockTime());
    }


}
