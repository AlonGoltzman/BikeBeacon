package com.bikebeacon.ui;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bikebeacon.R;

/**
 * Created by Alon on 9/10/2017.
 */

public class ContactsDialogFragment extends DialogFragment {

    private TextView mAmountRead;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View dialog = inflater.inflate(R.layout.contacts_dialog, container, false);
        mAmountRead = dialog.findViewById(R.id.contacts_amount);
        return dialog;
    }

    public void updateText(String newText) {
        if (mAmountRead != null)
            mAmountRead.setText(newText);
    }
}
