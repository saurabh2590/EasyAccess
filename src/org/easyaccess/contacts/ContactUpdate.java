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

import java.util.ArrayList;

import org.easyaccess.R;
import org.easyaccess.SwipingUtils;
import org.easyaccess.TTS;
import org.easyaccess.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ContactUpdate extends Activity implements KeyListener {

	/**
	 * Displays options to update the details of the contact.
	 */

	/** Declare variables and UI elements **/
	private EditText editField;
	private TextView txtType;
	private Button btnSave, btnSetPrimary;// , btnDelete;
	private Spinner spinnerType;
	private SpinnerAdapter adapter;
	private String id, name, number, numType, email;
	private int typeIndex;
	private int editFlag, currentSelection = -1, deletedFlag = 0;
	private final int NAME = 1;
	private final int NUMBER = 2;
	private final int EMAIL = 3;

	private InputMethodManager inputKeyboard;
	/**
	 * Attaches onKey listener to the Button passed as a parameter to the
	 * method. If enter key on the keyboard or center key on the keypad is
	 * pressed, the value of the parameter passed is checked.
	 * 
	 * @param button
	 *            The button with which the onKeyListener is to be associated.
	 * @param buttonFlag
	 *            If the value of buttonFlag is 1, the number is set as primary
	 *            by calling the setPrimaryNumberAndGiveFeedback method. If the
	 *            value of buttonFlag is 2, the email ID is set as primary by
	 *            calling the setPrimaryMailAndGiveFeedback method.
	 */

	void attachKeyListener(final Button button, final int buttonFlag) {
		button.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						switch (buttonFlag) {
						case 1:
							setPrimaryNumberAndGiveFeedback();
							break;
						case 2:
							setPrimaryMailAndGiveFeedback();
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
	 * Sets the email as primary by calling the setPrimaryMail method, and gives
	 * the feedback to the user if keyboard is connected to the device and
	 * accessibility services are disabled.
	 */
	void setPrimaryMailAndGiveFeedback() {
		if (setPrimaryMail(ContactUpdate.this.id, ContactUpdate.this.email)) {
			// give feedback
			// check if keyboard is connected but accessibility services are
			// disabled
			if (!Utils.isAccessibilityEnabled(getApplicationContext())
					&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
				TTS.speak(getResources().getString(
						R.string.setprimarymailsuccess));
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.setprimarymailsuccess),
					Toast.LENGTH_SHORT).show();
			btnSetPrimary.setVisibility(View.GONE);
		} else {
			// give feedback
			// check if keyboard is connected but accessibility services are
			// disabled
			if (!Utils.isAccessibilityEnabled(getApplicationContext())
					&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
				TTS.speak(getResources().getString(
						R.string.setprimarymailfailed));
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.setprimarymailfailed),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Sets the number as primary by calling the setPrimaryNumber method, and
	 * gives the feedback to the user if keyboard is connected to the device and
	 * accessibility services are disabled.
	 */
	void setPrimaryNumberAndGiveFeedback() {
		if (setPrimaryNumber(ContactUpdate.this.id, ContactUpdate.this.number)) {
			// give feedback
			// check if keyboard is connected but accessibility services are
			// disabled
			if (!Utils.isAccessibilityEnabled(getApplicationContext())
					&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
				TTS.speak(getResources().getString(
						R.string.setprimarynumbersuccess));
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.setprimarynumbersuccess),
					Toast.LENGTH_SHORT).show();
			btnSetPrimary.setVisibility(View.GONE);
		} else {
			// give feedback
			// check if keyboard is connected but accessibility services are
			// disabled
			if (!Utils.isAccessibilityEnabled(getApplicationContext())
					&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
				TTS.speak(getResources().getString(
						R.string.setprimarynumberfailed));
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.setprimarynumberfailed),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Checks if the number of the contact is set as primary.
	 * 
	 * @param contactId
	 *            The contact ID of the contact.
	 * @param contactNumber
	 *            The number of the contact that is to be checked.
	 * @return Returns true if the number is set as primary, otherwise returns
	 *         false.
	 */
	boolean isPrimaryNumber(String contactId, String contactNumber) {
		Cursor cursor = getContentResolver().query(Phone.CONTENT_URI,
				new String[] { Phone.DATA, Phone.IS_PRIMARY },
				Phone.CONTACT_ID + "=?", new String[] { contactId }, null);
		int index = cursor.getColumnIndex(Phone.DATA);
		while (cursor.moveToNext()) {
			String phoneNumber = cursor.getString(index);
			if (phoneNumber.equals(contactNumber)) {
				// number is primary
				if (!(cursor.getString(cursor.getColumnIndex(Phone.IS_PRIMARY))
						.equals("0"))) {
					cursor.close();
					return true;
				}
			}
		}
		cursor.close();
		return false;
	}

	/**
	 * Checks if the email ID of the contact is set as primary.
	 * 
	 * @param contactId
	 *            The contact ID of the contact.
	 * @param emailId
	 *            The email ID of the contact that is to be checked.
	 * @return Returns true if the email ID is set as primary, otherwise returns
	 *         false.
	 */

	@SuppressLint("InlinedApi")
	boolean isPrimaryMail(String contactId, String emailId) {
		Cursor cursor = getContentResolver().query(
				ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
				new String[] { contactId }, null);

		while (cursor.moveToNext()) {
			String mail = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
			Log.e(mail,
					cursor.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Email.IS_PRIMARY)));

			if (emailId.equals(mail)) {
				if (!(cursor
						.getString(cursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.IS_PRIMARY))
						.equals("0"))) {
					// email id is primary
					return true;
				}
			}
		}
		cursor.close();
		return false;
	}

	/**
	 * Sets a number as the primary number for the contact.
	 * 
	 * @param contactId
	 *            The contact ID of the contact.
	 * @param contactNumber
	 *            The number of the contact that is to be set as primary.
	 * @return Returns true if the number was successfully set as primary,
	 *         otherwise returns false.
	 */
	boolean setPrimaryNumber(String contactId, String contactNumber) {
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		@SuppressWarnings("unused")
		Cursor cursor = getContentResolver().query(Phone.CONTENT_URI, null,
				Phone.CONTACT_ID + "=?", new String[] { contactId }, null);
		String where = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?";

		String[] params = new String[] { contactId, contactNumber };

		ops.add(ContentProviderOperation
				.newUpdate(ContactsContract.Data.CONTENT_URI)
				.withSelection(where, params)
				.withValue(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY, 1)
				.build());

		try {
			getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		} catch (OperationApplicationException e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * Sets an email ID as the primary email ID for the contact.
	 * 
	 * @param contactId
	 *            The contact ID of the contact.
	 * @param emailId
	 *            The email ID of the contact that is to be set as primary.
	 * @return Returns true if the email ID was successfully set as primary,
	 *         otherwise returns false.
	 */
	@SuppressLint("InlinedApi")
	boolean setPrimaryMail(String contactId, String emailId) {
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		@SuppressWarnings("unused")
		Cursor cursor = getContentResolver().query(
				ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
				new String[] { contactId }, null);
		String where = ContactsContract.CommonDataKinds.Email.CONTACT_ID
				+ " = ? AND " + ContactsContract.CommonDataKinds.Email.ADDRESS
				+ " = ?";

		String[] params = new String[] { contactId, emailId };

		ops.add(ContentProviderOperation
				.newUpdate(Data.CONTENT_URI)
				.withSelection(where, params)
				.withValue(ContactsContract.CommonDataKinds.Email.IS_PRIMARY, 1)
				.build());

		try {
			getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		} catch (OperationApplicationException e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * Attaches onTouchListener and onFocusListener to the EditText.
	 * 
	 * @param editText
	 *            The editText with which the onTouchListener and
	 *            onFocusChangeListener are to be associated.
	 */
	void attachOnTouchAndFocusListener(final EditText editText) {
		final String text = editText.getText().toString().trim();
		editText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus && !(text.equals(""))) {
					// check if keyboard is connected but accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
						if (text.equals("")) {
							// empty EditText
							TTS.speak(editText.getHint().toString());
						} else {
							if (text.matches("-?\\d+(\\.\\d+)?")) {
								TTS.readNumber(text);
							} else {
								TTS.speak(text);
							}
						}
					}
				}
			}
		});
	}

	/**
	 * Announces the text that is passed as a parameter and causes the device to
	 * vibrate for 300 milliseconds.
	 * 
	 * @param text
	 *            The string that is to be read aloud.
	 */
	public void giveFeedback(String text) {
		// vibrate
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(300);
		// TTS feedback
		TTS.speak(text);
	}

	/**
	 * Saves the contact in the phone
	 * 
	 * @return true on success and false on failure.
	 */
	boolean saveContact() {
		ArrayList<ContentProviderOperation> op_list = new ArrayList<ContentProviderOperation>();
		op_list.add(ContentProviderOperation
				.newInsert(ContactsContract.RawContacts.CONTENT_URI)
				.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
				.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
				.build());

		// first and last names
		op_list.add(ContentProviderOperation
				.newInsert(Data.CONTENT_URI)
				.withValueBackReference(Data.RAW_CONTACT_ID, 0)
				.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
				.withValue(
						ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
						this.editField.getText().toString()).build());

		op_list.add(ContentProviderOperation
				.newInsert(Data.CONTENT_URI)
				.withValueBackReference(Data.RAW_CONTACT_ID, 0)
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
						this.number)
				.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
						this.typeIndex)
				.withValue(ContactsContract.CommonDataKinds.Phone.LABEL,
						this.spinnerType.getSelectedItem().toString()).build());

		try {
			@SuppressWarnings("unused")
			ContentProviderResult[] results = getContentResolver().applyBatch(
					ContactsContract.AUTHORITY, op_list);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Updates the details of a contact.
	 * 
	 * @param contactId
	 *            The contact ID of the contact.
	 * @param flag
	 *            If the value of flag corresponds to the value in the constant
	 *            NAME, the name of the contact is updated. If the value of flag
	 *            corresponds to the value in the constant NUMBER, the number of
	 *            the contact is updated. If the value of flag corresponds to
	 *            the value in the constant EMAIL, the email ID of the contact
	 *            is updated.
	 * @return Returns true on successful update and false on failure.
	 */

	@SuppressLint("InlinedApi")
	boolean editContact(String contactId, int flag) {
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		switch (flag) {
		case NAME:
			ops.add(ContentProviderOperation
					.newUpdate(Data.CONTENT_URI)
					.withSelection(
							Data.CONTACT_ID + "=? AND " + Data.MIMETYPE + "='"
									+ StructuredName.CONTENT_ITEM_TYPE + "'",
							new String[] { contactId })
					.withValue(StructuredName.DISPLAY_NAME,
							this.editField.getText().toString()).build());
			break;
		case NUMBER:
			ops.add(ContentProviderOperation
					.newUpdate(Data.CONTENT_URI)
					.withSelection(
							Data.CONTACT_ID
									+ "=? AND "
									+ ContactsContract.Data.MIMETYPE
									+ "='"
									+ ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
									+ "'", new String[] { contactId })
					.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
							this.editField.getText().toString())
					.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
							this.typeIndex)
					.withValue(ContactsContract.CommonDataKinds.Phone.LABEL,
							this.spinnerType.getSelectedItem().toString())
					.build());
			break;
		case EMAIL:
			ops.add(ContentProviderOperation
					.newUpdate(Data.CONTENT_URI)
					.withSelection(
							Data.CONTACT_ID
									+ "=? AND "
									+ ContactsContract.Data.MIMETYPE
									+ "='"
									+ ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
									+ "'", new String[] { contactId })
					.withValue(ContactsContract.CommonDataKinds.Email.ADDRESS,
							this.editField.getText().toString()).build());
			break;
		}
		try {
			@SuppressWarnings("unused")
			ContentProviderResult[] results = getContentResolver().applyBatch(
					ContactsContract.AUTHORITY, ops);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Create the Contact Details menu activity (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.contactupdate);
		super.onCreate(savedInstanceState);

		inputKeyboard = (InputMethodManager) getApplicationContext()
	            .getSystemService(Context.INPUT_METHOD_SERVICE);

		// get UI elements
		editField = (EditText) findViewById(R.id.editField);
	//	editField.setFocusable(false);
		txtType = (TextView) findViewById(R.id.txtType);
		btnSave = (Button) findViewById(R.id.btnUpdate);
		btnSetPrimary = (Button) findViewById(R.id.btnSetPrimary);
		spinnerType = (Spinner) findViewById(R.id.spinnerType);

		// Find easyaccess-specific Back and Home buttons
		Button btnNavigationBack = (Button) findViewById(R.id.btnNavigationBack);
		Button btnNavigationHome = (Button) findViewById(R.id.btnNavigationHome);

		// If Back navigation button is pressed, go back to previous activity
		btnNavigationBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		// If Home navigation button is pressed, go back to previous activity
		btnNavigationHome.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
				Intent intent = new Intent(getApplicationContext(),
						SwipingUtils.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				startActivity(intent);
			}
		});

		OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) {
					TTS.speak(((TextView) view).getText().toString());
				}
			}
		};

		// Attach onFocusChange listener to back and home buttons
		btnNavigationBack.setOnFocusChangeListener(focusChangeListener);
		btnNavigationHome.setOnFocusChangeListener(focusChangeListener);

		adapter = new SpinnerAdapter(getApplicationContext(), Utils.numberType);
		spinnerType.setAdapter(adapter);

		TextWatcher textWatcher = new TextWatcher() {

			@SuppressLint("DefaultLocale")
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				if (deletedFlag != 1) {
					if (cs.length() > 0) {
						// check if keyboard is connected but accessibility
						// services are disabled
						if (!Utils
								.isAccessibilityEnabled(getApplicationContext())
								&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
							if (cs.toString()
									.substring(cs.length() - 1, cs.length())
									.matches("(?![@',&] )\\p{Punct}")) {
								if (editField.getText().toString()
										.matches("-?\\d+(\\.\\d+)?")) {
									TTS.readNumber(editField.getText()
											.toString());
								} else {
									TTS.speak(editField.getText().toString());
								}
							} else {
								TTS.speak(cs.toString().substring(
										cs.length() - 1, cs.length()));
							}
						}
					}
				} else {
					deletedFlag = 0;
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {

			}
		};

		textWatcher = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				// check if keyboard is connected but accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
					if (cs.toString().matches("(?![@',&])\\p{Punct}"))
						TTS.speak(editField.getText().toString());
					else
						TTS.speak(cs.toString());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		};

		editField.addTextChangedListener(textWatcher);

		if (getIntent().getExtras() != null) {
			// id, name, number, numtype, email
			this.id = getIntent().getExtras().getString("id");

			if (getIntent().hasExtra("name")) {
				this.editFlag = NAME;
				this.name = getIntent().getExtras().getString("name");
				editField.setText(this.name);
				
				editField.setContentDescription(this.name.replaceAll(
						".(?=[0-9])", "$0 "));
				txtType.setVisibility(View.GONE);
				spinnerType.setVisibility(View.GONE);
				btnSetPrimary.setVisibility(View.GONE);
			}
			if (getIntent().hasExtra("number")) {
				this.editFlag = NUMBER;
				this.number = getIntent().getExtras().getString("number");
				editField.setText(this.number);
				editField.setContentDescription(this.number.replaceAll(
						".(?=[0-9])", "$0 "));
				editField.setInputType(InputType.TYPE_CLASS_PHONE);
				this.numType = getIntent().getExtras().getString("numtype");
				txtType.setVisibility(View.VISIBLE);
				spinnerType.setVisibility(View.VISIBLE);
				// mobile
				if (numType
						.equals((getResources().getStringArray(R.array.type))[0]))
					spinnerType.setSelection(0);
				// home
				else if (numType.equals((getResources()
						.getStringArray(R.array.type))[1]))
					spinnerType.setSelection(1);
				// work
				else if (numType.equals((getResources()
						.getStringArray(R.array.type))[2]))
					spinnerType.setSelection(2);
				// work mobile
				else if (numType.equals((getResources()
						.getStringArray(R.array.type))[3]))
					spinnerType.setSelection(3);
				// home fax
				else if (numType.equals((getResources()
						.getStringArray(R.array.type))[4]))
					spinnerType.setSelection(4);
				// pager
				else if (numType.equals((getResources()
						.getStringArray(R.array.type))[5]))
					spinnerType.setSelection(5);
				// other
				else
					spinnerType.setSelection(6);
				if (isPrimaryNumber(this.id, this.number)) {
					btnSetPrimary.setVisibility(View.GONE);
				} else {
					// if not primary, display set primary button Set Primary
					// set delete button's visibility to GONE
					btnSetPrimary.setVisibility(View.VISIBLE);

					attachKeyListener(btnSetPrimary, 1);

					btnSetPrimary.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							setPrimaryNumberAndGiveFeedback();
						}
					});
				}
			}
			if (getIntent().hasExtra("mail")) {
				this.editFlag = EMAIL;
				this.email = getIntent().getExtras().getString("mail");
				editField
						.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
				editField.setText(this.email);
				txtType.setVisibility(View.GONE);
				spinnerType.setVisibility(View.GONE);
				if (isPrimaryMail(this.id, this.email)) {
					btnSetPrimary.setVisibility(View.GONE);
				} else {
					// if not primary, display set primary button, on click of
					// Set Primary
					// set delete button's visibility to GONE
					btnSetPrimary.setVisibility(View.VISIBLE);

					attachKeyListener(btnSetPrimary, 2);

					btnSetPrimary.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							setPrimaryMailAndGiveFeedback();
						}
					});
				}
			}
		}

		txtType.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) {
					giveFeedback(((TextView) view).getText().toString());
				}
			}
		});

		spinnerType.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				Spinner spinner = (Spinner) view;
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_DOWN:
						currentSelection++;
						if (currentSelection == spinner.getCount()) {
							currentSelection = 0;
						}
						giveFeedback(spinnerType.getItemAtPosition(
								currentSelection).toString());
						spinner.setSelection(currentSelection);
						break;
					case KeyEvent.KEYCODE_DPAD_UP:
						currentSelection--;
						if (currentSelection <= -1) {
							currentSelection = spinnerType.getCount() - 1;
						} else {
							giveFeedback(spinnerType.getItemAtPosition(
									currentSelection).toString());
							spinnerType.setSelection(currentSelection);
						}
					}
				}
				return false;
			}
		});

		spinnerType.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (spinnerType.getSelectedItem().toString()
						.equalsIgnoreCase("mobile")) {
					ContactUpdate.this.typeIndex = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
				} else if (spinnerType.getSelectedItem().toString()
						.equalsIgnoreCase("home")) {
					ContactUpdate.this.typeIndex = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
				} else if (spinnerType.getSelectedItem().toString()
						.equalsIgnoreCase("work")) {
					ContactUpdate.this.typeIndex = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
				} else if (spinnerType.getSelectedItem().toString()
						.equalsIgnoreCase("workmobile")) {
					ContactUpdate.this.typeIndex = ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE;
				} else if (spinnerType.getSelectedItem().toString()
						.equalsIgnoreCase("work")) {
					ContactUpdate.this.typeIndex = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
				} else if (spinnerType.getSelectedItem().toString()
						.equalsIgnoreCase("pager")) {
					ContactUpdate.this.typeIndex = ContactsContract.CommonDataKinds.Phone.TYPE_PAGER;
				} else if (spinnerType.getSelectedItem().toString()
						.equalsIgnoreCase("other")) {
					ContactUpdate.this.typeIndex = ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		btnSave.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) {
					giveFeedback(((Button) view).getText().toString());
				}
			}
		});

		btnSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// get Id of contact
				// edit contact
				if (editContact(ContactUpdate.this.id,
						ContactUpdate.this.editFlag)) {
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.contactupdated),
							Toast.LENGTH_SHORT).show();
					// check if keyboard is connected but accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
						TTS.speak(getResources().getString(
								R.string.contactupdated));
					finish();
				} else {
					Toast.makeText(
							getApplicationContext(),
							getResources()
									.getString(R.string.contactnotupdated),
							Toast.LENGTH_SHORT).show();
					// check if keyboard is connected but accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
						TTS.speak(getResources().getString(
								R.string.contactnotupdated));
				}
			}
		});
		
		editField.setSelection(editField.length());
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
			TTS.speak(getResources().getString(R.string.update));
		}
		// get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.contactupdate);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.text.method.KeyListener#clearMetaKeyState(android.view.View,
	 * android.text.Editable, int)
	 */
	@Override
	public void clearMetaKeyState(View arg0, Editable arg1, int arg2) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.text.method.KeyListener#getInputType()
	 */
	@Override
	public int getInputType() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.text.method.KeyListener#onKeyDown(android.view.View,
	 * android.text.Editable, int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(View arg0, Editable arg1, int arg2, KeyEvent arg3) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.text.method.KeyListener#onKeyOther(android.view.View,
	 * android.text.Editable, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyOther(View arg0, Editable arg1, KeyEvent arg2) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.text.method.KeyListener#onKeyUp(android.view.View,
	 * android.text.Editable, int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyUp(View arg0, Editable arg1, int arg2, KeyEvent arg3) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent)
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			//if (!editField.hasFocus()) {
			
			System.out.println("is focused "+editField.hasFocus());
				if (!inputKeyboard.isAcceptingText()) {
				
				if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
					// go to the previous screen
					// check if keyboard is connected and accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
						TTS.speak(getString(R.string.btnNavigationBack));
					finish();
				} else if (event.getKeyCode() == KeyEvent.KEYCODE_F1) {
					// go to the home screen
					// check if keyboard is connected and accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
						TTS.speak(getString(R.string.btnNavigationHome));
					finish();
					Intent intent = new Intent(getApplicationContext(),
							SwipingUtils.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
					startActivity(intent);
				} else
					return super.dispatchKeyEvent(event);
			} else {
				if (editField.hasFocus()) {
					deletedFlag = 1;
					String editFieldText = editField.getText().toString();

					if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
						if (editFieldText.length() != 0) {
							// check if keyboard is connected and accessibility
							// services are disabled
							if (!Utils
									.isAccessibilityEnabled(getApplicationContext())
									&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
								if (editFieldText.substring(
										editFieldText.length() - 1,
										editFieldText.length()).matches(
										"-?\\d+(\\.\\d+)?")) {
									TTS.speak(getString(R.string.deleted)
											+ " "
											+ editFieldText.substring(
													editFieldText.length() - 1,
													editFieldText.length())
											+ ". "
											+ TTS.readNumber(editFieldText.substring(
													0, editFieldText.length() - 1)));
								} else {
									TTS.speak(getString(R.string.deleted)
											+ " "
											+ editFieldText.substring(
													editFieldText.length() - 1,
													editFieldText.length())
											+ ". "
											+ editFieldText.substring(0,
													editFieldText.length() - 1));
								}
							}
							editField.setText(editFieldText.substring(0,
									editFieldText.length() - 1));
							editField.setContentDescription(editFieldText
									.replaceAll(".(?=[0-9])", "$0 "));
							editField.setSelection(editFieldText.length()-1);
							
							return false;
						} else {
							// check if keyboard is connected and accessibility
							// services are disabled
							if (!Utils
									.isAccessibilityEnabled(getApplicationContext())
									&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
								TTS.speak(getString(R.string.btnNavigationBack));
							finish();
						}
					}
					else {
						return super.dispatchKeyEvent(event);
					}
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}
}
