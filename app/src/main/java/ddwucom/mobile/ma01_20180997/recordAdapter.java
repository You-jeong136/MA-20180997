package ddwucom.mobile.ma01_20180997;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

class recordAdapter extends CursorAdapter {
    //즐겨찾기 화면에서 _ db 검색한 cursor 넘겨주면, 해당 데이터 리스트 뷰로 뿌려줌.

    LayoutInflater inflater;
    int layout;

    public recordAdapter(Context context, int layout, Cursor c){
        super(context, c, recordAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layout = layout;

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = inflater.inflate(layout, parent, false);
        ViewHolder holder = new ViewHolder();
        v.setTag(holder);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if(holder.tvMyCName == null){
            holder.tvMyCName = view.findViewById(R.id.tvMyCName);
            holder.tvMyCAdmin = view.findViewById(R.id.tvMyCAdmin);
            holder.tvRecordDate = view.findViewById(R.id.tvRecordDate);
        }

        holder.tvMyCName.setText(cursor.getString(cursor.getColumnIndex(ccbaDBHelper.COL_NAME)));
        holder.tvMyCAdmin.setText(cursor.getString(cursor.getColumnIndex(ccbaDBHelper.COL_ADMIN)));
        holder.tvRecordDate.setText(cursor.getString(cursor.getColumnIndex(ccbaDBHelper.COL_DATE)));
    }

    static class ViewHolder {

        public ViewHolder() {
            tvMyCName = null;
            tvMyCAdmin = null;
            tvRecordDate = null;
        }

        TextView tvMyCAdmin;
        TextView tvMyCName;
        TextView tvRecordDate;

    }
}
