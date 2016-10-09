package uk.ac.mmu.webmd.orgillv7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author Samuel Orgill 15118305
 * @version 7, 25/3/2016
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Context context;
    LocationManager locMan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Is called then the map button is clicked, loads the map.
     * @param googleMap
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;


        try {
            context = this.getApplicationContext();
            locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);


            locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    //Adds a marker at the users current location
                    LatLng loc = new LatLng(lat, lon);
                    mMap.addMarker(new MarkerOptions().position(loc).title("You are here...").snippet("...and you're hungry."));

                    ArrayList<String> listLat = new ArrayList<String>();
                    ArrayList<String> listLon = new ArrayList<String>();
                    ArrayList<String> listName = new ArrayList<String>();
                    ArrayList<String> listRatings = new ArrayList<String>();
                    ArrayList<String> listAddress = new ArrayList<String>();
                    ArrayList<String> listDate = new ArrayList<String>();

                    try {

                        //Build the URL, inserting the latitude and longitude
                        String s1 = "http://sandbox.kriswelsh.com/hygieneapi/hygiene.php?op=s_loc&lat=";
                        String s2 = "&long=";

                        URL url = new URL(s1 + lat + s2 + lon);
                        HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();

                        InputStreamReader isr = new InputStreamReader(urlCon.getInputStream());

                        BufferedReader br = new BufferedReader(isr);

                        String line;
                        while ((line = br.readLine()) != null) {

                            JSONArray jArray = new JSONArray(line);

                            for (int i = 0; i < jArray.length(); i++) {

                                JSONObject jObject = (JSONObject) jArray.get(i);

                                //Gets the attributes from the JSON data
                                String lati = jObject.getString("Latitude");
                                String longi = jObject.getString("Longitude");
                                String bName = jObject.getString("BusinessName");
                                String ratings = jObject.getString("RatingValue");

                                if(jObject.getString("AddressLine1").isEmpty()){
                                    String address = jObject.getString("AddressLine2") + ", " +
                                            jObject.getString("PostCode");
                                    listAddress.add(address);
                                }else{
                                    String address = jObject.getString("AddressLine1") + ", "
                                            + jObject.getString("AddressLine2") + ", " +
                                            jObject.getString("PostCode");
                                    listAddress.add(address);
                                }
                                String date = jObject.getString("RatingDate");

                                listLat.add(lati);
                                listLon.add(longi);
                                listName.add(bName);
                                listRatings.add(ratings);

                                listDate.add(date);
                            }
                        }
                    } catch (SecurityException e) {
                        throw new ArrayIndexOutOfBoundsException();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //Sets the initial level of zoom
                    float zoomLevel = 14.0f;

                    //Loops through the arraylists to get information
                    for(int j = 0; j < listLat.size(); j++) {
                        String lt = listLat.get(j);
                        String ld = listLon.get(j);
                        String bName = listName.get(j);
                        String rating = listRatings.get(j);
                        String address = listAddress.get(j);
                        String date = listDate.get(j);

                        //Sets the longitude and latitude to the users current location
                        double ltd = Double.parseDouble(lt);
                        double lng = Double.parseDouble(ld);
                        LatLng place1 = new LatLng(ltd, lng);

                        if(rating.equals("5")) {
                            Bitmap bm5 = BitmapFactory.decodeResource(getResources(), R.drawable.fivesmall).copy(Bitmap.Config.ARGB_8888, true);
                            mMap.addMarker(new MarkerOptions().position(place1).title(bName + " | " + rating + "* Very Good (" + date + ")").snippet(address).icon(BitmapDescriptorFactory.fromBitmap(bm5)));

                        } if(rating.equals("4")){
                            Bitmap bm4 = BitmapFactory.decodeResource(getResources(), R.drawable.foursmall).copy(Bitmap.Config.ARGB_8888, true);
                            mMap.addMarker(new MarkerOptions().position(place1).title(bName + " | " + rating + "* Good (" + date + ")").snippet(address).icon(BitmapDescriptorFactory.fromBitmap(bm4)));

                        }  if(rating.equals("3")){
                            Bitmap bm3 = BitmapFactory.decodeResource(getResources(), R.drawable.threesmall).copy(Bitmap.Config.ARGB_8888, true);
                            mMap.addMarker(new MarkerOptions().position(place1).title(bName + " | " + rating + "* Generally Satisfactory (" + date + ")").snippet(address).icon(BitmapDescriptorFactory.fromBitmap(bm3)));

                        }  if(rating.equals("2")){
                            Bitmap bm2 = BitmapFactory.decodeResource(getResources(), R.drawable.twosmall).copy(Bitmap.Config.ARGB_8888, true);
                            mMap.addMarker(new MarkerOptions().position(place1).title(bName + " | " + rating + "* Improvement Necessary(" + date + ")").snippet(address).icon(BitmapDescriptorFactory.fromBitmap(bm2)));

                        }  if(rating.equals("1")){
                            Bitmap bm1 = BitmapFactory.decodeResource(getResources(), R.drawable.onesmall).copy(Bitmap.Config.ARGB_8888, true);
                            mMap.addMarker(new MarkerOptions().position(place1).title(bName + " | " + rating + "* Major Improvement Necessary (" + date + ")").snippet(address).icon(BitmapDescriptorFactory.fromBitmap(bm1)));

                        }  if(rating.equals("0")){
                            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.zerosmall).copy(Bitmap.Config.ARGB_8888, true);
                            mMap.addMarker(new MarkerOptions().position(place1).title(bName + " | " + rating + "* Urgent Improvement Needed (" + date + ")").snippet(address).icon(BitmapDescriptorFactory.fromBitmap(bm)));

                        }  if(rating.equals("-1")){
                            Bitmap bmEx = BitmapFactory.decodeResource(getResources(), R.drawable.exempt).copy(Bitmap.Config.ARGB_8888, true);
                            mMap.addMarker(new MarkerOptions().position(place1).title(bName + " | Exempt (" + date + ")").snippet(address).icon(BitmapDescriptorFactory.fromBitmap(bmEx)));

                        }

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place1, zoomLevel));

                    }


                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });


        } catch (SecurityException e) {
            throw new ArrayIndexOutOfBoundsException();
        }

    }
}
