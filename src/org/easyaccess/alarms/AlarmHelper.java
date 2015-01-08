package org.easyaccess.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class AlarmHelper {

    public static void setAlarm(Context context, int hourOfDay, int minute) {

        Calendar calendar = prepareCalendar(hourOfDay, minute);
        setAlarmManager(context, calendar);
    }

    private static Calendar prepareCalendar(int hourOfDay, int minute) {
        Calendar calNow = Calendar.getInstance();
        Calendar calSet = (Calendar) calNow.clone();

        calSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calSet.set(Calendar.MINUTE, minute);
        calSet.set(Calendar.SECOND, 0);
        calSet.set(Calendar.MILLISECOND, 0);

        if (calSet.compareTo(calNow) <= 0) {
            // Today Set time passed, count to tomorrow
            calSet.add(Calendar.DATE, 1);
        }
        return calSet;
    }


    private static void setAlarmManager(Context context, Calendar targetCal) {

        Log.d("alarm", "Alarm is set " + targetCal.getTime() + "\n" + "***\n");

        Intent intent = new Intent(context, AlarmSnoozeApp.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

    }
}
