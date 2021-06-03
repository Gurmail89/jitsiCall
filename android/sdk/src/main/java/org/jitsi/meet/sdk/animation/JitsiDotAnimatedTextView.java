package org.jitsi.meet.sdk.animation;

import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Rect;
import android.widget.LinearLayout;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by gurmail on 13/08/20.
 *
 * @author gurmail
 */

public class JitsiDotAnimatedTextView extends AppCompatTextView {

    private Handler threadHandler = null;

    private Runnable runnable = null;

    private int dotsCount = 3; // default is 4 dots count

    private Long animationDelayTime = 500l; // default is 5 ms

    private int tempDots = 0;


    public JitsiDotAnimatedTextView(Context context) {
        super(context);
    }

    public JitsiDotAnimatedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JitsiDotAnimatedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     *call to start showing Animation
     */
    public void showDotsAnimation() {
        setWidthToRemoveAnimationLack();
        setText("");
        if (threadHandler == null && runnable == null) {
            threadHandler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    threadHandler.postDelayed(this, animationDelayTime);
                    if (tempDots == dotsCount) {
                        tempDots = 0;
                        setText("");
                    } else {
                        setText(getDot(++tempDots));
                    }
                    invalidate();
                }
            };
            runnable.run();
        }
    }

    /**
     *Simple hack to overcome layout vibration
     */
    public void setWidthToRemoveAnimationLack() {
        this.setVisibility(View.INVISIBLE);
        setText(getDot(dotsCount));
        Rect bounds = new Rect();
        Paint textPaint = this.getPaint();
        textPaint.getTextBounds(getDot(dotsCount), 0, this.length(), bounds);
        int width = bounds.width() + 100;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(params);
        this.setVisibility(View.VISIBLE);
    }

    /**
     *logic to set dots according to dots count
     */
    private String getDot(int dotNo) {
        /*StringBuilder sb = new StringBuilder();
        for(int i=0;i<dotNo;i++) {
            sb.append(".");
        }
        return sb.toString();*/
        StringBuilder sb = new StringBuilder();
        int i = 1;
        int var4 = dotNo;
        if (i <= dotNo) {
            while(true) {
                sb.append(".");
                if (i == var4) {
                    break;
                }

                ++i;
            }
        }
        return sb.toString();
    }

    public void stopAnimation() {
        threadHandler.removeCallbacks(runnable);
    }

    public void noOfDots(int dotsCount) {
        this.dotsCount = dotsCount;
    }

    public void animationDelay(Long animationDelayTime) {
        this.animationDelayTime = animationDelayTime;
    }

}
