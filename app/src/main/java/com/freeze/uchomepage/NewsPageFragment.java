package com.freeze.uchomepage;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2016/10/9.
 */

public class NewsPageFragment extends Fragment {

    public DragTracker getDragTrack() {
        return dragTrack;
    }

    private DragTracker dragTrack;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_page, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArcBottomView arcBottomView = (ArcBottomView) view.findViewById(R.id.arc_view);
        dragTrack = (DragTracker) view.findViewById(R.id.drag_tracker);

        dragTrack.addDragActionReceiver(arcBottomView);
    }
}
