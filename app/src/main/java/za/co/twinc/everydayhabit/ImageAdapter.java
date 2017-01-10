package za.co.twinc.everydayhabit;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

/*
 * Created by wilco on 2016/11/03.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private int[] items;
    public final int[] ticks;

    public ImageAdapter(Context c, int[] log_items) {
        mContext = c;
        items = log_items;
        ticks = new int[]{R.drawable.tick_green, R.drawable.tick_red, R.drawable.tick_orange_2};
    }

    public int getCount() {
        //TODO: Not hardcode this
        return 49;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new TextView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textV;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            textV = new TextView(mContext);
            textV.setLayoutParams(new GridView.LayoutParams(65, 65));


            if (items[position] >= 0){
                textV.setBackgroundResource(ticks[items[position]]);
            }
            else{
                textV.setText(""+(position+1));
                textV.setTextSize(18);
                textV.setTypeface(null, Typeface.BOLD);
                textV.setGravity(Gravity.CENTER);
            }

            //textV.setPadding(18, 18, 18, 18);


        } else {
            textV = (TextView) convertView;
        }

        return textV;
    }
}
