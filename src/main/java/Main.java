import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;


/*
 * Quickly made Discord bot to view live results for the US 2018 Midterms.
 *
 * */
public class Main {

    // Discord
    public static String DISCORD_API_KEY = "";
    public static IDiscordClient discordClient;
    public static List<Long> DISCORD_CHANNEL_IDS = new ArrayList<>();

    // Data
    public static final String HOUSE_KEY = "H";
    public static final String SENATE_KEY = "S";
    public static final String GOV_KEY = "G";
    public static final String DEM_COUNT_KEY = "d";
    public static final String REP_COUNT_KEY = "r";
    public static final String DATA_JSON = "https://data.cnn.com/ELECTION/2018November6/bop/combined.json";
    public static final String COVERAGE = "https://www.cnn.com/politics/live-news/election-day-2018/index.html";

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Missing API Key and Channel ID(s)!");
            return;
        }

        for (int i = 1; i < args.length; i++) {
            DISCORD_CHANNEL_IDS.add(Long.parseLong(args[i]));
        }

        DISCORD_API_KEY = args[0];

        discordClient = new ClientBuilder().withRecommendedShardCount().withToken(DISCORD_API_KEY).login();
        discordClient.getDispatcher().registerListener(new DiscordListener());
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
    public static int getCount(String response, String race, String countKey) {
        JsonParser jsonParser = new JsonParser();
        if (response == null) return 0;

        JsonObject jsonObject = jsonParser.parse(getJsonResponse()).getAsJsonObject();

        return jsonObject.get(race).getAsJsonObject().get(countKey).getAsInt();
    }

    public static void sendMessage(long channel_id, EmbedObject embedObject) {
        RequestBuffer.request(() -> {
            discordClient.getChannelByID(channel_id).sendMessage(embedObject);
        });
    }

    public static class DiscordListener {

        @EventSubscriber
        public void onReady(ReadyEvent readyEvent) {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {

                // Do this so only changes are displayed.
                int lastHouseDems = 0;
                int lastHouseReps = 0;

                int lastSenateDems = 0;
                int lastSenateReps = 0;

                @Override
                public void run() {
                    String response = getJsonResponse();

                    int houseDems = getCount(response, HOUSE_KEY, DEM_COUNT_KEY);
                    int houseReps = getCount(response, HOUSE_KEY, REP_COUNT_KEY);

                    int senateDems = getCount(response, SENATE_KEY, DEM_COUNT_KEY);
                    int senateReps = getCount(response, SENATE_KEY, REP_COUNT_KEY);

                    if (houseDems == lastHouseDems && houseReps == lastHouseReps && senateDems == lastSenateDems && senateReps == lastSenateReps) {
                        return; // No changes. So don't display anything.
                    }

                    lastHouseDems = houseDems;
                    lastHouseReps = houseReps;
                    lastSenateDems = senateDems;
                    lastSenateReps = senateReps;

                    System.out.println("==========================");
                    System.out.println("House Race");
                    System.out.println("Dems: " + houseDems);
                    System.out.println("Reps: " + houseReps);

                    System.out.println("Senate Race");
                    System.out.println("Dems: " + senateDems);
                    System.out.println("Reps: " + senateReps);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                    String time = dateFormat.format(new Date());

                    EmbedBuilder embedBuilder = new EmbedBuilder().withTitle("US Midterm Elections")
                            .appendField("House Race",
                                    "Dems: " + houseDems + "\n" +
                                            "Reps: " + houseReps, true)
                            .appendField("Senate Race",
                                    "Dems: " + senateDems + "\n" +
                                            "Reps: " + senateReps, true)
                            .withFooterText(time).withUrl(COVERAGE);

                    for (long channel : DISCORD_CHANNEL_IDS) {
                        sendMessage(channel, embedBuilder.build());
                    }
                }
            }, 1000, 1000 * 60 * 1); // Check every 5 mins.
        }
    }
}
