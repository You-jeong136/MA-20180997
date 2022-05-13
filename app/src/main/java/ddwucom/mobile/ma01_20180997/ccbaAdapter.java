package ddwucom.mobile.ma01_20180997;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class ccbaAdapter extends BaseAdapter {

    public static final String TAG = "ccbaAdapter";

    private LayoutInflater inflater;
    private Context context;
    private int layout;
    private ArrayList<ccbaDTO> list;
    private NetworkManager networkManager = null;

    public ccbaAdapter(Context context, int resource, ArrayList<ccbaDTO> list) {
        this.context = context;
        this.layout = resource;
        this.list = list;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).get_id();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView with position : " + position);
        viewHolder holder;
        final int post = position;

        if(convertView == null){
            convertView = inflater.inflate(layout, parent, false);
            holder = new viewHolder();
            holder.tvName = (TextView) convertView.findViewById(R.id.tvMyCName);
            holder.tvAdmin = (TextView) convertView.findViewById(R.id.tvMyCAdmin);

            convertView.setTag(holder);
        } else {
            holder = (viewHolder) convertView.getTag();
        }

        holder.tvName.setText(String.valueOf(list.get(position).getName()));
        holder.tvAdmin.setText(String.valueOf(list.get(position).getAdmin()));

        return convertView;
    }

    public void setList(ArrayList<ccbaDTO> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    static  class viewHolder {
        TextView tvName;
        TextView tvAdmin;
    }


}
