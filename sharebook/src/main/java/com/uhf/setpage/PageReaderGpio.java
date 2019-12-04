package com.uhf.setpage;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.reader.base.CMD;
import com.reader.base.ERROR;
import com.reader.base.ReaderBase;
import com.reader.helper.ISO180006BOperateTagBuffer;
import com.reader.helper.InventoryBuffer;
import com.reader.helper.OperateTagBuffer;
import com.reader.helper.ReaderHelper;
import com.reader.helper.ReaderSetting;
import com.uhf.LogList;
import com.rocktech.sharebook.R.id;
import com.rocktech.sharebook.R.layout;
import com.uhf.UHFApplication;
import com.ui.base.BaseActivity;

public class PageReaderGpio extends BaseActivity {
	private LogList mLogList;
	
	private TextView mGetGpio;
	private TextView mSetGpio3, mSetGpio4;
	
	private RadioGroup mGroupGpio1, mGroupGpio2, mGroupGpio3, mGroupGpio4;

	private ReaderHelper mReaderHelper;
	private ReaderBase mReader;
	
	private static ReaderSetting m_curReaderSetting;
    private static InventoryBuffer m_curInventoryBuffer;
    private static OperateTagBuffer m_curOperateTagBuffer;
    private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;
    
    private LocalBroadcastManager lbm;
    
    @Override
    protected void onResume() {
    	if (mReader != null) {
    		if (!mReader.IsAlive())
    			mReader.StartWait();
    	}
    	super.onResume();
    };
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layout.page_reader_gpio);
		((UHFApplication) getApplication()).addActivity(this);
		
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
			mReader = mReaderHelper.getReader();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_curReaderSetting = mReaderHelper.getCurReaderSetting();
		m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
		m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
		m_curOperateTagISO18000Buffer = mReaderHelper.getCurOperateTagISO18000Buffer();

		mLogList = (LogList) findViewById(id.log_list);
		mGetGpio = (TextView) findViewById(id.get_gpio);
		mGetGpio.setOnClickListener(setGpioOnClickListener);
		mSetGpio3 = (TextView) findViewById(id.set_gpio3);
		mSetGpio3.setOnClickListener(setGpioOnClickListener);
		mSetGpio4 = (TextView) findViewById(id.set_gpio4);
		mSetGpio4.setOnClickListener(setGpioOnClickListener);
		
		mGroupGpio1 = (RadioGroup) findViewById(id.group_gpio1);
		mGroupGpio2 = (RadioGroup) findViewById(id.group_gpio2);
		mGroupGpio3 = (RadioGroup) findViewById(id.group_gpio3);
		mGroupGpio4 = (RadioGroup) findViewById(id.group_gpio4);
		
		lbm  = LocalBroadcastManager.getInstance(this);
		
		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_READER_SETTING);
		lbm.registerReceiver(mRecv, itent);
		
		updateView();
	}
	
	private void updateView() {
		if (m_curReaderSetting.btGpio1Value == 0x00) {
			mGroupGpio1.check(id.get_gpio1_0);
		} else if (m_curReaderSetting.btGpio1Value == 0x01) {
			mGroupGpio1.check(id.get_gpio1_1);
		}
		
		if (m_curReaderSetting.btGpio2Value == 0x00) {
			mGroupGpio2.check(id.get_gpio2_0);
		} else if (m_curReaderSetting.btGpio2Value == 0x01) {
			mGroupGpio2.check(id.get_gpio2_1);
		}
		
		if (m_curReaderSetting.btGpio3Value == 0x00) {
			mGroupGpio3.check(id.set_gpio3_0);
		} else if (m_curReaderSetting.btGpio3Value == 0x01) {
			mGroupGpio3.check(id.set_gpio3_1);
		}
		
		if (m_curReaderSetting.btGpio4Value == 0x00) {
			mGroupGpio4.check(id.set_gpio4_0);
		} else if (m_curReaderSetting.btGpio4Value == 0x01) {
			mGroupGpio4.check(id.set_gpio4_1);
		}
	}
	
	
	private OnClickListener setGpioOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			switch(arg0.getId()) {
			case id.get_gpio:
				mReader.readGpioValue(m_curReaderSetting.btReadId);
				break;
			case id.set_gpio3:
				byte btGpio3Value = (byte) (mGroupGpio3.getCheckedRadioButtonId() == id.set_gpio3_0 ? 0x00 : 0x01);
				mReader.writeGpioValue(m_curReaderSetting.btReadId, (byte)0x03, btGpio3Value);
				m_curReaderSetting.btGpio3Value = btGpio3Value;
				break;
			case id.set_gpio4:
				byte btGpio4Value = (byte) (mGroupGpio3.getCheckedRadioButtonId() == id.set_gpio4_0 ? 0x00 : 0x01);
				mReader.writeGpioValue(m_curReaderSetting.btReadId, (byte)0x04, btGpio4Value);
				m_curReaderSetting.btGpio4Value = btGpio4Value;
				break;
			}
		}
	};
	
	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_READER_SETTING)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				
				if (btCmd == CMD.READ_GPIO_VALUE || btCmd == CMD.WRITE_GPIO_VALUE) {
					updateView();
				}
				
            } else if (intent.getAction().equals(ReaderHelper.BROADCAST_WRITE_LOG)) {
            	mLogList.writeLog((String)intent.getStringExtra("log"), intent.getIntExtra("type", ERROR.SUCCESS));
            }
		}
	};
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mLogList.tryClose()) return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (lbm != null)
			lbm.unregisterReceiver(mRecv);
	}
}

