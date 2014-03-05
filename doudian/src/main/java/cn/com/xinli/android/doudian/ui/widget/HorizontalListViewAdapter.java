package cn.com.xinli.android.doudian.ui.widget;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.com.xinli.android.doudian.R;
import cn.com.xinli.android.doudian.model.ProgramSimple;
import cn.com.xinli.android.doudian.ui.widget.HorizontalListView.ArrayAdapter;
import cn.com.xinli.android.doudian.utils.ImageDownloaderLruCache;

public class HorizontalListViewAdapter extends ArrayAdapter<ProgramSimple> {
	private ImageDownloaderLruCache mImageDownloaderLruCache;
	OnItemClickListener mOnItemClickListener;
	private Context mContext;

	public HorizontalListViewAdapter(Context context,
			ImageDownloaderLruCache imageDownloaderLruCache) {
		mContext = context;
		mImageDownloaderLruCache = imageDownloaderLruCache;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		View view = convertView;
		if (view == null) {
			view = LayoutInflater.from(mContext).inflate(
					R.layout.horizontal_list_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.textView = (TextView) view.findViewById(R.id.card_text);
			viewHolder.updateView = (TextView) view
					.findViewById(R.id.card_update);
			viewHolder.imageView = (ImageView) view
					.findViewById(R.id.card_image);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
        ProgramSimple video = getItem(position);
		viewHolder.textView.setText(video.getName());
        if (video.getUpdateStatus() != null && video.getUpdateStatus().endsWith("f")) {
            viewHolder.updateView.setText("全 "+video.getUpdateStatus().substring(0,video.getUpdateStatus().length()-1)+" 集");
            viewHolder.updateView.setVisibility(View.VISIBLE);
        } else if (video.getUpdateStatus() != null && Integer.parseInt(video.getUpdateStatus()) > 0) {
			viewHolder.updateView.setText("更新至第" + video.getUpdateStatus() + "集");
			viewHolder.updateView.setVisibility(View.VISIBLE);
		}else {
			viewHolder.updateView.setText("");
			viewHolder.updateView.setVisibility(View.INVISIBLE);
		}
		final int hashCode = video.getHashCode();
		mImageDownloaderLruCache.setImageThumbSize(300);
        mImageDownloaderLruCache.download(video.getPoster(),viewHolder.imageView, null);
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnItemClickListener != null) {
					mOnItemClickListener.onItemClick(hashCode, position);
				} else {
					Log.e("Adapter", "mOnItemClickListener is not implate!");
				}
			}
		});
		return view;
	}

	private static class ViewHolder {
		TextView textView;
		TextView updateView;
		ImageView imageView;
	}

	public interface OnItemClickListener {
		void onItemClick(int hashCode, int position);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}

	public final OnItemClickListener getOnItemSelectedListener() {
		return mOnItemClickListener;
	}
}
