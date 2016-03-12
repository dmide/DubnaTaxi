package ru.dmide.dubnataxi;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * Created by dmide on 10/03/2016.
 */
public class ViewHelper {

    public static ValueAnimator expand(final View v, int duration, int targetHeight) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        lp.height = 0;
        v.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, targetHeight);
        valueAnimator.addUpdateListener(animation -> {
            lp.height = (int) animation.getAnimatedValue();
            v.requestLayout();
        });
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
        return valueAnimator;
    }

    public static ValueAnimator collapse(final View v, int duration) {
        final ViewGroup.LayoutParams lp = v.getLayoutParams();
        final int initialHeight = lp.height;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(initialHeight, 0);
        valueAnimator.addUpdateListener(animation -> {
            lp.height = (int) animation.getAnimatedValue();
            v.requestLayout();
        });
        valueAnimator.addListener(new AnimationEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(View.GONE);
                lp.height = initialHeight;
            }
        });
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
        return valueAnimator;
    }

    public static int calcListViewHeight(Context c, int itemsCount) {
        Resources r = c.getResources();
        float listItemHeight = r.getDimension(R.dimen.list_item_height);
        float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, listItemHeight, r.getDisplayMetrics());
        return Math.round(itemsCount * dp);
    }

    public static Snackbar makeStyledSnack(@NonNull View view, int resId, int duration) {
        return makeStyledSnack(view, view.getContext().getString(resId), duration);
    }

    public static Snackbar makeStyledSnack(@NonNull View view, @NonNull CharSequence text, int duration) {
        Snackbar snack = Snackbar.make(view, text, duration);
        TextView tv = (TextView) snack.getView().findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        return snack;
    }

    public static abstract class AnimationEndListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }
}
