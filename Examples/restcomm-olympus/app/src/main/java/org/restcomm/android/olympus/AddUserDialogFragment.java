/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * For questions related to commercial use licensing, please contact sales@telestax.com.
 *
 */

package org.restcomm.android.olympus;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.log4j.chainsaw.Main;

import java.util.ArrayList;
import java.util.Map;

import static org.restcomm.android.olympus.ContactsController.CONTACT_KEY;
import static org.restcomm.android.olympus.ContactsController.CONTACT_VALUE;

public class AddUserDialogFragment extends AppCompatDialogFragment {
   public static final int DIALOG_TYPE_ADD_CONTACT = 0;
   public static final int DIALOG_TYPE_UPDATE_CONTACT = 1;

   private ArrayList<Map<String, String>> contactList;
   // Use this instance of the interface to deliver action events
   ContactDialogListener listener;
   private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 56;

   //Defining Button and EditText variables
   public Button buttonImportUsers;
   EditText txtUsername;
   EditText txtSipuri;
   boolean permissionsStatus = false;

   /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
   public interface ContactDialogListener {
      public void onDialogPositiveClick(int type, String username, String sipuri);

      public void onDialogNegativeClick();
   }

   /**
    * Create a new instance of MyDialogFragment, providing "num"
    * as an argument.
    */
   public static AddUserDialogFragment newInstance(int type, String username, String sipuri) {
      AddUserDialogFragment f = new AddUserDialogFragment();

      // Supply num input as an argument.
      Bundle args = new Bundle();
      args.putInt("type", type);
      if (type == DIALOG_TYPE_UPDATE_CONTACT) {
         args.putString(CONTACT_KEY, username);
         args.putString(CONTACT_VALUE, sipuri);
      }
      f.setArguments(args);

      return f;
   }

   // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);
      // Verify that the host activity implements the callback interface
      try {
         // Instantiate the NoticeDialogListener so we can send events to the host
         listener = (ContactDialogListener) activity;
      } catch (ClassCastException e) {
         // The activity doesn't implement the interface, throw exception
         throw new ClassCastException(activity.toString()
                 + " must implement ContactDialogListener");
      }
   }

   @Override
   public void onDetach() {
      super.onDetach();
      listener = null;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

   }

    /* Not to be used when onCreateDialog is overriden (it is for non-alert dialog fragments
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog_add_contact, container, false);
        txtUsername = (EditText)v.findViewById(R.id.editText_username);
        txtSipuri = (EditText)v.findViewById(R.id.editText_sipuri);

        return v;
    }
    */

   // Notice that for this doesn't work if onCreateView has been overriden as described above. To add
   // custom view when using alert we need to use builder.setView() as seen below
   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState) {
      // Get the layout inflater
      View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_add_contact, null);
      txtUsername = (EditText) view.findViewById(R.id.editText_username);
      txtSipuri = (EditText) view.findViewById(R.id.editText_sipuri);
      buttonImportUsers = (Button) view.findViewById(R.id.button_contactFromPhone);

      String title = "Add Contact";
      String positiveText = "Add";
      if (getArguments().getInt("type") == DIALOG_TYPE_UPDATE_CONTACT) {
         title = "Update Contact";
         positiveText = "Update";

         txtUsername.setText(getArguments().getString(CONTACT_KEY, ""));
         txtSipuri.setText(getArguments().getString(CONTACT_VALUE, ""));
         // username is not modifiable
         txtUsername.setEnabled(false);
      }

      checkPermissions();

      buttonImportUsers.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            importContacts();
         }
      });

      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);

      //Checking if we have the permission to read contacts when the dialog gets invoked


      // Inflate and set the layout for the dialog
      // Pass null as the parent view because its going in the dialog layout
      builder.setView(view)
              .setTitle(title)
              .setPositiveButton(positiveText,
                      new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                            listener.onDialogPositiveClick(getArguments().getInt("type"), txtUsername.getText().toString(),
                                    txtSipuri.getText().toString());
                         }
                      }
              )
              .setNegativeButton("Cancel",
                      new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                            listener.onDialogNegativeClick();
                         }
                      }
              );
      return builder.create();


   }
   /*
    *Method used to import Phone contacts into the app
    */

   private void importContacts() {

      if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

         ActivityCompat.requestPermissions(getActivity(),
                 new String[]{Manifest.permission.READ_CONTACTS},
                 MY_PERMISSIONS_REQUEST_READ_CONTACTS);

         Toast.makeText(getContext(), "Please accept the permissions to proceed", Toast.LENGTH_SHORT).show();

      } else {


         //Calling in the ContactAdapter Sub-class of MainFragment and passing the constructor and ArrayList to it
            MainFragment.ContactAdapter contactAdapter = new MainFragment().new ContactAdapter(getContext(),contactList);
         //Defining a cursor to query each value of the CONTENT_URI Column
         final Cursor phones = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
         final ContactsController contactsController = new ContactsController(getContext());
         //retrieving the contacts in Olympus's Table in an ArrayList
         contactList = contactsController.retrieveContacts();

         final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "Loading", "Importing the contacts", true);

         //Initiating a background thread as we don't wanna slow down the UI Thread :)
         AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

               while (phones.moveToNext()) {


                  String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                  String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                  try {
                     //Adding the instantaneous Phone contact to the db and Notifying the view that A value has been added
                     contactsController.addContact(contactList, name, phoneNumber);

                  } catch (Exception e) {
                     e.printStackTrace();
                  }

                  //TODO: Fix the bug to update the Contact's list without destroying the lifeCycle

               }


               progressDialog.dismiss();

            }
         });
         contactAdapter.notifyDataSetChanged();
      }


   }

   public void checkPermissions() {


      if (ContextCompat.checkSelfPermission(getContext(),
              Manifest.permission.READ_CONTACTS)
              != PackageManager.PERMISSION_GRANTED) {

         // Should we show an explanation?
         if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                 Manifest.permission.READ_CONTACTS)) {

            //Don't need to add anything over here for now --Sagar Vakkala

         } else {

            //Didn't add any explanation right now --Sagar Vakkala

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            //Requesting permissions for reading contacts


         }
      }
   }
}