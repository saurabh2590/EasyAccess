/*
	
	Copyright 2014 Caspar Isemer, Eva Krueger and IDEAL Group Inc.(http://www.ideal-group.org), http://easyaccess.org
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
		http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License. 
 */
package org.easyaccess.contacts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.R;
import org.easyaccess.TTS;
import org.easyaccess.Utils;
import org.easyaccess.phonedialer.ContactManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Displays options that can be performed on the contact, such as delete, export
 * contacts to SD card, import contact, delete contact.
 */
public class ContactsOtherOptions extends EasyAccessActivity {

	/** Declare variables and UI elements **/
	private TextView contactName;
	private Button btnDelete, btnCopyToSD, btnImport, btnExport;
	private String id, contactNumber;
	private Handler handler;
	private final int EXPORTONECONTACT = 1;
	private final int EXPORTALLCONTACTS = 2;

	/**
	 * Attaches onKey listener to the Button passed as a parameter to the
	 * method. If enter key on the keyboard or center key on the keypad is
	 * pressed, the value of the parameter passed is checked.
	 * 
	 * @param button
	 *            The button with which the onKeyListener is to be associated.
	 * @param buttonFlag
	 *            If the value of buttonFlag is 1, an alert dialog is displayed,
	 *            to confirm the delete operation. If the value of buttonFlag is
	 *            2, the contact is copied to the SD card. If the value of
	 *            buttonFlag is 3, all the contacts are exported to the SD card.
	 *            If the value of buttonFlag is 4, contacts are imported from
	 *            the SD card.
	 */

	private void attachKeyListener(Button button, final int buttonFlag) {
		button.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						switch (buttonFlag) {
						case 1:
							confirmDelete("Are you sure you want to delete the contact?");
							break;
						case 2:
							copyToSDcard();
							break;
						case 3:
							export();
							break;
						case 4:
							importContact();
							break;
						}
						break;
					}
				}
				return false;
			}
		});
	}

	/**
	 * Imports contacts from the SD card.
	 */
	private void importContact() {
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		final File[] files = new File(Environment.getExternalStorageDirectory()
				+ File.separator + getResources().getString(R.string.appName))
				.listFiles();
		if (files == null) {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.filesabsent),
					Toast.LENGTH_SHORT).show();
			// check if keyboard is connected but accessibility services are
			// disabled
			if (!Utils.isAccessibilityEnabled(getApplicationContext())
					&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
				TTS.speak(getResources().getString(R.string.filesabsent));
		} else {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.importing),
					Toast.LENGTH_SHORT).show();
			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					// check if keyboard is connected but accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
						TTS.speak(getResources().getString(R.string.importing));
					for (int i = 0; i < files.length; i++) {
						if (files[i].getName().endsWith(".vcf")) {
							try {
								intent.setDataAndType(Uri.fromFile(new File(
										files[i].getAbsolutePath())),
										"text/x-vcard");
								startActivity(intent);
							} catch (Exception e) {
								TTS.speak(getResources().getString(
										R.string.importfailed));
							}
						}
					}
					TTS.speak(getResources().getString(R.string.importsuccess));
				}
			};
			new Thread(runnable).start();
		}
	}

	/**
	 * Exports all contacts to the SD card as .vcf files.
	 */
	private void export() {
		if (TTS.isSpeaking())
			TTS.stop();
		if (getSDcardStatus()) {
			if (TTS.isSpeaking())
				TTS.stop();
			// check if keyboard is connected but accessibility services are
			// disabled
			if (!Utils.isAccessibilityEnabled(getApplicationContext())
					&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
				TTS.speak(getResources().getString(R.string.exporting));
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.exporting),
					Toast.LENGTH_SHORT).show();
			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					Bundle bundle = new Bundle();
					Message message = new Message();
					Cursor cursor = getContentResolver().query(
							Data.CONTENT_URI, null, null, null, null);
					if (getVCF(cursor)) {
						bundle.putBoolean("success", true);
					} else {
						bundle.putBoolean("success", false);
					}
					bundle.putInt("type", EXPORTALLCONTACTS);
					message.setData(bundle);
					handler.sendMessage(message);
				}
			};
			new Thread(runnable).start();
		}
	}

	/**
	 * Exports one contact to the SD card as a .vcf file.
	 */
	private void copyToSDcard() {
		if (getSDcardStatus()) {
			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					Bundle bundle = new Bundle();
					Message message = new Message();
					Cursor cursor = getContentResolver()
							.query(Data.CONTENT_URI,
									null,
									Data.CONTACT_ID + " = ?",
									new String[] { ContactsOtherOptions.this.id },
									null);
					if (getVCF(cursor)) {
						bundle.putBoolean("success", true);
					} else {
						bundle.putBoolean("success", false);
					}
					bundle.putInt("type", EXPORTONECONTACT);
					message.setData(bundle);
					handler.sendMessage(message);
				}
			};
			new Thread(runnable).start();
		}
	}

	/**
	 * Confirms whether the user wants to delete the contact.
	 * 
	 * @param message
	 *            The message to be displayed in the AlertDialog.
	 */
	private void confirmDelete(String message) {
		TTS.stop();
		if (Utils.isAccessibilityEnabled(getApplicationContext()))
			giveFeedback(message);
		AlertDialog confirmBox = new AlertDialog.Builder(this)
				// set message, title, and icon
				.setTitle(getResources().getString(R.string.btnTextMsgsDelete))
				.setMessage(message)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (deleteContact(ContactsOtherOptions.this.contactNumber)) {
									// check if keyboard is connected but
									// accessibility services are disabled
									if (!Utils
											.isAccessibilityEnabled(getApplicationContext())
											&& getResources()
													.getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
										TTS.speak(getResources().getString(
												R.string.deletesuccess));
									Toast.makeText(
											getApplicationContext(),
											getResources().getString(
													R.string.deletesuccess),
											Toast.LENGTH_SHORT).show();
									// go to ContactsApp
									finish();
									Intent intent = new Intent(
											getApplicationContext(),
											ContactsApp.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
											| Intent.FLAG_ACTIVITY_NEW_TASK);
									startActivity(intent);
								} else {
									// check if keyboard is connected but
									// accessibility services are disabled
									if (!Utils
											.isAccessibilityEnabled(getApplicationContext())
											&& getResources()
													.getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
										TTS.speak(getResources().getString(
												R.string.deletefailure));
									Toast.makeText(
											getApplicationContext(),
											getResources().getString(
													R.string.deletefailure),
											Toast.LENGTH_SHORT).show();
								}
								dialog.dismiss();
							}

						})

				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						dialog.dismiss();

					}
				}).create();
		confirmBox.show();
	}

	/**
	 * Deletes the specified contact.
	 * 
	 * @param number
	 *            The number of the contact that is to be deleted.
	 * @return true if the contact was deleted successfully, false if the
	 *         contact could not be deleted.
	 */
	private boolean deleteContact(String number) {
		Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		Cursor cur = getContentResolver().query(contactUri, null, null, null,
				null);
		try {
			if (cur.moveToFirst()) {
				do {
					String lookupKey = cur
							.getString(cur
									.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
					Uri uri = Uri.withAppendedPath(
							ContactsContract.Contacts.CONTENT_LOOKUP_URI,
							lookupKey);
					getContentResolver().delete(uri, null, null);
					return true;

				} while (cur.moveToNext());
			}

		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
		return false;
	}

	/**
	 * Returns true if the SD card can be used. Otherwise, returns false.
	 * 
	 * @return true if the SD card is available for use. Otherwie, returns
	 *         false.
	 */
	private boolean getSDcardStatus() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED_READ_ONLY)) {
				if (TTS.isSpeaking())
					TTS.stop();
				// check if keyboard is connected but accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(getResources().getString(R.string.mediareadonly));
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.mediareadonly),
						Toast.LENGTH_SHORT).show();
				return false;
			} else {
				return true;
			}
		}
		if (TTS.isSpeaking())
			TTS.stop();
		// check if keyboard is connected but accessibility services are
		// disabled
		if (!Utils.isAccessibilityEnabled(getApplicationContext())
				&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
			TTS.speak(getResources().getString(R.string.mediaunmounted));
		Toast.makeText(getApplicationContext(),
				getResources().getString(R.string.mediaunmounted),
				Toast.LENGTH_SHORT).show();
		return false;
	}

	/**
	 * Exports one contact to the SD card.
	 * 
	 * @param cursor
	 *            The cursor that consists of the details of the contacts.
	 * @return boolean if can return the .vcf file for the contact.
	 */
	public boolean getVCF(Cursor cursor) {
		while (cursor.moveToNext()) {
			String lookupKey = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
			Uri uri = Uri.withAppendedPath(
					ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);

			AssetFileDescriptor fd;
			try {
				fd = getContentResolver().openAssetFileDescriptor(uri, "r");
				FileInputStream fis = fd.createInputStream();
				byte[] buf = new byte[(int) fd.getDeclaredLength()];
				fis.read(buf);
				String VCard = new String(buf);
				String vfile = cursor.getString(
						cursor.getColumnIndex("display_name")).replaceAll(
						"[^a-zA-Z0-9_]", "")
						+ "_"
						+ cursor.getString(cursor
								.getColumnIndex(ContactsContract.Contacts._ID))
						+ ".vcf";
				String path = Environment.getExternalStorageDirectory()
						.toString()
						+ File.separator
						+ getResources().getString(R.string.appName);
				File directory = new File(path);
				if (!directory.exists())
					directory.mkdirs();
				File outputFile = new File(path, vfile);
				@SuppressWarnings("resource")
				FileOutputStream mFileOutputStream = new FileOutputStream(
						outputFile);
				mFileOutputStream.write(VCard.toString().getBytes());
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/**
	 * Create the Contact Details menu activity (non-Javadoc)
	 * 
	 * @see org.easyaccess.EasyAccessActivity#onCreate(android.os.Bundle)
	 */
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.contactsotheroptions);
		super.onCreate(savedInstanceState);

		/** get UI elements **/
		contactName = (TextView) findViewById(R.id.txtContactsName);
		btnDelete = (Button) findViewById(R.id.btnDelete);
		btnCopyToSD = (Button) findViewById(R.id.btnCopyToSD);
		btnImport = (Button) findViewById(R.id.btnImportFromSD);
		btnExport = (Button) findViewById(R.id.btnExportToSD);

		/** get the details of the contact from the number **/
		if (getIntent().getExtras() != null) {
			this.id = getIntent().getExtras().getString("id");
			this.contactNumber = getIntent().getExtras().getString("number");
		}

		HashMap<String, ArrayList<String>> contactDetails = new ContactManager(
				getApplicationContext()).getDetailsFromId(this.id);

		contactName.setText(contactDetails.get("name").get(0));
		contactName.setContentDescription(contactDetails.get("name").get(0)
				.replaceAll(".(?=[0-9])", "$0 "));

		contactName.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) {
					giveFeedback(((TextView) view).getText().toString());
				}
			}
		});

		attachListener(btnDelete);
		attachListener(btnCopyToSD);
		attachListener(btnImport);
		attachListener(btnExport);

		attachKeyListener(btnDelete, 1);

		btnDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				confirmDelete("Are you sure you want to delete the contact?");
			}
		});

		attachKeyListener(btnCopyToSD, 2);

		btnCopyToSD.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				copyToSDcard();
			}
		});

		btnExport.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				export();
			}
		});

		attachKeyListener(btnExport, 3);

		btnImport.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				importContact();
			}
		});

		attachKeyListener(btnImport, 4);

		handler = new Handler() {
			public void handleMessage(Message message) {
				if (message.getData().getInt("type") == EXPORTONECONTACT) {
					if (message.getData().getBoolean("success") == true) {
						TTS.stop();
						// check if keyboard is connected but accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
							TTS.speak(getResources().getString(
									R.string.copycontactsuccess));
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(
										R.string.copycontactsuccess),
								Toast.LENGTH_SHORT).show();
					} else {
						TTS.stop();
						// check if keyboard is connected but accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
							TTS.speak(getResources().getString(
									R.string.copycontactfailure));
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(
										R.string.copycontactfailure),
								Toast.LENGTH_SHORT).show();
					}
				} else if (message.getData().getInt("type") == EXPORTALLCONTACTS) {
					if (message.getData().getBoolean("success") == true) {
						TTS.stop();
						// check if keyboard is connected but accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
							TTS.speak(getResources().getString(
									R.string.copyallcontactssuccess));
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(
										R.string.copyallcontactssuccess),
								Toast.LENGTH_SHORT).show();
					} else {
						TTS.stop();
						// check if keyboard is connected but accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
							TTS.speak(getResources().getString(
									R.string.copyallcontactsfailure));
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(
										R.string.copyallcontactsfailure),
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		// check if keyboard is connected and accessibility services are
		// disabled
		if (!Utils.isAccessibilityEnabled(getApplicationContext())
				&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
			TTS.speak(getResources().getString(R.string.otheroptions));
		}
		// get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.contactsotheroptions);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}
}
