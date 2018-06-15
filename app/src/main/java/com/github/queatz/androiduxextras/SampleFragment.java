package com.github.queatz.androiduxextras;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SampleFragment extends Fragment {

    public static final String EXTRA_SLIDE_NUMBER = "slideNumber";

    private int slideNumber;

    @Override
    public void setArguments(Bundle args) {
        slideNumber = args.getInt(EXTRA_SLIDE_NUMBER);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sample, container, false);

        TextView textView = view.findViewById(R.id.textView);
        textView.setText(getString(R.string.slide_number, slideNumber + 1));

        view.setBackgroundResource(new int[] {
                R.color.red500,
                R.color.purple500,
                R.color.lightblue500,
                R.color.green500,
                R.color.amber500
        }[slideNumber]);

        return view;
    }
}
