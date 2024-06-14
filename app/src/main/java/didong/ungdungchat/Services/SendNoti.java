package didong.ungdungchat.Services;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SendNoti {

    Interceptor interceptor = chain -> {
        Request request = chain.request().newBuilder()
                .addHeader("content-type", "application/json")
                .build();
        return chain.proceed(request);
    };

    OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().addInterceptor(interceptor);
    SendNoti sendNoti = new Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/v1/projects/ungdungchat-9ea1c/messages:send")
            .client(okHttpClientBuilder.build())
            .build()
            .create(SendNoti.class);

    @POST()
    void sendNoti(@Header("Authorization") String key, @Body String title, @Body String body, @Body String token, @Body String notification);
}
