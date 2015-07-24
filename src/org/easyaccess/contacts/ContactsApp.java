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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.easyaccess.R;
import org.easyaccess.SwipingUtils;
import org.easyaccess.TTS;
import org.easyaccess.Utils;
import org.easyaccess.phonedialer.ContactManager;
import org.easyaccess.phonedialer.PhoneDialerApp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The Contacts option is used for searching a contact that already exists in
 * the phone. The user may view the details of the contact, edit the contact, or
 * perform operations such as calling, exporting to SD Card, importing from SD
 * card, sending a text message, making a call to the contact, deleting the
 * contact etc. The user may also add a new contact.
 */

@SuppressLint("DefaultLocale")
public class ContactsApp extends Activity implements KeyListener {

	/** Declare variables and UI elements **/
	private ContactManager contactManager;
	private ArrayList<String> name, number;
	private ListView contactsListView;
	private EditText inputContacts;
	private ContactsAdapter contactsAdapter;
	private Button btnCall, btnSave;
	private ArrayList<String> numberArrayList, contactIdArrayList, idArrayList,
			nameArrayList;
	private HashMap<String, ArrayList<String>> contactsMap;
	private ProgressBar progressBar;
	private Handler handler;
	private ObjectOutputStream outputStream;
	private File file;
	private int currentSelection = -1, deletedFlag = 0, count = 0;
	public final int ALL_CONTACTS = 1;

	/**
	 * Launches the ContactsDetailsMenu activity to view the details of the
	 * contact selected from the ListView.
	 * 
	 * @param position
	 *            The position of the selected item in the ListView.
	 */
	void launchDetailsActivity(int position) {
		// hide the keyboard
		InputMethodManager imm = (InputMethodManager) getApplicationContext()
				.getSystemService(Service.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(inputContacts.getWindowToken(), 0);
		// view details
		Intent intent = new Intent(getApplicationContext(),
				ContactsDetailsMenu.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (numberArrayList.size() > 0) {
			intent.putExtra("number", numberArrayList.get(position));
			intent.putExtra("name", nameArrayList.get(position));
			intent.putExtra("id", idArrayList.get(position));
		}
		startActivity(intent);
		finish();
	}

	/**
	 * Launches the PhoneDialerApp activity to make a call to the number or
	 * contact specified by the user.
	 * 
	 * @param inputText
	 *            The number or name of the contact to be called.
	 */
	void callContact(String inputText) {
		Intent intent = new Intent(getApplicationContext(),
				PhoneDialerApp.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("call", inputText);
		startActivity(intent);
		finish();
	}

	/**
	 * Launches the SaveContact activity to save the number in the phone's
	 * contacts.
	 * 
	 * @param inputText
	 *            The number that is to be saved in the phone's contacts.
	 */
	void saveContact(String inputText) {
		Intent intent = new Intent(getApplicationContext(), SaveContact.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("number", inputText);
		startActivity(intent);
		finish();
	}

	/**
	 * Attaches onKey listener to the Button passed as a parameter to the
	 * method. If enter key on the keyboard or center key on the keypad is
	 * pressed, the value of the parameter passed is checked.
	 * 
	 * @param button
	 *            The button with which the onKeyListener is to be associated.
	 * @param buttonFlag
	 *            If the value of buttonFlag is 1, a call is made to the
	 *            specified number or name. If the value of buttonFlag is 2, the
	 *            number is saved in the phone's contacts.
	 */
	void attachKeyListener(Button button, final int buttonFlag) {
		button.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						switch (buttonFlag) {
						case 1:
							callContact(inputContacts.getText().toString());
							break;
						case 2:
							saveContact(inputContacts.getText().toString());
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
	 * Loads the items in the list depending on the characters typed by the
	 * user. It displays a Call button to call the number or contact. It will
	 * display a Save button if the number is not saved in the phone’s contacts.
	 */
	void loadList() {
		int found = 0;
		numberArrayList = new ArrayList<String>();
		nameArrayList = new ArrayList<String>();
		idArrayList = new ArrayList<String>();
		if (!(inputContacts.getText().toString().matches("-?\\d+(\\.\\d+)?"))) {
			for (int i = 0; i < name.size(); i++) {
				
				String[] splited_name = name.get(i).toString().split("\\s+");

				for(int j=0;j<splited_name.length;j++){
					if (splited_name[j]
							.toLowerCase()
							.startsWith(
									inputContacts.getText().toString()
											.toLowerCase())){
						nameArrayList.add(name.get(i));
						numberArrayList.add(number.get(i));
						idArrayList.add(contactIdArrayList.get(i));
						contactsAdapter = new ContactsAdapter(
								getApplicationContext(), nameArrayList);
						found = 1;
					}
				}
				
			}
		} else {
			// user entered a number
			for (int i = 0; i < number.size(); i++) {
				if (number.get(i).toString()
						.startsWith(inputContacts.getText().toString())) {
					nameArrayList.add((new ContactManager(
							getApplicationContext()).getNameFromNumber(number
							.get(i))).get("name"));
					numberArrayList.add(number.get(i));
					idArrayList.add(contactIdArrayList.get(i));
					contactsAdapter = new ContactsAdapter(
							getApplicationContext(), nameArrayList);
					found = 1;
				}
			}
		}

		if (found == 1) {
			btnCall.setVisibility(View.GONE);
			btnSave.setVisibility(View.GONE);
			contactsListView.setAdapter(contactsAdapter);
		} else {
			// if user typed a number that is not present in Contacts, display
			// Call and Save buttons
			if (inputContacts.getText().toString().matches("-?\\d+(\\.\\d+)?")) {
				btnCall.setVisibility(View.VISIBLE);
				btnSave.setVisibility(View.VISIBLE);

				btnCall.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View arg0, boolean arg1) {
						// vibrate
						Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						vibrator.vibrate(200);
						// TTS feedback
						TTS.speak(btnCall.getText().toString());
					}
				});

				attachKeyListener(btnCall, 1);

				// go to PhoneDialerApp on click
				btnCall.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						callContact(inputContacts.getText().toString());
					}
				});

				btnSave.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View view, boolean hasFocus) {
						if (hasFocus) {
							// vibrate
							Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
							vibrator.vibrate(200);
							// TTS feedback
							TTS.speak(btnCall.getText().toString());
							finish();
						}
					}
				});

				attachKeyListener(btnSave, 2);

				// go to SaveContact on click
				btnSave.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						saveContact(inputContacts.getText().toString());
					}
				});
			} else {
				// user typed letters in the alphabet
				contactsAdapter = new ContactsAdapter(getApplicationContext(),
						new ArrayList<String>());
				btnCall.setVisibility(View.GONE);
				btnSave.setVisibility(View.GONE);
				contactsListView.setAdapter(contactsAdapter);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent)
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (!inputContacts.hasFocus()) {
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
				if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
					
					System.out.println("Deleeeeeeetteeeeee");
					deletedFlag = 1;
					String inputContactsText = inputContacts.getText()
							.toString();
					if (inputContactsText.length() != 0) {
						// check if keyboard is connected and accessibility
						// services are disabled
//						if (Utils
//								.isAccessibilityEnabled(getApplicationContext())
//								) {
							
							if (!Utils
									.isAccessibilityEnabled(getApplicationContext())
									&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
					
							if (inputContactsText.substring(
									inputContactsText.length() - 1,
									inputContactsText.length()).matches(
									"-?\\d+(\\.\\d+)?")) {
								
								System.out.println(" speecking "+getString(R.string.deleted)
										+ " "
										+ inputContactsText.substring(
												inputContactsText.length() - 1,
												inputContactsText.length())
										+ ". "
										+ TTS.readNumber(inputContactsText
												.substring(0, inputContactsText
														.length() - 1)));
								
								TTS.speak(getString(R.string.deleted)
										+ " "
										+ inputContactsText.substring(
												inputContactsText.length() - 1,
												inputContactsText.length())
										+ ". "
										+ TTS.readNumber(inputContactsText
												.substring(0, inputContactsText
														.length() - 1)));
							} else {
								
								System.out.println(" speecking "+getString(R.string.deleted)
										+ " "
										+ inputContactsText.substring(
												inputContactsText.length() - 1,
												inputContactsText.length())
										+ ". "
										+ inputContactsText.substring(0,
												inputContactsText.length() - 1));
								
								TTS.speak(getString(R.string.deleted)
										+ " "
										+ inputContactsText.substring(
												inputContactsText.length() - 1,
												inputContactsText.length())
										+ ". "
										+ inputContactsText.substring(0,
												inputContactsText.length() - 1));
							}
						}
						inputContacts.setText(inputContactsText.substring(0,
								inputContactsText.length() - 1));
						inputContacts.setContentDescription(inputContactsText
								.replaceAll(".(?=[0-9])", "$0 "));
						inputContacts.setSelection(inputContactsText.length()-1,
								inputContactsText.length()-1);
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
				} else {
					return super.dispatchKeyEvent(event);
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}

	/**
	 * Create the Contacts activity (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@SuppressLint({ "HandlerLeak", "DefaultLocale" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.contacts);
		super.onCreate(savedInstanceState);

		// get the UI elements
		contactsListView = (ListView) findViewById(R.id.lstContacts);
		inputContacts = (EditText) findViewById(R.id.inputContactsSearch);
		btnCall = (Button) findViewById(R.id.btnCall);
		btnSave = (Button) findViewById(R.id.btnSave);

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

		contactsListView.setVisibility(View.GONE);
		inputContacts.setEnabled(false);
		inputContacts.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus
						&& !(inputContacts.getText().toString().equals(""))) {
					// check if keyboard is connected but accessibility services
					// are disabled
					if (!Utils.isAccessibilityEnabled(getApplicationContext())
							&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
						if (inputContacts.getText().toString().equals("")) {
							// empty EditText
							TTS.speak(inputContacts.getHint().toString());
						} else {
							if (inputContacts.getText().toString()
									.matches("-?\\d+(\\.\\d+)?")) {
								TTS.readNumber(inputContacts.getText()
										.toString());
							} else {
								TTS.speak(inputContacts.getText().toString());
							}
						}
					}
				}
			}
		});

		name = new ArrayList<String>();
		number = new ArrayList<String>();
		contactManager = new ContactManager(getApplicationContext());
		// display all contacts in the listview

		// get all contacts and pass to the list adapter
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);

		Toast.makeText(getApplicationContext(),
				getResources().getString(R.string.loadingcontacts),
				Toast.LENGTH_SHORT).show();
		Runnable runnable = new Runnable() {

			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				if (TTS.isSpeaking())
					TTS.stop();
				// check if keyboard is connected but accessibility services are
				// disabled
				if (!Utils.isAccessibilityEnabled(getApplicationContext())
						&& getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
					TTS.speak(getResources()
							.getString(R.string.loadingcontacts));
				file = new File(getDir("data", MODE_PRIVATE), "contacts");
				ObjectInputStream inputStream = null;
				if (file.length() != 0) {
					try {
						inputStream = new ObjectInputStream(
								new FileInputStream(file));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						try {
							ContactsApp.this.contactsMap = (HashMap<String, ArrayList<String>>) inputStream
									.readObject();
							inputStream.close();
							// fetch contacts and display, call this thread,
							// pass to handler
							new Thread(new Runnable() {
								public void run() {
									name = ContactsApp.this.contactsMap
											.get("name");
									number = ContactsApp.this.contactsMap
											.get("number");
									idArrayList = ContactsApp.this.contactsMap
											.get("id");
									numberArrayList = number;
									nameArrayList = name;
									contactIdArrayList = idArrayList;
									Bundle bundle = new Bundle();
									Message message = new Message();
									bundle.putInt("type", ALL_CONTACTS);
									message.setData(bundle);
									handler.sendMessage(message);
									new Thread(new Runnable() {
										public void run() {
											ContactsApp.this.contactsMap = contactManager
													.getAllContacts();
											Bundle bundle = new Bundle();
											Message message = new Message();
											bundle.putInt("type", ALL_CONTACTS);
											message.setData(bundle);
											handler.sendMessage(message);
										}
									}).start();
								}
							}).start();
						} catch (EOFException e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						ContactsApp.this.outputStream = new ObjectOutputStream(
								new FileOutputStream(file));
						ContactsApp.this.contactsMap = contactManager
								.getAllContacts();
						ContactsApp.this.outputStream
								.writeObject(ContactsApp.this.contactsMap);
						ContactsApp.this.outputStream.close();
						name = contactsMap.get("name");
						number = contactsMap.get("number");
						idArrayList = contactsMap.get("id");
						numberArrayList = number;
						nameArrayList = name;
						contactIdArrayList = idArrayList;
						Bundle bundle = new Bundle();
						Message message = new Message();
						bundle.putInt("type", ALL_CONTACTS);
						message.setData(bundle);
						handler.sendMessage(message);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		new Thread(runnable).start();

		contactsListView.setFastScrollEnabled(true);
		contactsListView.setScrollingCacheEnabled(true);
		contactsListView.setItemsCanFocus(true);

		// handle click events
		contactsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				// hide the keyboard
				InputMethodManager imm = (InputMethodManager) getApplicationContext()
						.getSystemService(Service.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(inputContacts.getWindowToken(), 0);
				// view details
				Intent intent = new Intent(getApplicationContext(),
						ContactsDetailsMenu.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				if (numberArrayList.size() > 0) {
					intent.putExtra("number", numberArrayList.get(position));
					intent.putExtra("name", nameArrayList.get(position));
					intent.putExtra("id", idArrayList.get(position));
				}
				startActivity(intent);
				finish();
			}

		});

		contactsListView.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				ListView lstView = (ListView) view;
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						launchDetailsActivity(currentSelection);
						break;
					case KeyEvent.KEYCODE_DPAD_DOWN:
						currentSelection++;
						if (currentSelection == lstView.getCount()) {
							currentSelection = 0;
						} else {
							Utils.giveFeedback(
									getApplicationContext(),
									contactsListView.getItemAtPosition(
											currentSelection).toString());
							lstView.setSelection(currentSelection);
						}
						break;
					case KeyEvent.KEYCODE_DPAD_UP:
						currentSelection--;
						if (currentSelection == -1) {
							currentSelection = contactsListView.getCount() - 1;
						} else {
							Utils.giveFeedback(
									getApplicationContext(),
									contactsListView.getItemAtPosition(
											currentSelection).toString());
							lstView.setSelection(currentSelection);
						}
						break;
					}
				}
				return false;
			}
		});

		inputContacts.addTextChangedListener(new TextWatcher() {

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
								if (inputContacts.getText().toString()
										.matches("-?\\d+(\\.\\d+)?")) {
									TTS.readNumber(inputContacts.getText()
											.toString());
								} else {
									TTS.speak(inputContacts.getText()
											.toString());
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
				loadList();
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {

			}
		});

		handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				if (message.getData().getInt("type") == ALL_CONTACTS) {
					progressBar.setVisibility(View.GONE);
					// display the contacts in the ListView
					contactsListView.setVisibility(View.VISIBLE);
					contactsAdapter = new ContactsAdapter(
							getApplicationContext(), name);
					contactsListView.setAdapter(contactsAdapter);
					inputContacts.setEnabled(true);
					try {
						if (ContactsApp.this.contactsMap.size() != 0) {
							count++;
							if (count == 2) {
								ContactsApp.this.outputStream = new ObjectOutputStream(
										new FileOutputStream(file));
								ContactsApp.this.outputStream
										.writeObject(ContactsApp.this.contactsMap);
								name = ContactsApp.this.contactsMap.get("name");
								contactsListView.setVisibility(View.VISIBLE);
								contactsAdapter = new ContactsAdapter(
										getApplicationContext(), name);
								contactsListView.setAdapter(contactsAdapter);
								number = ContactsApp.this.contactsMap
										.get("number");
								idArrayList = ContactsApp.this.contactsMap
										.get("id");
								numberArrayList = number;
								nameArrayList = name;
								contactIdArrayList = idArrayList;
								ContactsApp.this.outputStream.close();
								count = 0;
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
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
			TTS.speak(getResources().getString(R.string.loadingcontacts));
		}
		btnCall.setVisibility(View.GONE);
		btnSave.setVisibility(View.GONE);
		inputContacts.setText("");
		contactManager = new ContactManager(getApplicationContext());

		// get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.contacts);
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
}
