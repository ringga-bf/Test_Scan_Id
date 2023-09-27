package demo.test.com.testidcard;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by A@H on 2018/3/8.
 */

public class MyPagerAdapter extends PagerAdapter {
    private List<String> docs=new ArrayList<String>();
    private Context context;

    public int getSpacewidth() {
        return spacewidth;
    }

    private int spacewidth;
    public  MyPagerAdapter(Context context,List<String> docs){
        this.context=context;
        this.docs=docs;
    }
    @Override
    public int getCount() {
        return docs.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {

        final ImageView imageView = new ImageView(context);
        container.addView(imageView);
        ImageLoader.getInstance().clearMemoryCache();
        ImageLoader.getInstance().loadImage("file://" + docs.get(position),/*Options,*/ new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                imageView.setImageBitmap(loadedImage);
            };
        });

        return imageView;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((View) object);
    }
}
