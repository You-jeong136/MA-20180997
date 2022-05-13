package ddwucom.mobile.ma01_20180997;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ccbaInfoActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 300;
    public static final String TAG = "ccbaInfoActivity";

    TextView tvCName;
    TextView tvCCMA;
    TextView tvCCSI;
    TextView tvCAdmin;
    EditText etMyMemo;
    ImageView mImageView;

    String mCurrentPhotoPath = null;

    SQLiteDatabase db;
    ccbaDBHelper helper;
    ccbaDTO ccbaDTO;

    Boolean flag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ccba_info);

        ccbaDTO = (ccbaDTO) getIntent().getSerializableExtra("ccba");

        tvCName = findViewById(R.id.tvCName);
        tvCCMA = findViewById(R.id.tvCCMA);
        tvCCSI = findViewById(R.id.tvCCSI);
        tvCAdmin = findViewById(R.id.tvCAdmin);
        etMyMemo = findViewById(R.id.etMyMemo);
        mImageView = findViewById(R.id.mimageView);

        //setting도 해주기.
        tvCName.setText(ccbaDTO.getName());
        tvCCMA.setText(ccbaDTO.ccmaToString());
        tvCCSI.setText(ccbaDTO.ccsiToSting());
        tvCAdmin.setText(ccbaDTO.getAdmin());

        helper = new ccbaDBHelper(this);
        db = helper.getWritableDatabase();

        // 이미 해당항목이 기록되어 있는지 확인하고, 없으면 insert, 있으면 update
        String query = "select "+ helper.COL_MEMO + ", "+ helper.COL_IMAGE +" from " + helper.TABLE_NAME + " where _id = ?";
        Cursor cursor = db.rawQuery(query, new String[] {String.valueOf(ccbaDTO.get_id())});

        if(cursor.getCount() == 1) {
            flag = true;
            cursor.moveToNext();
            etMyMemo.setText(cursor.getString(cursor.getColumnIndex(helper.COL_MEMO)));
            mCurrentPhotoPath = cursor.getString(cursor.getColumnIndex(helper.COL_IMAGE));
            setPic();

        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onClick(View v) {
        SimpleDateFormat dateFormat = new SimpleDateFormat ( "YY/MM/dd_HH:mm");
        Date time = new Date();

        switch (v.getId()){
            case R.id.btnRecord :
                ccbaDTO.setMemo(etMyMemo.getText().toString());

                //이전에 기록한게 있을 경우 (update)
                if(flag) {
                    ContentValues row = new ContentValues();
                    row.put(helper.COL_MEMO, ccbaDTO.getMemo());
                    row.put(helper.COL_DATE, dateFormat.format(time));
                    row.put(helper.COL_IMAGE, mCurrentPhotoPath);

                    String whereClause = helper.COL_ID + "=?";
                    String []whereArgs = new String[] {String.valueOf(ccbaDTO.get_id())};

                    int result = db.update(helper.TABLE_NAME, row, whereClause, whereArgs);

                    if(result > 0) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("ccba", ccbaDTO);
                        setResult(RESULT_OK, resultIntent);
                    } else {
                        setResult(RESULT_CANCELED);
                    }

                } else { //기록 없을 경우 (insert)
                    ContentValues row = new ContentValues();
                    row.put(helper.COL_MEMO, ccbaDTO.getMemo()); //memo값
                    row.put(helper.COL_ID, ccbaDTO.get_id());
                    row.put(helper.COL_CCMA, ccbaDTO.getCcma());
                    row.put(helper.COL_CNUM, ccbaDTO.getCcmaNum());
                    row.put(helper.COL_CCSI, ccbaDTO.getCcsi());
                    row.put(helper.COL_ADMIN, ccbaDTO.getAdmin());
                    row.put(helper.COL_NAME, ccbaDTO.getName());
                    row.put(helper.COL_LAT, ccbaDTO.getLat());
                    row.put(helper.COL_LNG, ccbaDTO.getLng());
                    row.put(helper.COL_DATE, dateFormat.format(time));
                    row.put(helper.COL_IMAGE, mCurrentPhotoPath);

                    long result = db.insert(helper.TABLE_NAME, null, row);
                    if(result > 0) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("ccba", ccbaDTO);
                        setResult(RESULT_OK, resultIntent);
                    } else {
                        setResult(RESULT_CANCELED);
                    }
                }
                helper.close();
                finish();
                break;
            case R.id.btnBack :
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.btnShare :
                if(etMyMemo.getText().toString().equals("")){
                    ccbaDTO.setMemo("기록할 텍스트를 입력하세요.");
                } else {
                    ccbaDTO.setMemo(etMyMemo.getText().toString());
                }
                shareTwitter(ccbaDTO);
                break;
            case R.id.btnPicture :
                dispatchTakePictureIntent();
                break;
        }
        //버튼 클릭 시 키보드 내리기
        InputMethodManager imm = (InputMethodManager) getSystemService (Context. INPUT_METHOD_SERVICE );
        imm.hideSoftInputFromWindow ( etMyMemo.getWindowToken(), 0 );

    }

    public void shareTwitter(ccbaDTO ccba) {
        String strLink = null;
        String myConent = "[모바일 응용 _ 개인 프로젝트]\n" +
                            "⊙내 주변 문화재 검색결과 공유\n" +
                            "*문화재 : " + ccba.getName() +
                            "\n*종목 : " + ccba.ccmaToString() +
                            "\n*감상 : " + ccba.getMemo();
        try {
            strLink = String.format("http://twitter.com/intent/tweet?text=%s",
                    URLEncoder.encode(myConent, "utf-8"));
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(strLink));
        startActivity(intent);
    }

    /*원본 사진 파일 저장*/
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //요청 처리할 수 있는 카메라 앱이 있을 경우
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            //사진을 저장할 파일 생성
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }catch(IOException e){
                e.printStackTrace();
            }
            //파일을 정상적으로 생성시
            if(photoFile != null){
                //외부 앱이 접근 가능하도록 만든(fileProvider 이용) 앱 전용 폴더의 file URI
                Uri photoUri = FileProvider.getUriForFile(this,
                        "ddwu.com.mobile.multimedia.photo.fileprovider",
                        photoFile); //manifest에 적었던 authority 작성.
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }

    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        if(targetW == 0){
            targetW = 120;
            targetH = 100;
        }
        // Get the dimensions of the bitmap (원본 정보)
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        Log.d(TAG, "setPic() _ mcurrentPath : " + mCurrentPhotoPath);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        Log.d(TAG, "setPic() _ bmpOptions width : " + photoW);
        Log.d(TAG, "setPic() _ bmpOptions height : " + photoH);

        Log.d(TAG, "setPic() _ target width : " + targetW);
        Log.d(TAG, "setPic() _ target height : " + targetH);

        // Determine how much to scale down the image (비율 확인)
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View (비율 조정)
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap); //화면 출력
    }

    /*현재 시간 정보를 사용하여 파일 정보 생성*/
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); //외부저장소의 앱 전용폴더에 사진 저장 (앱 삭제 시 해당 폴더 파일도 삭제)
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "createfile _ mcurrentPath : " + mCurrentPhotoPath);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            setPic();
        }
    }

}