package adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.softcsoftware.aio.R;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("UnusedDeclaration")
public class FileAdapter extends BaseAdapter implements Serializable {

    private static ArrayList<File> fileList;
    private LayoutInflater layoutInflater;

    @Deprecated
    public FileAdapter(Context context, ArrayList<File> dataList, Typeface fontM) {
        fileList = dataList;
        layoutInflater = LayoutInflater.from(context);
    }

    public FileAdapter(Context context, ArrayList<File> dataList) {
        fileList = dataList;
        layoutInflater = LayoutInflater.from(context);
    }


    public int getCount() {
        return fileList.size();
    }

    public Object getItem(int position) {
        return fileList.get(position);
    }

    public String getFilePath(int position) {
        return fileList.get(position).getPath();
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.abs_file_list_row, null);
            holder = new ViewHolder();
            holder.fileName = (TextView) view.findViewById(R.id.title);
            holder.fileThumb = (ImageView) view.findViewById(R.id.thump_photo);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        String name = fileList.get(position).getName();
        holder.fileName.setText(name);

        if (fileList.get(position).isDirectory()) {
            holder.fileThumb.setImageResource(R.drawable.abs_folder_icon);
        } else {
            holder.fileThumb.setImageResource(R.drawable.abs_file_icon);
        }
        return view;
    }

    private static class ViewHolder {
        TextView fileName;
        ImageView fileThumb;
    }
}
