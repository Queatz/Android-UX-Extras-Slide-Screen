package com.github.queatz.slidescreen;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public interface SlideScreenAdapter {
    int getCount();
    Fragment getSlide(int slide);
    FragmentManager getFragmentManager();
}
