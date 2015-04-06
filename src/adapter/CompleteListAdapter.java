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
import data.object_holder.DownloadData;
import tools.FileCatalog;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>CompleteListAdapter is the Adapter that show the downloaded task file to the list view of
 * {@link activity.ADownloadManager}.
 * </p>
 *
 * @author shibaprasad
 * @version 1.2
 */
public class CompleteListAdapter extends BaseAdapter {

    private List<DownloadData> downloadData;
    private LayoutInflater layoutInflater;
    private List<View> viewList;


    /**
     * Public constructor.
     *
     * @param context          the activity context.
     * @param downloadDataList the Download data list.
     */
    public CompleteListAdapter(Context context, List<DownloadData> downloadDataList) {
        layoutInflater = LayoutInflater.from(context);
        this.downloadData = downloadDataList;
        viewList = new ArrayList<View>();
    }

    /**
     * Match the file format name and set the right image for image view.
     *
     * @param fileName  the file name
     * @param imageView the image view.
     */
    public static void matchFormat(String fileName, ImageView imageView) {
        String name = fileName.toLowerCase();

        for (String format : FileCatalog.ARCHIVE) {
            if (name.endsWith(format))
                imageView.setImageResource(R.drawable.ic_archive_format);
        }

        for (String format : FileCatalog.DOCUMENT) {
            if (name.endsWith(format))
                imageView.setImageResource(R.drawable.ic_document_format);
        }

        for (String format : FileCatalog.IMAGES) {
            if (name.endsWith(format))
                imageView.setImageResource(R.drawable.ic_image_format);
        }

        for (String format : FileCatalog.MUSIC) {
            if (name.endsWith(format))
                imageView.setImageResource(R.drawable.ic_music_format);
        }

        for (String format : FileCatalog.PROGRAM) {
            if (name.endsWith(format))
                imageView.setImageResource(R.drawable.ic_program_format);
        }

        for (String format : FileCatalog.VIDEO) {
            if (name.endsWith(format))
                imageView.setImageResource(R.drawable.ic_video_format);
        }

    }

    public DownloadData getDownloadDataList(int position) {
        return this.downloadData.get(position);
    }

    public void notifyData() {
        this.notifyDataSetChanged();
    }

    public int getCount() {
        return downloadData.size();
    }

    public Object getItem(int position) {
        return downloadData.get(position);
    }

    @Deprecated
    public long getItemId(int position) {
        return 0;
    }

    public View getViewByIndex(int position) {
        return viewList.get(position);
    }

    @SuppressLint({"InflateParams", "WrongViewCast"})
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.layout_complete_download_task_list_row, null);
            holder = new ViewHolder();
            holder.thumb_photo = (ImageView) view.findViewById(R.id.thump_photo);
            holder.fileName = (TextView) view.findViewById(R.id.title);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        matchFormat(this.getDownloadDataList(position).getFileName(), holder.thumb_photo);
        holder.fileName.setText(this.getDownloadDataList(position).getFileName());


        viewList.add(view);
        return view;
    }

    public class ViewHolder {
        public ImageView thumb_photo;
        public TextView fileName;
    }

}

