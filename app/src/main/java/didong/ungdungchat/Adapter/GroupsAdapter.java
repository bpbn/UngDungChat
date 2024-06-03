package didong.ungdungchat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import didong.ungdungchat.Model.Groups;
import didong.ungdungchat.R;

public class GroupsAdapter extends BaseAdapter {

    Context context;
    int layout;
    List<Groups> groupsList;

    public GroupsAdapter(Context context, int layout, List<Groups> groupsList) {
        this.context = context;
        this.layout = layout;
        this.groupsList = groupsList;
    }

    @Override
    public int getCount() {
        return groupsList.size();
    }
    @Override
    public Object getItem(int position) {
        return groupsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if(view == null){
            view = View.inflate(context, layout,null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }
        Groups groups = groupsList.get(position);
        Picasso.get().load(groups.getImage()).
                resize(100,100).placeholder(R.drawable.baseline_groups_24).into(holder.img);
        holder.txtGroupName.setText((groups.getName()));
        return view;
    }

    class ViewHolder
    {
        ImageView img;
        TextView txtGroupName;
        ViewHolder(View v){
            img = (ImageView) v.findViewById(R.id.groups_profile_image);
            txtGroupName = (TextView) v.findViewById(R.id.groups_profile_name);
        }
    }
}
