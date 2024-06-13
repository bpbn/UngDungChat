package didong.ungdungchat.Services;

import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.auth.GoogleAuthCredential;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AccessToken {
    public static final String firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging";

    public String getAccessToken() {
        try {
            String jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"ungdungchat-9ea1c\",\n" +
                    "  \"private_key_id\": \"0f8237bb5ba1801a68aa6f29310fb0dbcae2a6fc\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDfDsooTv/aJBDg\\n74QK4Astp0HkfPqtxC0CRhV+YmZ8e8EOB45I9PNyPXGzAWBmF/qb/rAYRnTwZIvd\\n50+eq9b8NshFbQPjvDYrmZJXmFjtxle1fnZif0w4NtxJ4mZ0eysXNYqZ1xvWuERj\\nVDJsDswCRish9eMsh3TgU2TFQIf7JZbvzPwCdAbD+Ksar4ZmavKPI9ZSRafsp+rH\\ngkoNVma/bFkXI2o4FALH5CWQvafNassFseywDws/nUPVTRIeLybxR+4Qb2F/KaTI\\nqDrdQi+G9PEBSF7Ga/8CkragY/83cHzfOyUj+mhXizf0rSzgenfMmMlbEY0b8Ohg\\nBcwmrfkjAgMBAAECggEAAnZ+Y8oGLiEXb2FUHsPDSYxswE4+rfO7nQPEIoM0Aq3D\\ngplJ/y9F9vTCiKBm4i2B+6LEdv0a1XB6lARIGerhhoS5qiFvKJudHL6CfW6iJqQM\\nGNOWWeaKfs8RmF58uZeSj187+FmMEMMPVRr1RRc3yExcI0ZVF85MafkaIaTzzznh\\nWxSzUhxxUH/F0FZ02LbZLI30Q3ncgpeafkydpPLdC6Kqr12jK3tjJyZtFPTjLLYs\\ntBOUQZ7V02GTKt9Xt2XtmYi+cBDCrfmEpd1a9VvkalEZTWXUCj47JHRfDYTmpAb7\\n5rc6okSgjGCAqnScj62QRDp3xK6PMaOP3L+KR27TwQKBgQD/1hl2ySVxoZLby6Gk\\n01cLsNgdHsV5SD40XitOLfXtD/91ESoL0tqlc6QLSxIZm/3K1Ntwa3GO4Ul7r+/u\\ncLGcGT/dLxN5ZIRp6C4uqONaaLcwd2pBSwgkHl51URo9vgOHml7SG0K55J/TeFUx\\ntBYNU/RJbvxRxpQ/1z9UbSGeiwKBgQDfM1JgPrbUeyo7FmvwBN26rBCJ3KEjkLIs\\nRUQz/afff2nYlwKDIRFqcp88uQMtq2Oa6+/FsuknfIazO5bZdZWmhxSK1RvcsQx7\\nLe+dZ6dRm4P37DjY7M5fxS0m1P+kTf5Elm+f7lyFurfgpIaOT0mu6HrJ8YbBBlqT\\nzew2DLc6yQKBgHzWEQjYb+/5onqj65VNFt16hon3GO7sZIYTKVh1rtnlg4axQG4S\\njmRLgFGOaR+b8YRGx2kc1IWewsea+x3DWbdMzs/0B7GlbcNr6xUTjhTwoesBahTA\\nMxHEy9y9HWvhS0Gg/wHREYc52w9KWW/uaxBH1hz7Dh4tIuvf8WQV+GCvAoGBAN7k\\n2qPAnUZOBRtixcG4qlDr2cxC5yykKDorpfOhoQ6BqAsiquCWmlTKSJkUTcdCu8KP\\nUTAF/e5v3fiJTpi1YxAdH3eRYDShHky4z+xKAf5uJnjkjtSaj3F2cCOyW+UR+hU2\\nghydpgnqMAyHYNQm+/dOtKiMqTyEAtpNr1E+m5B5AoGBAJIJ/m/EI3SQ6eatD2jq\\njhWtO1+CwYixGFtsUYBdAQGpSoloB8QdqPobaT620jtkcTxmYXqDqI2PCAopmD1p\\nW6gs4v+S94wz9pVsM7zucPQdqGL48uuIC6RKNtDpxWtqnKmKw1URBlW2d4PLeLRG\\ne2znUNYkjBY9N0qTFIPWHa30\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-ng578@ungdungchat-9ea1c.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"117090718346412241627\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-ng578%40ungdungchat-9ea1c.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n";

            InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream).createScoped(firebaseMessagingScope);
            credentials.refresh();
            return credentials.refreshAccessToken().getTokenValue();

        }catch (Exception e) {
            Log.e("AccessToken", "getAccessToken: ", e);
        }
        return null;
    }
}
