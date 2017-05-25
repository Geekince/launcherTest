package com.kapp.knews.utils;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * description：动画辅助类
 * <br>author：caowugao
 * <br>time： 2017/01/03 12:26
 */

public class AnimatorUtil {
    private AnimatorUtil() {
    }


    /**
     * 移动动画
     *
     * @param startView 动画的起点位置
     * @param endView   动画的终点位置
     * @param mContext
     * @param root      父窗体 用于添加的动画的View
     * @param time      动画持续时间单位s
     * @param listener
     */
    public static void startMoveAnimator(final View startView, View endView, Context mContext, final ViewGroup root,
                                         final float time, final Animator.AnimatorListener listener) {

        //新建一个ImageView 用于动画显示
        final ImageView moveView = new ImageView(mContext);
        //确定ImageView大小与传进来的ImageView相同
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(startView.getWidth(), startView
                .getHeight());
        //获取ImageView的图片 并设置在新的ImageView上

        final Bitmap startBitmap = BitmapUtil.getViewBitmap(startView);
        moveView.setImageDrawable(new BitmapDrawable(mContext.getResources(), startBitmap));

        //计算父控件的位置
        int[] parent = new int[2];
        root.getLocationInWindow(parent);

        //计算起点控件位置
        int[] startLocation = new int[2];
        startView.getLocationInWindow(startLocation);

        //计算终点控件位置
        int[] endLocation = new int[2];
        endView.getLocationInWindow(endLocation);

        //确定ImageView的位置与startView相同
        params.leftMargin = startLocation[0] - parent[0] - root.getPaddingLeft();
        params.topMargin = startLocation[1] - parent[1] - root.getPaddingTop();
        root.addView(moveView, params);

        //计算两者的横向X轴的距离差
        int XtoX = endLocation[0] - startLocation[0] + endView.getWidth() / 2 - startView.getWidth() / 2;
        //根据距离 时间 获取到对应的X轴的初速度
        final float xv = XtoX / time;

        //计算两者的横向X轴的距离差
        int YtoY = endLocation[1] - startLocation[1];
        //根据距离 时间 初始设置的Y轴初速度与X轴初速度相同 获取到竖直方向上的加速度
        final float g;
        if (xv > 0) {
            g = (YtoY + xv * time) / time / time * 2;
        } else {
            g = (YtoY - xv * time) / time / time * 2;
        }
        ValueAnimator va = new ValueAnimator();
        va.setDuration((long) (time * 1000));
        va.setObjectValues(new PointF(0, 0));
        //计算位置
        va.setEvaluator(new TypeEvaluator<PointF>() {
            @Override
            public PointF evaluate(float v, PointF pointF, PointF t1) {
                PointF point = new PointF();
                point.x = v * xv * time;
                if (xv > 0) {
                    point.y = g * (v * time) * (v * time) / 2 - xv * v * time;
                } else {
                    point.y = g * (v * time) * (v * time) / 2 + xv * v * time;

                }
                return point;
            }
        });


        va.start();

        //设置动画
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                PointF point = (PointF) valueAnimator.getAnimatedValue();
                moveView.setTranslationX(point.x);
                moveView.setTranslationY(point.y);
            }
        });

        //在动画结束时去掉动画添加的ImageView
        va.addListener(new android.animation.AnimatorListenerAdapter() {

            @Override
            public void onAnimationCancel(Animator animation) {
                if (null != listener) {
                    listener.onAnimationCancel(animation);
                }

            }

            @Override
            public void onAnimationStart(Animator animation) {
                if (null != listener) {
                    listener.onAnimationStart(animation);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if (null != listener) {
                    listener.onAnimationRepeat(animation);

                }
            }


            @Override
            public void onAnimationEnd(Animator animator) {
                root.removeView(moveView);
                BitmapUtil.recycleBitmap(startBitmap);
                if (null != listener) {
                    listener.onAnimationEnd(animator);

                }
            }
        });
    }

}
