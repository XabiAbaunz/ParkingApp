package com.lksnext.ParkingXAbaunz.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.lksnext.ParkingXAbaunz.R;
import com.lksnext.ParkingXAbaunz.view.MainActivity;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "parking_reservations";
    private static final String CHANNEL_NAME = "Reservas de Parking";
    private static final String CHANNEL_DESCRIPTION = "Notificaciones sobre el estado de tus reservas";

    public static final String EXTRA_NOTIFICATION_TYPE = "notification_type";
    public static final String EXTRA_RESERVATION_ID = "reservation_id";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_TITLE = "title";

    public static final String TYPE_START_REMINDER = "start_reminder";
    public static final String TYPE_END_REMINDER = "end_reminder";

    @Override
    public void onReceive(Context context, Intent intent) {
        String notificationType = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE);
        String reservationId = intent.getStringExtra(EXTRA_RESERVATION_ID);
        String title = intent.getStringExtra(EXTRA_TITLE);
        String message = intent.getStringExtra(EXTRA_MESSAGE);

        createNotificationChannel(context);
        showNotification(context, title, message, reservationId);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(Context context, String title, String message, String reservationId) {
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                reservationId.hashCode(),
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 250, 250, 250});

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(reservationId.hashCode(), builder.build());
    }
}