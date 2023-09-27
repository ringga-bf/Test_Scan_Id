package com.batamfast.testscanid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import demo.test.com.testidcard.CopyAssetsUtil;
import demo.test.com.testidcard.MyPagerAdapter;
import demo.test.com.testidcard.OcrThread;
import demo.test.com.testidcard.RootCmd;
import product.idcard.android.IDCardAPI;

public class ScanViewActivity extends Activity implements ViewPager.OnPageChangeListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.MOUNT_UNMOUNT_FILESYSTEMS"};

    IDCardAPI idCardAPI = new IDCardAPI();
    private CopyAssetsUtil copyAssetsUtil;
    private int current;
    String result = "";
    private TextView textView;
    private ViewPager myViewPager;
    private MyPagerAdapter myPagerAdapter;
    private List<String> tupianPath = new ArrayList<String>();
    private ProgressDialog mdialog;
    private ViewGroup viewGroup;
    private ImageView[] tips;
    private  String strPath = Environment.getExternalStorageDirectory().toString();
    public static boolean isExitApp=false;//是否退出APP
    public static boolean isFirstOnCreate=true;//是否是第一次创建
    public static int InitSucess=-1;//初始化返回值
    public static String    strImagePath ="";
    public static String    strImagePathIR = "";//红外图片路径
    public static String    strImagePathUV = "";//紫外图片路径
    public static String    strHeadImagePath = "";
    public static String    strHeadImagePathEC = "";
    public static String    strSIDHeadPath = "";
    public static String    strSIDHeadPathName = "";
    public static String    strTestPath = "";

    //消息循环 //end -> onCreate
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!isExitApp) {
                switch (msg.what) {
                    case 0:
                        mdialog = ProgressDialog.show(ScanViewActivity.this, "", "核心初始化中....");
                        break;
                    case 10:
                        if (mdialog != null) {
                            mdialog.dismiss();
                            mdialog = null;
                        }
                        break;
                    case 15:
                        result = (String) msg.obj;
                        textView.setText(result);
                        break;
                    case 20:
                        result = (String) msg.obj;
                        textView.setText(result);
                        openOCrRecoge();
                        break;
                    case 80:
                        result = (String) msg.obj;
                        textView.setText(result);
                        openOCrRecoge();
                        break;
                    case 100:
                        tupianPath.clear();
                        //添加数据源
                        addPathDate(strImagePath);
                        addPathDate(strImagePathIR);
                        addPathDate(strImagePathUV);
                        addPathDate(strHeadImagePath);
                        addPathDate(strHeadImagePathEC);
                        addPathDate(strSIDHeadPathName);
                        showImage();
                        result = (String) msg.obj;
                        textView.setText(result);
                        openOCrRecoge();
                        break;
                    case 101:
                        openOCrRecoge();
                        break;
                    case 102:
                        tupianPath.clear();
                        //添加数据源
                        addPathDate(strImagePath);
                        addPathDate(strImagePathIR);
                        addPathDate(strImagePathUV);
                        addPathDate(strHeadImagePath);
                        addPathDate(strHeadImagePathEC);
                        showImage();
                        result = (String) msg.obj;
                        textView.setText(result);
                        openOCrRecoge();
                        break;
                    case 103:
                        tupianPath.clear();
                        //添加数据源
                        addPathDate(strSIDHeadPathName);
                        showImage();
                        result = (String) msg.obj;
                        textView.setText(result);
                        openOCrRecoge();
                        break;
                    default:
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);//写权限
        textView = (TextView) findViewById(R.id.tv_show_result);
        myViewPager = (ViewPager) findViewById(R.id.viewpager);
        viewGroup=(ViewGroup) findViewById(R.id.tip_group);

        //修改/dev/bus/usb/文件夹的权限，注意添加\n
        String commend = "chmod -R 777 /dev/bus/usb/  \n";
        if( RootCmd.isRoot()){
            String result = RootCmd.execRootCmd(commend);
            result = RootCmd.execRootCmd(commend);
        }

        //实例化拷贝资源类
        copyAssetsUtil = new CopyAssetsUtil(ScanViewActivity.this);
        isFirstOnCreate=true;
        bindDate();
        //end -> onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        isExitApp=false;
        openOCrRecoge();
    }
    /**
     *    开启识别线程
     */
    private void openOCrRecoge(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(true)
                {
                    strTestPath = strPath + "/AndroidWTone/test.jpg";
                    strImagePath =strPath + "/AndroidWTone/test_Image.jpg";
                    strImagePathIR = strPath + "/AndroidWTone/test_ImageIR.jpg";
                    strImagePathUV = strPath + "/AndroidWTone/test_ImageUV.jpg";
                    strHeadImagePath = strPath + "/AndroidWTone/test_ImageHead.jpg";
                    strHeadImagePathEC = strPath + "/AndroidWTone/test_ImageHeadEc.jpg";
                    strSIDHeadPath = strPath + "/AndroidWTone/IDCardProdTest/";
                    strSIDHeadPathName = strPath + "/AndroidWTone/IDCardProdTest/zp.bmp";
                }
                else
                {
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
                    java.util.Date date=new java.util.Date();
                    String str=sdf.format(date);

                    strImagePath =strPath + "/AndroidWTone/"+str+"_Image.jpg";
                    strImagePathIR = strPath + "/AndroidWTone/"+str+"_ImageIR.jpg";
                    strImagePathUV = strPath + "/AndroidWTone/"+str+"_ImageUV.jpg";
                    strHeadImagePath = strPath + "/AndroidWTone/"+str+"_HeadImage.jpg";
                    strHeadImagePathEC = strPath + "/AndroidWTone/"+str+"_HeadImageEC.jpg";
                    strSIDHeadPath = strPath + "/AndroidWTone/IDCardProdTest/";
                    strSIDHeadPathName = strPath + "/AndroidWTone/IDCardProdTest/zp.bmp";
                }
                //开启OCR
                OcrThread  ocrThread=new OcrThread(handler,idCardAPI,copyAssetsUtil);
                ocrThread.start();//end -> run
            }
        },200);

    }

    private long mPressedTime = 0;

    @Override
    public void onBackPressed() {
        long mNowTime = System.currentTimeMillis();//获取第一次按键时间
        if((mNowTime - mPressedTime) > 2000){//比较两次按键时间差
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mPressedTime = mNowTime;
        }
        else {//退出程序
            isExitApp = true;
            idCardAPI.FreeIDCard();
            this.finish();
            System.exit(0);
        }
    }


    /**
     * 更新数据源
     * @param path
     */
    private void addPathDate(String path) {
        if(path!=null&&!path.equals("")){
            File file = new File(path);
            if (file.exists()) {
                tupianPath.add(path);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isExitApp=true;
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        isExitApp=true;
        idCardAPI.FreeIDCard();
        super.onDestroy();
    }


    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageSelected(int position) {
        current = position;

        for (int i = 0; i < tips.length; i++) {
            if (i == position) {
                tips[i].setBackgroundResource(R.mipmap.page_indicator_focused);
            } else {
                tips[i].setBackgroundResource(R.mipmap.page_indicator_unfocused);
            }
        }
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * 绑定数据设置adapter
     */
    private void bindDate() {
        myPagerAdapter = new MyPagerAdapter(ScanViewActivity.this, tupianPath);
        myViewPager.setAdapter(myPagerAdapter);
        myViewPager.setOnPageChangeListener(this);
        myViewPager.setCurrentItem(current);
    }

    private void showImage()
    {

        viewGroup.removeAllViews();
        tips = new ImageView[tupianPath.size()];
        for (int i = 0; i < tips.length; i++) {
            ImageView imageView = new ImageView(ScanViewActivity.this);
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(10, 10));
            tips[i] = imageView;
            if (i == 0) {
                tips[i].setBackgroundResource(R.mipmap.page_indicator_focused);
            } else {
                tips[i].setBackgroundResource(R.mipmap.page_indicator_unfocused);
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(10,
                    10));
            layoutParams.leftMargin = 5;
            layoutParams.rightMargin = 5;
            viewGroup.addView(imageView, layoutParams);
        }
        current = 0;
        myPagerAdapter.notifyDataSetChanged();
        if(tupianPath.size()>0){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;//这个参数设置为true才有效，
            Bitmap bmp = BitmapFactory.decodeFile(tupianPath.get(0), options);//这里的bitmap是个空
            int outHeight=options.outHeight;
            int outWidth= options.outWidth;
            FrameLayout.LayoutParams fl=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            textView.setLayoutParams(fl);
        }
    }
    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //申请权限结果回调：
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 通过grantResults判断有没有获取到权限，如果没有，用下面的方法判断用户点禁止的时候有没有勾选不在提示。
        for (String permission : permissions) {
            //如果返回true表示用户点了禁止获取权限，但没有勾选不再提示。
            //返回false表示用户点了禁止获取权限，并勾选不再提示。
            //我们可以通过该方法判断是否要继续申请权限
            boolean shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
        }
    }
}