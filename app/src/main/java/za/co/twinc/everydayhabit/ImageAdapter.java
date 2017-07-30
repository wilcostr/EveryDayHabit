package za.co.twinc.everydayhabit;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import java.util.Locale;

import static za.co.twinc.everydayhabit.MainActivity.NUM_LOG_ENTRIES;

/*
 * Created by wilco on 2016/11/03.
 */

class ImageAdapter extends BaseAdapter {
    final private Context mContext;
    final private int[] items;
    final private int offset;
    private final int[] ticks;

    ImageAdapter(Context c, int[] log_items, int habit_offset) {
        mContext = c;
        items = log_items;
        offset = habit_offset;
        ticks = new int[]{R.drawable.tick_green, R.drawable.tick_red, R.drawable.tick_orange, android.R.drawable.ic_menu_help};
    }

    public int getCount() {
        return NUM_LOG_ENTRIES;
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
            int scaledSize = (int) mContext.getResources().getDisplayMetrics().density * 32;
            textV = new TextView(mContext);
            textV.setLayoutParams(new GridView.LayoutParams(scaledSize, scaledSize));

            if (items[position] >= 0){
                textV.setBackgroundResource(ticks[items[position]]);
                if (items[position]==3 && Build.VERSION.SDK_INT >= 21)
                    textV.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#000000")));
            }
            else{
                textV.setText(String.format(Locale.UK, "%d", position + 1 + offset));
                textV.setTextSize(18);
                textV.setTypeface(null, Typeface.BOLD);
                textV.setGravity(Gravity.CENTER);
            }

        } else {
            textV = (TextView) convertView;
        }

        return textV;
    }
}
