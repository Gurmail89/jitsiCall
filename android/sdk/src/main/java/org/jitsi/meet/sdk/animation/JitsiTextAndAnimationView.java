package org.jitsi.meet.sdk.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.jitsi.meet.sdk.R;

/**
 * Created by gurmail on 13/08/20.
 *
 * @author gurmail
 */

public final class JitsiTextAndAnimationView extends LinearLayout {
    @NotNull
    public TextView textToShow;
    @NotNull
    public JitsiDotAnimatedTextView animatedTextView;

    @NotNull
    public final TextView getTextToShow() {
        TextView var10000 = this.textToShow;
        return var10000;
    }

    public final void setTextToShow(@NotNull TextView var1) {
        this.textToShow = var1;
    }

    @NotNull
    public final JitsiDotAnimatedTextView getAnimatedTextView() {
        JitsiDotAnimatedTextView var10000 = this.animatedTextView;
        return var10000;
    }

    public final void setAnimatedTextView(@NotNull JitsiDotAnimatedTextView var1) {
        this.animatedTextView = var1;
    }

    private final void showTextAndAnimation(Context context, AttributeSet attrs) {
        LinearLayout.inflate(context, R.layout.layout, (ViewGroup)this);
        View var10001 = this.findViewById(R.id.text_to_show);
        this.textToShow = (TextView)var10001;
        var10001 = this.findViewById(R.id.progress_dots_txt);
        this.animatedTextView = (JitsiDotAnimatedTextView)var10001;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.JitsiTextAndAnimationView, 0, 0);

        try {
            CharSequence text = ta.getText(R.styleable.JitsiTextAndAnimationView_setText);
            CharSequence textHint = ta.getText(R.styleable.JitsiTextAndAnimationView_setTextHint);
            int color = ta.getInt(R.styleable.JitsiTextAndAnimationView_setTextColor, 0);
            float textSize = ta.getFloat(R.styleable.JitsiTextAndAnimationView_setTextSize, 0.0F);
            int dotsCount = ta.getInt(R.styleable.JitsiTextAndAnimationView_numberOfDots, 3);
            if (text != null) {
                this.setText(text);
            }

            if (textHint != null) {
                this.setTextHint(textHint);
            }

            if (color != 0) {
                this.setTextColor(color);
            }

            if (textSize != 0.0F) {
                this.setTextSize(textSize);
            }

            if (dotsCount != 0) {
                this.noOfDots(dotsCount);
            }
        } finally {
            ta.recycle();
        }

        JitsiDotAnimatedTextView var10000 = this.animatedTextView;
        var10000.showDotsAnimation();
    }

    public final void setText(@NotNull CharSequence text) {
        TextView var10000 = this.textToShow;
        var10000.setText(text);
    }

    public final void setTextSize(float size) {
        TextView var10000 = this.textToShow;
        var10000.setTextSize(size);
        JitsiDotAnimatedTextView var2 = this.animatedTextView;
        var2.setTextSize(size);
    }

    public final void setTextHint(@NotNull CharSequence textHint) {
        TextView var10000 = this.textToShow;
        var10000.setHint(textHint);
    }

    public final void setTextColor(int color) {
        TextView var10000 = this.textToShow;
        var10000.setTextColor(color);
        JitsiDotAnimatedTextView var2 = this.animatedTextView;
        var2.setTextColor(color);
    }

    public final void stopAnimation() {
        JitsiDotAnimatedTextView var10000 = this.animatedTextView;
        var10000.stopAnimation();
    }

    public final void noOfDots(int dotsCount) {
        JitsiDotAnimatedTextView var10000 = this.animatedTextView;
        var10000.noOfDots(dotsCount);
    }

    public final void animationDelay(long animationDelayTime) {
        JitsiDotAnimatedTextView var10000 = this.animatedTextView;
        var10000.animationDelay(animationDelayTime);
    }

    public JitsiTextAndAnimationView(@NotNull Context context) {
        super(context);
    }

    public JitsiTextAndAnimationView(@NotNull Context context, @NotNull AttributeSet attrs) {
        super(context, attrs);
        this.showTextAndAnimation(context, attrs);
    }

    public JitsiTextAndAnimationView(@NotNull Context context, @NotNull AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.showTextAndAnimation(context, attrs);
    }
}
