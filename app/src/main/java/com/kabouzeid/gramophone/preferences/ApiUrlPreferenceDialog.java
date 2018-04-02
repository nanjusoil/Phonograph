package com.kabouzeid.gramophone.preferences;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.provider.BlacklistStore;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ApiUrlPreferenceDialog extends DialogFragment{
    public static final String TAG = ApiUrlPreferenceDialog.class.getSimpleName();

    private ArrayList<String> paths;

    public static ApiUrlPreferenceDialog newInstance() {
        return new ApiUrlPreferenceDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        refreshRemoteUrlData();
        return new MaterialDialog.Builder(getContext())
                .title(R.string.remote_api_url)
                .positiveText(android.R.string.ok)
                .neutralText(R.string.clear_action)
                .negativeText(R.string.add_action)
                .items(paths)
                .autoDismiss(false)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override

                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        final View v = inflater.inflate(R.layout.preference_dialog_url, null);
                        EditText editText = (EditText) (v.findViewById(R.id.editText));
                        editText.setText(PreferenceUtil.getInstance(getContext()).getRemoteAPIUrl(), TextView.BufferType.EDITABLE);

                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.remote_api_url_detail)
                                .setView(v)
                                .setPositiveButton( R.string.confirm, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PreferenceUtil.getInstance(getContext()).setRemoteAPIUrl(editText.getText().toString());
                                        refreshRemoteUrlData();
                                    }
                                })
                                .show();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        final View v = inflater.inflate(R.layout.preference_dialog_url, null);
                        EditText editText = (EditText) (v.findViewById(R.id.editText));
                        editText.setText(PreferenceUtil.getInstance(getContext()).getRemoteAPIUrl(), TextView.BufferType.EDITABLE);

                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.remote_api_url_detail)
                                .setView(v)
                                .setPositiveButton( R.string.confirm, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PreferenceUtil.getInstance(getContext()).setRemoteAPIUrl(editText.getText().toString());
                                        refreshRemoteUrlData();
                                    }
                                })
                                .show();

                    }
                })
                // clear
                .onNeutral((materialDialog, dialogAction) -> new MaterialDialog.Builder(getContext())
                        .title(R.string.clear_blacklist)
                        .content(R.string.do_you_want_to_clear_the_blacklist)
                        .positiveText(R.string.clear_action)
                        .negativeText(android.R.string.cancel)
                        .onPositive((materialDialog1, dialogAction1) -> {
                            PreferenceUtil.getInstance(getContext()).setRemoteAPIUrl("");
                            refreshRemoteUrlData();
                        }).show())
                // add
                .onPositive((materialDialog, dialogAction) -> dismiss())
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        final View v = inflater.inflate(R.layout.preference_dialog_url, null);
                        EditText editText = (EditText) (v.findViewById(R.id.editText));
                        editText.setText(PreferenceUtil.getInstance(getContext()).getRemoteAPIUrl(), TextView.BufferType.EDITABLE);

                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.remote_api_url_detail)
                                .setView(v)
                                .setPositiveButton( R.string.confirm, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PreferenceUtil.getInstance(getContext()).setRemoteAPIUrl(editText.getText().toString());
                                        refreshRemoteUrlData();
                                    }
                                })
                                .show();
                    }
                })
                .build();
    }

    private void refreshRemoteUrlData() {
        paths = new ArrayList<String>(1);
        paths.add(PreferenceUtil.getInstance(getContext()).getRemoteAPIUrl());

        MaterialDialog dialog = (MaterialDialog) getDialog();
        if (dialog != null) {
            String[] pathArray = new String[paths.size()];
            pathArray = paths.toArray(pathArray);
            dialog.setItems((CharSequence[]) pathArray);
        }
    }

}