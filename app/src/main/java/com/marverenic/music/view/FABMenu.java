package com.marverenic.music.view;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.marverenic.music.R;

import java.util.ArrayList;
import java.util.List;

public class FABMenu extends FloatingActionButton implements View.OnClickListener {

    private static final String TAG = "FABMenu";

    private final List<FloatingActionButton> children = new ArrayList<>();
    private final List<TextView> labels = new ArrayList<>();
    private final List<String> childNames = new ArrayList<>();
    private boolean childrenVisible = false;

    public FABMenu(Context context) {
        super(context);
        setOnClickListener(this);
    }

    public FABMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    public FABMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(this);
    }

    public void addChild(@DrawableRes int icon, OnClickListener onClickListener, String label){
        children.add(buildChild(icon, onClickListener, label));
        labels.add(buildChildLabel(label));
        childNames.add(label);
    }

    public void addChild(@DrawableRes int icon, OnClickListener onClickListener, @StringRes int label){
        final String name = getResources().getString(label);
        children.add(buildChild(icon, onClickListener, name));
        labels.add(buildChildLabel(name));
        childNames.add(name);
    }

    private FloatingActionButton buildChild(@DrawableRes int icon, final OnClickListener onClickListener, String label){
        FloatingActionButton button = (FloatingActionButton)
                LayoutInflater.from(getContext())
                        .inflate(R.layout.mini_fab, (ViewGroup) getParent(), true)
                        .findViewWithTag("fab-null");

        button.setTag("fab-" + label);
        button.setImageResource(icon);
        button.setVisibility(GONE);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(v);
                hideChildren();
            }
        });

        if (getParent() instanceof CoordinatorLayout){
            final float padding = getResources().getDimension(R.dimen.fab_margin);
            final float dpScale = getResources().getDisplayMetrics().density;

            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) button.getLayoutParams();
            params.rightMargin += padding;
            params.bottomMargin = (int) (56 * dpScale + padding * (2 + children.size()) + 40 * dpScale * children.size());

            button.setLayoutParams(params);
        }
        else{
            Log.e(TAG, "Parent must be a CoordinatorLayout to properly set margin");
        }

        return button;
    }

    private TextView buildChildLabel(String name){
        TextView label = (TextView)
                LayoutInflater.from(getContext())
                        .inflate(R.layout.mini_fab_label, (ViewGroup) getParent(), true)
                        .findViewWithTag("fab-label-null");

        label.setTag("fab-label-" + label);
        label.setText(name);
        label.setVisibility(GONE);

        if (getParent() instanceof CoordinatorLayout){
            final float padding = getResources().getDimension(R.dimen.fab_margin);
            final float dpScale = getResources().getDisplayMetrics().density;

            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) label.getLayoutParams();
            params.rightMargin += padding + 40 * dpScale;
            params.bottomMargin = (int) (56 * dpScale + 4 * dpScale + padding * (2 + labels.size()) + 40 * dpScale * labels.size());

            label.setLayoutParams(params);
        }
        else{
            Log.e(TAG, "Parent must be a CoordinatorLayout to properly set margin");
        }

        return label;
    }

    public void show(){
        Animation fabAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fab_in);
        fabAnim.setDuration(300);
        fabAnim.setInterpolator(getContext(), android.R.interpolator.decelerate_quint);

        startAnimation(fabAnim);

        // Make sure the FAB is visible when the animation starts
        setVisibility(View.VISIBLE);
    }

    public void hide(){
        if (childrenVisible){
            hideChildren();
            childrenVisible = false;
        }

        Animation fabAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fab_out);
        fabAnim.setDuration(300);
        fabAnim.setInterpolator(getContext(), android.R.interpolator.accelerate_quint);

        startAnimation(fabAnim);

        // Make sure to hide the FAB after the animation finishes and reset its rotation
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.GONE);
                setRotation(0f);
            }
        }, 300);
    }

    public void showChildren(){
        childrenVisible = true;

        // Start a sliding animation on each child
        for (int i = 0; i < children.size(); i++) {
            final FloatingActionButton child = children.get(i);
            final TextView label = labels.get(i);

            final float padding = getResources().getDimension(R.dimen.fab_margin);
            final float dpScale = getResources().getDisplayMetrics().density;

            final float dY = 28 * dpScale + padding + (padding + 40 * dpScale) * i;

            TranslateAnimation translateAnim = new TranslateAnimation(0, 0, dY, 0);
            AlphaAnimation fadeAnim = new AlphaAnimation(0, 1);

            AnimationSet slideFadeAnim = new AnimationSet(true);
            slideFadeAnim.addAnimation(translateAnim);
            slideFadeAnim.addAnimation(fadeAnim);
            slideFadeAnim.setInterpolator(getContext(), android.R.interpolator.decelerate_quint);
            slideFadeAnim.setDuration(300 + 25 * i);

            child.startAnimation(slideFadeAnim);

            // Make sure the FABs are visible when the animation starts
            child.setVisibility(VISIBLE);
        }

        //Delay the label animation
        postDelayed(new Runnable() {
            @Override
            public void run() {
                final AlphaAnimation fadeAnim = new AlphaAnimation(0, 1);
                fadeAnim.setDuration(400);
                fadeAnim.setInterpolator(getContext(), android.R.interpolator.decelerate_quint);

                for (TextView l : labels) {
                    l.setVisibility(VISIBLE);
                    l.startAnimation(fadeAnim);
                }
            }
        }, 500 + 25 * children.size());

        // Rotate the main FAB icon by 45 degrees to form a close button
        Animation rotateAnim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                setRotation(45 * interpolatedTime);
            }
        };
        rotateAnim.setInterpolator(getContext(), android.R.interpolator.decelerate_quint);
        rotateAnim.setDuration(300);

        startAnimation(rotateAnim);
    }

    public void hideChildren(){
        childrenVisible = false;

        Animation fabAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fab_out);
        fabAnim.setDuration(300);
        fabAnim.setInterpolator(getContext(), android.R.interpolator.accelerate_quint);

        Animation labelAnim = AnimationUtils.loadAnimation(getContext(), R.anim.abc_fade_out);
        labelAnim.setDuration(300);
        labelAnim.setInterpolator(getContext(), android.R.interpolator.accelerate_quint);

        for (FloatingActionButton c : children) {
            c.startAnimation(fabAnim);
        }
        for (TextView l : labels){
            l.startAnimation(labelAnim);
        }

        // Make sure to hide the FABs after the animation finishes
        postDelayed(new Runnable() {
            @Override
            public void run() {
                for (FloatingActionButton c : children) {
                    c.setVisibility(GONE);
                }
                for (TextView l : labels){
                    l.setVisibility(GONE);
                }
            }
        }, 300);

        // Rotate the main FAB icon by 45 degrees to invert the original rotation
        Animation slideAnim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                setRotation(45 + 45 * interpolatedTime);
            }
        };
        slideAnim.setInterpolator(getContext(), android.R.interpolator.decelerate_quint);
        slideAnim.setDuration(300);

        startAnimation(slideAnim);
    }

    @Override
    public void onClick(View v) {
        if (childrenVisible){
            hideChildren();
        }
        else{
            showChildren();
        }
    }
}