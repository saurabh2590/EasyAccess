package org.easyaccess.phonedialer;

import java.util.HashMap;

import org.easyaccess.R;
import org.easyaccess.TTS;
import org.easyaccess.Utils;
import org.easyaccess.phonedialer.Accelerometer.AccelerometerListener;
import org.easyaccess.settings.SettingsContentObserver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.widget.Toast;

/**
 * Listens to change in state of the phone.
 */
public class CallStateService extends Service implements OnInitListener,
		AccelerometerListener {

	/** Declare variables **/
	private Context cxt;
	private TelephonyManager telephonyManager;
	private CallStateListener callStateListener;
	private LocalBroadcastManager broadcaster;
	private TextToSpeech tts;
	private HashMap<String, String> callingDetails;
	private BroadcastReceiver bReceiver;
	public int callState;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {

		telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		callStateListener = new CallStateListener();
		callState = telephonyManager.getCallState();
		telephonyManager.listen(callStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);
		callState = telephonyManager.getCallState();
		broadcaster = LocalBroadcastManager.getInstance(this);

		tts = new TextToSpeech(this, this);

		this.bReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(Utils.INCOMING_CALL)) {

					cxt = context;
					String number = intent.getStringExtra("message");
					callingDetails = new ContactManager(getBaseContext())
							.getNameFromNumber(number);
					// play ringtone
					// get custom ringtone
					playRingtone(number);
					// announce number
					// announceCaller(callingDetails, number);
					// Display Calling Activity in order to receive key events
					Utils.callingDetails = callingDetails;
					intent = new Intent(getBaseContext(), CallingScreen.class);
					intent.putExtra("type", Utils.INCOMING);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);

				} else if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent
						.getAction())) {
					// new outgoing call
					final String number = intent
							.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
					callingDetails = new ContactManager(getBaseContext())
							.getNameFromNumber(number);
					Utils.callingDetails = callingDetails;
				}
			}
		};

		LocalBroadcastManager.getInstance(this).registerReceiver(
				(this.bReceiver), new IntentFilter(Utils.INCOMING_CALL));
		LocalBroadcastManager.getInstance(this).registerReceiver(
				(this.bReceiver),
				new IntentFilter("android.intent.action.PHONE_STATE"));
		if (Accelerometer.isSupported(this)) {
			// Start Accelerometer Listening
			Accelerometer.startListening(this);
		}

		MediaButton_Receiver mediaReceiver = new MediaButton_Receiver();
		IntentFilter filterVolume = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
		registerReceiver(mediaReceiver, filterVolume);

		SettingsContentObserver mSettingsContentObserver = new SettingsContentObserver(
				this, new Handler());
		getApplicationContext().getContentResolver().registerContentObserver(
				android.provider.Settings.System.CONTENT_URI, true,
				mSettingsContentObserver);
	}

	/**
	 * Defines the class for receiving event on Media Button
	 */
	private class MediaButton_Receiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Plays the ringtone associated with the number passed as a parameter.
	 * 
	 * @param number
	 *            The number associated with the incoming call.
	 */
	void playRingtone(String number) {
		Uri queryUri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));

		String[] columns = new String[] { ContactsContract.Contacts.CUSTOM_RINGTONE };

		Cursor contactsCursor = getContentResolver().query(queryUri, columns,
				null, null, null);

		if (contactsCursor.moveToFirst()) {
			if (contactsCursor.getString(contactsCursor
					.getColumnIndex(ContactsContract.Contacts.CUSTOM_RINGTONE)) == null) {
				// no custom ringtone has been set
				Utils.ringtone = RingtoneManager.getRingtone(getBaseContext(),
						Settings.System.DEFAULT_RINGTONE_URI);
				Utils.ringtone.play();
			} else {
				Utils.ringtone = RingtoneManager
						.getRingtone(
								getBaseContext(),
								Uri.parse(contactsCursor.getString(contactsCursor
										.getColumnIndex(ContactsContract.Contacts.CUSTOM_RINGTONE))));
				Utils.ringtone.play();
			}
		}

	}

	/**
	 * Class to listen to the current Call state. 
	 */
	private final class CallStateListener extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int newState, String incomingNumber) {
			switch (callState) {
			case TelephonyManager.CALL_STATE_IDLE:
				if (newState == TelephonyManager.CALL_STATE_OFFHOOK) {
					// idle to off hook: new outgoing call
					Utils.off_hook = 1;
					Utils.ringing = 0;

					Intent intent = new Intent(getBaseContext(),
							CallingScreen.class);
					intent.putExtra("type", Utils.OUTGOING);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				} else if (newState == TelephonyManager.CALL_STATE_RINGING) {
					// idle to ringing: new incoming call
					Utils.ringing = 1;
					new CallManager(getApplicationContext())
							.setNumber(incomingNumber);
					sendResult(incomingNumber, Utils.INCOMING_CALL);
				}
				break;

			case TelephonyManager.CALL_STATE_OFFHOOK:
				if (newState == TelephonyManager.CALL_STATE_IDLE) {
					// off hook to idle: call disconnected/ended, close Calling
					// screen
					Utils.off_hook = 0;
					Utils.ringing = 0;
					sendResult(getResources().getString(R.string.call_ended),
							Utils.CALL_ENDED);
				} else if (newState == TelephonyManager.CALL_STATE_RINGING) {
					// off hook to ringing: another call waiting
					Utils.ringing = 1;
					new CallManager(getApplicationContext())
							.setNumber(incomingNumber);
					sendResult(incomingNumber, Utils.INCOMING_CALL);
				} else if (newState == TelephonyManager.CALL_STATE_OFFHOOK) {
					// off hook to off hook: one call disconnected/ended
					Utils.ringing = 0;
				}
				break;

			case TelephonyManager.CALL_STATE_RINGING:
				if (newState == TelephonyManager.CALL_STATE_OFFHOOK) {
					// ringing to off hook: call answered/received
					Utils.off_hook = 1;
					Utils.ringing = 0;
					if (Utils.ringtone != null && Utils.ringtone.isPlaying()) {
						Utils.ringtone.stop();
					}
				} else if (newState == TelephonyManager.CALL_STATE_IDLE) {
					// ringing to idle: missed call
					if (Utils.ringtone.isPlaying()) {
						Utils.ringtone.stop();
					}
					Utils.ringing = 0;
					Utils.off_hook = 0;
					sendResult(
							getResources().getString(R.string.call_rejected),
							Utils.CALL_ENDED);
				}
				break;
			}
			callState = newState;
		}

		/**
		 * Broadcasts the state of the call passed as parameter, along with a
		 * message, if any.
		 * 
		 * @param message
		 *            consists of the message to be passed with the intent.
		 * @param intentType
		 *            indicates the state of a call.
		 */
		public void sendResult(String message, String intentType) {
			Intent intent = new Intent(intentType);
			if (message != null)
				intent.putExtra("message", message);
			broadcaster.sendBroadcast(intent);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.speech.tts.TextToSpeech.OnInitListener#onInit(int)
	 */
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.ERROR) {
			Toast.makeText(getApplicationContext(),
					this.getResources().getString(R.string.ttsError),
					Toast.LENGTH_LONG).show();
		} else {
			TTS.setObject(tts);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.justdroid.justdroid.phonedialer.Accelerometer.AccelerometerListener
	 * #onAccelerationChanged(float, float, float)
	 */
	@Override
	public void onAccelerationChanged(float x, float y, float z) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.justdroid.justdroid.phonedialer.Accelerometer.AccelerometerListener
	 * #onShake(float)
	 */
	@Override
	public void onShake(float force) {
		if (Utils.ringing == 1) {
			// answer call
			Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
			buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
					KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
			cxt.sendOrderedBroadcast(buttonUp,
					"android.permission.CALL_PRIVILEGED");
		}
	}
}