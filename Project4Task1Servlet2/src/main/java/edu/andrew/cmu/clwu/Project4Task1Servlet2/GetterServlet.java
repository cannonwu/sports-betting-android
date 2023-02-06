// Cannon Wu
// AndrewID: clwu

package edu.andrew.cmu.clwu.Project4Task1Servlet2;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@WebServlet(name = "getterServlet", urlPatterns = "/sport")
public class GetterServlet extends HttpServlet {
    private String message;

    public void init() {
        message = "";
    }

    // Receive HTTP Request from Android Client
    // Extract name parameter from Android Client Request
    // Create API Request using name parameter to sports data aggregator API: https://the-odds-api.com/
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("GETTING");
        System.out.println(request.getParameter("name"));
        // Extract name parameter from request
        String sportName = request.getParameter("name");

        // Create API Get Request URL with endpoint, developer key, string from Android Client
        String apiURL = "https://api.the-odds-api.com/v4/sports/" + sportName +
                "/odds/?apiKey=fa3e3c2270200bebc6117c845d736b13&regions=us&markets=h2h";
        //String apiURL = "https://api.the-odds-api.com/v4/sports/?apiKey=fa3e3c2270200bebc6117c845d736b13";

        // Initialize variables to hold data returned by API
        StringBuilder apiReply = new StringBuilder();
        String homeTeam = null;
        String awayTeam = null;
        Date gameTime = null;

        // Code from: https://github.com/CMU-Heinz-95702/lab7-rest-programming/blob/main/README.md
        // Make URL Connection to API and read response
        try {
            /*
             * Create an HttpURLConnection.  This is useful for setting headers
             * and for getting the path of the resource that is returned (which
             * may be different than the URL above if redirected).
             * HttpsURLConnection (with an "s") can be used if required by the site.
             */
            // Create URL to send to API
            System.out.println("Creating servlet connection");
            URL url = new URL(apiURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            connection.setRequestProperty("Accept", "text/plain");
            System.out.println("Servlet connection done");

            // Read all JSON-formatted text data returned by the API
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String str = "";
            // Read each line of "in" until done, adding each to "response"
            while ((str = in.readLine()) != null) {
                // str is one line of text readLine() strips newline characters
                apiReply.append(str);
            }
            in.close();

            System.out.println(apiReply);

            // Convert apiReply to JSON Array object
            Gson gson = new Gson();

            // Parse JSON data to only extract what we want: homeTeam, awayTeam, gameTime
            // Code from: https://bytesofgigabytes.com/java/how-to-convert-string-to-json-array-in-java/
            JSONParser parser = new JSONParser();
            try {
                Object object = (Object) parser.parse(apiReply.toString());
                JSONArray jsonArray = (JSONArray) object;

                // Loop through JSON Array, create data map to extract wanted data
                for(int i=0; i<jsonArray.size();i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
                    for(Map.Entry<String, JsonElement> entry : entries) {
                        System.out.println(entry.getKey() + " -> "+entry.getValue());
                        String s = entry.getKey() + " -> "+entry.getValue();
                        // Use regex to identify wanted data and store it in respective variables
                        if (s.matches("home_team(.*)")) {
                            homeTeam = s.substring(13);
                        }
                        if (s.matches("away_team(.*)")) {
                            awayTeam = s.substring(13);
                        }
                        if (s.matches("commence_time(.*)")) {
                            String time = s.substring(17);
                            // Code from https://stackoverflow.com/questions/19112357/java-simpledateformatyyyy-mm-ddthhmmssz-gives-timezone-as-ist
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                            gameTime = sdf.parse(time);
                        }
                        if (homeTeam != null && awayTeam != null && gameTime != null) {
                            break;
                        }
                    }
                }
            // Catch Exceptions
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }

            // Print to console response status
            System.out.println(homeTeam);
            System.out.println(awayTeam);
            System.out.println(gameTime);
            System.out.println("Response captured");

        } catch (IOException e) {
            System.out.println("Eeek, an exception");
        }

        // Convert servlet response to JSON-formatted string (general msg, homeTeam, awayTeam, gameTime)
        String jsonResponse =
                "{\"message\":" + 0 + "," +
                "\"homeTeam\":\"" + homeTeam + "\"," +
                "\"awayTeam\":\"" + awayTeam + "\"," +
                "\"gameTime\":\"" + gameTime + "\"}";

        // Send response to client
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        out.print(jsonResponse);
        out.flush();
    }

    public void destroy() {
    }
}