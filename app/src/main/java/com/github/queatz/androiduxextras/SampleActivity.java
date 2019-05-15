package com.github.queatz.androiduxextras;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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
                return SampleActivity.this.getSupportFragmentManager();
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
