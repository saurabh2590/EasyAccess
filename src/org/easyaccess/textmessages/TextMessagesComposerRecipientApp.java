/*
	   _           _      _           _     _ 
	  (_)         | |    | |         (_)   | |
	   _ _   _ ___| |_ __| |_ __ ___  _  __| |
	  | | | | / __| __/ _` | '__/ _ \| |/ _` |
	  | | |_| \__ \ || (_| | | | (_) | | (_| |
	  | |\__,_|___/\__\__,_|_|  \___/|_|\__,_|
	 _/ |                                     
	|__/ 
	
	Copyright 2013 Caspar Isemer and and Eva Krueger, http://justdroid.org	
	
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

package org.easyaccess.textmessages;

import java.util.ArrayList;
import java.util.HashMap;

import org.easyaccess.R;
import org.easyaccess.SwipingUtils;
import org.easyaccess.TTS;
import org.easyaccess.Utils;
import org.easyaccess.contacts.ContactsAdapter;
import org.easyaccess.phonedialer.ContactManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/** Activity that can be used by the user to select or type the name or number of the recipient of
 * the text message. **/

public class TextMessagesComposerRecipientApp extends Activity implements KeyListener{
	
	private Button btnProceed;
	private EditText editRecipient;
	private ListView namesListView;
	private ArrayList<String> name, type, contactId, number;
	private ContactsAdapter adapter;
	private int currentSelection = -1, deletedFlag = 0;
	
	/**
	* Launches the TextMessagesComposerApp activity and passes the recipient's number to the 
	* activity.
	*/
	void proceed() {
		Intent intent = new Intent(getApplicationContext(), TextMessagesComposerApp.class);	 				
		intent.putExtra("number", editRecipient.getText().toString());
		startActivity(intent);
	}

	/**
	* Attaches onKeyListener to the button passed as a parameter to the method. 
	* The method corresponding to the button on which the enter key of the keyboard or the center 
	* key of the keypad is pressed.
	* @param button The button with which the onKeyListener is to be associated.
	*/
	void attachKeyListener(Button button) {
		button.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if(keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch(keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						proceed();
						break;
					}
				}
				return false;
			}
		});
	}
	
	/**
	* Launches the TextMessagesComposerApp activity and passes the recipient name, number and 
	* type of number.
	* @param name The name of the recipient.
	* @param type The type of the number to which the message is to be sent. E.g. Mobile, Phone etc.
	* @param number The number of the recipient to which the message is to be sent.
	*/
	void startNewActivity(String name, String type, String number) {
		Intent intent = new Intent(getApplicationContext(), TextMessagesComposerApp.class);
		intent.putExtra("name", name);
		intent.putExtra("type", type);
		intent.putExtra("number", number);
		startActivity(intent);
	}
	
	/** Create the Text Messages Composer activity **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.textmessages_composer_recipient);
		super.onCreate(savedInstanceState);
		
		/** Find UI elements **/
		btnProceed = (Button) findViewById(R.id.btnProceed);
	    editRecipient = (EditText) findViewById(R.id.inputTextMessagesRecipient);
	    namesListView = (ListView) findViewById(R.id.lstNames);
		
	    /** Find justdroid-specific Back and Home buttons **/
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
	        	Intent intent = new Intent(getApplicationContext(), SwipingUtils.class);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
	        	startActivity(intent);
	        }
	    });
		
		OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if(hasFocus) {
					TTS.speak(((TextView)view).getText().toString());
				}
			}
		};
		
		/** Attach onFocusChange listener to back and home buttons **/
		btnNavigationBack.setOnFocusChangeListener(focusChangeListener);
		btnNavigationHome.setOnFocusChangeListener(focusChangeListener);
		
		
	    Utils.attachListener(getApplicationContext(), btnProceed);
	    
	    btnProceed.setVisibility(View.GONE);
	    
		/** If Send button is pressed, send the text message to the selected recipient **/
	    btnProceed.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	 proceed();
	        }
	    });
	    
	    namesListView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				if(contactId.size() == 0) {
					startNewActivity(name.get(position).toString(), type.get(position).toString(), 
							number.get(position).toString());
				}
				else {
					startNewActivity(name.get(position).toString(), type.get(position).toString(), 
							number.get(position).toString());
				}
			}
		});
	    
	    namesListView.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				ListView lstView = (ListView) view;
				if(keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					switch(keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						if(contactId.size() == 0) {
							startNewActivity(name.get(currentSelection).toString(), 
									type.get(currentSelection).toString(), 
									number.get(currentSelection).toString());
						}
						else {
							startNewActivity(name.get(currentSelection).toString(), 
									type.get(currentSelection).toString(), 
									number.get(currentSelection).toString());
						}
						break;
					case KeyEvent.KEYCODE_DPAD_DOWN:
						currentSelection++;
						if(currentSelection == lstView.getCount()) {
							currentSelection = 0;
						}
						else {
							Utils.giveFeedback(getApplicationContext(), 
									namesListView.getItemAtPosition(currentSelection).toString());
							lstView.setSelection(currentSelection);
						}
						break;
					case KeyEvent.KEYCODE_DPAD_UP:
						currentSelection--;
						if(currentSelection == -1) {
							currentSelection = namesListView.getCount() - 1;
						}
						else {
							Utils.giveFeedback(getApplicationContext(), 
									namesListView.getItemAtPosition(currentSelection).toString());
							lstView.setSelection(currentSelection);
						}
						break;
					}
				}
				return false;
			}
		});
	    
	    editRecipient.addTextChangedListener(new TextWatcher() {

            @SuppressLint("DefaultLocale")
			@Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2,
                    int arg3) {
            	
            	if(deletedFlag != 1) {
	            	if(cs.length() > 0) {
		            	//check if keyboard is connected but accessibility services are disabled
			        	if(!Utils.isAccessibilityEnabled(getApplicationContext()) && 
			        			getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
			        		if(cs.toString().substring(cs.length()-1, cs.length()).matches("(?![@',&])\\p{Punct}")) {
			        			if(editRecipient.getText().toString().matches("-?\\d+(\\.\\d+)?")) {
			        				TTS.readNumber(editRecipient.getText().toString());
			        			}
			        			else {
				        			TTS.speak(editRecipient.getText().toString());
			        			}
			        		}
			        		else {
			        			TTS.speak(cs.toString().substring(cs.length()-1, cs.length()));
			        		}
			        	}
	            	}
            	}
            	else {
            		deletedFlag = 0;
            	}
            	
            	ArrayList<String> arrayListToBeDisplayed = new ArrayList<String>();
            	//check if user entered a letter
            	if(!(editRecipient.getText().toString().trim().equals("")) && 
            			!(editRecipient.getText().toString().matches("-?\\d+(\\.\\d+)?"))) {
            		btnProceed.setVisibility(View.GONE);
	            	//search if name exists in contacts
            		HashMap<String, ArrayList<String>> listOfNames = new 
            				ContactManager(getApplicationContext()).
            				getNamesStartingWith(editRecipient.getText().toString());
            		name = new ArrayList<String>();
            		contactId = new ArrayList<String>();
            		type = new ArrayList<String>();
            		number = new ArrayList<String>();
            		
            		for(int i=0; i<((ArrayList<String>)(listOfNames.get("name"))).size(); i++) {
            			if(!contactId.contains(((ArrayList<String>)(listOfNames.get("id"))).get(i).toString())) 
            			{
            				name.add(((ArrayList<String>)(listOfNames.get("name"))).get(i).toString());
            				contactId.add(((ArrayList<String>)(listOfNames.get("id"))).get(i).toString());
            				type.add(((ArrayList<String>)(listOfNames.get("type"))).get(i).toString());
            				number.add(((ArrayList<String>)(listOfNames.get("number"))).get(i).toString());
            				arrayListToBeDisplayed.add(name.get(name.size()-1) + " " + 
            				type.get(type.size()-1));
            			}
            		}
            		
            	}
            	else {
            		if(!(editRecipient.getText().toString().trim().equals(""))) {
	            		//user entered a number
	            		btnProceed.setVisibility(View.VISIBLE);
	            		//search if number exists in contacts
	            		HashMap<String, ArrayList<String>> listOfNames = new 
	            				ContactManager(getApplicationContext()).
	            				getNamesWithNumber(editRecipient.getText().toString());
	            		name = new ArrayList<String>();
	            		number = new ArrayList<String>();
	            		type = new ArrayList<String>();
	            		contactId = new ArrayList<String>();
	            		for(int i=0; i<((ArrayList<String>)(listOfNames.get("name"))).size(); i++) {
	            			if(!number.contains(((ArrayList<String>)
	            					(listOfNames.get("number"))).get(i).toString())) 
	            			{
	            				name.add(((ArrayList<String>)(listOfNames.get("name"))).
	            						get(i).toString());
	            				number.add(((ArrayList<String>)(listOfNames.get("number"))).
	            						get(i).toString());
	            				type.add(((ArrayList<String>)(listOfNames.get("type"))).
	            						get(i).toString());
	            				contactId.add(((ArrayList<String>)(listOfNames.get("id"))).
	            						get(i).toString());
	            				arrayListToBeDisplayed.add(name.get(name.size()-1) + " " +
	            						type.get(type.size()-1));
	            			}
	            		}
	            	}
            		else {
            			//empty input
            			btnProceed.setVisibility(View.GONE);
            		}
            	}
            	adapter = new ContactsAdapter(getApplicationContext(), arrayListToBeDisplayed);
            	namesListView.setAdapter(adapter);           	
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                    int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable arg0) {

            }
        });
	}	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if(event.getAction() == KeyEvent.ACTION_DOWN){ //&& !(inputContacts.getText().toString().trim().equals(""))) {
			if(!editRecipient.hasFocus()) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {//go to the previous screen
			    	//check if keyboard is connected and accessibility services are disabled
		        	if(!Utils.isAccessibilityEnabled(getApplicationContext()) && getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
		        		TTS.speak("Back");
		        	finish();
			    }
			    else if(event.getKeyCode() == KeyEvent.KEYCODE_F1) {//go to the home screen
			    	//check if keyboard is connected and accessibility services are disabled
		        	if(!Utils.isAccessibilityEnabled(getApplicationContext()) && 
		        			getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
		        		TTS.speak("Home");
					finish();
					Intent intent = new Intent(getApplicationContext(), SwipingUtils.class);
		        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		        	intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		        	startActivity(intent);
			    }
			    else
			    	return super.dispatchKeyEvent(event);
			}
			else {
				if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
					deletedFlag = 1;
					if(editRecipient.getText().toString().length() != 0) {
						//check if keyboard is connected and accessibility services are disabled
			        	if(!Utils.isAccessibilityEnabled(getApplicationContext()) && 
			        			getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
			        		if(editRecipient.getText().toString().
			        				substring(editRecipient.getText().toString().length()-1, 
			        						editRecipient.getText().toString().length()).
        							matches("-?\\d+(\\.\\d+)?")) {
			        			TTS.speak("Deleted " + editRecipient.getText().toString().
			        					substring(editRecipient.getText().toString().length()-1, 
			        							editRecipient.getText().toString().length()) + 
			        							". " + TTS.readNumber(editRecipient.getText().toString().
					        					substring(0, 
					        							editRecipient.getText().toString().length()-1)));
			        		}
			        		else {
			        			TTS.speak("Deleted " + editRecipient.getText().toString().
			        					substring(editRecipient.getText().toString().length()-1, 
			        							editRecipient.getText().toString().length()) + 
			        							". " + editRecipient.getText().toString().
					        					substring(0, 
					        							editRecipient.getText().toString().length()-1));
			        		}
			        	}
			        	editRecipient.setText(editRecipient.getText().toString().substring(0, 
								editRecipient.getText().toString().length()-1));
			        	editRecipient.setContentDescription(editRecipient.getText().toString()
			        			.replaceAll(".(?=[0-9])", "$0 "));
						editRecipient.setSelection(editRecipient.getText().toString().length(), 
								editRecipient.getText().toString().length());
						return false;
					}
					else {
						//check if keyboard is connected and accessibility services are disabled
			        	if(!Utils.isAccessibilityEnabled(getApplicationContext()) && 
			        			getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
			        		TTS.speak("Back");
			        	finish();
					}
				}
				else {
					return super.dispatchKeyEvent(event);
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//check if keyboard is connected and accessibility services are disabled
    	if(!Utils.isAccessibilityEnabled(getApplicationContext()) &&
    			getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
    		TTS.speak(getResources().getString(R.string.textMessageRecipient));
    	}
		//get the root layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.textmessagescomposer);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}

	@Override
	public void clearMetaKeyState(View arg0, Editable arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getInputType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean onKeyDown(View arg0, Editable arg1, int arg2, KeyEvent arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onKeyOther(View arg0, Editable arg1, KeyEvent arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onKeyUp(View arg0, Editable arg1, int arg2, KeyEvent arg3) {
		// TODO Auto-generated method stub
		return false;
	}
}
