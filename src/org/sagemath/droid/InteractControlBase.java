package org.sagemath.droid;

import android.content.Context;
import android.widget.LinearLayout;

public abstract class InteractControlBase 
    	extends LinearLayout {
	private final static String TAG = "InteractControlBase";

	protected final String variable;
	protected final InteractView interactView;

	
	public InteractControlBase(InteractView interactView, String variable, Context context) {
		super(context);
		this.interactView = interactView;
		this.variable = variable;
		setFocusable(true);
		setFocusableInTouchMode(true);
	}

	protected String getVariableName() {
		return variable;
	}
	
	protected abstract Object getValue();
	

	protected int countDigitsAfterComma(String s) {
		int pos = s.lastIndexOf('.');
		if (pos == -1)
			return 0;
		else
			return s.length() - pos - 1;
	}
}
