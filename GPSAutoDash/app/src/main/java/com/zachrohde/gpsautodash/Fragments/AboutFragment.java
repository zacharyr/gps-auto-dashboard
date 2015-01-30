package com.zachrohde.gpsautodash.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.zachrohde.gpsautodash.R;

public class AboutFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.about_dialog_title);
        builder.setMessage(R.string.about_dialog_message)
                .setPositiveButton(R.string.about_website, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        openWebURL("http://zachrohde.com");
                    }
                })
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AboutFragment.this.getDialog().cancel();
                    }
                });


        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void openWebURL(String inURL) {
        Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(inURL));
        startActivity(browse);
    }
}
