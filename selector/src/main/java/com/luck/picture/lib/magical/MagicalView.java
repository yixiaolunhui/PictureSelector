package com.luck.picture.lib.magical;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.luck.picture.lib.R;
import com.luck.picture.lib.utils.DensityUtil;

/**
 * @author：luck
 * @date：2021/12/15 11:06 上午
 * @describe：MagicalView
 */
public class MagicalView extends FrameLayout {


    private float mAlpha = 0;

    FrameLayout contentLayout;
    View backgroundView;

    long animationDuration = 250;
    private int mOriginLeft;
    private int mOriginTop;
    private int mOriginHeight;
    private int mOriginWidth;

    private final int screenWidth;
    private final int screenHeight;
    private int targetImageTop;
    private int targetImageWidth;
    private int targetImageHeight;
    private int targetEndLeft;

    int releaseLeft = 0;
    float releaseY = 0;
    int releaseWidth = 0;
    int releaseHeight = 0;
    int realWidth;
    int realHeight;

    int imageLeftOfAnimatorEnd = 0;
    int imageTopOfAnimatorEnd = 0;
    int imageWidthOfAnimatorEnd = 0;
    int imageHeightOfAnimatorEnd = 0;

    MagicalViewWrapper imageWrapper;
    boolean isDrag = false;
    boolean isAnimating = false;

    public MagicalView(Context context) {
        this(context, null);
    }

    public MagicalView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MagicalView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        screenWidth = DensityUtil.getScreenWidth(context);
        screenHeight = DensityUtil.getScreenHeight(context);
        inflate(context, R.layout.ps_magical_layout, this);
        contentLayout = findViewById(R.id.contentLayout);
        backgroundView = findViewById(R.id.backgroundView);
        backgroundView.setAlpha(mAlpha);
        imageWrapper = new MagicalViewWrapper(contentLayout);
    }

    public void showWithoutView(int realWidth, int realHeight, boolean showImmediately) {
        this.realWidth = realWidth;
        this.realHeight = realHeight;
        mOriginLeft = 0;
        mOriginTop = 0;
        mOriginWidth = 0;
        mOriginHeight = 0;

        setVisibility(View.VISIBLE);
        setOriginParams();
        min2NormalAndDrag2Min(targetImageTop, targetEndLeft, targetImageWidth, targetImageHeight);

        if (showImmediately) {
            mAlpha = 1f;
            backgroundView.setAlpha(mAlpha);
        } else {
            mAlpha = 0f;
            backgroundView.setAlpha(mAlpha);
            contentLayout.setAlpha(0f);
            contentLayout.animate().alpha(1f).setDuration(animationDuration).start();
            backgroundView.animate().alpha(1f).setDuration(animationDuration).start();
        }
        setShowEndParams();
    }

    public void putData(int left, int top, int originWidth, int originHeight, int realWidth, int realHeight) {
        this.realWidth = realWidth;
        this.realHeight = realHeight;
        mOriginLeft = left;
        mOriginTop = top;
        mOriginWidth = originWidth;
        mOriginHeight = originHeight;
    }

    /**
     * 重新设置宽高  因为如果图片还未加载出来  默认宽高为全屏
     */
    public void resetSize(int w, int h) {
        if (this.realWidth == w && this.realHeight == h) {
            return;
        }
        this.realWidth = w;
        this.realHeight = h;
        setOriginParams();
        beginShow(true);
    }

    public void show(boolean showImmediately) {
        mAlpha = showImmediately ? mAlpha = 1f : 0f;
        backgroundView.setAlpha(mAlpha);
        setVisibility(View.VISIBLE);
        setOriginParams();
        beginShow(showImmediately);
    }

    private void setOriginParams() {
        int[] locationImage = new int[2];
        contentLayout.getLocationOnScreen(locationImage);
        targetEndLeft = 0;
        if (screenWidth / (float) screenHeight < realWidth / (float) realHeight) {
            targetImageWidth = screenWidth;
            targetImageHeight = (int) (targetImageWidth * (realHeight / (float) realWidth));
            targetImageTop = (screenHeight - targetImageHeight) / 2;
        } else {
            targetImageHeight = screenHeight;
            targetImageWidth = (int) (targetImageHeight * (realWidth / (float) realHeight));
            targetImageTop = 0;
            targetEndLeft = (screenWidth - targetImageWidth) / 2;
        }

        imageWrapper.setWidth(mOriginWidth);
        imageWrapper.setHeight(mOriginHeight);
        imageWrapper.setMarginLeft(mOriginLeft);
        imageWrapper.setMarginTop(mOriginTop);

    }

    private void beginShow(final boolean showImmediately) {
        if (showImmediately) {
            mAlpha = 1f;
            backgroundView.setAlpha(mAlpha);
            min2NormalAndDrag2Min(targetImageTop, targetEndLeft, targetImageWidth, targetImageHeight);
            setShowEndParams();
        } else {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    min2NormalAndDrag2Min(value, mOriginTop, targetImageTop, mOriginLeft, targetEndLeft,
                            mOriginWidth, targetImageWidth, mOriginHeight, targetImageHeight);
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    setShowEndParams();
                }
            });
            valueAnimator.setDuration(animationDuration).start();
            changeBackgroundViewAlpha(false);
        }
    }

    private void setShowEndParams() {
        isAnimating = false;
        setImageDataOfAnimatorEnd();
        changeContentViewToFullscreen();
        if (onMagicalViewCallback != null) {
            onMagicalViewCallback.showFinish(MagicalView.this, false);
        }
    }

    private void min2NormalAndDrag2Min(float animRatio, float startY, float endY, float startLeft, float endLeft,
                                       float startWidth, float endWidth, float startHeight, float endHeight) {
        min2NormalAndDrag2Min(false, animRatio, startY, endY, startLeft, endLeft, startWidth, endWidth, startHeight, endHeight);
    }

    private void min2NormalAndDrag2Min(float endY, float endLeft, float endWidth, float endHeight) {
        min2NormalAndDrag2Min(true, 0, 0, endY, 0, endLeft, 0, endWidth, 0, endHeight);
    }

    private void min2NormalAndDrag2Min(boolean showImmediately, float animRatio, float startY, float endY, float startLeft, float endLeft,
                                       float startWidth, float endWidth, float startHeight, float endHeight) {
        if (showImmediately) {
            imageWrapper.setWidth(endWidth);
            imageWrapper.setHeight(endHeight);
            imageWrapper.setMarginLeft((int) (endLeft));
            imageWrapper.setMarginTop((int) endY);
            return;
        }
        float xOffset = animRatio * (endLeft - startLeft);
        float widthOffset = animRatio * (endWidth - startWidth);
        float heightOffset = animRatio * (endHeight - startHeight);
        float topOffset = animRatio * (endY - startY);
        imageWrapper.setWidth(startWidth + widthOffset);
        imageWrapper.setHeight(startHeight + heightOffset);
        imageWrapper.setMarginLeft((int) (startLeft + xOffset));
        imageWrapper.setMarginTop((int) (startY + topOffset));
    }


    public void backToMin() {
        backToMin(false);
    }

    private void backToMin(boolean isDrag) {
        if (isAnimating) {
            return;
        }
        if (mOriginWidth == 0 || mOriginHeight == 0) {
            backToMinWithoutView();
            return;
        }
        beginBackToMin(false);
        if (!isDrag) {
            backToMinWithTransition();
            return;
        }
        resetContentScaleParams();
        reRebuildSize();
        setReleaseParams();
        beginBackToMin(true);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                min2NormalAndDrag2Min(value, releaseY, mOriginTop, releaseLeft, mOriginLeft, releaseWidth, mOriginWidth, releaseHeight, mOriginHeight);
            }
        });
        valueAnimator.setDuration(animationDuration).start();
        if (onMagicalViewCallback != null) {
            onMagicalViewCallback.onRelease(false, true);
        }
        changeBackgroundViewAlpha(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void backToMinWithTransition() {
        contentLayout.post(new Runnable() {
            @Override
            public void run() {
                TransitionManager.beginDelayedTransition((ViewGroup) contentLayout.getParent(), new TransitionSet()
                        .setDuration(animationDuration)
                        .addTransition(new ChangeBounds())
                        .addTransition(new ChangeTransform())
                        .addTransition(new ChangeImageTransform())

                );
                beginBackToMin(true);
                contentLayout.setTranslationX(0);
                contentLayout.setTranslationY(0);
                imageWrapper.setWidth(mOriginWidth);
                imageWrapper.setHeight(mOriginHeight);
                imageWrapper.setMarginTop(mOriginTop);
                imageWrapper.setMarginLeft(mOriginLeft);
                if (onMagicalViewCallback != null) {
                    onMagicalViewCallback.onRelease(false, true);
                }
                changeBackgroundViewAlpha(true);
            }
        });
    }

    private void resetContentScaleParams() {
        if (contentLayout.getScaleX() != 1) {
            Rect rectF = new Rect();
            contentLayout.getGlobalVisibleRect(rectF);

            RectF dst = new RectF(0, 0, screenWidth, screenHeight);
            contentLayout.getMatrix().mapRect(dst);
            contentLayout.setScaleX(1);
            contentLayout.setScaleY(1);

            imageWrapper.setWidth(dst.right - dst.left);
            imageWrapper.setHeight(dst.bottom - dst.top);
            imageWrapper.setMarginLeft((int) (imageWrapper.getMarginLeft() + dst.left));
            imageWrapper.setMarginTop((int) (imageWrapper.getMarginTop() + dst.top));
        }
    }

    private void reRebuildSize() {

    }

    private void beginBackToMin(boolean isResetSize){
        if (isResetSize) {
            contentLayout.getLayoutParams().width = 360;
            contentLayout.getLayoutParams().height = 360;
//            sketchImageView.scaleType = ImageView.ScaleType.CENTER_CROP;
        }
    }


    private void setReleaseParams() {
        //到最小时,先把imageView的大小设置为imageView可见的大小,而不是包含黑色空隙部分
        // set imageView size to visible size,not include black background
        // 注意:这里 imageWrapper.getHeight() 获取的高度 是经过拖动缩放后的
        // there imageWrapper.getHeight() is scaled height
        float draggingToReleaseScale = imageWrapper.getHeight() / (float) screenHeight;
        if (imageWrapper.getHeight() != imageHeightOfAnimatorEnd) {
            releaseHeight = (int) (draggingToReleaseScale * imageHeightOfAnimatorEnd);
        } else {
            releaseHeight = imageWrapper.getHeight();
        }
        if (imageWrapper.getWidth() != imageWidthOfAnimatorEnd) {
            releaseWidth = (int) (draggingToReleaseScale * imageWidthOfAnimatorEnd);
        } else {
            releaseWidth = imageWrapper.getWidth();
        }
        if (imageWrapper.getMarginTop() != imageTopOfAnimatorEnd) {
            releaseY = imageWrapper.getMarginTop() + (int) (draggingToReleaseScale * imageTopOfAnimatorEnd);
        } else {
            releaseY = imageWrapper.getMarginTop();
        }
        if (imageWrapper.getMarginLeft() != imageLeftOfAnimatorEnd) {
            releaseLeft = imageWrapper.getMarginLeft() + (int) (draggingToReleaseScale * imageLeftOfAnimatorEnd);
        } else {
            releaseLeft = imageWrapper.getMarginLeft();
        }
        imageWrapper.setWidth(releaseWidth);
        imageWrapper.setHeight(releaseHeight);
        imageWrapper.setMarginTop((int) releaseY);
        imageWrapper.setMarginLeft(releaseLeft);
    }


    private void backToMinWithoutView() {
        contentLayout.animate().alpha(0f).setDuration(animationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (onMagicalViewCallback != null) {
                            onMagicalViewCallback.onMojitoViewFinish();
                        }
                    }
                }).start();
        backgroundView.animate().alpha(0f).setDuration(animationDuration).start();
        if (onMagicalViewCallback != null) {
            onMagicalViewCallback.onRelease(false, true);
        }
    }

    /**
     * @param isToZero 是否透明
     */
    private void changeBackgroundViewAlpha(final boolean isToZero) {
        final float end = isToZero ? 0 : 1f;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mAlpha, end);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                isAnimating = true;
                mAlpha = (Float) animation.getAnimatedValue();
                backgroundView.setAlpha(mAlpha);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                if (isToZero) {
                    if (onMagicalViewCallback != null) {
                        onMagicalViewCallback.onMojitoViewFinish();
                    }
                }
            }
        });
        valueAnimator.setDuration(animationDuration);
        valueAnimator.start();
    }


    //不消费该事件会导致事件交还给上级
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    private boolean isTouchPointInContentLayout(View view, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        return y >= top && y <= bottom && x >= left && x <= right;
    }

    public void setMagicalContent(View view) {
        contentLayout.addView(view);
    }

    private void setViewPagerLocking(boolean lock) {
        if (onMagicalViewCallback != null) {
            onMagicalViewCallback.onLock(lock);
        }
    }

    private void changeContentViewToFullscreen() {
        targetImageHeight = screenHeight;
        targetImageWidth = screenWidth;
        targetImageTop = 0;
        imageWrapper.setHeight(screenHeight);
        imageWrapper.setWidth(screenWidth);
        imageWrapper.setMarginTop(0);
        imageWrapper.setMarginLeft(0);
    }

    private void setImageDataOfAnimatorEnd() {
        imageLeftOfAnimatorEnd = imageWrapper.getMarginLeft();
        imageTopOfAnimatorEnd = imageWrapper.getMarginTop();
        imageWidthOfAnimatorEnd = imageWrapper.getWidth();
        imageHeightOfAnimatorEnd = imageWrapper.getHeight();
    }

    private OnMagicalViewCallback onMagicalViewCallback;

    public void setOnMojitoViewCallback(OnMagicalViewCallback onMagicalViewCallback) {
        this.onMagicalViewCallback = onMagicalViewCallback;
    }

    public boolean isDrag() {
        return isDrag;
    }

    public void setBackgroundAlpha(float mAlpha) {
        this.mAlpha = mAlpha;
        backgroundView.setAlpha(mAlpha);
    }
}