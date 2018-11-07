import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/*
 * Quickly made Discord bot to view live results for the US 2018 Midterms.
 *
 * */
public class Main {

    public static String DISCORD_API_KEY = "";
    public static IDiscordClient discordClient;

    public static final String HOUSE_KEY = "H";
    public static final String SENATE_KEY = "S";
    public static final String GOV_KEY = "G";

    public static final String DEM_COUNT_KEY = "d";
    public static final String REP_COUNT_KEY = "r";

    public static final String DATA_JSON = "https://data.cnn.com/ELECTION/2018November6/bop/combined.json";


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Missing API Key!");
            return;
        }

        DISCORD_API_KEY = args[0];

        discordClient = new ClientBuilder().withRecommendedShardCount().withToken(DISCORD_API_KEY).login();
        discordClient.getDispatcher().registerListener(new DiscordListener());

        System.out.println("DEMS: " + getCount(HOUSE_KEY, DEM_COUNT_KEY));
        System.out.println("REPS: " + getCount(HOUSE_KEY, REP_COUNT_KEY));

    }


    public static String getJsonResponse() {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(DATA_JSON).openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            return response.toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * @param race,    HOSUE_INDEX, SENATE_KEY, GOV_KEY
     * @param countKey DEM_COUNT_KEY, REP_COUNT_KEY
     */
    public static int getCount(String race, String countKey) {


        JsonParser jsonParser = new JsonParser();
        String response = getJsonResponse();
        if (response == null) return 0;

        JsonObject jsonObject = jsonParser.parse(getJsonResponse()).getAsJsonObject();

        return 0;
    }

    public static class DiscordListener {

        @EventSubscriber
        public void onReady(ReadyEvent readyEvent) {
            System.out.println("Hello World!");
        }
    }
}
