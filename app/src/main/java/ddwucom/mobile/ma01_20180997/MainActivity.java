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
    Double LAT, LNG;  // 검색 위치
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


        //adpater 해놓고 설정.
        adapter = new ccbaAdapter(this, R.layout.listview_ccba, resultList);
        lvList.setAdapter(adapter);


        lvList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //롱 클릭 할 경우 지도 보여주기.
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
                //클릭 할 경우 해당 아이템의 상세페이지로 넘어가기.
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
                //아이템 클릭 시 내 메모 목록으로 넘어가기. (intent)
                Intent intent = new Intent(MainActivity.this, ccbaRecordActivity.class);
                startActivity(intent);
                finish();
        }
        return true;
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnSearch: //주소 검색 시
                select1 = true;
                select2 = false;
                tvNoResult.setVisibility(View.INVISIBLE);
                btncloseMap.setVisibility(View.INVISIBLE);
                mapFView.setVisibility(View.INVISIBLE);
                startLatLngService(); //주소의 위경도를 알아냄 -> searchCcba()를 통해 xml 파싱함.
                break;
            case R.id.btnMyGps : //내 위치 주소로 검색 시
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
                Log.d(TAG, "현재 위치 : "  + currentLoc.getLatitude() + " / " + currentLoc.getLongitude());
                SearchCcba(currentLoc.getLatitude(), currentLoc.getLongitude());
                break;
            case R.id.btnCloseMap :
                btncloseMap.setVisibility(View.INVISIBLE);
                mapFView.setVisibility(View.INVISIBLE);
        }
        //버튼 클릭 시 키보드 내리기
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
                    Toast.makeText(this, "기록에 실패하였습니다", Toast.LENGTH_SHORT).show();
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
            adapter.setList(resultList);    // Adapter 에 결과 List 를 설정 후 notify

            if(resultList.size() == 0){
                tvNoResult.setVisibility(View.VISIBLE);
            }
            progressDlg.dismiss();
        }

    }
    /* 주소 → 위도/경도 변환 IntentService 실행 */
    private void startLatLngService() {
        String address = etSearchAddr.getText().toString();
        Intent intent = new Intent(this, FetchLatLngIntentService.class);
        intent.putExtra(Constants.RECEIVER, latLngResultReceiver);
        intent.putExtra(Constants.ADDRESS_DATA_EXTRA, address);
        startService(intent);
    }

    /* 주소 → 위도/경도 변환 ResultReceiver */
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
                    etSearchAddr.setText("다시 시도해주세요");
                } else {
                    LatLng latlng = latLngList.get(0);
                    mylatitude = latlng.latitude;
                    mylongitude = latlng.longitude;

                    SearchCcba(mylatitude, mylongitude);
                }

            } else {
                etSearchAddr.setText("다시 시도해주세요");
            }
        }
    }

    private void SearchCcba (double lat, double lng){
        if(lat != 0 && lng != 0){
            //etSearchAddr.setText(String.valueOf(lat) + "," + String.valueOf(lng)); //lat, lng 잘 넘어왔나 확인용.
            LAT = lat; LNG = lng;
            new NetworkAsyncTask().execute(apiAddress);
        }
    }


    OnMapReadyCallback mapReadyCallBack = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;
            Location lastLocation = null;
            LatLng  currentLoc = new LatLng(37.579617, 126.9748523); //처음 기준 위치 : 경복궁/

            mGoogleMap.clear();

            if(checkPermission()) {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if(select1) //위치를 검색했을 경우
                currentLoc = new LatLng(LAT, LNG);
            else if (select2) { //현재 위치를 기준으로 검색했을 경우
                currentLoc = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                Log.d(TAG, "현재 위치 : "  + currentLoc.latitude + " / " + currentLoc.longitude);
            }

            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15));

            Marker centerMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(currentLoc)
                    .title("검색기준"));
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
                //퍼미션을 획득할 경우 현재 위치 로딩 실행
                locationUpdate();
            } else {
                Toast.makeText(this, "앱 실행을 위해 권한 허용이 필요함.", Toast.LENGTH_SHORT).show();
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