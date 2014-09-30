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
import org.easyaccess.phonedialer.ContactManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Saves the contact in the phone.
 */

public class SaveContact extends Activity implements KeyListener {
	/** Declare variables and UI elements **/
	private EditText editName, editNumber, editEmail;
	private TextView txtType;
	private Button btnSave;
	private TextWatcher textWatcher;
	private Spinner spinnerType;
	private SpinnerAdapter adapter;
	private String number;
	private int typeIndex;
	private int currentSelection = -1, deletedFlagName = 0,
			deletedFlagNumber = 0, deletedFlagEmail = 0;
	private boolean editFlag;

	/**
	 * Attaches onKey listener to the Button passed as a parameter to the
	 * method. If enter key on the keyboard or center key on the keypad is
	 * pressed, saveAndGiveFeedback method is called.
	 * 
	 * @param button
	 *            The button with which the onKeyListener is to be associated.
	 */
	void attachKeyListener(final Button button) {
		button.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						saveAndGiveFeedback();
						break;
					}
				}
				return false;
			}
		});
	}

	/**
	 * Attaches onTouchListener and onFocusListener to the EditText passed as a
	 * parameter.
	 * 
	 * @param editText
	 *            The EditText with which the onTouchListener and
	 *            onFocusListener are to be associated.
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
	 * Saves the new contact.
	 * 
	 * @return Returns true on success, and false on failure.
	 **/
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
						this.editName.getText().toString()).build());

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
		op_list.add(ContentProviderOperation
				.newInsert(Data.CONTENT_URI)
				.withValueBackReference(Data.RAW_CONTACT_ID, 0)
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Email.DATA,
						editEmail.getText().toString()).build());

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
	 * Updates a contact.
	 * 
	 * @param id
	 *            The contact ID of the contact.
	 * @return Returns true on success and false on failure.
	 */
	boolean editContact(String id) {
		ArrayList<ContentProviderOperation> op_list = new ArrayList<ContentProviderOperation>();

		ContentResolver cr = getContentResolver();

		String where = Data.RAW_CONTACT_ID + "=?";
		String[] params = new String[] { id };

		Cursor phoneCur = getContentResolver().query(
				ContactsContract.Data.CONTENT_URI, null, where, params, null);
		phoneCur.moveToFirst();

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

		// first and last names
		ops.add(ContentProviderOperation
				.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
				.withSelection(Data._ID + "=?", new String[] { id })
				.withValue("display_name", this.editName.getText().toString())
				.build());

		op_list.add(ContentProviderOperation
				.newUpdate(Data.CONTENT_URI)
				.withSelection(Data._ID + "=?", new String[] { id })
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
						this.editNumber.getText().toString())
				.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
						this.typeIndex)
				.withValue(ContactsContract.CommonDataKinds.Phone.LABEL,
						this.spinnerType.getSelectedItem().toString()).build());
		op_list.add(ContentProviderOperation
				.newUpdate(Data.CONTENT_URI)
				.withSelection(Data._ID + "=?", new String[] { id })
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Email.DATA,
						editEmail.getText().toString()).build());

		phoneCur.close();

		try {
			ContentProviderResult[] result = cr.applyBatch(
					ContactsContract.AUTHORITY, ops);
			if (result[0] == null)
				return false;
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
	 * Edits the contact by calling editContact() and gives the feedback to the
	 * user if keyboard is connected and accessibility services are disabled.
	 */
	void saveAndGiveFeedback() {
		if (editFlag == true) {
			// get Id of contact
			String id = new ContactManager(getApplicationContext())
					.getId(SaveContact.this.number);
			// edit contact
			if (editContact(id)) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.contactupdated),
						Toast.LENGTH_SHORT).show();
				// check if keyboard is connected but accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(getResources().getString(R.string.contactupdated));
				finish();
			} else {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.contactnotupdated),
						Toast.LENGTH_SHORT).show();
				// check if keyboard is connected but accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(getResources().getString(
							R.string.contactnotupdated));
			}
		} else {
			if (saveContact()) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.contactsaved),
						Toast.LENGTH_SHORT).show();
				// check if keyboard is connected but accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(getResources().getString(R.string.contactsaved));
				finish();
			} else {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.contactnotsaved),
						Toast.LENGTH_SHORT).show();
				// check if keyboard is connected but accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(getResources()
							.getString(R.string.contactnotsaved));
			}
		}
	}

	/**
	 * Create the Contact Details menu activity (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.savecontact);
		super.onCreate(savedInstanceState);

		/** get UI elements **/
		editName = (EditText) findViewById(R.id.editName);
		editNumber = (EditText) findViewById(R.id.editNumber);
		editEmail = (EditText) findViewById(R.id.editEmail);
		txtType = (TextView) findViewById(R.id.txtType);
		btnSave = (Button) findViewById(R.id.btnSave);
		spinnerType = (Spinner) findViewById(R.id.spinnerType);
		adapter = new SpinnerAdapter(getApplicationContext(), Utils.numberType);
		spinnerType.setAdapter(adapter);

		/** Find easyaccess-specific Back and Home buttons **/
		Button btnNavigationBack = (Button) findViewById(R.id.btnNavigationBack);
		Button btnNavigationHome = (Button) findViewById(R.id.btnNavigationHome);

		/** If Back navigation button is pressed, go back to previous activity **/
		btnNavigationBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		/** If Home navigation button is pressed, go back to previous activity **/
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

		/** Attach onFocusChange listener to back and home buttons **/
		btnNavigationBack.setOnFocusChangeListener(focusChangeListener);
		btnNavigationHome.setOnFocusChangeListener(focusChangeListener);

		if (getIntent().getExtras() != null) {
			this.number = getIntent().getExtras().getString("number");
			editNumber.setText(this.number);
			editNumber.setContentDescription(this.number.replaceAll(
					".(?=[0-9])", "$0 "));
			if (getIntent().getExtras().getString("name") != null) {
				editName.setText(getIntent().getExtras().getString("name"));
				editName.setContentDescription(getIntent().getExtras()
						.getString("name").replaceAll(".(?=[0-9])", "$0 "));
			}
			if (getIntent().getExtras().getString("email") != null) {
				editEmail.setText(getIntent().getExtras().getString("email"));
			}
			if (getIntent().hasExtra("edit")) {
				editFlag = getIntent().getExtras().getBoolean("edit");
			}
		}

		textWatcher = new TextWatcher() {

			@SuppressLint("DefaultLocale")
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				if (editName.hasFocus()) {
					if (deletedFlagName != 1) {
						if (cs.length() > 0) {
							// check if keyboard is connected but accessibility
							// services are disabled
							if (!Utils
									.isAccessibilityEnabled(getApplicationContext())
									&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
								if (cs.toString()
										.substring(cs.length() - 1, cs.length())
										.matches("(?![@',&])\\p{Punct}")) {
									if (editName.getText().toString()
											.matches("-?\\d+(\\.\\d+)?")) {
										TTS.readNumber(editName.getText()
												.toString());
									} else {
										TTS.speak(editName.getText().toString());
									}
								} else {
									TTS.speak(cs.toString().substring(
											cs.length() - 1, cs.length()));
								}
							}
						}
					} else {
						deletedFlagName = 0;
					}
				} else if (editNumber.hasFocus()) {
					if (deletedFlagNumber != 1) {
						if (cs.length() > 0) {
							// check if keyboard is connected but accessibility
							// services are disabled
							if (!Utils
									.isAccessibilityEnabled(getApplicationContext())
									&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
								if (cs.toString()
										.substring(cs.length() - 1, cs.length())
										.matches("(?![@',&] )\\p{Punct}")) {
									if (editNumber.getText().toString()
											.matches("-?\\d+(\\.\\d+)?")) {
										TTS.readNumber(editNumber.getText()
												.toString());
									} else {
										TTS.speak(editNumber.getText()
												.toString());
									}
								} else {
									TTS.speak(cs.toString().substring(
											cs.length() - 1, cs.length()));
								}
							}
						}
					} else {
						deletedFlagNumber = 0;
					}
				} else if (editEmail.hasFocus()) {
					if (deletedFlagEmail != 1) {
						if (cs.length() > 0) {
							// check if keyboard is connected but accessibility
							// services are disabled
							if (!Utils
									.isAccessibilityEnabled(getApplicationContext())
									&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
								if (cs.toString()
										.substring(cs.length() - 1, cs.length())
										.matches("(?![@',&] )\\p{Punct}")) {
									if (editEmail.getText().toString()
											.matches("-?\\d+(\\.\\d+)?")) {
										TTS.readNumber(editEmail.getText()
												.toString());
									} else {
										TTS.speak(editEmail.getText()
												.toString());
									}
								} else {
									TTS.speak(cs.toString().substring(
											cs.length() - 1, cs.length()));
								}
							}
						}
					} else {
						deletedFlagEmail = 0;
					}
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

		editName.addTextChangedListener(textWatcher);
		editEmail.addTextChangedListener(textWatcher);
		editNumber.addTextChangedListener(textWatcher);

		txtType.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) {
					Utils.giveFeedback(getApplicationContext(),
							((TextView) view).getText().toString());
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
						Utils.giveFeedback(getApplicationContext(), spinnerType
								.getItemAtPosition(currentSelection).toString());
						spinner.setSelection(currentSelection);
						break;
					case KeyEvent.KEYCODE_DPAD_UP:
						currentSelection--;
						if (currentSelection <= -1) {
							currentSelection = spinnerType.getCount() - 1;
						} else {
							Utils.giveFeedback(
									getApplicationContext(),
									spinnerType.getItemAtPosition(
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
					SaveContact.this.typeIndex = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
				} else if (spinnerType.getSelectedItem().toString()
						.equalsIgnoreCase("home")) {
					SaveContact.this.typeIndex = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
				} else if (spinnerType.getSelectedItem().toString()
						.equalsIgnoreCase("work")) {
					SaveContact.this.typeIndex = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
				} else if (spinnerType.getSelectedItem().toString()
						.equalsIgnoreCase("workmobile")) {
					SaveContact.this.typeIndex = ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE;
				} else if (spinnerType.getSelectedItem().toString()
						.equalsIgnoreCase("work")) {
					SaveContact.this.typeIndex = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
				} else if (spinnerType.getSelectedItem().toString()
						.equalsIgnoreCase("pager")) {
					SaveContact.this.typeIndex = ContactsContract.CommonDataKinds.Phone.TYPE_PAGER;
				} else if (spinnerType.getSelectedItem().toString()
						.equalsIgnoreCase("other")) {
					SaveContact.this.typeIndex = ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;
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
					Utils.giveFeedback(getApplicationContext(), ((Button) view)
							.getText().toString());
				}
			}
		});

		btnSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				saveAndGiveFeedback();
			}
		});

		attachKeyListener(btnSave);

		attachOnTouchAndFocusListener(editName);
		attachOnTouchAndFocusListener(editNumber);
		attachOnTouchAndFocusListener(editEmail);
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
			TTS.speak(getResources().getString(R.string.savecontact));
		}
		// get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.savecontact);
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
		if (event.getAction() == KeyEvent.ACTION_DOWN) { // &&
															// !(editName.getText().toString().trim().equals("")))
															// {
			if (!editName.hasFocus() && !editNumber.hasFocus()
					&& !editEmail.hasFocus()) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {// go to the
																	// previous
																	// screen
					// check if keyboard is connected and accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
						TTS.speak("Back");
					finish();
				} else if (event.getKeyCode() == KeyEvent.KEYCODE_F1) {// go to
																		// the
																		// home
																		// screen
					// check if keyboard is connected and accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
						TTS.speak("Home");
					finish();
					Intent intent = new Intent(getApplicationContext(),
							SwipingUtils.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
					startActivity(intent);
				} else
					return super.dispatchKeyEvent(event);
			} else {
				if (editName.hasFocus()) {
					if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
						deletedFlagName = 1;
						if (editName.getText().toString().length() != 0) {
							// check if keyboard is connected and accessibility
							// services are disabled
							if (!Utils
									.isAccessibilityEnabled(getApplicationContext())
									&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
								if (editName
										.getText()
										.toString()
										.substring(
												editName.getText().toString()
														.length() - 1,
												editName.getText().toString()
														.length())
										.matches("-?\\d+(\\.\\d+)?")) {
									TTS.speak("Deleted "
											+ editName
													.getText()
													.toString()
													.substring(
															editName.getText()
																	.toString()
																	.length() - 1,
															editName.getText()
																	.toString()
																	.length())
											+ ". "
											+ TTS.readNumber(editName
													.getText()
													.toString()
													.substring(
															0,
															editName.getText()
																	.toString()
																	.length() - 1)));
								} else {
									TTS.speak("Deleted "
											+ editName
													.getText()
													.toString()
													.substring(
															editName.getText()
																	.toString()
																	.length() - 1,
															editName.getText()
																	.toString()
																	.length())
											+ ". "
											+ editName
													.getText()
													.toString()
													.substring(
															0,
															editName.getText()
																	.toString()
																	.length() - 1));
								}
							}
							editName.setText(editName
									.getText()
									.toString()
									.substring(
											0,
											editName.getText().toString()
													.length() - 1));
							editName.setContentDescription(editName.getText()
									.toString().replaceAll(".(?=[0-9])", "$0 "));
							editName.setSelection(editName.getText().toString()
									.length(), editName.getText().toString()
									.length());
							return false;
						} else {
							// check if keyboard is connected and accessibility
							// services are disabled
							if (!Utils
									.isAccessibilityEnabled(getApplicationContext())
									&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
								TTS.speak("Back");
							finish();
						}
					} else {
						return super.dispatchKeyEvent(event);
					}
				} else if (editNumber.hasFocus()) {
					if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
						TTS.speak("Deleted "
								+ editNumber
										.getText()
										.toString()
										.substring(
												editNumber.getText().toString()
														.length() - 1,
												editNumber.getText().toString()
														.length()));
						deletedFlagNumber = 1;
						if (editNumber.getText().toString().length() != 0) {
							// check if keyboard is connected and accessibility
							// services are disabled
							if (!Utils
									.isAccessibilityEnabled(getApplicationContext())
									&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
								if (editNumber
										.getText()
										.toString()
										.substring(
												editNumber.getText().toString()
														.length() - 1,
												editNumber.getText().toString()
														.length())
										.matches("-?\\d+(\\.\\d+)?")) {
									TTS.speak("Deleted "
											+ editNumber
													.getText()
													.toString()
													.substring(
															editNumber
																	.getText()
																	.toString()
																	.length() - 1,
															editNumber
																	.getText()
																	.toString()
																	.length())
											+ ". "
											+ TTS.readNumber(editNumber
													.getText()
													.toString()
													.substring(
															0,
															editNumber
																	.getText()
																	.toString()
																	.length() - 1)));
								} else {
									TTS.speak("Deleted "
											+ editNumber
													.getText()
													.toString()
													.substring(
															editNumber
																	.getText()
																	.toString()
																	.length() - 1,
															editNumber
																	.getText()
																	.toString()
																	.length())
											+ ". "
											+ editNumber
													.getText()
													.toString()
													.substring(
															0,
															editNumber
																	.getText()
																	.toString()
																	.length() - 1));
								}
							}
							editNumber.setText(editNumber
									.getText()
									.toString()
									.substring(
											0,
											editNumber.getText().toString()
													.length() - 1));
							editNumber.setContentDescription(editNumber
									.getText().toString()
									.replaceAll(".(?=[0-9])", "$0 "));
							editNumber.setSelection(editNumber.getText()
									.toString().length(), editNumber.getText()
									.toString().length());
							return false;
						} else {
							// check if keyboard is connected and accessibility
							// services are disabled
							if (!Utils
									.isAccessibilityEnabled(getApplicationContext())
									&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
								TTS.speak("Back");
							finish();
						}
					} else {
						return super.dispatchKeyEvent(event);
					}
				} else if (editEmail.hasFocus()) {
					if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
						TTS.speak("Deleted "
								+ editEmail
										.getText()
										.toString()
										.substring(
												editEmail.getText().toString()
														.length() - 1,
												editEmail.getText().toString()
														.length()));
						deletedFlagEmail = 1;
						if (editEmail.getText().toString().length() != 0) {
							// check if keyboard is connected and accessibility
							// services are disabled
							if (!Utils
									.isAccessibilityEnabled(getApplicationContext())
									&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
								if (editEmail
										.getText()
										.toString()
										.substring(
												editEmail.getText().toString()
														.length() - 1,
												editEmail.getText().toString()
														.length())
										.matches("-?\\d+(\\.\\d+)?")) {
									TTS.speak("Deleted "
											+ editEmail
													.getText()
													.toString()
													.substring(
															editEmail.getText()
																	.toString()
																	.length() - 1,
															editEmail.getText()
																	.toString()
																	.length())
											+ ". "
											+ TTS.readNumber(editEmail
													.getText()
													.toString()
													.substring(
															0,
															editEmail.getText()
																	.toString()
																	.length() - 1)));
								} else {
									TTS.speak("Deleted "
											+ editEmail
													.getText()
													.toString()
													.substring(
															editEmail.getText()
																	.toString()
																	.length() - 1,
															editEmail.getText()
																	.toString()
																	.length())
											+ ". "
											+ editEmail
													.getText()
													.toString()
													.substring(
															0,
															editEmail.getText()
																	.toString()
																	.length() - 1));
								}
							}
							editEmail.setText(editEmail
									.getText()
									.toString()
									.substring(
											0,
											editEmail.getText().toString()
													.length() - 1));
							editEmail.setSelection(editEmail.getText()
									.toString().length(), editEmail.getText()
									.toString().length());
							return false;
						} else {
							// check if keyboard is connected and accessibility
							// services are disabled
							if (!Utils
									.isAccessibilityEnabled(getApplicationContext())
									&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
								TTS.speak("Back");
							finish();
						}
					} else {
						return super.dispatchKeyEvent(event);
					}
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}
}
