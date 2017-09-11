package com.bikebeacon.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.bikebeacon.R;
import com.bikebeacon.pojo.Contact;
import com.bikebeacon.pojo.ContactViewHolder;

import java.util.ArrayList;

/**
 * Created by Alon on 9/10/2017.
 */

class ContactsListAdapter extends RecyclerView.Adapter<ContactViewHolder> {

    private ArrayList<Contact> mContacts;
    private Context mContext;

    ContactsListAdapter(ArrayList<Contact> contacts, Context context) {
        mContacts = contacts;
        mContext = context;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContactViewHolder(LayoutInflater.from(mContext).inflate(R.layout.contact_view, parent, false), mContacts);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        System.out.println(mContacts.get(position));
        holder.update(mContacts.get(position));
        holder.getSelected().setTag(position);
        holder.getSelected().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mContacts.get((Integer) compoundButton.getTag()).setSelected(b);
            }
        });
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
