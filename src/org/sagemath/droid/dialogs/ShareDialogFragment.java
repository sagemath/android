package org.sagemath.droid.dialogs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;
import org.sagemath.droid.R;
import org.sagemath.droid.utils.BusProvider;

/**
 * @author Nikhil Peter Raj
 */
public class ShareDialogFragment extends DialogFragment {
    private static final String TAG = "SageDroid:ShareDialogFragment";

    public static interface OnRequestOutputListener {
        public void onRequestOutput();
    }

    private OnRequestOutputListener listener;

    private String shareUrl;
    private int selected = -1;

    private static final String ARG_URL = "shareURL";

    public static ShareDialogFragment getInstance(String permalinkUrl) {
        ShareDialogFragment fragment = new ShareDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, permalinkUrl);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (OnRequestOutputListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        shareUrl = getArguments().getString(ARG_URL);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_share_title);
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.dialog_share_choices), -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selected = which;
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                switch (selected) {
                    case 0:
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(shareUrl));
                        startActivity(intent);
                        dismiss();
                        break;

                    case 1:
                        intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, shareUrl);
                        startActivity(intent);
                        dismiss();
                        break;

                    case 2:

                        int sdk = Build.VERSION.SDK_INT;
                        if (sdk >= Build.VERSION_CODES.HONEYCOMB) {
                            ClipboardManager manager = (ClipboardManager) getActivity()
                                    .getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText("shareUrl", shareUrl);
                            manager.setPrimaryClip(clipData);
                        } else {
                            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity()
                                    .getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboard.setText(shareUrl);
                        }
                        Toast.makeText(getActivity(), R.string.toast_link_copied, Toast.LENGTH_SHORT).show();
                        dismiss();
                        break;

                    case 3:
                        listener.onRequestOutput();
                        break;

                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return builder.create();
    }
}
