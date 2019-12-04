package com.uhf;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableRow;
import android.widget.TextView;

import com.reader.base.CMD;
import com.reader.base.ERROR;
import com.reader.base.ReaderBase;
import com.reader.helper.ISO180006BOperateTagBuffer;
import com.reader.helper.InventoryBuffer;
import com.reader.helper.OperateTagBuffer;
import com.reader.helper.ReaderHelper;
import com.reader.helper.ReaderSetting;
import com.uhf.setpage.PageReaderAddress;
import com.uhf.setpage.PageReaderAntDetector;
import com.uhf.setpage.PageReaderAntenna;
import com.uhf.setpage.PageReaderBeeper;
import com.uhf.setpage.PageReaderFirmwareVersion;
import com.uhf.setpage.PageReaderGpio;
import com.uhf.setpage.PageReaderIdentifier;
import com.uhf.setpage.PageReaderMaskTag;
import com.uhf.setpage.PageReaderMonza;
import com.uhf.setpage.PageReaderOutPower;
import com.uhf.setpage.PageReaderProfile;
import com.uhf.setpage.PageReaderRegion;
import com.uhf.setpage.PageReaderReturnLoss;
import com.uhf.setpage.PageReaderTemperature;
import com.ui.base.BaseActivity;
import com.rocktech.sharebook.R;


public class Setting extends BaseActivity {
    private Context mContext;

    private TableRow mSettingResetRow;
    private TableRow mSettingReaderAddressRow;
    private TextView mSettingReaderAddressText;
    private TableRow mSettingIdentifierRow;
    private TextView mSettingIdentifierText;
    private TableRow mSettingFirmwareVersionRow;
    private TextView mSettingFirmwareVersionText;
    private TableRow mSettingTemperatureRow;
    private TextView mSettingTemperatureText;
    private TableRow mSettingGpioRow;
    private TextView mSettingGpioText;
    private TableRow mSettingBeeperRow;
    private TextView mSettingBeeperText;
    private TableRow mSettingOutPowerRow;
    private TextView mSettingOutPowerText;
    private TableRow mSettingAntennaRow;
    private TextView mSettingAntennaText;
    private TableRow mSettingReturnLossRow;
    private TextView mSettingReturnLossText;
    private TableRow mSettingAntDetectorRow;
    private TextView mSettingAntDetectorText;
    private TableRow mSettingMonzaRow;
    private TextView mSettingMonzaText;
    private TableRow mSettingRegionRow;
    private TextView mSettingRegionText;
    private TableRow mSettingProfileRow;
    private TextView mSettingProfileText;
    private TableRow mSettingMonitorRow;
    private TextView mSettingMonitorText;
    private TextView mVersionInfo;
    private TableRow mSettingMaskTagRow;
    private TextView mSettingMaskTagText;


    private ReaderBase mReader;
    private ReaderHelper mReaderHelper;

    private LogList mLogList;

    private static ReaderSetting m_curReaderSetting;
    private static InventoryBuffer m_curInventoryBuffer;
    private static OperateTagBuffer m_curOperateTagBuffer;
    private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;

    @Override
    protected void onResume() {
        if (mReader != null) {
            if (!mReader.IsAlive())
                mReader.StartWait();
        }
        super.onResume();
    }

    ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        ((UHFApplication) getApplication()).addActivity(this);
        mContext = this;

        try {
            mReaderHelper = ReaderHelper.getDefaultHelper();
            mReader = mReaderHelper.getReader();
        } catch (Exception e) {
            return;
        }

        initSettingView();

        m_curReaderSetting = mReaderHelper.getCurReaderSetting();
        m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
        m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
        m_curOperateTagISO18000Buffer = mReaderHelper.getCurOperateTagISO18000Buffer();

    }

    public void setLogList(LogList logList) {
        mLogList = logList;
    }

    private void initSettingView() {

        mSettingResetRow = (TableRow) findViewById(R.id.table_row_setting_reset);
        mSettingResetRow.setOnClickListener(setSettingOnClickListener);
        mSettingReaderAddressRow = (TableRow) findViewById(R.id.table_row_setting_reader_address);
        mSettingReaderAddressRow.setOnClickListener(setSettingOnClickListener);
        mSettingReaderAddressText = (TextView) findViewById(R.id.text_setting_reader_address);

        mSettingIdentifierRow = (TableRow) findViewById(R.id.table_row_setting_identifier);
        mSettingIdentifierRow.setOnClickListener(setSettingOnClickListener);
        mSettingIdentifierText = (TextView) findViewById(R.id.text_setting_identifier);

        mSettingFirmwareVersionRow = (TableRow) findViewById(R.id.table_row_setting_firmware_version);
        mSettingFirmwareVersionRow.setOnClickListener(setSettingOnClickListener);
        mSettingFirmwareVersionText = (TextView) findViewById(R.id.text_setting_firmware_version);

        mSettingTemperatureRow = (TableRow) findViewById(R.id.table_row_setting_temperature);
        mSettingTemperatureRow.setOnClickListener(setSettingOnClickListener);
        mSettingTemperatureText = (TextView) findViewById(R.id.text_setting_temperature);

        mSettingGpioRow = (TableRow) findViewById(R.id.table_row_setting_gpio);
        mSettingGpioRow.setOnClickListener(setSettingOnClickListener);
        mSettingGpioText = (TextView) findViewById(R.id.text_setting_gpio);

        mSettingBeeperRow = (TableRow) findViewById(R.id.table_row_setting_beeper);
        mSettingBeeperRow.setOnClickListener(setSettingOnClickListener);
        mSettingBeeperText = (TextView) findViewById(R.id.text_setting_beeper);


        mSettingOutPowerRow = (TableRow) findViewById(R.id.table_row_setting_out_power);
        mSettingOutPowerRow.setOnClickListener(setSettingOnClickListener);
        mSettingOutPowerText = (TextView) findViewById(R.id.text_setting_out_power);

        mSettingAntennaRow = (TableRow) findViewById(R.id.table_row_setting_antenna);
        mSettingAntennaRow.setOnClickListener(setSettingOnClickListener);
        mSettingAntennaText = (TextView) findViewById(R.id.text_setting_antenna);

        mSettingReturnLossRow = (TableRow) findViewById(R.id.table_row_setting_return_loss);
        mSettingReturnLossRow.setOnClickListener(setSettingOnClickListener);
        mSettingReturnLossText = (TextView) findViewById(R.id.text_setting_return_loss);

        mSettingAntDetectorRow = (TableRow) findViewById(R.id.table_row_setting_ant_detector);
        mSettingAntDetectorRow.setOnClickListener(setSettingOnClickListener);
        mSettingAntDetectorText = (TextView) findViewById(R.id.text_setting_ant_detector);

        mSettingMonzaRow = (TableRow) findViewById(R.id.table_row_setting_monza);
        mSettingMonzaRow.setOnClickListener(setSettingOnClickListener);
        mSettingMonzaText = (TextView) findViewById(R.id.text_setting_monza);

        mSettingRegionRow = (TableRow) findViewById(R.id.table_row_setting_region);
        mSettingRegionRow.setOnClickListener(setSettingOnClickListener);
        mSettingRegionText = (TextView) findViewById(R.id.text_setting_region);

        mSettingProfileRow = (TableRow) findViewById(R.id.table_row_setting_profile);
        mSettingProfileRow.setOnClickListener(setSettingOnClickListener);
        mSettingProfileText = (TextView) findViewById(R.id.text_setting_profile);

        mSettingMonitorRow = (TableRow) findViewById(R.id.table_row_monitor);
        mSettingMonitorRow.setOnClickListener(setSettingOnClickListener);
        mSettingMonitorText = (TextView) findViewById(R.id.text_setting_monitor);

        mSettingMaskTagRow = (TableRow) findViewById(R.id.table_row_setting_mask_tag);
        mSettingMaskTagRow.setOnClickListener(setSettingOnClickListener);
        mSettingMaskTagText = (TextView) findViewById(R.id.text_setting_mask_tag);

        mVersionInfo = (TextView) findViewById(R.id.version_info);
        initVersionInfo(mVersionInfo);

    }

    private void writeLog(String strLog, byte type) {
        if (mLogList != null)
            mLogList.writeLog(strLog, type);
    }

    public void refreshReaderSetting(byte btCmd) {
        switch (btCmd) {

        }
    }

    private OnClickListener setSettingOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Intent intent = null;
            switch (arg0.getId()) {
                case R.id.table_row_setting_reset:
                    mReader.reset(m_curReaderSetting.btReadId);
                    writeLog(CMD.format(CMD.RESET), ERROR.SUCCESS);
                    break;
                case R.id.table_row_setting_reader_address:
                    intent = new Intent().setClass(mContext, PageReaderAddress.class);
                    break;
                case R.id.table_row_setting_identifier:
                    intent = new Intent().setClass(mContext, PageReaderIdentifier.class);
                    break;
                case R.id.table_row_setting_firmware_version:
                    intent = new Intent().setClass(mContext, PageReaderFirmwareVersion.class);
                    break;
                case R.id.table_row_setting_temperature:
                    intent = new Intent().setClass(mContext, PageReaderTemperature.class);
                    break;
                case R.id.table_row_setting_gpio:
                    intent = new Intent().setClass(mContext, PageReaderGpio.class);
                    break;
                case R.id.table_row_setting_beeper:
                    intent = new Intent().setClass(mContext, PageReaderBeeper.class);
                    break;
                case R.id.table_row_setting_out_power:
                    intent = new Intent().setClass(mContext, PageReaderOutPower.class);
                    break;
                case R.id.table_row_setting_antenna:
                    intent = new Intent().setClass(mContext, PageReaderAntenna.class);
                    break;
                case R.id.table_row_setting_return_loss:
                    intent = new Intent().setClass(mContext, PageReaderReturnLoss.class);
                    break;
                case R.id.table_row_setting_ant_detector:
                    intent = new Intent().setClass(mContext, PageReaderAntDetector.class);
                    break;
                case R.id.table_row_setting_monza:
                    intent = new Intent().setClass(mContext, PageReaderMonza.class);
                    break;
                case R.id.table_row_setting_region:
                    intent = new Intent().setClass(mContext, PageReaderRegion.class);
                    break;
                case R.id.table_row_setting_profile:
                    intent = new Intent().setClass(mContext, PageReaderProfile.class);
                    break;
                case R.id.table_row_monitor:
                    intent = new Intent().setClass(mContext, Monitor.class);
                    break;
                case R.id.table_row_setting_mask_tag:
                    intent = new Intent().setClass(mContext, PageReaderMaskTag.class);
                    break;
                default:
                    intent = null;
            }

            if (intent != null) mContext.startActivity(intent);
        }
    };

    private void initVersionInfo(TextView textView) {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = null;
        String version = null;
        try {
            pi = pm.getPackageInfo(mContext.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (pi != null)
            version = pi.versionName;
        textView.setText(mContext.getResources().getString(R.string.version)
                + "  " + version);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}
