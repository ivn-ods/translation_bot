package bot;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.OptionalDouble;

@Service
public class BotService {
    @Value("${telegram_url}")
    private   String telegramURL;

    @Value("${google_url}")
    private   String googleURL;

    @Value("${google_key}")
    private   String googleKey;

    private final WebClient webClient;
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    private int offset;

    @Autowired
    public BotService(WebClient webClient) {
        this.webClient = webClient;
    }


    public UpdateResponse getMessagesFromTelegram(int offset) {
        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("offset", String.valueOf(offset));
        return webClient
                .post()
                .uri(telegramURL + "getUpdates")
                .body(BodyInserters.fromFormData(bodyValues))
                .retrieve()
                .bodyToMono(UpdateResponse.class)
                .block();
    }

    public GoogleResponse getTranslationFromGoogle(String text, String languageCode) {
        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("q", text);
        bodyValues.add("key", googleKey);
        bodyValues.add("target", languageCode);

        return webClient
                .post()
                .uri(googleURL)
                .body(BodyInserters.fromFormData(bodyValues))
                .retrieve()
                .bodyToMono(GoogleResponse.class)
                .block();
    }

    public String sendMessage(String text, int chat) {
        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.add("chat_id", String.valueOf(chat));
        bodyValues.add("text", text);
        return webClient
                .post()
                .uri(telegramURL + "sendMessage")
                .body(BodyInserters.fromFormData(bodyValues))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    //Average latin letters code is close to ~70. Cyrillic symbol code close to ~1070
    //Language detected just by average letter code number
    public boolean isGerman(String text) {
        OptionalDouble averageSymbol = text.chars()
                .filter(item -> item != '.' && item != ',' && item != ' ')
                .average();
        if (averageSymbol.isPresent()) {
            return averageSymbol.getAsDouble() < 500;
        }
        else return true;
    }


    @Scheduled(fixedRate = 5000)
    public void checkForNewRequests() {
        try {
            UpdateResponse updateResponse = this.getMessagesFromTelegram(this.offset+1);
            for (UpdateResponse.Result result : updateResponse.result) {
                String message = result.message.text;
                int chat = result.message.chat.id;
                this.offset = result.update_id;
                String language = isGerman(message) ? "uk"  :  "de";
                try {
                    GoogleResponse googleResponse = getTranslationFromGoogle(message, language);
                    try {
                        sendMessage(googleResponse.data.translations.get(0).translatedText, chat);
                    }
                    catch (Exception e) {
                        logger.warn("Error during reply to Telegram" + e.getMessage());
                    }
                }
                catch (Exception e) {
                    logger.warn("Error during request to geoogle" + e.getMessage());
                }
                // System.out.println("message sent: " + message);

            }
        }
        catch (Exception e) {
            logger.warn("Error during check for updates to Telegram" + e.getMessage());
        }

    }
}
