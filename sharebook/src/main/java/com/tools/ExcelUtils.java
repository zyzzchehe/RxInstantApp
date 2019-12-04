package com.tools;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import com.reader.helper.InventoryBuffer;
import com.rocktech.sharebook.R;
import com.uhf.UHFApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelUtils {
    public static WritableFont arial14font = null;

    public static WritableCellFormat arial14format = null;
    public static WritableFont arial10font = null;
    public static WritableCellFormat arial10format = null;
    public static WritableFont arial12font = null;
    public static WritableCellFormat arial12format = null;


    public static boolean isFirstInventy = true;
    public static boolean checklockstate = false;
    public static int sharebookdoornum =  -1;

    public final static String UTF8_ENCODING = "UTF-8";
    public final static String GBK_ENCODING = "GBK";

    private final static String[] mContent = new String[]{"ID", "EPC", "PC",
            UHFApplication.getContext().getResources().getString(R.string.real_list_times),
            "RSSI",UHFApplication.getContext().getString(R.string.real_list_freq)};

    private final static String FILE_SUFFIX = ".xls";

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void format() {
        try {
            arial14font = new WritableFont(WritableFont.ARIAL, 14,
                    WritableFont.BOLD);
            arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);
            arial14format = new WritableCellFormat(arial14font);
            arial14format.setAlignment(jxl.format.Alignment.CENTRE);
            arial14format.setBorder(jxl.format.Border.ALL,
                    jxl.format.BorderLineStyle.THIN);
            arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);
            arial10font = new WritableFont(WritableFont.ARIAL, 10,
                    WritableFont.BOLD);
            arial10format = new WritableCellFormat(arial10font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);
            arial10format.setBorder(jxl.format.Border.ALL,
                    jxl.format.BorderLineStyle.THIN);
            arial10format.setBackground(jxl.format.Colour.LIGHT_BLUE);
            arial12font = new WritableFont(WritableFont.ARIAL, 12);
            arial12format = new WritableCellFormat(arial12font);
            arial12format.setBorder(jxl.format.Border.ALL,
                    jxl.format.BorderLineStyle.THIN);
        } catch (WriteException e) {

            e.printStackTrace();
        }
    }

    public static void initExcel(String fileName, String[] colName) {
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet("tags", 0);
            sheet.addCell((WritableCell) new Label(0, 0, fileName,
                    arial14format));
            for (int col = 0; col < colName.length; col++) {
                sheet.addCell(new Label(col, 0, colName[col], arial10format));
            }
            workbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block  
                    e.printStackTrace();
                }
            }
        }

    }

    public static void writeTagToExcel(String fileName, List<InventoryBuffer.InventoryTagMap> maps) {

        fileName = fileName.trim() + FILE_SUFFIX;
        initExcel(fileName,mContent);
        writeObjListToExcel(maps,fileName);
    }

    @SuppressWarnings("unchecked")
    public static <T> void writeObjListToExcel(List<T> objList,
                                               String fileName) {
        if (objList != null && objList.size() > 0) {
            WritableWorkbook writebook = null;
            InputStream in = null;
            try {
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);
                in = new FileInputStream(new File(fileName));
                Workbook workbook = Workbook.getWorkbook(in);
                writebook = Workbook.createWorkbook(new File(fileName),
                        workbook);
                WritableSheet sheet = writebook.getSheet(0);
                for (int j = 0; j < objList.size(); j++) {
                    ArrayList<String> list = new ArrayList<String>();
                    InventoryBuffer.InventoryTagMap map = ((InventoryBuffer.InventoryTagMap)(objList.get(j)));
                    list.add(j + 1 + "");
                    list.add(map.strEPC);
                    list.add(map.strPC);
                    list.add(map.nReadCount + "");
                    list.add(map.strRSSI);
                    list.add(map.strFreq);
                    for (int i = 0; i < list.size(); i++) {
                        sheet.addCell(new Label(i, j + 1, list.get(i),
                                arial12format));
                    }
                }
                writebook.write();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writebook != null) {
                    try {
                        writebook.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

   /*
    public static  List<BillObject> read2DB(File f, Context con) {
        ArrayList<BillObject> billList = new ArrayList<BillObject>();  
        try {  
            Workbook course = null;  
            course = Workbook.getWorkbook(f);  
            Sheet sheet = course.getSheet(0);
              
            Cell cell = null;
            for (int i = 1; i < sheet.getRows(); i++) {  
                BillObject tc = new BillObject();  
                cell = sheet.getCell(1, i);  
                tc.setFood(cell.getContents());  
                cell = sheet.getCell(2, i);  
                tc.setClothes(cell.getContents());  
                cell = sheet.getCell(3, i);  
                tc.setHouse(cell.getContents());  
                cell = sheet.getCell(4, i);  
                tc.setVehicle(cell.getContents());  
                Log.d("gaolei", "Row"+i+"---------"+tc.getFood() + tc.getClothes()
                        + tc.getHouse() + tc.getVehicle());  
                billList.add(tc);  
                  
            }  
              
        } catch (Exception e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
        return billList;  
    }
    */

    public static Object getValueByRef(Class cls, String fieldName) {
        Object value = null;
        fieldName = fieldName.replaceFirst(fieldName.substring(0, 1), fieldName
                .substring(0, 1).toUpperCase());
        String getMethodName = "get" + fieldName;
        try {
            Method method = cls.getMethod(getMethodName);
            value = method.invoke(cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}  