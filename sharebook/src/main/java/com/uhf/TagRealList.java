package com.uhf;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import com.reader.helper.InventoryBuffer;
import com.reader.helper.InventoryBuffer.InventoryTagMap;
import com.reader.helper.ReaderHelper;
import com.uhf.tagpage.RealListAdapter;
import com.rocktech.sharebook.R;

import java.util.ArrayList;
import java.util.List;

public class TagRealList extends LinearLayout {
	
	
	private Context mContext;
	private TableRow mTagRealRow;
	private ImageView mTagRealImage;
	private TextView mListTextInfo;

	private TextView mMinRSSIText, mMaxRSSIText;

	private ReaderHelper mReaderHelper;

	private List<InventoryTagMap> data;
	private RealListAdapter mRealListAdapter;
	private ListView mTagRealList;

	private View mTagsRealListScrollView;
	//add by lei.li 2016/12/26
	private TextView mUIDText;

	private WindowManager wm;

	private static InventoryBuffer m_curInventoryBuffer;

	private OnItemSelectedListener mOnItemSelectedListener;

	public interface OnItemSelectedListener {
		public void onItemSelected(View arg1, int arg2, long arg3);
	}

	public TagRealList(Context context, AttributeSet attrs) {
		super(context, attrs);
		initContext(context);
	}

	public TagRealList(Context context) {
		super(context);
		initContext(context);
	}

	@SuppressWarnings("deprecation")
	private void initContext(Context context) {
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.tag_real_list, this);

		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		data = new ArrayList<InventoryTagMap>();
		m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();

		mTagsRealListScrollView = findViewById(R.id.tags_real_list_scroll_view);
		wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		LayoutParams lp = (LayoutParams) mTagsRealListScrollView
				.getLayoutParams();
		lp.height = (int) (wm.getDefaultDisplay().getHeight() / 2.7);
		mTagsRealListScrollView.setLayoutParams(lp);
		mTagsRealListScrollView.invalidate();
		// mTagsRealListScrollView.setVisibility(View.GONE);
		mUIDText = (TextView) findViewById(R.id.uid_text);

		mTagRealRow = (TableRow) findViewById(R.id.table_row_tag_real);
		mTagRealImage = (ImageView) findViewById(R.id.image_prompt);
		mTagRealImage.setImageDrawable(getResources()
				.getDrawable(R.drawable.up));
		mListTextInfo = (TextView) findViewById(R.id.list_text_info);
		mListTextInfo.setText(getResources().getString(R.string.open_tag_list));

		mMinRSSIText = (TextView) findViewById(R.id.min_rssi_text);
		mMaxRSSIText = (TextView) findViewById(R.id.max_rssi_text);

		mTagRealRow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				LayoutParams lp = (LayoutParams) mTagsRealListScrollView
						.getLayoutParams();

				if (lp.height <= wm.getDefaultDisplay().getHeight() / 2) {
					// mTagsRealListScrollView.setVisibility(View.VISIBLE);

					lp.height = (int) (wm.getDefaultDisplay().getHeight() / 1.5);
					mTagsRealListScrollView.setLayoutParams(lp);
					mTagsRealListScrollView.invalidate();

					mTagRealImage.setImageDrawable(getResources().getDrawable(
							R.drawable.down));
					mListTextInfo.setText(getResources().getString(
							R.string.close_tag_list));
				} else {
					// mTagsRealListScrollView.setVisibility(View.GONE);

					lp.height = (int) (wm.getDefaultDisplay().getHeight() / 2.7);
					mTagsRealListScrollView.setLayoutParams(lp);
					mTagsRealListScrollView.invalidate();

					mTagRealImage.setImageDrawable(getResources().getDrawable(
							R.drawable.up));
					mListTextInfo.setText(getResources().getString(
							R.string.open_tag_list));
				}
			}
		});

		mTagRealList = (ListView) findViewById(R.id.tag_real_list_view);
		mRealListAdapter = new RealListAdapter(mContext, data);
		mTagRealList.setAdapter(mRealListAdapter);

		mTagRealList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

				if (mOnItemSelectedListener != null)
					mOnItemSelectedListener.onItemSelected(arg1, arg2, arg3);
			}

		});
	}

	public void setOnItemSelectedListener(
			OnItemSelectedListener onItemSelectedListener) {
		mOnItemSelectedListener = onItemSelectedListener;
	}

	public final void clearText() {
		mMinRSSIText.setText("0dBm");
		mMaxRSSIText.setText("0dBm");
	}

	public final void refreshText() {
		if (m_curInventoryBuffer.nMinRSSI == 0 && m_curInventoryBuffer.nMaxRSSI == 0) {
			mMinRSSIText.setText("0dBm");
			mMaxRSSIText.setText("0dBm");
		} else {
			mMinRSSIText.setText((m_curInventoryBuffer.nMinRSSI - 129) + "dBm");
			mMaxRSSIText.setText((m_curInventoryBuffer.nMaxRSSI - 129) + "dBm");
		}
	}

	public final void refreshList() {
		data.clear();
		data.addAll(m_curInventoryBuffer.lsTagList);
		mRealListAdapter.notifyDataSetChanged();
		// add by lei.li 2016/11/12 this code 
				/*if (mTagRealList.getChildCount() != 0) {
					mTagsRealListScrollView.findViewById(R.id.tag_type)
							.getLayoutParams().width = mTagRealList.getChildAt(0)
							.getWidth();
							*/
		invaildate();
					/*
			
				}*/
		// add by lei.li 2016/11/12
		/*
		 * mTagRealList.getLayoutParams().width =
		 * UITools.getWidestView(mContext, mRealListAdapter);
		 * if(mTagRealList.getChildCount() != 0)
		 * mTagRealList.getChildAt(0).findViewById
		 * (R.id.epc_text).getLayoutParams().width =
		 * (UITools.getWidestViewChild(mContext, mRealListAdapter,
		 * R.id.epc_text)); Log.e("zYYYYYYYYYYYYY", "::::::::::" +
		 * UITools.getWidestViewChild(mContext, mRealListAdapter,
		 * R.id.epc_text));
		 */
	}
	// add by lei.li 2016/12/26
	
	private void invaildate() {
		if (mUIDText != null) {
			mUIDText.setWidth(mRealListAdapter.mWidthest);
			Log.e("change the width", mUIDText.getWidth() + "::::::::::::::");
			mUIDText.invalidate();   
		}
	}
	
	// add by lei.li 2016/11/11
	private int lengthestData() {
		int widest = 0;
		for (InventoryTagMap itm : m_curInventoryBuffer.lsTagList) {
			if (widest < itm.strEPC.length())
				widest = itm.strEPC.length();
		}

		return widest * 16;
	}
}
