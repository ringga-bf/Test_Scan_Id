package demo.test.com.testidcard;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;


import com.batamfast.testscanid.R;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;


/**
 * Created by A@H on 2017/11/3.
 */

public class RecycleApplication extends Application {



    @Override
    public void onCreate() {
        super.onCreate();
        initLoder();
    }
    void initLoder(){
        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                // 设置图片在下载期间显示的图片
                .showImageOnLoading(R.mipmap.no_picture)
                .showImageForEmptyUri(R.mipmap.no_picture)
                // 设置图片加载/解码过程中错误时候显示的图片
                .showImageOnFail(R.mipmap.no_picture)
                // 设置下载的图片是否缓存在内存中
                .cacheInMemory(true)
                // 设置下载的图片是否缓存在SD卡中
                .cacheOnDisk(false)
                // 设置图片在下载前是否重置，复位
                .resetViewBeforeLoading(false)
                // 保留Exif信息
                .considerExifParams(false)
                // 设置图片的解码类型
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                RecycleApplication.this)
                //即保存的每个缓存文件的最大长宽
                .memoryCacheExtraOptions(400, 400)
                //设置缓存的详细信息，最好不要设置这个
//                .diskCacheExtraOptions(400, 400, null)
                // 线程池内加载的数量
                .threadPoolSize(5)
                //线程优先级  default Thread.NORM_PRIORITY - 1
                .threadPriority(Thread.NORM_PRIORITY)
                // default FIFO
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                // default
//                .denyCacheImageMultipleSizesInMemory()
                //你可以通过自己的内存缓存实现
//                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
//                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13)
                // default
                .diskCache(new UnlimitedDiskCache(StorageUtils.getCacheDirectory(RecycleApplication.this,true)))
                //硬盘缓存50MB
                .diskCacheSize(50 * 1024 * 1024)
                //缓存的File数量
                .diskCacheFileCount(100)
                //将保存的时候的URI名称用HASHCODE加密
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                // default
                .imageDownloader(new BaseImageDownloader(RecycleApplication.this))
                // default
                .imageDecoder(new BaseImageDecoder(false))
                // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                // default
                .defaultDisplayImageOptions(imageOptions)
                .build();
        ImageLoader.getInstance().init(config);

    }


}
