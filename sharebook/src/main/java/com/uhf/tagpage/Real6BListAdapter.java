package com.uhf.tagpage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.reader.helper.ISO180006BOperateTagBuffer.ISO180006BOperateTagMap;
import com.rocktech.sharebook.R.id;
import com.rocktech.sharebook.R.layout;
import java.util.List;

public class Real6BListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;

	private Context mContext;
	
	private List<ISO180006BOperateTagMap> listMap;
	
	public final class ListItemView{                     
		public TextView mIdText;
		public TextView mUIDText;
		public TextView mAntennaText;
		public TextView mTimesText;
    }

	public Real6BListAdapter(Context context, List<ISO180006BOperateTagMap> listMap) {
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		this.listMap = listMap;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listMap.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ListItemView  listItemView = null;
		if (convertView == null) {
			listItemView = new ListItemView();
			convertView = mInflater.inflate(layout.tag_real_6b_list_item, null);
			listItemView.mIdText = (TextView)convertView.findViewById(id.id_text);
			listItemView.mUIDText = (TextView)convertView.findViewById(id.uid_text);
			listItemView.mAntennaText = (TextView)convertView.findViewById(id.antenna_text);
			listItemView.mTimesText = (TextView)convertView.findViewById(id.times_text);
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}
		
		ISO180006BOperateTagMap map = listMap.get(position);
		
		listItemView.mIdText.setText(String.valueOf(position + 1));
		listItemView.mUIDText.setText(map.strUID);
		listItemView.mAntennaText.setText(String.valueOf(map.btAntId & 0xFF));
		listItemView.mTimesText.setText(String.valueOf(map.nTotal));
		return convertView;

	}	
}
