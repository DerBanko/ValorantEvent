package tv.banko.valorantevent.api;

import okhttp3.*;
import org.json.JSONObject;
import tv.banko.valorantevent.tournament.rank.Rank;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class RankAPI {

    private final static String URL = "https://api.henrikdev.xyz/valorant/v1/mmr/eu/%s/%s";
    private final OkHttpClient client;

    public RankAPI() {
        client = new OkHttpClient();
    }

    /**
     * @param nameWithTag For example: User#EUW
     * @return a response
     */
    public CompletableFuture<RankAPIResponse> getRank(String nameWithTag) {

        CompletableFuture<RankAPIResponse> future = new CompletableFuture<>();

        if (!nameWithTag.contains("#")) {
            throw new IllegalArgumentException("nameWithTag doesn't contain a # (" + nameWithTag + ")");
        }

        new Thread(() -> {

            String[] nameArray = nameWithTag.split("#");

            Request request = new Request.Builder()
                    .url(String.format(URL, nameArray[0], nameArray[1]))
                    .build();

            Call call = client.newCall(request);

            try {
                Response response = call.execute();

                if (response.code() != 200) {
                    future.completeAsync(() -> new RankAPIResponse(response.code(), null));
                    return;
                }

                ResponseBody body = response.body();

                if (body == null) {
                    future.completeAsync(() -> new RankAPIResponse(404, null));
                    return;
                }

                JSONObject json = new JSONObject(body.string());

                if(json.getInt("status") != 200) {
                    future.completeAsync(() -> new RankAPIResponse(json.getInt("status"), null));
                    return;
                }

                JSONObject data = json.getJSONObject("data");

                Rank rank = Rank.byId(data.getInt("currenttier"));

                future.completeAsync(() -> new RankAPIResponse(200, rank));
            } catch (IOException e) {
                e.printStackTrace();
                future.completeAsync(() -> new RankAPIResponse(999, null));
            }
        }).start();

        return future;
    }

    public static record RankAPIResponse(int status, Rank rank) {
    }
}
