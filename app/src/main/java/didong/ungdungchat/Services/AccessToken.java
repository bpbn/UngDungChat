package didong.ungdungchat.Services;

import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;
import com.google.firebase.auth.GoogleAuthCredential;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AccessToken {
    public static final String firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging";

    public String getAccessToken() {
        try {
            String jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"ungdungchat-9ea1c\",\n" +
                    "  \"private_key_id\": \"ebb6e57d22cb4d37ec85b13f400e9eab5a0ec219\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7L2gMbyDmGwIP\\nrfgho9J1W2wdQmvgu4nvk2ZCPHmIghShGhCjRQ/Gt9KTWo6BNEIFUBSybZkQaElo\\n47FMOT9IZlwYEuGuH5QGLwu3ZS8RKbzXWfKexIa8pCj3NC90RGEi/5INSPCu3fyp\\njvJ1oEfYHFjgLBYfR5wXFWrAIcTwxB+Z07WhnHgmK8AUiZPTj+8x9+CqUlVldr6Y\\nxL+D/gUj+DyQdDrj/UIFVoSyvBRSG2cN/d+idGQjCQ8fTg5B5QjFNmAorfkRGauj\\nEIVxbuluVEkNXdK6ccDeloLqHt5yyf8iymQhNYe6VgIflc/RmfF+0nhO/xuUfkwQ\\nzhJMEw7tAgMBAAECggEAM6atK2Euzs/9DZ6PrZiq5j9H4fsGEYkuUrF1silFz+k1\\nEA9FMAhKhicLMCX41KY64YRrcaDUb7aXKx5JOiNB1xSHrQzowUdtbRdmQEDnb/D4\\nbuQfErhgBYW22B/KSGbl9uZWwtuxrk8K/S64sMR9jqCj1ZfUsuTrqWL7hfDLizPa\\nPEKBzJ/E0o0sYAdSgsBbWugYerZcMU/qDDFrLbyCYpET1vucAfNdJuHNCASo2QOM\\n8XL0IUod/o1jrvLqKlNInJyQFIUbg5XCeHzaJOQnB+LD3HvFRNhuSPLgJyMIjCD9\\nPI6pM6OAyEsj4Ib+E0/Uok7931dY9hEd1Gb/CzipcQKBgQDgQY7eaS9oAqM4UcMn\\nDoOxan4XFyprlfli0M7wrLi3MWQhxF2vOGHFvPfzx8hbvLwwlmRBTviZ1kTvg1+z\\npLnKHxue6ArLX5VL4Wpb/Ulc+aYKQIRAk6+y4dV6AdlZtiWuDGn8rUBuGdqlfRqI\\nbkb8U0oy37onUZOCC26eRSwdZwKBgQDVrn9rBeUHubrxAk/uIK65ZrZFbgbNrDYF\\nQdLvo3at8H20MUi6tA2nSR7O1uIKuN60KOSuLXTkZPQcCXaFPBgB5F//PbuVToTp\\nRBCxNwQkJxRX1sOwYVco3U+ZnITxTFVc1+vKAP/aUstBviFg8x+oEtlpajZlt5dT\\nbW/wV+EoiwKBgQCyHN+wXrK8VXv1u1rYpnqKax6CkG+sy40rotT56vU66wuwlhoi\\nlRFy0EfPY5oK4rhFju1JwcNJzBkscpeMQwUdN9/kMkCAjwYxInQrY9zk75OlCOEB\\nuUsPNLctFFLhIf6DYnC7s48842P5lIeCXLQcrLp99ZwQpoYV/6Z4JlCHeQKBgCz5\\nl3QNP5GKtCA6Hly+oEgBqGegqunrT0t3+U/bFgWyUqL0pJw3fZAyQbvDWrxrAOTm\\nnotSogHYhdDI5RtR9LOhP9tXQP5CEpOOcfz62XJKj2uVloavVksRmcl5OjoXmPf7\\nsnNIFpH7TzC9NgVT5tvXa89WPbDXHZC/vgnWVVvVAoGAOvOd38UAVQT5gK6A87sP\\n/MOh0XUwo4OEsZqrY1tuMeKZhwx2HndLRkv9dlkSAQQUeFYe6wwgvQALXTNv3vL6\\nXrExmUS9XCIc4MQhRlSx0ALNUfcXO0vj2EP6bS+7u4cDeIP73wPVGM3GtXWvu5Q5\\ni9EKJHSSKr5dmGkQ1SJqjeE=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-ng578@ungdungchat-9ea1c.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"117090718346412241627\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-ng578%40ungdungchat-9ea1c.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}";

            InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream).createScoped(Lists.newArrayList(firebaseMessagingScope));
            credentials.refresh();
            return credentials.refreshAccessToken().getTokenValue();

        }catch (Exception e) {
            Log.e("AccessToken", "getAccessToken: ", e);
        }
        return null;
    }
}
