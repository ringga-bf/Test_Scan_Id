package demo.test.com.testidcard;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.batamfast.testscanid.ScanViewActivity;
import com.libIDCardReader.IDCardReader;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import product.idcard.android.IDCardAPI;

/**
 * Created by A@H on 2018/3/12.
 */

public class OcrThread extends Thread {
    private ImageView[] tips;
    private String strPath = Environment.getExternalStorageDirectory().toString();
    private String kernalResourcePath=  strPath + "/AndroidWTone/IDCardProdTest/";
    private CopyAssetsUtil copyAssetsUtil;//CopyAssetsUtil
    private IDCardAPI idCardAPI;
    private Handler handler;
    static  boolean bInitSIDSuccess;
    //二代证读卡
    static IDCardReader myReader = new IDCardReader();
    public  OcrThread(Handler handler,IDCardAPI idCardAPI,CopyAssetsUtil copyAssetsUtil){
        this.handler=handler;
        this.copyAssetsUtil=copyAssetsUtil;
        this.idCardAPI=idCardAPI;
    }
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public synchronized   void run() {
        Log.i("string",kernalResourcePath);
        String  result = "";
        if (ScanViewActivity.isExitApp) {
            return;
        }
        if (ScanViewActivity.isFirstOnCreate) {
            ScanViewActivity.isFirstOnCreate = false;
            sendMessage(0, result);
            copyAssetsUtil.copyAssets();

            //核心初始化
            ScanViewActivity.InitSucess = returnInitIDCard();

            sendMessage(10, result);
            if (ScanViewActivity.InitSucess != 0) {

                result = "初始化失败，返回值为:" + String.valueOf(ScanViewActivity.InitSucess);
                sendMessage(15, result);
                return;
            } else {

                if(idCardAPI.GetSupportSID() == 0)
                {
                    if (myReader.openPort() != 0x90)
                    {
                        bInitSIDSuccess = false;
                    }
                    else
                    {
                        bInitSIDSuccess = true;
                    }
                }
                else
                {
                    bInitSIDSuccess = false;
                }
                //*/
                //设置指示灯状态
                idCardAPI.SetIOStatus(5,1);
                result = "初始化成功，请放入证件测试, ";
                result += idCardAPI.GetVersionInfo();
                result += "\n";
                sendMessage(15, result);
                idCardAPI.SetConfigByFile(kernalResourcePath + "/IDCardConfig.ini");
            }
        }

        //检测设备是否在线
        AtomicInteger nRet = new AtomicInteger(CheckDevice());
        if (nRet.get() != 0) {
            return;
        }

        //图片保存路径+名称
        String strImagePathTmp = strPath + "/androidWTone/Image.jpg";

        long timeStart = System.currentTimeMillis();
        int returnID = idCardAPI.DetectDocument();
        if (returnID == 1 && ScanViewActivity.InitSucess == 0) {
            idCardAPI.SetIOStatus(5,0);
            idCardAPI.SetIOStatus(6,0);
            //分类
            timeStart = System.currentTimeMillis();
            int nCardType[] = {-1};
            nRet.set(0);
            File file = new File(ScanViewActivity.strImagePath);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            int claasIf = idCardAPI.AutoProcessIDCard(nCardType);

            if (claasIf <= 0) {
                result = "自动分类失败,分类返回的数值为:" + String.valueOf(claasIf);
                //保存图像
                idCardAPI.SaveImageEx(strImagePathTmp, 2);
                idCardAPI.SetIOStatus(6,1);
                sendMessage(20, result);
                return;
            }
            result += "证件名称：" + idCardAPI.GetIDCardName() + "\n";
            //获取识别结果
            if (claasIf != 10000) {
                for (int i = 1; i < 50; ++i) {
                    String strFN = idCardAPI.GetFieldName(i);
                    if (strFN == null)
                        break;
                    String strRR = idCardAPI.GetRecogResult(i);
                    if (!strRR.isEmpty()) {
                        result += strFN + ":" + strRR + "\n";
                    }
                }
            }
            //获取条码结果
            if ((nCardType[0] & 4) > 0) {
                int nCount = idCardAPI.GetBarcodeCount();
                for (int i = 0; i < nCount; i++) {
                    String strResult = idCardAPI.GetBarcodeRecogResult(i);
                    if (strResult == null)
                        break;
                    result += strResult + "\n";
                }
            }

            result += "总耗时：" + String.valueOf((System.currentTimeMillis()  - timeStart)) + "ms\n";

            nRet.set(idCardAPI.SaveImageEx(ScanViewActivity.strImagePath, 31));
            result += "保存图片：" + String.valueOf(nRet.get()) + "\n";

            idCardAPI.SetIOStatus(5,1);

            //发送消息，继续开启线程
            if(claasIf == 2 || claasIf == 3)//如果是二代证，尝试读二代证芯片
            {
                String strReadSIDResult = ReadCard(ScanViewActivity.strSIDHeadPath);
                if(!strReadSIDResult.isEmpty()) {
                    result += strReadSIDResult;
                    sendMessage(100, result);
                }
                else
                {
                    sendMessage(102, result);
                }
            }
            else
            {
                sendMessage(102, result);
            }
        } else {
            String strReadSIDResult = ReadCard(ScanViewActivity.strSIDHeadPath);
            if(!strReadSIDResult.isEmpty()){
                sendMessage(103, strReadSIDResult);
             }else{
                sendMessage(101, result);
             }
        }
    }

    /**
     * 初始化识别核心
     * @return
     */
    private int returnInitIDCard() {
        return idCardAPI.InitIDCard("", 1, kernalResourcePath);
    }

    private  int CheckDevice() {
        //检测设备是否在线
        int nRet = idCardAPI.CheckDeviceOnlineEx();
        if (nRet == 2) {
            //设备掉线
            idCardAPI.FreeIDCard();
            return 2;
        } else if (nRet == 3) {
            //核心初始化
            String result = "";
            ScanViewActivity.InitSucess = returnInitIDCard();
            sendMessage(10, result);
            if (ScanViewActivity.InitSucess != 0) {

                result = "初始化失败，返回值为:" + String.valueOf(ScanViewActivity.InitSucess);
                sendMessage(15, result);
                return 1;
            } else {

                if(idCardAPI.GetSupportSID() == 0)
                {
                    if (myReader.openPort() != 0x90)
                    {
                        bInitSIDSuccess = false;
                    }
                    else
                    {
                        bInitSIDSuccess = true;
                    }
                }
                else
                {
                    bInitSIDSuccess = false;
                }
                //*/
                //设置指示灯状态
                idCardAPI.SetIOStatus(5,1);
                result = "初始化成功，请放入证件测试";
                sendMessage(15, result);
                idCardAPI.SetConfigByFile(kernalResourcePath + "/IDCardConfig.ini");
            }
        }
        else {
            return 0;
        }
        return 0;
    }

    /**
     * 发送消息通知界面更新
     * @param what
     * @param result 结果
     */
    private void sendMessage(int what, String result) {
        Message message = new Message();
        message.what = what;
        message.obj = result;
        handler.sendMessage(message);
    }

    private String ReadCard(String strPath)
    {
        if( !bInitSIDSuccess )
        {
            return "";
        }
        //return "";
       ///*
        int nRet = myReader.read();
        System.out.println("read failed, nRet:" + nRet);
        if(nRet != 0x41){
            return "";
        }
        String strResult = "";
        String name = myReader.getResult(IDCardReader.MSG_NAME);
        strResult += "\n 姓名:" ;
        strResult += name;

        String sex = myReader.getResult(IDCardReader.MSG_SEX);
        strResult += "\n 性别:" ;
        strResult += sex;

        String nation = myReader.getResult(IDCardReader.MSG_NATION);
        strResult += "\n 民族:" ;
        strResult += nation;

        String addr = myReader.getResult(IDCardReader.MSG_ADDR);
        strResult += "\n 住址:" ;
        strResult += addr;

        String birth = myReader.getResult(IDCardReader.MSG_BIRTH);
        strResult += "\n 出生日期:" ;
        strResult += birth;

        String dep = myReader.getResult(IDCardReader.MSG_DEPARTMENT);
        strResult += "\n 签发机关:" ;
        strResult += dep;

        String IdNum = myReader.getResult(IDCardReader.MSG_ID_NUM);
        strResult += "\n 身份号码:" ;
        strResult += IdNum;

        String Sdate = myReader.getResult(IDCardReader.MSG_START_DATE);
        strResult += "\n 签发日期:" ;
        strResult += Sdate;

        String Edate = myReader.getResult(IDCardReader.MSG_END_DATE);
        strResult += "\n 有效期至:" ;
        strResult += Edate;

        String fp = myReader.getResult(IDCardReader.MSG_FP);
        strResult += "\n 指纹信息:" ;
        strResult += fp;

        myReader.getBMP(strPath);

        return strResult;
    }


    /**************新添加的接口**************/
    /********2019.01.10 created @CXY********/
    //获取当前设备名
    private String ReturnGetCurrentDevice()
    {
        String result="";
        if(ScanViewActivity.InitSucess != 0)
        {
            System.out.print("初始化失败");
            result = "初始化失败，返回值为:" + String.valueOf(ScanViewActivity.InitSucess);
            return result;
        }
        result="当前设备名："+ idCardAPI.GetCurrentDevice();
        return result;
    }

    //检测设备是否在线
    public int CheckDeviceOnline()
    {
        if(ScanViewActivity.InitSucess != 0)
        {
            System.out.print("初始化失败");
            return -1;
        }
        return idCardAPI.CheckDeviceOnline();
    }

    //检测设备是否在线(只要有设备都可以)
    private int CheckDeviceOnlineEx()
    {
        if(ScanViewActivity.InitSucess != 0)
        {
            System.out.print("初始化失败");
            return -1;
        }
        return idCardAPI.CheckDeviceOnlineEx();
    }

    //获取设备序列号
    private String GetDeviceSN()
    {
        String sRes=idCardAPI.GetDeviceSN();
        int nlen=sRes.length();
        return sRes;
    }

    //获取设备类型
    private int GetDeviceType()
    {
        return idCardAPI.GetDeviceType();
    }

    /************************************************************************/
    /* 核实设备内的SI
    nSize:表示jstrlpSI中字符数目
    返回值：0成功；1设备未初始化；2不支持；3读取出错；4不匹配*/
    /************************************************************************/
    //核实设备的SI  ReadDeviceSI未导出，即客户只可以WriteDeviceSI和CheckDeviceSI
    private int CheckDeviceSI(String strlpSI,int nBuffLen)
    {
        return idCardAPI.CheckDeviceSI(strlpSI,nBuffLen);
    }


    /************************************************************************/
    /* 写入设备内的SI
    nSize:表示jstrlpSI中字符数目
    返回值：0成功；1设备未初始化；2不支持
    /************************************************************************/
    private int WriteDeviceSI(String strlpSI,int nBuffLen)
    {
        return idCardAPI.WriteDeviceSI(strlpSI,nBuffLen);
    }

}
