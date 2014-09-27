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
package org.easyaccess.phonedialer;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;

/**
 * Consists of methods that return various details about the contacts stored in
 * the phone.
 */
public class ContactManager {

	/** Declare variables **/
	private Context context;

	/**
	 * Constructor for initializing the Contact Manager
	 * 
	 * @param context
	 */
	public ContactManager(Context context) {
		this.context = context;
	}

	/**
	 * Searches for the number in the phone's contacts and returns a string
	 * consisting of the name and type of the number.
	 * 
	 * @param number
	 *            The number which is to be searched in the phone's contacts.
	 * @return HashMap<String, String> A HashMap that consists of the name of
	 *         the contact, type of the number, and the number itself. If the
	 *         number is not saved in the phone, the string consists of only the
	 *         number.
	 */

	public HashMap<String, String> getNameFromNumber(String number) {
		// Store name and type of number in result
		HashMap<String, String> result = new HashMap<String, String>();
		// Store the number
		result.put("number", number);
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));

		Cursor cursor = this.context.getContentResolver().query(
				lookupUri,
				new String[] { ContactsContract.Contacts.DISPLAY_NAME,
						PhoneLookup.TYPE }, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				// Store the name of the contact
				result.put("name", cursor.getString(0));
				// Store the type of number
				result.put("type", context
						.getString(ContactsContract.CommonDataKinds.Phone
								.getTypeLabelResource(cursor.getInt(1))));
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return result;
	}

	/**
	 * Searches for the number in the phone's contacts and returns a string
	 * consisting of the name, type of the number, and ID of the contact.
	 * 
	 * @param number
	 *            The number which is to be searched in the phone's contacts.
	 * @return HashMap<String, ArrayList> A HashMap that consists of the name of
	 *         the contact, type of the number, ID of the contact and the number
	 *         itself. If the number is not saved in the phone, the string
	 *         consists of only the number.
	 */

	public HashMap<String, ArrayList<String>> getNamesWithNumber(
			String strNumber) {
		// Store name and type of number in result
		HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		ArrayList<String> name, number, type, id;
		name = new ArrayList<String>();
		number = new ArrayList<String>();
		type = new ArrayList<String>();
		id = new ArrayList<String>();
		Cursor cursor = context.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] { Phone.DISPLAY_NAME, Phone.TYPE, Phone.NUMBER,
						Phone.CONTACT_ID }, Phone.NUMBER + " LIKE ?",
				new String[] { strNumber + "%" }, null);
		if (cursor != null)
			while (cursor.moveToNext()) {
				name.add(cursor.getString(0));
				type.add(Phone.getTypeLabel(this.context.getResources(),
						cursor.getInt(1), "").toString());
				number.add(cursor.getString(2));
				id.add(cursor.getString(3));
			}
		result.put("name", name);
		result.put("number", number);
		result.put("type", type);
		result.put("id", id);
		return result;
	}

	/**
	 * Searches for the names that start with the parameter passed, in the
	 * phone's contacts and returns a string consisting of the name, type of the
	 * number and the ID of the contact.
	 * 
	 * @param strName
	 *            The name which is to be searched in the phone's contacts.
	 * @return HashMap<String, ArrayList> A HashMap that consists of the name of
	 *         the contact, type of the number, and the Id of the contact.
	 */

	public HashMap<String, ArrayList<String>> getNamesStartingWith(
			String strName) {
		// Store name, type of number and ID in result
		HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		ArrayList<String> name, id, type, number;
		name = new ArrayList<String>();
		id = new ArrayList<String>();
		type = new ArrayList<String>();
		number = new ArrayList<String>();
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";
		Cursor cursor = context
				.getContentResolver()
				.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						new String[] {
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
								Phone.DISPLAY_NAME, Phone.TYPE, Phone.NUMBER },
						ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
								+ " LIKE ?", new String[] { strName + "%" },
						sortOrder);
		try {
			while (cursor.moveToNext()) {
				id.add(cursor.getString(0));
				name.add(cursor.getString(1));
				type.add(Phone.getTypeLabel(this.context.getResources(),
						cursor.getInt(2), "").toString());
				number.add(cursor.getString(3));
			}
			result.put("name", name);
			result.put("type", type);
			result.put("id", id);
			result.put("number", number);
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return result;
	}

	/**
	 * Searches for the ID of the contact and returns the corresponding phone
	 * number.
	 * 
	 * @param idOfContact
	 *            The ID of the contact which is to be searched.
	 * @return String The number of the contact.
	 */

	public String getNumber(String idOfContact) {
		String contactNumber = "";
		Cursor cursor = context.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] { Phone.NUMBER },
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? ",
				new String[] { idOfContact }, null);
		if (cursor.moveToNext()) {
			contactNumber = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
		}
		return contactNumber;
	}

	/**
	 * Searches for the ID of the contact and returns the corresponding name,
	 * numbers, type of the numbers and emails of the contact.
	 * 
	 * @param id
	 *            The ID of the contact which is to be searched.
	 * @return HashMap<String, ArrayList> A HashMap that consists of the name of
	 *         the contact, numbers , types of the numbers, emails associated
	 *         with the contact and the Id itself.
	 */

	@SuppressLint("InlinedApi")
	public HashMap<String, ArrayList<String>> getDetailsFromId(String id) {
		ArrayList<String> ids, name = null, number = null, type = null, email = null;
		HashMap<String, ArrayList<String>> contacts = new HashMap<String, ArrayList<String>>();
		ids = new ArrayList<String>();

		Cursor cur = context.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, null,
				ContactsContract.Contacts._ID + " = ?", new String[] { id },
				"display_name" + " ASC");
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				name = new ArrayList<String>();
				number = new ArrayList<String>();
				type = new ArrayList<String>();
				email = new ArrayList<String>();
				String displayName = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				name.add(displayName);
				ids.add(id);
				Cursor pCur = context.getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = ? ", new String[] { id }, null);
				while (pCur.moveToNext()) {
					String phoneNumber = pCur
							.getString(pCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					number.add(phoneNumber);
					int numberType = pCur.getInt(pCur
							.getColumnIndex(Phone.TYPE));
					String contactNumberType = Phone.getTypeLabel(
							this.context.getResources(), numberType, "")
							.toString();
					type.add(contactNumberType);
				}
				pCur.close();
				Cursor emailCur = this.context.getContentResolver().query(
						ContactsContract.CommonDataKinds.Email.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Email.CONTACT_ID
								+ " = ?", new String[] { id }, null);
				while (emailCur.moveToNext()) {
					String mail = emailCur
							.getString(emailCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
					if (email.size() == 0 || !email.contains(mail))// ||
																	// emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.IS_PRIMARY)).equals("1"))
						email.add(mail);
				}
				emailCur.close();
			}
		}
		cur.close();
		contacts.put("name", name);
		contacts.put("numbers", number);
		contacts.put("types", type);
		contacts.put("emails", email);
		contacts.put("id", ids);
		return contacts;
	}

	/**
	 * Returns the details of all the contacts in the phone.
	 * 
	 * @return HashMap<String, ArrayList> A HashMap that consists of the name,
	 *         ID and primary number of all the contacts in the phone.
	 */

	public HashMap<String, ArrayList<String>> getAllContacts() {
		ArrayList<String> ids, name, number;
		HashMap<String, ArrayList<String>> contacts = new HashMap<String, ArrayList<String>>();
		ids = new ArrayList<String>();
		name = new ArrayList<String>();
		number = new ArrayList<String>();

		Cursor cur = context.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, null, null, null,
				"display_name" + " ASC");
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(cur
						.getColumnIndex(ContactsContract.Contacts._ID));

				String displayName = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				if (Integer
						.parseInt(cur.getString(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = context.getContentResolver().query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									//+ " = ? AND " +
							+ " = ?",
									//Data.IS_PRIMARY + "=1",
							new String[] { id }, null);
					if (pCur.moveToFirst()) {
						String phoneNumber = pCur
								.getString(pCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						number.add(phoneNumber);
						name.add(displayName);
						ids.add(id);
					}
					pCur.close();
				}
			}
		}
		cur.close();
		contacts.put("name", name);
		contacts.put("number", number);
		contacts.put("id", ids);
		return contacts;
	}

	/**
	 * Returns the ID of the number passed as a parameter.
	 * 
	 * @param contactNumber
	 *            The number whose ID is to be returned.
	 * @return String The ID of the contact. Returns null if contact is not
	 *         saved in the phone.
	 */

	public String getId(String contactNumber) {
		String contactId = null;
		ContentResolver contentResolver = context.getContentResolver();

		Uri uri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(contactNumber));

		String[] projection = new String[] {
				ContactsContract.Contacts.DISPLAY_NAME, PhoneLookup._ID };

		Cursor cursor = contentResolver
				.query(uri, projection, null, null, null);

		if (cursor != null) {
			while (cursor.moveToNext()) {
				contactId = cursor.getString(cursor
						.getColumnIndexOrThrow(PhoneLookup._ID));
				break;
			}
			cursor.close();
		}
		return contactId;
	}

	/**
	 * Returns the contact ID of the number passed as a parameter.
	 * 
	 * @param contactNumber
	 *            The number whose contact ID is to be returned.
	 * @return String The contact ID of the contact. Returns an empty string if
	 *         contact is not saved in the phone.
	 */

	String getContactId(String contactNumber) {
		Cursor cursor = context.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI,
				new String[] { Phone.CONTACT_ID }, Phone.NUMBER,
				new String[] { contactNumber }, null);
		while (cursor.moveToNext()) {
			return cursor.getString(cursor.getColumnIndex(Phone.CONTACT_ID));
		}
		return "";
	}
}
