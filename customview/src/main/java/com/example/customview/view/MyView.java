package com.example.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.customview.R;


public class MyView extends View {

    String TAG = "MyView";
    private String mContent;
    private Boolean mIsShow;
    private int mBackground;
    private int mSelect;
    private Paint paint;
    /**
     * 在代码中动态new 的话会调用该函数
     * @param context
     */
    public MyView(Context context) {
        super(context);
    }
    /**
     * 在xml配置时，会调用该构造函数
     * @param context
     * @param attrs
     */
    public MyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyViewStyle);
        if(typedArray != null){
            //这里要注意，String类型是没有默认值的，所以必须定义好，不然又是空指针大法
            mContent = typedArray.getString(R.styleable.MyViewStyle_mycontent);
            mIsShow = typedArray.getBoolean(R.styleable.MyViewStyle_isShow, true);
            mBackground = typedArray.getColor(R.styleable.MyViewStyle_mybackground, Color.RED);
            mSelect = typedArray.getInt(R.styleable.MyViewStyle_select, 0);
        }
        Log.d(TAG,"content:"+mContent);
        Log.d(TAG,"isShow:"+mIsShow);
        Log.d(TAG,"background:"+mBackground);
        Log.d(TAG,"select:"+mSelect);
    }
    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int getSize(int measureSpec){
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        Log.d(TAG,"specMode:"+specMode);
        Log.d(TAG,"specSize:"+specSize);
        switch (specMode){
            case MeasureSpec.EXACTLY:
                //当layout_width与layout_height　match_parent 为固定数值走这里
                result = 200;
                break;
            case MeasureSpec.AT_MOST:
                //当layout_width与layout_height定义为 wrap_content　就走这里
                result = Math.min(100,specSize);
                break;
            case MeasureSpec.UNSPECIFIED:
                //如果没有指定大小
                result = 400;
                break;
        }
        return result;
    }
    /**
     * 控件的宽和高
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getSize(widthMeasureSpec),getSize(heightMeasureSpec));
    }

    /**
     * 定义控件所处的布局
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d(TAG,"changed:"+changed);
        Log.d(TAG,"l:"+left);
        Log.d(TAG,"t:"+top);
        Log.d(TAG,"r:"+right);
        Log.d(TAG,"b:"+bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG,"onDraw");
        canvas.drawCircle(50, 50, 50, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG,"onSizeChanged");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d(TAG,"onFinishInflate");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG,"onTouchEvent");
        return super.onTouchEvent(event);
    }
}
