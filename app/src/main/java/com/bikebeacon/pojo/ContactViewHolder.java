package com.bikebeacon.pojo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bikebeacon.R;

import java.util.ArrayList;

/**
 * Created by Alon on 9/10/2017.
 */

public class ContactViewHolder extends RecyclerView.ViewHolder {

    private TextView name;
    private TextView phone;
    private CheckBox selected;
    private boolean update = true;

    private ArrayList<Contact> mContacts;

    public ContactViewHolder(View itemView, ArrayList<Contact> contacts) {
        super(itemView);
        name = itemView.findViewById(R.id.contact_name);
        phone = itemView.findViewById(R.id.contact_number);
        selected = itemView.findViewById(R.id.contact_selected);

        mContacts = contacts;
    }

    public void update(Contact newContact) {
        name.setText(newContact.getName());
        phone.setText(newContact.getPhoneNum());
        selected.setChecked(newContact.isSelected());
        update = false;
    }

    public CheckBox getSelected() {
        return selected;
    }
}
