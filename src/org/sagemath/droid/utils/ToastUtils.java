package org.sagemath.droid.utils;

import android.content.Context;
import com.github.johnpersano.supertoasts.SuperToast;

/**
 * @author Nikhil Peter Raj
 */
public class ToastUtils {

    public static SuperToast getAlertToast(Context context, int resId, int duration) {
        SuperToast toast = new SuperToast(context);
        toast.setIcon(android.R.drawable.ic_dialog_alert, SuperToast.IconPosition.LEFT);
        toast.setText(context.getString(resId));
        toast.setDuration(duration);
        return toast;
    }

}
