package org.sagemath.droid.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.nineoldandroids.view.animation.AnimatorProxy;
import org.sagemath.droid.R;

/**
 * Layout which an {@link android.widget.EditText} to show a floating label when the hint is hidden
 * due to the user inputting text.
 *
 * <p>Copied almost in entirety from
 * <a href="https://github.com/chrisbanes/philm/blob/master/app/src/main/java/app/philm/in/view/FloatLabelLayout.java">Chris Bane's Philm</a>
 * apart from tweaks to make it work with {@linkplain android.widget.AutoCompleteTextView} and older Android versions.</p>
 *
 * @see <a href="https://dribbble.com/shots/1254439--GIF-Mobile-Form-Interaction">Matt D. Smith on Dribble</a>
 * @see <a href="http://bradfrostweb.com/blog/post/float-label-pattern/">Brad Frost's blog post</a>
 */
public final class FloatLabelLayout extends FrameLayout {

    private static final long ANIMATION_DURATION = 150;

    private static final float DEFAULT_PADDING_LEFT_RIGHT_DP = 12f;

    private EditText mEditText;
    private AutoCompleteTextView mAutoCompleteTextView;
    private TextView mLabel;

    private int viewCount = 0;

    public FloatLabelLayout(Context context) {
        this(context, null);
    }

    public FloatLabelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatLabelLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray array = context
                .obtainStyledAttributes(attrs, R.styleable.FloatLabelLayout);

        final int sidePadding = array.getDimensionPixelSize(
                R.styleable.FloatLabelLayout_floatLabelSidePadding,
                dipsToPix(DEFAULT_PADDING_LEFT_RIGHT_DP));
        mLabel = new TextView(context);
        mLabel.setPadding(sidePadding, 0, sidePadding, 0);
        mLabel.setVisibility(INVISIBLE);

        mLabel.setTextAppearance(context,
                array.getResourceId(R.styleable.FloatLabelLayout_floatLabelTextAppearance,
                        android.R.style.TextAppearance_Small)
        );

        addView(mLabel, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        array.recycle();
    }

    @Override
    public final void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof AutoCompleteTextView) {
            params = performSetup(params);
            setAutoCompleteTextView((AutoCompleteTextView) child);

        } else if (child instanceof EditText) {
            params = performSetup(params);
            setEditText((EditText) child);
        }

        // Carry on adding the View...
        super.addView(child, index, params);
    }

    private ViewGroup.LayoutParams performSetup(ViewGroup.LayoutParams params) {
        // If we already have an EditText, throw an exception
        if (viewCount > 1) {
            throw new IllegalArgumentException("Can only have a single child");
        }

        // Update the layout params so that the EditText is at the bottom, with enough top
        // margin to show the label
        final LayoutParams lp = new LayoutParams(params);
        lp.gravity = Gravity.BOTTOM;
        lp.topMargin = (int) mLabel.getTextSize();


        return lp;
    }

    private void setEditText(EditText editText) {
        viewCount++;

        mEditText = editText;

        // Add a TextWatcher so that we know when the text input has changed
        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    // The text is empty, so hide the label if it is visible
                    if (mLabel.getVisibility() == View.VISIBLE) {
                        hideLabel();
                    }
                } else {
                    // The text is not empty, so show the label if it is not visible
                    if (mLabel.getVisibility() != View.VISIBLE) {
                        showLabel();
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });

        // Add focus listener to the EditText so that we can notify the label that it is activated.
        // Allows the use of a ColorStateList for the text color on the label
        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                handleFocusChange(focused);
            }
        });

        mLabel.setText(mEditText.getHint());
    }

    private void setAutoCompleteTextView(AutoCompleteTextView autoCompleteTextView) {
        viewCount++;

        mAutoCompleteTextView = autoCompleteTextView;

        mAutoCompleteTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    // The text is empty, so hide the label if it is visible
                    if (mLabel.getVisibility() == View.VISIBLE) {
                        hideLabel();
                    }
                } else {
                    // The text is not empty, so show the label if it is not visible
                    if (mLabel.getVisibility() != View.VISIBLE) {
                        showLabel();
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });

        // Add focus listener to the EditText so that we can notify the label that it is activated.
        // Allows the use of a ColorStateList for the text color on the label
        mAutoCompleteTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                handleFocusChange(focused);
            }
        });

        mLabel.setText(mAutoCompleteTextView.getHint());
    }

    /**
     * @return the {@link android.widget.EditText} text input
     */
    public EditText getEditText() {
        if (mEditText != null) {
            return mEditText;
        } else {
            return mAutoCompleteTextView;
        }
    }

    /**
     * @return the {@link android.widget.TextView} label
     */
    public TextView getLabel() {
        return mLabel;
    }

    /**
     * Show the label using an animation
     */

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void showLabel() {
        mLabel.setVisibility(View.VISIBLE);
        if (isApiHoneycombOrAbove()) {
            mLabel.setAlpha(0f);
            mLabel.setTranslationY(mLabel.getHeight());
            mLabel.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(null).start();
        } else {
            ViewHelper.setAlpha(mLabel, 0f);
            ViewHelper.setTranslationY(mLabel, mLabel.getHeight());
            ViewPropertyAnimator.animate(mLabel)
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(null).start();
        }
    }

    /**
     * Hide the label using an animation
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void hideLabel() {
        if (isApiHoneycombOrAbove()) {
            mLabel.setAlpha(1f);
            mLabel.setTranslationY(0f);
            mLabel.animate()
                    .alpha(0f)
                    .translationY(mLabel.getHeight())
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLabel.setVisibility(View.GONE);
                        }
                    }).start();
        } else {
            ViewHelper.setAlpha(mLabel, 1f);
            ViewHelper.setTranslationY(mLabel, 0f);
            ViewPropertyAnimator.animate(mLabel)
                    .alpha(0f)
                    .translationY(mLabel.getHeight())
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new com.nineoldandroids.animation.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {
                            mLabel.setVisibility(View.GONE);
                        }
                    }).start();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void handleFocusChange(final boolean focused) {
        if (isApiHoneycombOrAbove()) {
            mLabel.setActivated(focused);
        } else {
            if (focused && mLabel.getVisibility() == View.VISIBLE) {
                ObjectAnimator.ofFloat(mLabel, "alpha", 0.33f, 1f).start();
            } else if (mLabel.getVisibility() == View.VISIBLE) {
                AnimatorProxy.wrap(mLabel).setAlpha(1f);  //Need this for compat reasons
                ObjectAnimator.ofFloat(mLabel, "alpha", 1f, 0.33f).start();
            }
        }
    }

    /**
     * Helper method to convert dips to pixels.
     */
    private int dipsToPix(float dps) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps,
                getResources().getDisplayMetrics());
    }

    /**
     * Helper method to check API
     *
     * @return true if API>=14, false otherwise
     */
    private boolean isApiHoneycombOrAbove() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            return true;
        return false;
    }
}