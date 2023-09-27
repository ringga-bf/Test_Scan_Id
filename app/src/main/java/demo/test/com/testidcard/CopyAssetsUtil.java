package demo.test.com.testidcard;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by A@H on 2018/3/7.
 */

public class CopyAssetsUtil {
  private Context context;
  public CopyAssetsUtil(Context context){
    this.context=context;
  }
  public   void copyAssets(){
    try {
      String versionName = null;
      InputStream iStream = context.getAssets().open("idcardocr/"+"version.txt");
      int size_is = iStream.available();
      byte byte_new[] = new byte[size_is];
      iStream.read(byte_new);
      iStream.close();
      versionName = new String(byte_new);
      String versiontxt = "";
      String paths = getSDPath();
      if (paths != null && !paths.equals("")) {
        String versionpath = paths+"/AndroidWTone/IDCardProdTest/version.txt";
        File versionfile = new File(versionpath);
        if (versionfile.exists()) {
          FileReader fileReader = new FileReader(versionpath);
          BufferedReader br = new BufferedReader(fileReader);
          String r = br.readLine();
          while (r != null) {
            versiontxt += r;
            r = br.readLine();
          }
          br.close();
          fileReader.close();
        }
        if (!versionName.equals(versiontxt)) {
          File dir = new File(getSDPath()+"/AndroidWTone/IDCardProdTest/");
          if (!dir.exists()) {
            dir.mkdirs();
          }else {
            dir.delete();
          }
          getAssetToSD(paths+"/AndroidWTone/IDCardProdTest","idcardocr");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 获取sd卡根目录
   *
   * @return
   */
  private  String getSDPath() {
    File sdDir = null;
    boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
    if (sdCardExist) {
      sdDir = Environment.getExternalStorageDirectory();// 获取sdk跟目录
    }else{
      sdDir = Environment.getRootDirectory();// 获取内部存储根目录
   }
    return sdDir.toString();

  }
  /**
   * 。。。
   * @param url  SD卡路径
   * @param assetsName  资产目录的文件夹名称
   */
  private void getAssetToSD(String url,String assetsName){

    try {
      String[] fileContents = context.getAssets().list(assetsName);
      for (int i = 0; i <fileContents.length ; i++) {
        if(context.getAssets().list(assetsName+"/"+fileContents[i]).length>0){//如果大于0 则证明这是个文件夹
          copyFolderToSD(fileContents[i],url);
          //当然，这里进入到子文件夹，那么，我们就需要检查这个子文件夹中是否有需要拷贝的文件，就需要调用当前的方法
          //递归。。。
          getAssetToSD(url+"/"+fileContents[i],assetsName+"/"+fileContents[i]);
        }else{
          //如果不是文件夹 那么则进行拷贝工作，就是一些流文件的操作。。。
          getFileCopy(context.getAssets().open(assetsName+"/"+fileContents[i]),url+"/"+fileContents[i]);
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * 创建文件夹
   */
  private void copyFolderToSD(String folderFileName,String sdUrl){
    File file = new File(sdUrl,folderFileName);
    if(!file.exists()){
      file.mkdir();
    }
  }

  /**
   * 复制文件
   * @param is  文件流
   * @param mUrl 需要拷贝到SD卡的路径...
   * @throws IOException
   */
  private void getFileCopy(InputStream is,String mUrl) {
    File file = new File(mUrl);

    try {
      if(!file.exists()){
        file.createNewFile();
      }
      OutputStream myOutput = new FileOutputStream(mUrl);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = is.read(buffer)) > 0) {
        myOutput.write(buffer, 0, length);
      }
      myOutput.flush();
      myOutput.close();
      is.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  /**
   * 删除工作
   * @param url  需要删除内容的路径。。。
   */
  private void delFile(String url){
    File file = new File(url);
    if(!file.exists()){
      return;
    }

    String[] delFileList = file.list();
    for (int i = 0; i <delFileList.length ; i++) {
      String childFileUrl = url+"/"+delFileList[i];
      File childFile = new File(childFileUrl);
      if(childFile.isFile()){//判断是否是文件
        childFile.delete();
      }else{
        //不是文件
        delFile(childFileUrl);
      }
      //删除当前子文件夹
      childFile.delete();
    }

  }
}
