package com.example.adisyonuygulamajava.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ViewUtils {

    public static void sizeBelirle(View view, SizeType width, SizeType height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            params = new LinearLayout.LayoutParams(0, 0);
        }
        params.width = width.toLayoutParam();
        params.height = height.toLayoutParam();
        view.setLayoutParams(params);
    }

    public static void sizeBelirle(View view, int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            params = new LinearLayout.LayoutParams(0, 0);
        }
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }

    public static void marginEkle(View view, int top, int bottom, int left, int right) {
        ViewGroup.MarginLayoutParams params;
        ViewGroup.LayoutParams lp = view.getLayoutParams();

        if (lp instanceof ViewGroup.MarginLayoutParams) {
            params = (ViewGroup.MarginLayoutParams) lp;
        } else {
            params = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        params.setMargins(left, top, right, bottom);
        view.setLayoutParams(params);
    }

    public static void paddingEkle(View view, int top, int bottom, int left, int right) {
        view.setPadding(left, top, right, bottom);
    }

    public static void paddingEkleAll(View view, int all) {
        view.setPadding(all, all, all, all);
    }

    public static void paddingEkleHorizontal(View view, int horizontal) {
        view.setPadding(horizontal, 0, horizontal, 0);
    }

    public static void paddingEkleVertical(View view, int vertical) {
        view.setPadding(0, vertical, 0, vertical);
    }

    public static int dpToPx(int dp, Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale);
    }
}

