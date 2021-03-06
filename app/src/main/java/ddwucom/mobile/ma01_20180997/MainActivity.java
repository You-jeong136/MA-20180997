package ddwucom.mobile.ma01_20180997;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    
    final static int PERMISSION_REQ_CODE = 100;
    final int RECORD_CODE = 200;
    
    private LatLngResultReceiver latLngResultReceiver;


    private GoogleMap mGoogleMap;
    private LocationManager locationManager;

    Marker centerMarker;

    EditText etSearchAddr;
    TextView tvNoResult;
    ListView lvList;
    Button btncloseMap;
    String apiAddress;
    MapFragment mapFragment;
    View mapFView;

    int pos;
    Double LAT, LNG;  // κ²μ μμΉ
    ccbaAdapter adapter;
    ArrayList<ccbaDTO> resultList;
    ccbaXMLParser parser;
    NetworkManager networkManager;

    Boolean select1 = false;
    Boolean select2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etSearchAddr = findViewById(R.id.etSearchAddress);
        lvList = findViewById(R.id.lvCList);
        btncloseMap = findViewById(R.id.btnCloseMap);
        tvNoResult = findViewById(R.id.tvNoResult);

        resultList = new ArrayList();

        latLngResultReceiver = new LatLngResultReceiver(new Handler());

        apiAddress = getResources().getString(R.string.api_address1);
        networkManager = new NetworkManager(this);
        parser = new ccbaXMLParser();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(mapReadyCallBack);
        mapFView = findViewById(R.id.map);
        mapFView.setVisibility(View.INVISIBLE);


        //adpater ν΄λκ³  μ€μ .
        adapter = new ccbaAdapter(this, R.layout.listview_ccba, resultList);
        lvList.setAdapter(adapter);


        lvList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //λ‘± ν΄λ¦­ ν  κ²½μ° μ§λ λ³΄μ¬μ£ΌκΈ°.
                pos = position;

                mapFragment.getMapAsync(mapReadyCallBack);

                btncloseMap.setVisibility(View.VISIBLE);
                mapFView.setVisibility(View.VISIBLE);

                return true;
            }
        });

        lvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //ν΄λ¦­ ν  κ²½μ° ν΄λΉ μμ΄νμ μμΈνμ΄μ§λ‘ λμ΄κ°κΈ°.
                Intent intent = new Intent(MainActivity.this, ccbaInfoActivity.class);
                intent.putExtra("ccba", resultList.get(position));
                startActivityForResult(intent, RECORD_CODE);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_myMemo:
                //μμ΄ν ν΄λ¦­ μ λ΄ λ©λͺ¨ λͺ©λ‘μΌλ‘ λμ΄κ°κΈ°. (intent)
                Intent intent = new Intent(MainActivity.this, ccbaRecordActivity.class);
                startActivity(intent);
                finish();
        }
        return true;
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnSearch: //μ£Όμ κ²μ μ
                select1 = true;
                select2 = false;
                tvNoResult.setVisibility(View.INVISIBLE);
                btncloseMap.setVisibility(View.INVISIBLE);
                mapFView.setVisibility(View.INVISIBLE);
                startLatLngService(); //μ£Όμμ μκ²½λλ₯Ό μμλ -> searchCcba()λ₯Ό ν΅ν΄ xml νμ±ν¨.
                break;
            case R.id.btnMyGps : //λ΄ μμΉ μ£Όμλ‘ κ²μ μ
                select1 = false;
                select2 = true;
                etSearchAddr.setText("");
                tvNoResult.setVisibility(View.INVISIBLE);
                btncloseMap.setVisibility(View.INVISIBLE);
                mapFView.setVisibility(View.INVISIBLE);
                Location currentLoc = null;
                if(checkPermission()) {
                    currentLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                Log.d(TAG, "νμ¬ μμΉ : "  + currentLoc.getLatitude() + " / " + currentLoc.getLongitude());
                SearchCcba(currentLoc.getLatitude(), currentLoc.getLongitude());
                break;
            case R.id.btnCloseMap :
                btncloseMap.setVisibility(View.INVISIBLE);
                mapFView.setVisibility(View.INVISIBLE);
        }
        //λ²νΌ ν΄λ¦­ μ ν€λ³΄λ λ΄λ¦¬κΈ°
        InputMethodManager imm = (InputMethodManager) getSystemService (Context. INPUT_METHOD_SERVICE );
        imm.hideSoftInputFromWindow ( etSearchAddr.getWindowToken(), 0 );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECORD_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    break;
                case RESULT_CANCELED:
                    Toast.makeText(this, "κΈ°λ‘μ μ€ν¨νμμ΅λλ€", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    class NetworkAsyncTask extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDlg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDlg = ProgressDialog.show(MainActivity.this, "Wait", "Downloading...");
        }

        @Override
        protected String doInBackground(String... strings) {
            String address1 = strings[0];

            Log.d(TAG, address1);
            String result = null;
            // networking
            if(!address1.equals(""))
                result = networkManager.downloadContents(address1);
            else
                return "address null";
            if(result == null) return "server error";

            return result;
        }


        @Override
        protected void onPostExecute(String result) {
            //Log.d(TAG, result);
            resultList = parser.parse(result, LAT, LNG);
            adapter.setList(resultList);    // Adapter μ κ²°κ³Ό List λ₯Ό μ€μ  ν notify

            if(resultList.size() == 0){
                tvNoResult.setVisibility(View.VISIBLE);
            }
            progressDlg.dismiss();
        }

    }
    /* μ£Όμ β μλ/κ²½λ λ³ν IntentService μ€ν */
    private void startLatLngService() {
        String address = etSearchAddr.getText().toString();
        Intent intent = new Intent(this, FetchLatLngIntentService.class);
        intent.putExtra(Constants.RECEIVER, latLngResultReceiver);
        intent.putExtra(Constants.ADDRESS_DATA_EXTRA, address);
        startService(intent);
    }

    /* μ£Όμ β μλ/κ²½λ λ³ν ResultReceiver */
    class LatLngResultReceiver extends ResultReceiver {
        public LatLngResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            ArrayList<LatLng> latLngList = null;
            Double mylatitude = 0.0;
            Double mylongitude = 0.0;

            if (resultCode == Constants.SUCCESS_RESULT) {
                if (resultData == null) return;
                latLngList = (ArrayList<LatLng>) resultData.getSerializable(Constants.RESULT_DATA_KEY);
                if (latLngList == null) {
                    etSearchAddr.setText("λ€μ μλν΄μ£ΌμΈμ");
                } else {
                    LatLng latlng = latLngList.get(0);
                    mylatitude = latlng.latitude;
                    mylongitude = latlng.longitude;

                    SearchCcba(mylatitude, mylongitude);
                }

            } else {
                etSearchAddr.setText("λ€μ μλν΄μ£ΌμΈμ");
            }
        }
    }

    private void SearchCcba (double lat, double lng){
        if(lat != 0 && lng != 0){
            //etSearchAddr.setText(String.valueOf(lat) + "," + String.valueOf(lng)); //lat, lng μ λμ΄μλ νμΈμ©.
            LAT = lat; LNG = lng;
            new NetworkAsyncTask().execute(apiAddress);
        }
    }


    OnMapReadyCallback mapReadyCallBack = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;
            Location lastLocation = null;
            LatLng  currentLoc = new LatLng(37.579617, 126.9748523); //μ²μ κΈ°μ€ μμΉ : κ²½λ³΅κΆ/

            mGoogleMap.clear();

            if(checkPermission()) {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if(select1) //μμΉλ₯Ό κ²μνμ κ²½μ°
                currentLoc = new LatLng(LAT, LNG);
            else if (select2) { //νμ¬ μμΉλ₯Ό κΈ°μ€μΌλ‘ κ²μνμ κ²½μ°
                currentLoc = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                Log.d(TAG, "νμ¬ μμΉ : "  + currentLoc.latitude + " / " + currentLoc.longitude);
            }

            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15));

            Marker centerMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(currentLoc)
                    .title("κ²μκΈ°μ€"));
            centerMarker.setTag(0);

            if(resultList.size() != 0 ) {
                String cName = resultList.get(pos).getName();
                Marker y = mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(resultList.get(pos).getLat(), resultList.get(pos).getLng()))
                        .title(cName));
                y.setTag(0);
            }


            mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {

                }
            });

            mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {

                }
            });

            mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {

                }
            });
        }
    };

    private boolean checkPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQ_CODE);
                return false;
            }

        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]grantResults){
        if(requestCode == PERMISSION_REQ_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //νΌλ―Έμμ νλν  κ²½μ° νμ¬ μμΉ λ‘λ© μ€ν
                locationUpdate();
            } else {
                Toast.makeText(this, "μ± μ€νμ μν΄ κΆν νμ©μ΄ νμν¨.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void locationUpdate(){
        if(checkPermission()){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    60*1000, 0, locationListener);
        }

    }
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            LatLng currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
            Log.d(TAG, "loc : " + currentLoc.latitude + "," + currentLoc.longitude);

            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 17));
            if(centerMarker != null)
                centerMarker.setPosition(currentLoc);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onProviderDisabled(String provider) { }
    };






}