package com.example.projectcuoiky;

import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private final int space;

    public SpaceItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int column = position % 2;

        // Căn lề trái/phải cho từng cột
        outRect.left = column == 0 ? space : space / 2;
        outRect.right = column == 0 ? space / 2 : space;

        outRect.top = space;
        outRect.bottom = space;
    }
}
