package com.stonedog.gramophone.preferences;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.stonedog.gramophone.R;
import com.stonedog.gramophone.util.PreferenceUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ApiUrlPreferenceDialog extends DialogFragment {

    EditText apiUrlText;
    EditText apiAccesstokenText;

    public static ApiUrlPreferenceDialog newInstance() {
        return new ApiUrlPreferenceDialog();
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.preference_dialog_api_url, null);
        apiUrlText = (EditText) view.findViewById(R.id.api_url);
        apiAccesstokenText = (EditText) view.findViewById(R.id.api_accesstoken);

        apiUrlText.setText(PreferenceUtil.getInstance(getContext()).getRemoteAPIUrl());
        apiAccesstokenText.setText(PreferenceUtil.getInstance(getContext()).getRemoteAPIAccessToken());
        builder.setView(view)
                .setPositiveButton(R.string.login,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                PreferenceUtil.getInstance(getContext()).setRemoteAPIUrl(apiUrlText
                                        .getText().toString());
                                PreferenceUtil.getInstance(getContext()).setRemoteAPIAccessToken(apiAccesstokenText
                                        .getText().toString());

                            }
                        }).setNegativeButton(R.string.cancel, null);
        return builder.create();


    }
}