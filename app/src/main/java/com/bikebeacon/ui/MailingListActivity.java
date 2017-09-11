package com.bikebeacon.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bikebeacon.R;
import com.bikebeacon.background.dispatchers.NetworkDispatcher;
import com.bikebeacon.background.utility.SharedPreferencesManager;
import com.bikebeacon.pojo.Contact;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.bikebeacon.background.utility.Constants.PERMISSION_REQUEST_CODE;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_FIRST_RUN;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_UPLOADED_MAILING_LIST;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_UUID;
import static com.bikebeacon.background.utility.GeneralUtility.hasPermissions;
import static com.bikebeacon.background.utility.GeneralUtility.requestRuntimePermissions;
import static com.bikebeacon.background.utility.GeneralUtility.setUUID;

public class MailingListActivity extends Activity implements View.OnClickListener {


    public static final String TAG = MailingListActivity.class.getSimpleName();

    private RecyclerView mContactsList;
    private DialogFragment mDialog;

    private ArrayList<Contact> mContacts;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Object mailingListUploadedAlready = SharedPreferencesManager.getManager(this).get(SHARED_PREFERENCES_UPLOADED_MAILING_LIST);
        if (mailingListUploadedAlready != null && (Boolean) mailingListUploadedAlready) {
            startActivity(new Intent(MailingListActivity.this, MapsActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_mailing_list);

        if (!hasPermissions())
            if (requestRuntimePermissions(this))
                return;
        init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean granted = true;
            for (int result : grantResults)
                if (result != PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            if (!granted)
                Toast.makeText(this, getString(R.string.need_permissions), Toast.LENGTH_SHORT).show();
            else
                init();
        }
    }

    @Override
    public void onClick(View view) {
        mDialog = new UploadingDialogFragment();
        mDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        mDialog.show(getFragmentManager(), "");
        new ContactSubmission(mContacts, mHandler).execute();
    }


    private void init() {
        Button submit = findViewById(R.id.submit_mailing_list);
        mHandler = new Handler();

        submit.setOnClickListener(this);

        mContactsList = findViewById(R.id.mailing_list_contacts);
        mContactsList.setLayoutManager(new LinearLayoutManager(this));
        mDialog = new ContactsDialogFragment();
        mContacts = new ArrayList<>();

        mDialog.setCancelable(false);
        mDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        mDialog.show(getFragmentManager(), "");

        new ContactsFetcher(mContacts, mHandler).execute();
    }

    private void displayContacts() {
        Collections.sort(mContacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact contact1, Contact contact2) {
                return contact1.getName().compareTo(contact2.getName());
            }
        });
        mContactsList.setAdapter(new ContactsListAdapter(mContacts, this));
        mDialog.dismiss();
    }

    //==============================================================================================

    private class ContactSubmission extends AsyncTask<Void, Void, Boolean> implements Callback {

        private ArrayList<Contact> mContacts;
        private Handler mDoneHandler;

        ContactSubmission(ArrayList<Contact> contacts, Handler handler) {
            mContacts = contacts;
            mDoneHandler = handler;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            ArrayList<Contact> selected = new ArrayList<>();
            for (Contact contact : mContacts)
                if (contact.isSelected())
                    selected.add(contact);
            if (selected.size() > 0) {
                JSONArray contacts = new JSONArray();
                for (Contact contact : selected) {
                    JSONObject contactObj = contact.toJSON();
                    if (contactObj == null) {
                        Toast.makeText(MailingListActivity.this, getString(R.string.failed_uploading_contacts_json), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "doInBackground: Failed due to JSON error.");
                        return false;
                    }
                    contacts.put(contactObj);
                }
                NetworkDispatcher.getDispatcher()
                        .createRequest()
                        .method("POST")
                        .body(contacts.toString().getBytes())
                        .url(NetworkDispatcher.URL_TYPES.REGISTRATION.toString())
                        .build()
                        .execute(this);
                return true;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean flag) {
            super.onPostExecute(flag);
            if (flag == null)
                Toast.makeText(MailingListActivity.this, getString(R.string.select_contacts), Toast.LENGTH_SHORT).show();
            else if (!flag)
                Log.d(TAG, "onPostExecute() called with: flag = [" + flag + "] -> failed converting contacts to JSON.");
            else
                Log.d(TAG, "onPostExecute() worked.");

        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.e(TAG, "onFailure: Failed uploading contacts.", e);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if (response.code() != 200) {
                Log.d(TAG, "onResponse() called with: call = response = [" + response + "] failed: " + response.body().string());
                return;
            }
            Log.d(TAG, "onPostExecute() worked.");
            try {
                InputStream stream = response.body().byteStream();

                byte[] buffer = new byte[1024];
                int aRead;
                final StringBuilder builder = new StringBuilder();

                while ((aRead = stream.read(buffer)) != -1)
                    builder.append(new String(buffer, 0, aRead));

                stream.close();

                mDoneHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferencesManager.getManager(MailingListActivity.this)
                                .change(SHARED_PREFERENCES_FIRST_RUN, true)
                                .change(SHARED_PREFERENCES_UUID, builder.toString())
                                .change(SHARED_PREFERENCES_UPLOADED_MAILING_LIST, true);
                        mDialog.dismiss();
                        startActivity(new Intent(MailingListActivity.this, MapsActivity.class));
                        finish();
                    }
                });
                setUUID(builder.toString());
            } catch (IOException e) {
                Log.e(TAG, "onResponse: error reading input stream from response.", e);
            }
        }
    }


    //==============================================================================================

    private class ContactsFetcher extends AsyncTask<Void, Void, Void> {

        private Handler mHandler;
        private ArrayList<Contact> mList;
        private ContentResolver resolver;
        private int count = 0;

        ContactsFetcher(ArrayList<Contact> contacts, Handler handler) {
            mList = contacts;
            mHandler = handler;
            resolver = getContentResolver();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (cursor != null)
                if (cursor.getCount() > 0) {
                    final int cCount = cursor.getCount();
                    String contactIDColumn = ContactsContract.Contacts._ID;
                    String nameColumn = ContactsContract.Contacts.DISPLAY_NAME;
                    String hasNumberColumn = ContactsContract.Contacts.HAS_PHONE_NUMBER;
                    Uri phoneContentURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                    String phoneNumberColumn = ContactsContract.CommonDataKinds.Phone.NUMBER;
                    String phoneContactIDColumn = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
                    while (cursor.moveToNext()) {
                        mHandler.post(new Runnable() {
                            @SuppressLint("DefaultLocale")
                            @Override
                            public void run() {
                                ((ContactsDialogFragment) mDialog).updateText(String.format("Reading contacts: %d / %d", count++, cCount));
                            }
                        });
                        String id = cursor.getString(cursor.getColumnIndex(contactIDColumn));
                        String name = cursor.getString(cursor.getColumnIndex(nameColumn));
                        int hasNumber = cursor.getInt(cursor.getColumnIndex(hasNumberColumn));
                        if (hasNumber > 0) {
                            Cursor numberCursor = resolver.query(phoneContentURI, null, phoneContactIDColumn + " = ?", new String[]{id}, null);
                            if (numberCursor != null) {
                                String phoneNumber;
                                numberCursor.moveToNext();
                                phoneNumber = numberCursor.getString(numberCursor.getColumnIndex(phoneNumberColumn));
                                numberCursor.close();

                                mList.add(new Contact(name, phoneNumber));
                            }
                        }
                    }
                    cursor.close();
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            displayContacts();
        }
    }

}
