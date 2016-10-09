package uk.ac.mmu.webmd.orgillv7;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Samuel Orgill 15118305
 * @version 7, 25/3/2016
 * Main Activity with methods for getting the location, searching the ratings database
 * and returning results to the users device.
 */

public class MainActivity extends AppCompatActivity {

    TableLayout tabLay;
    TableRow tabRow;
    TextView label1, label2, label3;

    EditText display;

    private ImageView ratingView;
    private LocationManager locMan;
    private LocationListener locList;
    private Location mobLoc;
    private ToggleButton tglBtn;

    String longi, lati;
    Context context;

    private static ImageButton clkBtn;

    String out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLay = (TableLayout) findViewById(R.id.tableLayout);
        tabRow = (TableRow) findViewById(R.id.tableRow);
        label2 = (TextView) findViewById(R.id.textView);
        label3 = (TextView) findViewById(R.id.textView);
        display = (EditText) findViewById(R.id.editText);
        ratingView = (ImageView) findViewById(R.id.imageView);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * onCurrentLoc() method gets the devices current location and returns
     * the latitude and longitude.
     */

    /*Gets users location via GPS*/
    private void getCurrentLoc() {
        display.setText("");
        context = this.getApplicationContext();
        locMan = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locList = new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

            @Override
            public void onLocationChanged(Location location) {

                mobLoc = location;
            }
        };
        try {
            locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locList);
        }catch (SecurityException e) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Gets and returns longitude
     * @return longitude
     */
    public String getLon(){
        getCurrentLoc();


        try {
            if (mobLoc != null) {
                //Removes updates and saves battery power
                locMan.removeUpdates(locList);

                double lon = mobLoc.getLongitude();


                longi = Double.toString(lon);


            } else {
                longi = "Soz";
            }
        } catch (SecurityException e) {
            throw new ArrayIndexOutOfBoundsException();
        }

        return longi;

    }

    /**
     * Gets and returns latitude
     * @return latitude
     */
    public String getLat(){
        getCurrentLoc();


        try {
            if (mobLoc != null) {
                //Removes updates and saves battery power
                locMan.removeUpdates(locList);

                double lat = mobLoc.getLatitude();


                lati = Double.toString(lat);


            } else {
                lati = "Soz";
            }
        } catch (SecurityException e) {
            throw new ArrayIndexOutOfBoundsException();
        }

        return lati;

    }

    /**
     * Gets the nearest takeaways to the users location
     * @param v
     */
    public void onClickNearest(View v) {

        ArrayList<String> listID = new ArrayList<String>();
        ArrayList<String> listName = new ArrayList<String>();
        ArrayList<String> listRatings = new ArrayList<String>();
        ArrayList<String> listAddress = new ArrayList<String>();
        ArrayList<String> listDist = new ArrayList<String>();
        ArrayList<String> listAddress2 = new ArrayList<String>();

        try {

            String s1 = "http://sandbox.kriswelsh.com/hygieneapi/hygiene.php?op=s_loc&lat=";
            String s2 = "&long=";


            URL url = new URL(s1 + getLat() + s2 + getLon());
            HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();

            InputStreamReader isr = new InputStreamReader(urlCon.getInputStream());

            BufferedReader br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null) {

                JSONArray jArray = new JSONArray(line);

                for (int i = 0; i < jArray.length(); i++) {

                    JSONObject jObject = (JSONObject) jArray.get(i);
                    listID.add(jObject.getString("id"));
                    listName.add(jObject.getString("BusinessName"));
                    if(jObject.getString("AddressLine1").isEmpty()) {
                        String address = jObject.getString("AddressLine2");

                        listAddress.add(address);
                    } else {
                        String address = jObject.getString("AddressLine1") + ", " +
                                jObject.getString("AddressLine2");
                        listAddress.add(address);
                    }

                    if(jObject.getString("AddressLine3").isEmpty()) {
                        String address2 = jObject.getString("PostCode");
                        listAddress2.add(address2);
                    } else{
                        String address2 = jObject.getString("AddressLine3") + ", " +
                                jObject.getString("PostCode");
                        listAddress2.add(address2);
                    }

                    String rating = jObject.getString("RatingValue");
                    if(Objects.equals(rating, "-1")){
                        listRatings.add("Rating: " + "Exempt");
                    }else{
                        listRatings.add("Rating: " + rating);
                    }

                    String dist = jObject.getString("DistanceKM");
                    BigDecimal bd = new BigDecimal(dist);
                    bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
                    listDist.add(bd + " km away");

                }
            }
        } catch (SecurityException e) {
            throw new ArrayIndexOutOfBoundsException();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tabLay.removeAllViews();

        for (int j = 0; j < listName.size(); j++) {

            String theRating = listRatings.get(j);

            label1 = new TextView(this);
            ratingView = new ImageView(this);


            label1.setText(listName.get(j) + "\n"
                    + listAddress.get(j) + "\n" +
            listRatings.get(j) + " > " + listDist.get(j) + "\n ");

            ratingView.getResources().getDrawable(R.drawable.fivesmall);


            tabRow = new TableRow(this);

            label1.setTextSize(11);
            label1.setAllCaps(true);
            label1.setMinimumWidth(550);
            label1.setMaxWidth(550);


            tabRow.addView(label1);

            if(theRating.equals("Rating: 5")){
                ratingView.setImageResource(R.drawable.fivesmall);

                tabRow.addView(ratingView);
            } else if(theRating.equals("Rating: 4")){
                ratingView.setImageResource(R.drawable.foursmall);
                tabRow.addView(ratingView);
            } else if(theRating.equals("Rating: 3")) {
                ratingView.setImageResource(R.drawable.threesmall);
                tabRow.addView(ratingView);
            } else if(theRating.equals("Rating: 2")) {
                ratingView.setImageResource(R.drawable.twosmall);
                tabRow.addView(ratingView);
            } else if(theRating.equals("Rating: 1")) {
                ratingView.setImageResource(R.drawable.onesmall);
                tabRow.addView(ratingView);
            }else if(theRating.equals("Rating: 0")) {
                ratingView.setImageResource(R.drawable.zerosmall);
                tabRow.addView(ratingView);
            } else if (theRating.equals("Rating: Exempt")) {
                ratingView.setImageResource(R.drawable.exempt);
                tabRow.addView(ratingView);
            }

            tabLay.addView(tabRow);

        }

    }

    /**
     * Gets the ratings by postcode or name
     * @param v
     */
    public void onClickSearch(View v) {

        tglBtn = (ToggleButton) findViewById(R.id.toggleButton);

        if(tglBtn.isChecked()) {

            ArrayList<String> listID = new ArrayList<String>();
            ArrayList<String> listName = new ArrayList<String>();
            ArrayList<String> listRatings = new ArrayList<String>();
            ArrayList<String> listAddress = new ArrayList<String>();
            ArrayList<String> listAddress2 = new ArrayList<String>();



            try {

                String s1 = "http://sandbox.kriswelsh.com/hygieneapi/hygiene.php?op=s_postcode&postcode=";

                out = display.getText().toString();

                URL url = new URL(s1 + out);
                HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();

                InputStreamReader isr = new InputStreamReader(urlCon.getInputStream());

                BufferedReader br = new BufferedReader(isr);

                String line;
                while ((line = br.readLine()) != null) {

                    JSONArray jArray = new JSONArray(line);

                    for (int i = 0; i < jArray.length(); i++) {

                        JSONObject jObject = (JSONObject) jArray.get(i);

                        listID.add(jObject.getString("id"));
                        listName.add(jObject.getString("BusinessName"));
                        if(jObject.getString("AddressLine1").isEmpty()) {
                            String address = jObject.getString("AddressLine2");

                            listAddress.add(address);
                        } else {
                            String address = jObject.getString("AddressLine1") + ", " +
                                    jObject.getString("AddressLine2");
                            listAddress.add(address);
                        }

                        if(jObject.getString("AddressLine3").isEmpty()) {
                            String address2 = jObject.getString("PostCode");
                            listAddress2.add(address2);
                        } else{
                            String address2 = jObject.getString("AddressLine3") + ", " +
                                    jObject.getString("PostCode");
                            listAddress2.add(address2);
                        }
                        String rating = jObject.getString("RatingValue");
                        if(Objects.equals(rating, "-1")){
                            listRatings.add("Rating: " + "Exempt");
                        }else{
                            listRatings.add("Rating: " + rating);
                        }
                    }
                }
            } catch (SecurityException e) {
                throw new ArrayIndexOutOfBoundsException();
            } catch (Exception e) {
                e.printStackTrace();
            }

            tabLay.removeAllViews();

            for (int j = 0; j < listName.size(); j++) {

                String theRating = listRatings.get(j);

                label1 = new TextView(this);
                ratingView = new ImageView(this);

                label1.setText(listName.get(j) + "\n"
                        + listAddress.get(j) + "\n" +
                        listAddress2.get(j) + "\n" +
                        listRatings.get(j) + "\n");

                tabRow = new TableRow(this);

                label1.setTextSize(11);
                label1.setAllCaps(true);
                label1.setMaxWidth(550);
                label1.setMinimumWidth(550);


                tabRow.addView(label1);

                if(theRating.equals("Rating: 5")){
                    ratingView.setImageResource(R.drawable.fivesmall);

                    tabRow.addView(ratingView);
                } else if(theRating.equals("Rating: 4")){
                    ratingView.setImageResource(R.drawable.foursmall);
                    tabRow.addView(ratingView);
                } else if(theRating.equals("Rating: 3")) {
                    ratingView.setImageResource(R.drawable.threesmall);
                    tabRow.addView(ratingView);
                } else if(theRating.equals("Rating: 2")) {
                    ratingView.setImageResource(R.drawable.twosmall);
                    tabRow.addView(ratingView);
                } else if(theRating.equals("Rating: 1")) {
                    ratingView.setImageResource(R.drawable.onesmall);
                    tabRow.addView(ratingView);
                }else if(theRating.equals("Rating: 0")) {
                    ratingView.setImageResource(R.drawable.zerosmall);
                    tabRow.addView(ratingView);
                } else if (theRating.equals("Rating: Exempt")) {
                    ratingView.setImageResource(R.drawable.exempt);
                    tabRow.addView(ratingView);
                }

                tabLay.addView(tabRow);

            }

        } else {

            ArrayList<String> listID = new ArrayList<String>();
            ArrayList<String> listName = new ArrayList<String>();
            ArrayList<String> listRatings = new ArrayList<String>();
            ArrayList<String> listAddress = new ArrayList<String>();
            ArrayList<String> listAddress2 = new ArrayList<String>();

            try {

                String s1 = "http://sandbox.kriswelsh.com/hygieneapi/hygiene.php?op=s_name&name=";

                out = display.getText().toString();

                String newURL =  s1 + Uri.encode(out);

                URL url = new URL(newURL);
                HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();

                InputStreamReader isr = new InputStreamReader(urlCon.getInputStream());

                BufferedReader br = new BufferedReader(isr);

                String line;
                while ((line = br.readLine()) != null) {

                    JSONArray jArray = new JSONArray(line);

                    for (int i = 0; i < jArray.length(); i++) {
                        // the jArray.get(i) does iterate over the objects
                        // by changing the number we get the object from the array
                        JSONObject jObject = (JSONObject) jArray.get(i);

                        listID.add(jObject.getString("id"));
                        listName.add(jObject.getString("BusinessName"));

                        if(jObject.getString("AddressLine1").isEmpty()) {
                            String address = jObject.getString("AddressLine2");

                            listAddress.add(address);
                        } else {
                            String address = jObject.getString("AddressLine1") + ", " +
                                    jObject.getString("AddressLine2");
                            listAddress.add(address);
                        }

                        if(jObject.getString("AddressLine3").isEmpty()) {
                            String address2 = jObject.getString("PostCode");
                            listAddress2.add(address2);
                        } else{
                            String address2 = jObject.getString("AddressLine3") + ", " +
                                    jObject.getString("PostCode");
                            listAddress2.add(address2);
                        }
                        String rating = jObject.getString("RatingValue");
                        if(Objects.equals(rating, "-1")){
                            listRatings.add("Rating: " + "Exempt");
                        }else{
                            listRatings.add("Rating: " + rating);
                        }

                    }
                }
            } catch (SecurityException e) {
                throw new ArrayIndexOutOfBoundsException();
            } catch (Exception e) {
                e.printStackTrace();
            }

            tabLay.removeAllViews();

            for (int j = 0; j < listName.size(); j++) {

                String theRating = listRatings.get(j);

                label1 = new TextView(this);
                ratingView = new ImageView(this);

                label1.setText(listName.get(j) + "\n"
                        + listAddress.get(j) + "\n" +
                        listAddress2.get(j) + "\n" +
                        listRatings.get(j) + "\n");

                tabRow = new TableRow(this);

                label1.setTextSize(11);
                label1.setAllCaps(true);
                label1.setMaxWidth(550);
                label1.setMinimumWidth(550);

                tabRow.addView(label1);

                if(theRating.equals("Rating: 5")){
                    ratingView.setImageResource(R.drawable.fivesmall);

                    tabRow.addView(ratingView);
                } else if(theRating.equals("Rating: 4")){
                    ratingView.setImageResource(R.drawable.foursmall);
                    tabRow.addView(ratingView);
                } else if(theRating.equals("Rating: 3")) {
                    ratingView.setImageResource(R.drawable.threesmall);
                    tabRow.addView(ratingView);
                } else if(theRating.equals("Rating: 2")) {
                    ratingView.setImageResource(R.drawable.twosmall);
                    tabRow.addView(ratingView);
                } else if(theRating.equals("Rating: 1")) {
                    ratingView.setImageResource(R.drawable.onesmall);
                    tabRow.addView(ratingView);
                }else if(theRating.equals("Rating: 0")) {
                    ratingView.setImageResource(R.drawable.zerosmall);
                    tabRow.addView(ratingView);
                } else if (theRating.equals("Rating: Exempt")) {
                    ratingView.setImageResource(R.drawable.exempt);
                    tabRow.addView(ratingView);
                }

                tabLay.addView(tabRow);

            }

        }

    }

    /**
     * Gets most recently added ratings
     * @param v
     */
    public void clickRecent(View v){
        display.setText("Recent ratings");
        ArrayList<String> listID = new ArrayList<String>();
        ArrayList<String> listName = new ArrayList<String>();
        ArrayList<String> listRatings = new ArrayList<String>();
        ArrayList<String> listAddress = new ArrayList<String>();
        ArrayList<String> listAddress2 = new ArrayList<String>();

        try {

            String s1 = "http://sandbox.kriswelsh.com/hygieneapi/hygiene.php?op=s_recent";

            label3.setText(s1);
            label3.setTextSize(8);

            URL url = new URL(s1);
            HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();

            InputStreamReader isr = new InputStreamReader(urlCon.getInputStream());

            BufferedReader br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null) {

                JSONArray jArray = new JSONArray(line);

                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jObject = (JSONObject) jArray.get(i);

                    listID.add(jObject.getString("id"));
                    listName.add(jObject.getString("BusinessName"));
                    if(jObject.getString("AddressLine1").isEmpty()) {
                        String address = jObject.getString("AddressLine2");

                        listAddress.add(address);
                    } else {
                        String address = jObject.getString("AddressLine1") + ", " +
                                jObject.getString("AddressLine2");
                        listAddress.add(address);
                    }

                    if(jObject.getString("AddressLine3").isEmpty()) {
                        String address2 = jObject.getString("PostCode");
                        listAddress2.add(address2);
                    } else{
                        String address2 = jObject.getString("AddressLine3") + ", " +
                                jObject.getString("PostCode");
                        listAddress2.add(address2);
                    }

                    String rating = jObject.getString("RatingValue");
                    if(Objects.equals(rating, "-1")){
                        listRatings.add("Rating: " + "Exempt");
                    }else{
                        listRatings.add("Rating: " + rating);
                    }

                }
            }
        } catch (SecurityException e) {
            throw new ArrayIndexOutOfBoundsException();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tabLay.removeAllViews();

        for (int j = 0; j < listName.size(); j++) {

            String theRating = listRatings.get(j);

            label1 = new TextView(this);
            ratingView = new ImageView(this);

            label1.setText(listName.get(j) + "\n"
                    + listAddress.get(j) + "\n" +
                    listAddress2.get(j) + "\n" +
                    listRatings.get(j) + "\n");

            tabRow = new TableRow(this);

            label1.setTextSize(11);
            label1.setAllCaps(true);
            label1.setMaxWidth(550);
            label1.setMinimumWidth(550);

            tabRow.addView(label1);

            ratingView.setFocusable(true);

            if(theRating.equals("Rating: 5")){
                ratingView.setImageResource(R.drawable.fivesmall);

                tabRow.addView(ratingView);
            } else if(theRating.equals("Rating: 4")){
                ratingView.setImageResource(R.drawable.foursmall);
                tabRow.addView(ratingView);
            } else if(theRating.equals("Rating: 3")) {
                ratingView.setImageResource(R.drawable.threesmall);
                tabRow.addView(ratingView);
            } else if(theRating.equals("Rating: 2")) {
                ratingView.setImageResource(R.drawable.twosmall);
                tabRow.addView(ratingView);
            } else if(theRating.equals("Rating: 1")) {
                ratingView.setImageResource(R.drawable.onesmall);
                tabRow.addView(ratingView);
            }else if(theRating.equals("Rating: 0")) {
                ratingView.setImageResource(R.drawable.zerosmall);
                tabRow.addView(ratingView);
            } else if (theRating.equals("Rating: Exempt")) {
                ratingView.setImageResource(R.drawable.exempt);
                tabRow.addView(ratingView);
            }

            tabLay.addView(tabRow);


        }

    }

    /**
     * Button listener for the map icon
     * @param v
     */
    public void mapButtonListener(View v) {
        clkBtn = (ImageButton) findViewById(R.id.imageButton2);
        clkBtn.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), MapsActivity.class);

                        startActivity(i);
                    }
                });

    }

    /**
     * Clears the table layout
     * @param v
     */
    public void clickClear(View v){
        display.setText("");
        tabLay.removeAllViews();
    }






}