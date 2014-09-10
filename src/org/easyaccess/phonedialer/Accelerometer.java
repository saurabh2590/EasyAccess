package org.easyaccess.phonedialer;
/** Adapted from http://androidexample.com/MultiThread_in_Android/GUI/index.php?view=article_discription&aid=109&aaid=131 */
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Detects change in the acceleration of of the phone.
 */
public class Accelerometer {

	/** Declare variables **/
	private static Context aContext = null;

	/** Accuracy configuration */
	private static float threshold = 15.0f;
	private static int interval = 200;

	private static Sensor sensor;
	private static SensorManager sensorManager;
	private static AccelerometerListener listener;

	/** indicates whether or not Accelerometer Sensor is supported */
	private static Boolean supported;
	/** indicates whether or not Accelerometer Sensor is running */
	private static boolean running = false;

	/**
	 * Returns true if the manager is listening to orientation changes.
	 * 
	 * @return
	 */
	public static boolean isListening() {
		return running;
	}

	/**
	 * Unregisters listeners.
	 */
	public static void stopListening() {
		running = false;
		try {
			if (sensorManager != null && sensorEventListener != null) {
				sensorManager.unregisterListener(sensorEventListener);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Returns true if at least one Accelerometer sensor is available.
	 * 
	 * @param context
	 *            The context of the application.
	 * 
	 * @return true if at least one Accelerometer sensor is available.
	 */
	public static boolean isSupported(Context context) {
		aContext = context;
		if (supported == null) {
			if (aContext != null) {

				sensorManager = (SensorManager) aContext
						.getSystemService(Context.SENSOR_SERVICE);

				// Get all sensors in device
				List<Sensor> sensors = sensorManager
						.getSensorList(Sensor.TYPE_ACCELEROMETER);

				supported = Boolean.valueOf(sensors.size() > 0);

			} else {
				supported = Boolean.FALSE;
			}
		}
		return supported;
	}

	/**
	 * Configures the listener for shaking.
	 * 
	 * @param threshold
	 *            minimum acceleration variation for considering shaking.
	 * @param interval
	 *            minimum interval between to shake events.
	 */

	public static void configure(int threshold, int interval) {
		Accelerometer.threshold = threshold;
		Accelerometer.interval = interval;
	}

	/**
	 * Registers a listener and start listening.
	 * 
	 * @param accelerometerListener
	 *            callback for accelerometer events.
	 */

	public static void startListening(
			AccelerometerListener accelerometerListener) {

		sensorManager = (SensorManager) aContext
				.getSystemService(Context.SENSOR_SERVICE);

		// Take all sensors in device
		List<Sensor> sensors = sensorManager
				.getSensorList(Sensor.TYPE_ACCELEROMETER);

		if (sensors.size() > 0) {

			sensor = sensors.get(0);

			// Register Accelerometer Listener
			running = sensorManager.registerListener(sensorEventListener,
					sensor, SensorManager.SENSOR_DELAY_GAME);

			listener = accelerometerListener;
		}

	}

	/**
	 * Configures threshold and interval and registers a listener and start
	 * listening.
	 * 
	 * @param accelerometerListener
	 *            callback for accelerometer events.
	 * @param threshold
	 *            minimum acceleration variation for considering shaking.
	 * @param interval
	 *            minimum interval between to shake events.
	 */

	public static void startListening(
			AccelerometerListener accelerometerListener, int threshold,
			int interval) {
		configure(threshold, interval);
		startListening(accelerometerListener);
	}

	/**
	 * The listener that listens to events from the accelerometer listener.
	 */
	private static SensorEventListener sensorEventListener = new SensorEventListener() {

		private long now = 0;
		private long timeDiff = 0;
		private long lastUpdate = 0;
		private long lastShake = 0;

		private float x = 0;
		private float y = 0;
		private float z = 0;
		private float lastX = 0;
		private float lastY = 0;
		private float lastZ = 0;
		private float force = 0;

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		public void onSensorChanged(SensorEvent event) {
			now = event.timestamp;

			x = event.values[0];
			y = event.values[1];
			z = event.values[2];

			if (lastUpdate == 0) {
				lastUpdate = now;
				lastShake = now;
				lastX = x;
				lastY = y;
				lastZ = z;

			} else {
				timeDiff = now - lastUpdate;

				if (timeDiff > 0) {

					force = Math.abs(x + y + z - lastX - lastY - lastZ);

					if (Float.compare(force, threshold) > 0) {

						if (now - lastShake >= interval) {

							// trigger shake event
							listener.onShake(force);
						} else {

						}
						lastShake = now;
					}
					lastX = x;
					lastY = y;
					lastZ = z;
					lastUpdate = now;
				} else {
				}
			}
			// trigger change event
			listener.onAccelerationChanged(x, y, z);
		}

	};

	/**
	 * Interface for detecting change in acceleration or when the phone is
	 * shaken.
	 * 
	 */
	public interface AccelerometerListener {
		/**
		 * Detect the change in Acceleration in x,y,z
		 * 
		 * @param x
		 * @param y
		 * @param z
		 */
		public void onAccelerationChanged(float x, float y, float z);

		/**
		 * 
		 * @param force
		 */
		public void onShake(float force);
	}
}
