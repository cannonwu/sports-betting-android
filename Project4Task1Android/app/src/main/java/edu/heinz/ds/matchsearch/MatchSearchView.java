// Cannon Wu
// AndrewID: clwu

package edu.heinz.ds.matchsearch;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// Code from https://github.com/CMU-Heinz-95702/lab8-Android
public class MatchSearchView extends AppCompatActivity {

    MatchSearchView me = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
         * The click listener will need a reference to this object, so that upon successfully finding data from the API,
         * It can callback to this object with the results  The "this" of the OnClick will be the OnClickListener, not
         * this MatchSearchView.
         */
        final MatchSearchView ma = this;

        /*
         * Find the "submit" button, and add a listener to it
         */
        Button submitButton = (Button)findViewById(R.id.submit);

        // Add a listener to the send button
        submitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View viewParam) {
                String searchTerm = ((EditText)findViewById(R.id.searchTerm)).getText().toString();
                System.out.println("searchTerm = " + searchTerm);
                MatchSearchGet gp = new MatchSearchGet();
                gp.search(searchTerm, me, ma); // Done asynchronously in another thread.  It calls ip.dataReady() in this thread when complete.
            }
        });
    }

    /*
     * This is called by the MatchSearchGet object when the data is ready.  This allows for passing back the String data for updating the TextView
     */
    public void dataReady(double message, String homeTeam, String awayTeam, String gameTime) {
        // Create references to each UI element that needs to be updated
        TextView searchView = (EditText)findViewById(R.id.searchTerm);
        TextView resultView = (TextView) findViewById(R.id.textView2);
        TextView feedBack = (TextView) findViewById(R.id.textView);
        if (message == 0) {
            System.out.println("UI RECEIVED DATA");
            // Update free-standing TextView
            feedBack.setText(new StringBuilder().append("Here is the next upcoming game for the sport key: ").append(((EditText)findViewById(R.id.searchTerm)).getText().toString()).toString());
            // Update TextView that is contained in a CardView
            resultView.setText(new StringBuilder()
                    .append("\nHome Team: ").append(homeTeam)
                    .append("\nAway Team: ").append(awayTeam)
                    .append("\nDate: ").append(gameTime)
                    .toString());
        } else {
            // Handle exception
            System.out.println("NOT RECEIVED");
            feedBack.setText(new StringBuilder().append("Sorry, I could not find an upcoming game for ").append(((EditText)findViewById(R.id.searchTerm)).getText().toString()).toString());
        }
        // Reset search bar text
        searchView.setText("");
    }
}
