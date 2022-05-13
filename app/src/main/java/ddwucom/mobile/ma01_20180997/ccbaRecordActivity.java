package ddwucom.mobile.ma01_20180997;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ccbaRecordActivity extends AppCompatActivity {
    ListView lvcList = null;
    ccbaDBHelper helper;
    Cursor cursor;
    recordAdapter adapter;
    ccbaDTO ccba;

    TextView tvNoResult;

    final int RECORD_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ccba_record);

        lvcList = (ListView)findViewById(R.id.lvCList);
        tvNoResult = (TextView)findViewById(R.id.tvNoResults);

        helper = new ccbaDBHelper(this);
        adapter = new recordAdapter(this, R.layout.listview_record, cursor);
        lvcList.setAdapter(adapter);

        //리스트뷰 클릭 시 dto 만들어서 info 페이지로 넘기기
        lvcList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String cName = cursor.getString(cursor.getColumnIndex(ccbaDBHelper.COL_NAME));
                int _id = cursor.getInt(cursor.getColumnIndex(ccbaDBHelper.COL_ID));
                String ccma = cursor.getString(cursor.getColumnIndex(ccbaDBHelper.COL_CCMA));
                String cNum = cursor.getString(cursor.getColumnIndex(ccbaDBHelper.COL_CNUM));
                String ccsi = cursor.getString(cursor.getColumnIndex(ccbaDBHelper.COL_CCSI));
                String admin = cursor.getString(cursor.getColumnIndex(ccbaDBHelper.COL_ADMIN));
                Double lat = cursor.getDouble(cursor.getColumnIndex(ccbaDBHelper.COL_LAT));
                Double lng = cursor.getDouble(cursor.getColumnIndex(ccbaDBHelper.COL_LNG));
                String memo = cursor.getString(cursor.getColumnIndex(ccbaDBHelper.COL_MEMO));
                String date = cursor.getString(cursor.getColumnIndex(ccbaDBHelper.COL_DATE));

                ccba = new ccbaDTO(_id, cName, ccma, cNum, ccsi, admin, lat, lng, memo, date);

                Intent intent = new Intent(ccbaRecordActivity.this, ccbaInfoActivity.class);
                intent.putExtra("ccba", ccba);
                startActivityForResult(intent, RECORD_CODE);
            }
        });

        //리스트뷰 롱클릭 시 db에서 해당 기록 삭제
        lvcList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String cName = cursor.getString(cursor.getColumnIndex(ccbaDBHelper.COL_NAME));
                final long _id = id;

                AlertDialog.Builder builder = new AlertDialog.Builder(ccbaRecordActivity.this);
                builder.setTitle("기록 삭제")
                        .setMessage(cName + " 관련 기록을 삭제하시겠습니까?")
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SQLiteDatabase db = helper.getWritableDatabase();
                                db.execSQL("delete from " + ccbaDBHelper.TABLE_NAME +" where " + ccbaDBHelper.COL_ID + "=" + _id);

                                cursor = db.rawQuery("select * from " +ccbaDBHelper.TABLE_NAME, null);
                                adapter.changeCursor(cursor);
                                helper.close();

                            }
                        })
                        .setNegativeButton("취소", null)
                        .setCancelable(false)
                        .show();

                 return true;
            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();

        SQLiteDatabase db = helper.getReadableDatabase();
        cursor = db.rawQuery("select * from " + ccbaDBHelper.TABLE_NAME, null);

        adapter.changeCursor(cursor);

        if(cursor.getCount() == 0){
            tvNoResult.setVisibility(View.VISIBLE);
        } else {
            tvNoResult.setVisibility(View.INVISIBLE);
        }
        helper.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        cursor 사용 종료
        if (cursor != null) cursor.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_myMemo:
                //아이템 클릭 시 내 메모 목록으로 넘어가기. (intent)
                Intent intent = new Intent(ccbaRecordActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
        }
        return true;
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

}