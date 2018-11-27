package www.bkz.wifi;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public abstract class ListBaseAdapter<T, VH extends ListBaseAdapter.ViewHolder> extends BaseAdapter {
    protected List<T> data = new ArrayList<>();
    protected Context context;
    @LayoutRes
    protected int layoutRes;

    public ListBaseAdapter(List<T> data, @LayoutRes int layoutRes, Context context) {
        if (data != null) {
            this.data.addAll(data);
        }
        this.context = context;
        this.layoutRes = layoutRes;
    }

    public void refresh(List<T> data) {
        this.data.clear();
        if (data != null) {
            this.data.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void updateSingleRow(ListView listView, int position) {
        if (listView != null) {
            //第一个显示的item
            int visiblePos = listView.getFirstVisiblePosition();
            //item位置
            int offset = position - visiblePos;
            int count = listView.getChildCount();
            //在可见区域
            if (offset >= 0 && offset < count) {
                View convertView = listView.getChildAt(offset);
                initItemView(position, (VH) convertView.getTag());
            }
        }
    }

    @Override
    public final int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public final Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public final long getItemId(int position) {
        return position;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        final VH vh;
        if (null == convertView) {
            convertView = LayoutInflater.from(context).inflate(layoutRes, null, false);
            vh = onCreateViewHolder(convertView, parent);
            convertView.setTag(vh);
        } else {
            //noinspection unchecked
            vh = (VH) convertView.getTag();
        }
        initItemView(position, convertView, parent, vh);
        return convertView;
    }

    public void initItemView(int position, View convertView,
                             ViewGroup parent, VH vh) {
        initItemView(position, vh);
    }

    public abstract void initItemView(int position, VH vh);

    public abstract VH onCreateViewHolder(View convertView, ViewGroup parent);

    public static class ViewHolder {
        protected final View itemView;

        public ViewHolder(View itemView) {
            this.itemView = itemView;
        }
    }
}
