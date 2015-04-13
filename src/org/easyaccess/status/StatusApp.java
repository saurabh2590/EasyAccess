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
package org.easyaccess.status;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.easyaccess.EasyAccessActivity;
import org.easyaccess.Log;
import org.easyaccess.R;
import org.easyaccess.TTS;
import org.easyaccess.Utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.CallLog;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gm.contentprovider.GmailContract;

/**
 * The Status feature in easyaccess lists the status of the device's battery,
 * signal, data connection, missed calls, unread text messages, unread eMails,
 * date and time, bluetooth, brightness and GPS.
 */

@SuppressLint("DefaultLocale")
public class StatusApp extends EasyAccessActivity {

	/** Declare variables and UI elements **/
	private Handler handler;
	private View viewBeforeAlarm, viewBeforeLocation,
			viewBeforeBluetooth, viewBeforeSignal, viewBeforeUnreadEmails,
			viewBeforeMissedCalls, viewBeforeUnreadTextMessages,
			viewBeforeBrightness;
	private TextView txtStatusBattery, txtStatusSignal,
			txtStatusMissedCalls,
			txtStatusUnreadTextMessages, txtStatusUnreadEmails,
			txtStatusNextAlarm, txtStatusTimeDate, txtStatusLocation,
			txtStatusBluetooth, txtStatusBrightness;
	private ToggleButton toggleButtonWifi, toggleButtonMobileData;
	private LocationManager locManager;
	private BroadcastReceiver mReceiver;
	int currentSignalStrength;
	long oldTime;
	int dbm;

	/**
	 * Attaches onFocusChange listener to the TextView passed as a parameter to
	 * the method, to track the change in focus of the TextView. If the TextView
	 * receives focus, pass the content description of the TextView to
	 * giveFeedback().
	 * 
	 * @param textView
	 *            This is an instance of TextView.
	 */
	public void attachListener(final TextView textView) {
		textView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) {
					giveFeedback(textView.getContentDescription().toString());
				}
			}
		});
	}

	/**
	 * Announces the text that is passed as a parameter and causes the device to
	 * vibrate for 300 milliseconds.
	 * 
	 * @param text
	 *            This is the string that is to be read aloud.
	 */

	public void giveFeedback(String text) {
		// vibrate
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(300);
		// TTS feedback
		if (!TTS.isSpeaking()) {
			TTS.speak(text);
		}
	}

	/**
	 * Creates the Status activity. (non-Javadoc)
	 * 
	 * @see org.easyaccess.EasyAccessActivity#onCreate(android.os.Bundle)
	 */
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.status);
		super.onCreate(savedInstanceState);

		// Initialize variables
		currentSignalStrength = 1;
		oldTime = System.currentTimeMillis();
		dbm = -1024;

		// Find UI elements
		txtStatusBattery = (TextView) findViewById(R.id.txtStatusBattery);
		txtStatusSignal = (TextView) findViewById(R.id.txtStatusSignal);
		txtStatusMissedCalls = (TextView) findViewById(R.id.txtStatusMissedCalls);
		txtStatusUnreadTextMessages = (TextView) findViewById(R.id.txtStatusUnreadTextMessages);
		txtStatusUnreadEmails = (TextView) findViewById(R.id.txtStatusUnreadEmails);
		txtStatusNextAlarm = (TextView) findViewById(R.id.txtStatusNextAlarm);
		txtStatusTimeDate = (TextView) findViewById(R.id.txtStatusTimeDate);
		txtStatusLocation = (TextView) findViewById(R.id.txtStatusLocation);
		txtStatusBluetooth = (TextView) findViewById(R.id.txtStatusBluetooth);
		txtStatusBrightness = (TextView) findViewById(R.id.txtStatusBrightness);
		viewBeforeAlarm = findViewById(R.id.viewBeforeAlarm);
		viewBeforeLocation = findViewById(R.id.viewBeforeLocation);
		viewBeforeBluetooth = findViewById(R.id.viewBeforeBluetooth);
		viewBeforeUnreadTextMessages = findViewById(R.id.viewBeforeUnreadTextMessages);
		viewBeforeUnreadEmails = findViewById(R.id.viewBeforeUnreadEmails);
		viewBeforeMissedCalls = findViewById(R.id.viewBeforeMissedCalls);
		viewBeforeSignal = findViewById(R.id.viewBeforeSignal);
		viewBeforeBrightness = findViewById(R.id.viewBeforeBrightness);
		
		toggleButtonWifi = (ToggleButton) findViewById(R.id.toggleButtonWifi);
		toggleButtonMobileData = (ToggleButton) findViewById(R.id.toggleButtonMobileData);
		
		listenToChangeInSignalStrength();
		listenToChangeInGpsStatus();
		listenToChangeInBluetoothStatus();

		getStatusPeriodically(5);

		displayGpsStatus();
		displayBluetoothStatus();
		Log.setLogLevel(Log.DEBUG);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				updateStatus(msg);
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
		displayMissedCalls();

		// Apply the selected font color, font size and font type to the
		// activity
		LinearLayout layout = (LinearLayout) findViewById(R.id.status);
		Utils.applyFontColorChanges(getApplicationContext(), layout);
		Utils.applyFontSizeChanges(getApplicationContext(), layout);
		Utils.applyFontTypeChanges(getApplicationContext(), layout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(mReceiver);
	}

	/**
	 * Retrives and displays the number of missed calls. If the number if missed
	 * calls is 0, this status is not displayed.
	 **/
	public void displayMissedCalls() {
		if (getMissedCallCount() != 0) {
			viewBeforeMissedCalls.setVisibility(View.VISIBLE);
			txtStatusMissedCalls.setVisibility(View.VISIBLE);
			txtStatusMissedCalls.setText(Integer.toString(getMissedCallCount())
					+ " " + getString(R.string.txtStatusMissedCalls));
			txtStatusMissedCalls.setContentDescription(Integer
					.toString(getMissedCallCount())
					+ " "
					+ getString(R.string.txtStatusMissedCalls));
			attachListener(txtStatusMissedCalls);
		} else {
			viewBeforeMissedCalls.setVisibility(View.GONE);
			txtStatusMissedCalls.setVisibility(View.GONE);
		}
	}

	/**
	 * Retrieves the status periodically.
	 * 
	 * @param seconds
	 *            This is the interval (in seconds) at which the status of the
	 *            device will be updated periodically.
	 */

	public void getStatusPeriodically(int seconds) {
		ScheduledExecutorService executor = Executors
				.newSingleThreadScheduledExecutor();
		Runnable periodicTask = new Runnable() {
			public void run() {
				Message msg = new Message();
				Bundle bundle = new Bundle();

				// Retrieve battery level
				bundle.putString("batteryLevel", getBatteryLevel());

				// Check if device is connected to a network
				if (isConnected()) {
					if (isWifiConnected()) {
						bundle.putString("dataConnection", Utils.WIFI);
					} else if (is3gEnabled()) {
						bundle.putString("dataConnection", Utils.THREEG);
					} else {
						bundle.putString("dataConnection", Utils.TWOG);
					}
				} else {
					bundle.putString("dataConnection", "null");
				}

				// Retrieve the number of unread SMS
				bundle.putString("unreadText",
						Integer.toString(getUnreadText()));

				// Retrieve the number of unread eMails
				bundle.putString("unreadMails", getAllUnreadMails());

				// Retrieve the time and day of the next alarm
				bundle.putString("nextAlarm", getNextAlarm());

				// Retrieve the current time and date
				bundle.putString("currentTime", getCurrentDateAndTime());

				// Retrieve the brightness mode and status
				if (getBrightnessMode() == 0) { // manual
					bundle.putString("brightness", "0");
				} else {
					bundle.putString("brightness", "1");
				}
				if (System.currentTimeMillis() - oldTime >= 15 * 1000) {
					bundle.putString("signalStrength", "0");
				}
				msg.setData(bundle);
				handler.sendMessage(msg);
			}
		};
		// Execute the set of tasks periodically according to the specified
		// number of seconds to retrieve the updated values

		executor.scheduleAtFixedRate(periodicTask, 0, seconds, TimeUnit.SECONDS);
	}

	/**
	 * Updates the status of all the components.
	 * 
	 * @param msg
	 *            The status is updated based on the data in msg.
	 */

	public void updateStatus(Message msg) {

		displayBatteryLevel(msg);

		displaySignalStrength(msg);

		manageDataConnection();

		displayUnreadTextMessages(msg);

		displayUnreadEmails(msg);

		displayNextAlarm(msg);

		displayCurrentTimeAndDate(msg);

		displayBrightness(msg);
	}

	/**
	 * Displays the battery level in percentage.
	 * 
	 * @param msg
	 *            The battery level is retrieved from the data in msg.
	 **/

	public void displayBatteryLevel(Message msg) {
		txtStatusBattery.setText(getString(R.string.txtStatusBattery) + " "
				+ msg.getData().getString("batteryLevel") + "%");
		txtStatusBattery
				.setContentDescription(getString(R.string.txtStatusBattery)
						+ " " + msg.getData().getString("batteryLevel") + "%");
		attachListener(txtStatusBattery);
	}

	/**
	 * Displays the number of bars indicating the signal strength.
	 * 
	 * @param msg
	 *            The number of bars is retrieved from the data in msg.
	 **/

	public void displaySignalStrength(Message msg) {
		if (msg.getData().getString("signalStrength") != null
				&& msg.getData().getString("signalStrength").equals("0")) {
			txtStatusSignal.setText(getString(R.string.txtStatusSignal) + " "
					+ msg.getData().getString("signalStrength") + " bars");
			if (msg.getData().getString("signalStrength").equals("1")) {
				txtStatusSignal
						.setContentDescription(getString(R.string.txtStatusSignal)
								+ " "
								+ msg.getData().getString("signalStrength")
								+ " bars - poor");
			} else if (msg.getData().getString("signalStrength").equals("2")) {
				txtStatusSignal
						.setContentDescription(getString(R.string.txtStatusSignal)
								+ " "
								+ msg.getData().getString("signalStrength")
								+ " bars - fair");
			} else if (msg.getData().getString("signalStrength").equals("3")) {
				txtStatusSignal
						.setContentDescription(getString(R.string.txtStatusSignal)
								+ " "
								+ msg.getData().getString("signalStrength")
								+ " bars - average");
			} else if (msg.getData().getString("signalStrength").equals("4")) {
				txtStatusSignal
						.setContentDescription(getString(R.string.txtStatusSignal)
								+ " "
								+ msg.getData().getString("signalStrength")
								+ " bars - good");
			} else {
				txtStatusSignal
						.setContentDescription(getString(R.string.txtStatusSignal)
								+ " "
								+ msg.getData().getString("signalStrength")
								+ " bars - no signal");

			}
			attachListener(txtStatusSignal);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void manageDataConnection() {
		final WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		toggleButtonWifi.setChecked(wifiManager.isWifiEnabled());
		toggleButtonWifi.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				wifiManager.setWifiEnabled(isChecked);
			}
		});

		boolean mobileDataEnabled = false;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			Class cmClass = Class.forName(connectivityManager.getClass().getName());
			Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
			method.setAccessible(true);
			mobileDataEnabled = (Boolean) method.invoke(connectivityManager);
		} catch (Exception exception) {
			// Some problem accessible private API
			// TODO do whatever error handling you want here
		}
		toggleButtonMobileData.setChecked(mobileDataEnabled);

		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
			toggleButtonMobileData.setEnabled(true);
			toggleButtonMobileData.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					try {
						setMobileDataEnabled(isChecked);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} else {
			toggleButtonMobileData.setEnabled(false);
			// There is an security restriction on Android Lollipop to enable MobileData Programmatically, so disabling mobile data switch.
			// http://stackoverflow.com/questions/29340150/android-l-5-x-turn-on-off-mobile-data-programmatically
			// stackoverflow.com/questions/26539445/the-setmobiledataenabled-method-is-no-longer-callable-as-of-android-l-and-later
		}
	}
	
	private void setMobileDataEnabled(boolean enabled) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
	    final ConnectivityManager conman = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	    final Class conmanClass = Class.forName(conman.getClass().getName());
	    final Field connectivityManagerField = conmanClass.getDeclaredField("mService");
	    connectivityManagerField.setAccessible(true);
	    final Object connectivityManager = connectivityManagerField.get(conman);
	    final Class connectivityManagerClass =  Class.forName(connectivityManager.getClass().getName());
	    final Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
	    setMobileDataEnabledMethod.setAccessible(true);
	    setMobileDataEnabledMethod.invoke(connectivityManager, enabled);
	}
	
	
	/**
	 * Displays the number of unread text messages. If the number of unread text
	 * messages is 0, this status is not displayed.
	 * 
	 * @param msg
	 *            The number of unread text messages is retrieved from the data
	 *            in msg.
	 **/
	public void displayUnreadTextMessages(Message msg) {
		if (msg.getData().getString("unreadText").equals("")
				|| msg.getData().getString("unreadText").equals("0")) {
			txtStatusUnreadTextMessages.setVisibility(View.GONE);
			viewBeforeUnreadTextMessages.setVisibility(View.GONE);
		} else {
			txtStatusUnreadTextMessages.setVisibility(View.VISIBLE);
			viewBeforeUnreadTextMessages.setVisibility(View.VISIBLE);
			txtStatusUnreadTextMessages.setText(msg.getData().getString(
					"unreadText")
					+ " " + getString(R.string.txtStatusUnreadTextMessages));
			txtStatusUnreadTextMessages.setContentDescription(msg.getData()
					.getString("unreadText")
					+ " "
					+ getString(R.string.txtStatusUnreadTextMessages));
			attachListener(txtStatusUnreadTextMessages);
		}
	}

	/**
	 * Displays the number of unread eMails. If the number of unread eMails for
	 * any account is 0, the details for that account are not displayed.
	 * 
	 * @param msg
	 *            The number of unread eMails is retrieved from the data in msg.
	 **/

	public void displayUnreadEmails(Message msg) {
		if (msg.getData().getString("unreadMails").equals("")) {
			txtStatusUnreadEmails.setVisibility(View.GONE);
			viewBeforeUnreadEmails.setVisibility(View.GONE);
		} else {
			txtStatusUnreadEmails.setVisibility(View.VISIBLE);
			viewBeforeUnreadEmails.setVisibility(View.VISIBLE);
			txtStatusUnreadEmails.setText(Html.fromHtml(msg.getData()
					.getString("unreadMails")));
			txtStatusUnreadEmails.setContentDescription(msg.getData()
					.getString("unreadMails"));
			attachListener(txtStatusUnreadEmails);
		}
	}

	/**
	 * Displays the time and day of the next alarm. If no alarm is set, this
	 * status is not displayed.
	 * 
	 * @param msg
	 *            The details of the next alarm are retrieved from the data in
	 *            msg.
	 **/

	public void displayNextAlarm(Message msg) {
		if (msg.getData().getString("nextAlarm") != null) {
			viewBeforeAlarm.setVisibility(View.VISIBLE);
			txtStatusNextAlarm.setVisibility(View.VISIBLE);
			txtStatusNextAlarm.setText(getString(R.string.txtStatusNextAlarm)
					+ " " + msg.getData().getString("nextAlarm"));
			txtStatusNextAlarm
					.setContentDescription(getString(R.string.txtStatusNextAlarm)
							+ " " + msg.getData().getString("nextAlarm"));
			attachListener(txtStatusNextAlarm);
		} else {
			txtStatusNextAlarm.setVisibility(View.GONE);
			viewBeforeAlarm.setVisibility(View.GONE);
		}
	}

	/**
	 * Displays the current time and date.
	 * 
	 * @param msg
	 *            The current time and date are retrieved from the data in msg.
	 **/
	public void displayCurrentTimeAndDate(Message msg) {
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		txtStatusTimeDate.setText(today.format("%k:%M") + " on "
				+ msg.getData().getString("currentTime"));
		txtStatusTimeDate.setContentDescription(today.format("%k:%M") + " on "
				+ msg.getData().getString("currentTime"));
		attachListener(txtStatusTimeDate);
	}

	/**
	 * Displays whether GPS is on. If it is off, this status is not displayed.
	 */

	public void displayGpsStatus() {
		if (isGpsEnabled()) {
			txtStatusLocation.setText(getString(R.string.txtStatusLocation));
			txtStatusLocation
					.setContentDescription(getString(R.string.txtStatusLocation));
		} else {
			viewBeforeLocation.setVisibility(View.GONE);
			txtStatusLocation.setVisibility(View.GONE);
		}
		attachListener(txtStatusLocation);
	}

	/**
	 * Displays whether Bluetooth is on. If it is off, this status is not
	 * displayed.
	 */
	public void displayBluetoothStatus() {
		if (isBluetoothEnabled()) {
			txtStatusBluetooth
					.setContentDescription(getString(R.string.txtStatusBluetooth));
			attachListener(txtStatusBluetooth);
		} else {
			viewBeforeBluetooth.setVisibility(View.GONE);
			txtStatusBluetooth.setVisibility(View.GONE);
		}
	}

	/**
	 * Displays the brightness level of the screen. If the program is unable to
	 * retrieve the brightness mode or level, this status is not displayed.
	 * 
	 * @param msg
	 *            The brightness mode and level are retrieved from the data in
	 *            msg.
	 **/

	public void displayBrightness(Message msg) {
		txtStatusBrightness.setVisibility(View.VISIBLE);
		viewBeforeBrightness.setVisibility(View.VISIBLE);
		if (msg.getData().getString("brightness").equals(Utils.AUTOMATIC+"")) {
			txtStatusBrightness
					.setText(getString(R.string.txtStatusBrightness));
			txtStatusBrightness
					.setContentDescription(getString(R.string.txtStatusBrightness));
		} else {
			switch (getBrightnessValue()) {
			case 30:
				txtStatusBrightness
						.setText(getString(R.string.txtStatusBrightnessLow));
				txtStatusBrightness
						.setContentDescription(getString(R.string.txtStatusBrightnessLow));
				break;
			case 102:
				txtStatusBrightness
						.setText(getString(R.string.txtStatusBrightnessMedium));
				txtStatusBrightness
						.setContentDescription(getString(R.string.txtStatusBrightnessMedium));
				break;
			case 255:
				txtStatusBrightness
						.setText(getString(R.string.txtStatusBrightnessBright));
				txtStatusBrightness
						.setContentDescription(getString(R.string.txtStatusBrightnessBright));
				break;
			default:
				txtStatusBrightness.setVisibility(View.GONE);
				viewBeforeBrightness.setVisibility(View.GONE);
				break;
			}
		}
		attachListener(txtStatusBrightness);
	}

	/**
	 * Calculates the device's battery level in percentage.
	 * 
	 * @return String Returns the string representation of the battery level.
	 */

	@SuppressLint("DefaultLocale")
	public String getBatteryLevel() {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = getApplicationContext().registerReceiver(null,
				ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		float perct = (level / (float) scale) * 100;
		return String.format("%.0f", perct);
	}

	/**
	 * Listens to change in signal strength of the device.
	 */

	public void listenToChangeInSignalStrength() {
		SignalStrengthListener signalStrengthListener = new SignalStrengthListener();
		TelephonyManager telephonyManager = (TelephonyManager) StatusApp.this
				.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(signalStrengthListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}

	/**
	 * Listens to change in the GPS status of the device.
	 */

	public void listenToChangeInGpsStatus() {
		/** listen for change in GPS status **/
		locManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		/** Define a listener that responds to location updates **/
		LocationListener locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location arg0) {
			}

			@Override
			public void onProviderDisabled(String s) {
				if (LocationManager.GPS_PROVIDER.equals(s)) {
					viewBeforeLocation.setVisibility(View.GONE);
					txtStatusLocation.setVisibility(View.GONE);
				}
			}

			@Override
			public void onProviderEnabled(String s) {
				if (LocationManager.GPS_PROVIDER.equals(s)) {
					viewBeforeLocation.setVisibility(View.VISIBLE);
					txtStatusLocation.setVisibility(View.VISIBLE);
					txtStatusLocation
							.setText(getString(R.string.txtStatusLocation));
					txtStatusLocation
							.setContentDescription(getString(R.string.txtStatusLocation));
					attachListener(txtStatusLocation);
				}
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				viewBeforeLocation.setVisibility(View.VISIBLE);
				txtStatusLocation
						.setText(getString(R.string.txtStatusLocation));
				txtStatusLocation
						.setContentDescription(getString(R.string.txtStatusLocation));
				attachListener(txtStatusLocation);
			}
		};
		/*
		 * Register the listener with the Location Manager to receive GPS status
		 * updates
		 */
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
				locationListener);
	}

	/**
	 * Listens to change in the Bluetooth status of the device.
	 */

	public void listenToChangeInBluetoothStatus() {
		/* Receiver to listen to changes in bluetooth status */
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();

				if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
					final int state = intent.getIntExtra(
							BluetoothAdapter.EXTRA_STATE,
							BluetoothAdapter.ERROR);
					switch (state) {
					case BluetoothAdapter.STATE_ON:
						viewBeforeBluetooth.setVisibility(View.VISIBLE);
						txtStatusBluetooth.setVisibility(View.VISIBLE);
						break;
					default:
						viewBeforeBluetooth.setVisibility(View.GONE);
						txtStatusBluetooth.setVisibility(View.GONE);
						break;
					}
				}
			}
		};
		/*
		 * Register for broadcast event associated with change in state of
		 * Bluetooth
		 */
		IntentFilter filter = new IntentFilter(
				BluetoothAdapter.ACTION_STATE_CHANGED);
		this.registerReceiver(mReceiver, filter);
	}

	/**
	 * Checks if the device is connected to a network.
	 * 
	 * @return boolean Returns true if device is connected to a network,
	 *         otherwise it returns false.
	 */

	public boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return (info != null && info.isConnected());
	}

	/**
	 * Checks if device is connected to a Wifi network.
	 * 
	 * @return boolean Returns true if device is connected to a Wifi network,
	 *         otherwise it returns false.
	 */

	public boolean isWifiConnected() {
		ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
	}

	/**
	 * Checks if the device is currently using a 3G network connection.
	 * 
	 * @return boolean Returns true if device is using a 3G network connection,
	 *         otherwise it returns false.
	 */

	public boolean is3gEnabled() {
		if (((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
				.getNetworkType() >= TelephonyManager.NETWORK_TYPE_UMTS) {
			return true;
		}
		return false;
	}

	/**
	 * Retrieves the number of missed calls.
	 * 
	 * @return int Returns the number of calls from the call log that belong to
	 *         the MISSED_TYPE category.
	 */

	public int getMissedCallCount() {
		int count;
		String[] projection = { CallLog.Calls.TYPE };
		String where = CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE;
		Cursor cursor = this.getContentResolver().query(
				CallLog.Calls.CONTENT_URI, projection, where, null, null);
		count = cursor.getCount();
		cursor.close();
		return count;
	}

	/**
	 * Retrieves the number of unread text messages.
	 * 
	 * @return String Returns the string representation of the number of unread
	 *         SMS's.
	 */

	public int getUnreadText() {
		final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
		int count;
		Cursor cursor = getContentResolver().query(SMS_INBOX, null, "read = 0",
				null, null);
		count = cursor.getCount();
		cursor.close();
		return count;
	}

	/**
	 * Retrieves the details of the next alarm.
	 * 
	 * @return String Returns the string representation of the time and date of
	 *         the next alarm.
	 */

	public String getNextAlarm() {
		String nextAlarm = (android.provider.Settings.System.getString(
				getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED));
		if (nextAlarm.equals("")) {
			return null;
		}
		String time = nextAlarm.substring(nextAlarm.indexOf(" "));
		String day = nextAlarm.substring(0, nextAlarm.indexOf(" "));

		if (day.startsWith("Sun")) {
			day = "Sunday";
		} else if (day.startsWith("Mon")) {
			day = "Monday";
		} else if (day.startsWith("Tue")) {
			day = "Tuesday";
		} else if (day.startsWith("Wed")) {
			day = "Wednesday";
		} else if (day.startsWith("Thu")) {
			day = "Thursday";
		} else if (day.startsWith("Fri")) {
			day = "Friday";
		} else {
			day = "Saturday";
		}
		nextAlarm = time + ", " + day.toString();
		return nextAlarm;
	}

	/**
	 * Retrieves the current time and date in MMMM dd, yyyy format.
	 * 
	 * @return String Returns the string representation of the current time and
	 *         date.
	 */

	@SuppressLint("SimpleDateFormat")
	public String getCurrentDateAndTime() {
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
		String dateresult = sdf.format(cal.getTime());
		return dateresult;
	}

	/**
	 * Checks whether GPS is enabled.
	 * 
	 * @return boolean Returns true if GPS is enabled, otherwise returns false.
	 */

	public boolean isGpsEnabled() {
		PackageManager packageManager = getApplicationContext()
				.getPackageManager();
		if (packageManager
				.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS) == false)
			return false;
		LocationManager manager = (LocationManager) getApplicationContext()
				.getSystemService(Context.LOCATION_SERVICE);
		boolean gpsStatus = manager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return gpsStatus;
	}

	/**
	 * Checks whether bluetooth is enabled.
	 * 
	 * @return boolean Returns true if bluetooth is enabled, otherwise returns
	 *         false.
	 */

	public boolean isBluetoothEnabled() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			return false;
		} else {
			if (!bluetoothAdapter.isEnabled()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Retrieves screen brightness mode value.
	 * 
	 * @return int Returns the screen brightness mode value.
	 */
	public int getBrightnessMode() {
		int curBrightnessMode = 0;
		try {
			curBrightnessMode = android.provider.Settings.System.getInt(
					getContentResolver(),
					android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
			return curBrightnessMode;
		}
		return curBrightnessMode;
	}

	/**
	 * Retrieves screen brightness value.
	 * 
	 * @return int Returns the screen brightness value.
	 */
	public int getBrightnessValue() {
		int curBrightnessValue = 0;
		try {
			curBrightnessValue = android.provider.Settings.System.getInt(
					getContentResolver(),
					android.provider.Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
			return curBrightnessValue;
		}
		return curBrightnessValue;
	}

	/**
	 * Retrieves the number of unread emails for all accounts configured on the
	 * device.
	 * 
	 * @return String Returns the string representation of the number of unread
	 *         emails along with the email ID.
	 */

	public String getAllUnreadMails() {
		Account[] accounts = AccountManager.get(this).getAccountsByType(
				"com.google");
		ArrayList<String> accountName = new ArrayList<String>();
		String unreadEmails = "";
		int line = 1;
		for (Account account : accounts) {
			int count = 0;
			if (!(accountName.contains(account.name))) {
				accountName.add(account.name);
				count += getUnreadMailsByAccount(account.name);
				// count = -1 indicates that the number of unread eMails could
				// not be retrieved
				if (count > 0) {
					if (line > 1) {
						// add a new line before all the eMail statements
						// starting from the second statement
						unreadEmails += "<br/>";
					}
					unreadEmails += count + " "
							+ getString(R.string.txtStatusUnreadEmails)
							+ " in " + account.name + ".";
					line++;
				}
			}
		}
		return unreadEmails;
	}

	/**
	 * Retrieves the number of unread eMails for an account.
	 * 
	 * @param account
	 *            This is a string representation of the eMail account.
	 * @return int Returns the number of unread eMails for the account passed as
	 *         a parameter.
	 */

	public int getUnreadMailsByAccount(String account) {
		int unread = 0;
		Cursor cursor = null;
		try {
			cursor = getContentResolver().query(
					GmailContract.Labels.getLabelsUri(account), null, null,
					null, null);

		} catch (Exception e) {
			Log.w("SaGu", e);
		}
		if (cursor != null) {
			while (cursor.moveToNext()) {
				boolean labelInboxTrue = cursor
						.getString(
								cursor.getColumnIndex(GmailContract.Labels.CANONICAL_NAME))
						.equals(GmailContract.Labels.LabelCanonicalNames.CANONICAL_NAME_INBOX);
				boolean labelInboxPrimaryTrue = cursor
						.getString(
								cursor.getColumnIndex(GmailContract.Labels.CANONICAL_NAME))
						.equals(GmailContract.Labels.LabelCanonicalNames.CANONICAL_NAME_INBOX_CATEGORY_PRIMARY);
				if (labelInboxTrue || labelInboxPrimaryTrue) {
					unread = cursor
							.getInt(cursor
									.getColumnIndex(GmailContract.Labels.NUM_UNREAD_CONVERSATIONS));
				}
			}
			cursor.close();
		}
		return unread;
	}

	/**
	 * 
	 * SignalStrengthListener listens to the change in signal strength. The dbm
	 * value is retrieved, which is then converted to the equivalent number of
	 * bars.
	 */

	class SignalStrengthListener extends PhoneStateListener {

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			int strength = -1;
			dbm = -1024;
			if (signalStrength.isGsm()) {
				if (signalStrength.getGsmSignalStrength() != 99) {
					dbm = signalStrength.getGsmSignalStrength() * 2 - 113;
				} else {
					dbm = signalStrength.getGsmSignalStrength();
				}
			} else {
				dbm = signalStrength.getCdmaDbm();
			}
			oldTime = System.currentTimeMillis();
			if (dbm <= -111) {
				strength = 0;
			} else if (dbm <= -99 && dbm >= -110) {
				strength = 1;
			} else if (dbm <= -86 && dbm >= -98) {
				strength = 2;
			} else if (dbm <= -74 && dbm >= -85) {
				strength = 3;
			} else if ((dbm <= -61 && dbm >= -73) || (dbm >= 60 && dbm < 99)) {
				strength = 4;
			} else {
				viewBeforeSignal.setVisibility(View.GONE);
				txtStatusSignal.setVisibility(View.GONE);
			}
			if (strength != -1) {
				viewBeforeSignal.setVisibility(View.VISIBLE);
				txtStatusSignal.setVisibility(View.VISIBLE);
				txtStatusSignal.setText(getString(R.string.txtStatusSignal)
						+ " " + strength + " bars");
				txtStatusSignal
						.setContentDescription(getString(R.string.txtStatusSignal)
								+ " " + strength + " bars");
				attachListener(txtStatusSignal);
			}
		}
	};
}