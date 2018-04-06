package com.kabouzeid.gramophone.preferences;

import android.content.Context;
import android.util.AttributeSet;

import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEDialogPreference;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ApiUrlPreference extends ATEDialogPreference {
    public ApiUrlPreference(Context context) {
        super(context);
    }

    public ApiUrlPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ApiUrlPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ApiUrlPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}