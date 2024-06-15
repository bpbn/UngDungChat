package didong.ungdungchat.Services;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SendNotification {
    private final String userToken;
    private final String title;
    private final String body;
    private final Context context;
    private final String postUrl = "https://fcm.googleapis.com/v1/projects/ungdungchat-9ea1c/messages:send";
    public SendNotification(String userToken, String title, String body, Context context) {
        this.userToken = userToken;
        this.title = title;
        this.body = body;
        this.context = context;
    }

    public void send() {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject main = new JSONObject();
        try {
            JSONObject message = new JSONObject();
            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", body);
            message.put("token", userToken);
            message.put("notification", notification);
            main.put("message", message);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(postUrl, main, response -> {

            }, error -> {
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    AccessToken accessToken = new AccessToken();
                    String token = accessToken.getAccessToken();
                    Map<String, String> headers = new HashMap<>();
                    headers.put("content-type", "application/json");
                    headers.put("authorization", "Bearer " + token);
                    return headers;
                }
            };

            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            Log.e("SendNotification", e.getMessage());
        }
    }
}
