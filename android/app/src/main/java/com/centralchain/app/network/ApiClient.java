package com.centralchain.app.network;

import android.content.Context;

import org.json.JSONObject;

import java.util.Iterator;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// sets up retrofit
public class ApiClient {

    // backend hosted on ngrok
    // for a local backend use "http://10.0.2.2:8080/"
    public static final String BASE_URL = "https://parting-elope-defuse.ngrok-free.dev/";
    // live chat goes through the websocket
    public static final String SOCKET_URL = BASE_URL.replace("https://", "wss://").replace("http://", "ws://") + "ws/websocket";

    private static ApiService apiService;

    public static ApiService getApi(Context context) {
        if (apiService == null) {
            apiService = buildRetrofit(BASE_URL, context.getApplicationContext()).create(ApiService.class);
        }
        return apiService;
    }

    static Retrofit buildRetrofit(String baseUrl, Context context) {
        // add the token to every request
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    // skip the ngrok warning page
                    Request request = chain.request().newBuilder()
                            .addHeader("ngrok-skip-browser-warning", "true")
                            .build();
                    String token = null;
                    if (context != null) {
                        token = SessionManager.getToken(context);
                    }
                    if (token != null) {
                        request = request.newBuilder()
                                .addHeader("Authorization", "Bearer " + token)
                                .build();
                    }
                    return chain.proceed(request);
                })
                .build();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // get the error message from the api
    public static String getErrorMessage(Response<?> response) {
        // the backend sends 401 (not 403) when your role is not allowed
        if (response.code() == 401 || response.code() == 403) {
            return "Your role can't do this yet, or your login has expired";
        }
        try {
            String body = response.errorBody().string();
            JSONObject json = new JSONObject(body);
            if (json.has("message") && !json.getString("message").isEmpty()) {
                return json.getString("message");
            }
            // form errors come back as a list of fields
            if (json.has("errors")) {
                JSONObject errors = json.getJSONObject("errors");
                Iterator<String> fields = errors.keys();
                if (fields.hasNext()) {
                    return errors.getString(fields.next());
                }
            }
        } catch (Exception e) {
            // not json
        }
        return "Something went wrong (error " + response.code() + ")";
    }
}
