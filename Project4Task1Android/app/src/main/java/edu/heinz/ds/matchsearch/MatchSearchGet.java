// Cannon Wu
// AndrewID: clwu

package edu.heinz.ds.matchsearch;

import android.app.Activity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.google.gson.Gson;

// Code from https://github.com/CMU-Heinz-95702/lab8-Android
/* This class connects to a servlet which connects to a
 *    sports data aggregator API with a user-defined name parameter
 * Method BackgroundTask.doInBackground( ) does the background work
 * Method BackgroundTask.onPostExecute( ) is called when the background work is
 *    done; it calls *back* to report the results
 *
 */
public class MatchSearchGet {
    MatchSearchView ip = null;   // for callback
    String searchTerm = null;       // search sports data API thru servlet for this name

    // initialize communication variables
    double message = 10;
    String homeTeam = "";
    String awayTeam = "";
    String gameTime = "";

    // search( )
    // Parameters:
    // String searchTerm: the thing to search for
    // Activity activity: the UI thread activity
    // InterestingPicture ip: the callback method's class; here, it will be ip.pictureReady( )
    public void search(String searchTerm, Activity activity, MatchSearchView ip) {
        this.ip = ip;
        this.searchTerm = searchTerm;
        new BackgroundTask(activity).execute();
    }

    // class BackgroundTask
    // Implements a background thread. Adapted from one of the answers in:
    // https://stackoverflow.com/questions/58767733/the-asynctask-api-is-deprecated-in-android-11-what-are-the-alternatives
    // Modified by Prof. Barrett (cited above class)
    // The call to activity.runOnUiThread( ) is an Android Activity method that
    //    somehow "knows" to use the UI thread, even if it appears to create a
    //    new Runnable.

    private class BackgroundTask {

        private Activity activity; // The UI thread

        public BackgroundTask(Activity activity) {
            this.activity = activity;
        }

        private void startBackground() {
            new Thread(new Runnable() {
                public void run() {

                    doInBackground();
                    // Activity should be set to MainActivity.this
                    //    then this method uses the UI thread
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            onPostExecute();
                        }
                    });
                }
            }).start();
        }

        private void execute(){
            startBackground();
        }

        // doInBackground( ) implements background thread actions.
        private void doInBackground() {
            // Make connection to heroku-hosted servlet, which will call the API with the searchTerm
            search(searchTerm);
        }

        // onPostExecute( ) will run on the UI thread after the background
        //    thread completes.
        public void onPostExecute() {
            // Pass newly parsed JSON text data back to the UI thread to update visuals
            ip.dataReady(message,homeTeam,awayTeam,gameTime);
        }

        /*
         * Search API (thru servlet) for the searchTerm argument, and return textual data
         */
        private void search(String searchTerm) {

            // Send URL to Servlet
            String response = "";
            try {
                /*
                 * Create an HttpURLConnection.  This is useful for setting headers
                 * and for getting the path of the resource that is returned (which
                 * may be different than the URL above if redirected).
                 * HttpsURLConnection (with an "s") can be used if required by the site.
                 */
                // Create URL to send to web servlet
                System.out.println("Creating servlet connection");
                String servletURL =
                        "https://fierce-wildwood-03368.herokuapp.com/sport?name="
                        + searchTerm;     // add parameter to heroku app URL

                // Code from: https://github.com/CMU-Heinz-95702/lab7-rest-programming/blob/main/README.md
                URL url = new URL(servletURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
                connection.setRequestProperty("Accept", "text/plain");
                System.out.println("Servlet connection done");

                // Read all the text returned by the server
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                String str = "";
                // Read each line of "in" until done, adding each to "response"
                while ((str = in.readLine()) != null) {
                    // str is one line of text readLine() strips newline characters
                    response += str;
                }
                in.close();

                System.out.println("Printing Servlet Response: " + response);

                // Modify class variables to hold the 4 pieces of the returned JSON.
                // After execution, these strings will be passed back to the UI thread.
                Gson gson = new Gson();
                Map responseMap = gson.fromJson(response, Map.class);
                message = (double) responseMap.get("message");
                homeTeam = responseMap.get("homeTeam").toString();
                awayTeam = responseMap.get("awayTeam").toString();
                gameTime = responseMap.get("gameTime").toString();

            // Handle exception
            } catch (IOException e) {
                System.out.println("Eeek, an exception");
            }
        }
    }
}

