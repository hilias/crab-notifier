package com.crabnotifier.discord;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.crabnotifier.CrabNotifierConfig;
import com.crabnotifier.discord.responses.CreateChannelResponse;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

public class DiscordClient {
    private final OkHttpClient client;

    private final String baseURL = "https://discord.com/api/v10";
    private String channelId;
    private CrabNotifierConfig config;

    public DiscordClient(CrabNotifierConfig config) {
        this.config = config;
        this.client = new OkHttpClient();
    }

    public void sendMessage(String message, String userId) {
        if (channelId == null) {
            channelId = createChannel(userId);
        }
        if (channelId == null) {
            return;
        }

        sendRequest("/channels/" + channelId + "/messages", "{\"content\": \"" + message + "\"}");
    }


    public String createChannel(String userId) {
        String body = sendRequest("/users/@me/channels", "{\"recipient_id\": " + userId + "}");
        if (body == null) {
            return null;
        }

        Gson gson = new Gson();
        CreateChannelResponse response = gson.fromJson(body, CreateChannelResponse.class);

        return response.id;
    }

    private String sendRequest(String path, String body) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);

        System.out.println("Sending request to: " + baseURL + path);

        Request request = new Request.Builder()
                .url(baseURL + path)
                .header("Authorization", "Bot " + config.discordToken())
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if  (response.isSuccessful()) {
                return response.body().string();
            } else {
                return null;
            }
        } catch (Exception e) {
            Logger.getLogger(DiscordClient.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }
}
