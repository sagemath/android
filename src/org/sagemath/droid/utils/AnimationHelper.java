package org.sagemath.droid.utils;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import com.nineoldandroids.view.animation.AnimatorProxy;
import org.sagemath.droid.R;

/**
 * A helper which has animations for different API versions, as required.
 *
 * @author Nikhil Peter Raj
 */
public class AnimationHelper {

    public static boolean isIcsOrAbove() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static ObjectAnimator Nope(View view) {
        int delta = view.getResources().getDimensionPixelOffset(R.dimen.spacing_medium);

        PropertyValuesHolder pvhTranslateX = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X,
                Keyframe.ofFloat(0f, 0),
                Keyframe.ofFloat(.10f, -delta),
                Keyframe.ofFloat(.26f, delta),
                Keyframe.ofFloat(.42f, -delta),
                Keyframe.ofFloat(.58f, delta),
                Keyframe.ofFloat(.74f, -delta),
                Keyframe.ofFloat(.90f, delta),
                Keyframe.ofFloat(1f, 0f)
        );

        return ObjectAnimator.ofPropertyValuesHolder(view, pvhTranslateX).
                setDuration(500);
    }

    public static com.nineoldandroids.animation.ObjectAnimator SupportNope(View view) {
        float delta = view.getResources().getDimensionPixelOffset(R.dimen.spacing_medium);

        com.nineoldandroids.animation.PropertyValuesHolder pvhTranslateX =
                com.nineoldandroids.animation.PropertyValuesHolder.ofKeyframe("translationX",
                        com.nineoldandroids.animation.Keyframe.ofFloat(0f, 0f), com.nineoldandroids.animation.Keyframe.ofFloat(.10f, -delta)
                        , com.nineoldandroids.animation.Keyframe.ofFloat(.26f, delta), com.nineoldandroids.animation.Keyframe.ofFloat(.42f, -delta)
                        , com.nineoldandroids.animation.Keyframe.ofFloat(.58f, delta), com.nineoldandroids.animation.Keyframe.ofFloat(.74f, -delta)
                        , com.nineoldandroids.animation.Keyframe.ofFloat(.90f, delta), com.nineoldandroids.animation.Keyframe.ofFloat(1f, 0f));

        return com.nineoldandroids.animation.ObjectAnimator.ofPropertyValuesHolder(
                AnimatorProxy.NEEDS_PROXY ? AnimatorProxy.wrap(view) : view, pvhTranslateX)
                .setDuration(500);
    }

}
