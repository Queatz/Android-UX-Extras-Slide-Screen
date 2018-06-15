package com.github.queatz.slidescreen;

import android.app.Fragment;
import android.app.FragmentManager;

public interface SlideScreenAdapter {
    int getCount();
    Fragment getSlide(int slide);
    FragmentManager getFragmentManager();
}
