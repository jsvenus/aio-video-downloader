package adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.softcsoftware.aio.R;
import data.object_holder.Website;
import view_holder.Views;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("UnusedDeclaration")
public class WebsiteAdapter extends BaseAdapter implements Serializable {


    public boolean isScrolling = false;
    public boolean isStarting = false;
    private ArrayList<Website> siteList;
    private LayoutInflater layoutInflater;

    public WebsiteAdapter(Context context, ArrayList<Website> dataList) {
        siteList = dataList;
        layoutInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return siteList.size();
    }

    public ArrayList<Website> getSiteList() {
        return this.siteList;
    }

    public Object getItem(int position) {
        return siteList.get(position);
    }

    public String getUrl(int position) {
        return siteList.get(position).getUrl();
    }

    public long getItemId(int position) {
        return position;
    }


    @SuppressLint("InflateParams")
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.abs_list_row, null);
            holder = new ViewHolder();
            holder.siteName = (TextView) view.findViewById(R.id.snakeName);
            holder.siteFavicon = (TextView) view.findViewById(R.id.thump_photo);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        String name = siteList.get(position).getName();
        if (name.contains("(") && name.contains(")"))
            name = name.replace("Popular", "popular").replace("(", " [ ").replace(")", " ] ");


        Views.setTextView(holder.siteName, name, 17.44f);
        name.toCharArray();
        Views.setTextView(holder.siteFavicon, (name.toCharArray()[0] + "").toUpperCase(), 18f);

        return view;
    }


    private static class ViewHolder {
        TextView siteName;
        TextView siteFavicon;
    }

}






