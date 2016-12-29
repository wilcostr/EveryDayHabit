package za.co.twinc.everydayhabit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PageFragment extends Fragment {

    TextView textViewTemp;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_page_fragment, container, false);
        Bundle bundle = getArguments();
        textViewTemp = (TextView) view.findViewById(R.id.textViewTemp);
        textViewTemp.setText(Integer.toString(bundle.getInt("num")));
        return view;
    }

}
