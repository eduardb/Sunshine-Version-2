package com.example.android.sunshine.app;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ParallaxingOnScrollListener extends RecyclerView.OnScrollListener {

    private final View parallaxView;

    public ParallaxingOnScrollListener(View parallaxView) {
        this.parallaxView = parallaxView;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        int max = parallaxView.getHeight();
        if (dy > 0) {
            parallaxView.setTranslationY(Math.max(-max, parallaxView.getTranslationY() - dy / 2));
        } else {
            parallaxView.setTranslationY(Math.min(0, parallaxView.getTranslationY() - dy / 2));
        }
    }
}
