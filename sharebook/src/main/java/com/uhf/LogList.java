package com.uhf;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import com.rocktech.sharebook.R;
import com.reader.base.ERROR;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LogList extends LinearLayout {
	private Context mContext;
	private ListView mLogList;
	private TableRow mLogTextTableRow;
	private ImageView mLogTextImage;
	private TextView mLogInfoText, mLogHelpText;
	private ArrayAdapter<CharSequence> mLogListAdapter;
	private ArrayList<CharSequence> mLogListItem;
	
	//add by lei.li 2016/11/10
	private Button mClearLog;
	//add by lei.li 2016/11/10

	
	public LogList(Context context, AttributeSet attrs) {
		super(context, attrs);
		initContext(context);
	}
	
	public LogList(Context context) {  
        super(context);
        initContext(context);
    }

	private void initContext(Context context) {
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.log_list, this);
		
		mLogList = (ListView)findViewById(R.id.log_list_view);
		mLogTextTableRow = (TableRow) findViewById(R.id.table_row_log);
		mLogTextImage = (ImageView) findViewById(R.id.image_prompt);
		mLogList.setVisibility(View.GONE);
		mLogTextImage.setImageDrawable(getResources().getDrawable(R.drawable.up));
		mLogInfoText = (TextView) findViewById(R.id.list_text_info);
		mLogInfoText.setText(getResources().getString(R.string.open_caption));
		
		mLogHelpText = (TextView) findViewById(R.id.list_text_help);
		
		//add by lei.li 2016/11/10
		mClearLog = (Button) findViewById(R.id.clear_log);
		mClearLog.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doublClick();
			}
			
		});
		//add by lei.li 2016/11/10

		
		mLogListItem = new ArrayList<CharSequence>();
		
		mLogListAdapter = new ArrayAdapter<CharSequence>(mContext, R.layout.log_list_item, mLogListItem);
		
		mLogList.setAdapter(mLogListAdapter);
		
		mLogTextTableRow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (mLogList.getVisibility() != View.VISIBLE) {
					mLogList.setVisibility(View.VISIBLE);
					mLogTextImage.setImageDrawable(getResources().getDrawable(R.drawable.down));
					mLogInfoText.setText(getResources().getString(R.string.close_caption));
					mClearLog.setVisibility(View.VISIBLE);
				} else {
					mLogList.setVisibility(View.GONE);
					mLogTextImage.setImageDrawable(getResources().getDrawable(R.drawable.up));
					mLogInfoText.setText(getResources().getString(R.string.open_caption));
					mClearLog.setVisibility(View.GONE);
				}
			}
		});
	}

	public void writeLog(String strLog, int type) {
		Date now=new Date();
		SimpleDateFormat temp=new SimpleDateFormat("kk:mm:ss");
		SpannableString tSS = new SpannableString(temp.format(now) + ": " + strLog);
		tSS.setSpan(new ForegroundColorSpan(type == ERROR.SUCCESS ? Color.BLACK : Color.RED), 0, tSS.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  //��ɫ����
		mLogListItem.add(tSS);
		
		mLogHelpText.setText(tSS);
		
		if (mLogListItem.size() > 50)
			mLogListItem.remove(0);

		mLogListAdapter.notifyDataSetChanged();
	}
	
	public boolean tryClose() {
		boolean isVisibility = mLogList.getVisibility() == View.VISIBLE;
		if (isVisibility) {
			mLogList.setVisibility(View.GONE);
			mLogTextImage.setImageDrawable(getResources().getDrawable(R.drawable.up));
			mLogInfoText.setText(getResources().getString(R.string.open_caption));
		}
		
		return isVisibility;
	}
	
	private void doublClick(){
		mLogListItem.clear();
		mLogListAdapter.notifyDataSetChanged();
	}    
}
