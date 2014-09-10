package org.easyaccess.phonedialer;

import org.easyaccess.R;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

/**
 * Consists of methods that return the state of the SIM card and the state of
 * the network.
 */

public class CallManager extends PhoneStateListener {

	/** Declare UI elements **/
	private TelephonyManager telMgr;
	private String number, simState, serviceState;
	private final Context context;

	/**
	 * Constructor for CallManager
	 * 
	 * @param context
	 */
	CallManager(Context context) {
		this.context = context;
		setServiceState(this.context.getResources().getString(
				R.string.state_in_service));
	}

	/**
	 * Sets the number for this call.
	 * 
	 * @param number
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.telephony.PhoneStateListener#onServiceStateChanged(android.telephony
	 * .ServiceState)
	 */
	@Override
	public void onServiceStateChanged(ServiceState serviceState) {
		super.onServiceStateChanged(serviceState);
		String phonestate = "";
		// set the state of the service - serviceState according to the value in
		// serviceState
		switch (serviceState.getState()) {
		case ServiceState.STATE_EMERGENCY_ONLY:
			if (PhoneNumberUtils.isEmergencyNumber(this.number))
				phonestate = context.getResources().getString(
						R.string.state_in_service);
			else
				phonestate = context.getResources().getString(
						R.string.emergency_calls_only);
			setServiceState(phonestate);
			break;
		case ServiceState.STATE_OUT_OF_SERVICE:
			phonestate = context.getResources().getString(R.string.no_service);
			setServiceState(phonestate);
			break;
		case ServiceState.STATE_POWER_OFF:
			phonestate = context.getResources().getString(R.string.power_off);
			setServiceState(phonestate);
			break;
		case ServiceState.STATE_IN_SERVICE:
			phonestate = context.getResources().getString(
					R.string.state_in_service);
			setServiceState(phonestate);
			break;
		default:
			phonestate = context.getResources().getString(
					R.string.service_unknown_reason);
			setServiceState(phonestate);
		}
	}

	/**
	 * Gets the service state.
	 * 
	 * @return String that consits of the state of the service.
	 */
	public String getServiceState() {
		return this.serviceState;
	}

	/**
	 * Checks the status of the SIM card in the phone. Checks if the sim card is
	 * absent, network is locked, pin is required, POK code is required, sim
	 * card is ready to use or whether the state of the sim card is unknown.
	 * 
	 * @return String Returns the appropriate state.
	 */

	public String getSimState() {
		// get the availability of the SIM card
		telMgr = (TelephonyManager) this.context
				.getSystemService(Context.TELEPHONY_SERVICE);
		int simState = telMgr.getSimState();
		switch (simState) {
		case TelephonyManager.SIM_STATE_ABSENT:
			this.simState = this.context.getResources().getString(
					R.string.sim_absent);
			break;
		case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
			this.simState = this.context.getResources().getString(
					R.string.network_locked);
			break;
		case TelephonyManager.SIM_STATE_PIN_REQUIRED:
			this.simState = this.context.getResources().getString(
					R.string.pin_required);
			break;
		case TelephonyManager.SIM_STATE_PUK_REQUIRED:
			this.simState = this.context.getResources().getString(
					R.string.puk_required);
			break;
		case TelephonyManager.SIM_STATE_READY:
			this.simState = this.context.getResources().getString(
					R.string.sim_ready);
			break;
		case TelephonyManager.SIM_STATE_UNKNOWN:
			this.simState = this.context.getResources().getString(
					R.string.service_unknown_reason);
			break;
		}
		return this.simState;
	}

	/**
	 * Set the state of SIM Card
	 * 
	 * @param String that consists of the state of the SIM card.
	 */
	public void setState(String state) {
		this.simState = state;
	}

	/**
	 * Set thet state of the Phone Service.
	 * 
	 * @param state String that consists of the state of the service.
	 */
	public void setServiceState(String state) {
		this.serviceState = state;
	}
}
