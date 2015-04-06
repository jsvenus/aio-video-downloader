package adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.softcsoftware.aio.R;

import java.util.ArrayList;

import static activity.AWeb.Video;

@SuppressWarnings("UnusedDeclaration")
public class YoutubeVideoAdapter extends BaseAdapter {

    ArrayList<Video> dataList;
    LayoutInflater inflater;
    Context context;

    public YoutubeVideoAdapter(Context context, ArrayList<Video> list) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        dataList = list;
    }

    public Video getVideoByIndex(int position) {
        return dataList.get(position);
    }

    public void notifyData() {
        notifyDataSetChanged();
    }

    public int getCount() {
        return dataList.size();
    }

    public Object getItem(int position) {
        return dataList.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }


    @SuppressLint("InflateParams")
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = this.inflater.inflate(R.layout.layout_complete_download_task_list_row, null);
            viewHolder = new ViewHolder();
            viewHolder.format = (ImageView) view.findViewById(R.id.thump_photo);
            viewHolder.quality = (TextView) view.findViewById(R.id.title);

            viewHolder.format.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_video_format));

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.quality.setText(dataList.get(position).type);

        return view;
    }

    private static class ViewHolder {
        public TextView quality;
        public ImageView format;
    }

}

