package com.github.queatz.androiduxextras;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.queatz.slidescreen.SlideScreen;
import com.github.queatz.slidescreen.SlideScreenAdapter;

import static com.github.queatz.androiduxextras.SampleFragment.EXTRA_SLIDE_NUMBER;

public class SampleActivity extends AppCompatActivity {

    private SlideScreen slideScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        slideScreen = findViewById(R.id.slideScreen);
        slideScreen.setAdapter(new SlideScreenAdapter() {
            @Override
            public int getCount() {
                return 5;
            }

            @Override
            public Fragment getSlide(int slide) {
                Fragment fragment = new SampleFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(EXTRA_SLIDE_NUMBER, slide);
                fragment.setArguments(bundle);
                return fragment;
            }

            @Override
            public FragmentManager getFragmentManager() {
                return SampleActivity.this.getFragmentManager();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!slideScreen.isExpose()) {
            slideScreen.expose(true);
        } else {
            super.onBackPressed();
        }
    }
}
