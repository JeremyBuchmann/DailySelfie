package com.jeremybuchmann.dailyselfie;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Implements the receiver for the alarm that reminds the user to take
 * a selfie
 */
public class AlarmReceiver extends BroadcastReceiver
{
	private static int _NOTIFICATION_ID = 8264582;

	/**
	 * Receives the alarm broadcast and creates a notification to alert
	 * the user to take a selfie
	 *
	 * @param context
	 * @param intent
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Intent activityIntent = new Intent(context, MainActivity.class);

		// Had to set both of these flags to prevent the following situation:
		// tap the notification, the app launches; take a photo; tap
		// the "back" button; the view would then show all photos except for
		// the one that was just taken, presumably because the activity
		// started from the notification was started in a new task instead
		// of just bringing the old task forward; these two flags seem to
		// fix the issue.
		activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		// Create the PendingIntent to deliver the activity intent
		PendingIntent viewLaunchIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Build the notification
		Notification selfieNotification =
			new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_schedule_white_24dp)
				.setContentTitle("Daily Selfie")
				.setContentText("Time to take your daily selfie!")
				.setContentIntent(viewLaunchIntent)
				.build();

		// Get a reference to the NotificationManager
		NotificationManager notificationManager =
			(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		// Send the notification
		notificationManager.notify(_NOTIFICATION_ID, selfieNotification);
	}
}
