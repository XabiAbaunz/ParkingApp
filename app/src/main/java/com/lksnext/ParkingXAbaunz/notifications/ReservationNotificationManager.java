package com.lksnext.ParkingXAbaunz.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.lksnext.ParkingXAbaunz.domain.Reserva;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReservationNotificationManager {

    private static final String TAG = "NotificationManager";
    private Context context;
    private AlarmManager alarmManager;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public ReservationNotificationManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public ReservationNotificationManager() {}

    public void scheduleNotifications(Reserva reserva) {
        if (reserva == null || reserva.getHora() == null) {
            Log.e(TAG, "Reserva o hora es null");
            return;
        }

        // Verificar permisos antes de programar
        if (!canScheduleExactAlarms()) {
            Log.w(TAG, "No se pueden programar alarmas exactas. Permisos insuficientes.");
            return;
        }

        try {
            Date fechaReserva = dateFormatter.parse(reserva.getFecha());
            if (fechaReserva == null) {
                Log.e(TAG, "No se pudo parsear la fecha de la reserva");
                return;
            }

            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(fechaReserva);
            long startSeconds = reserva.getHora().getHoraInicio();
            int startHours = (int) (startSeconds / 3600);
            int startMinutes = (int) ((startSeconds % 3600) / 60);
            startCalendar.set(Calendar.HOUR_OF_DAY, startHours);
            startCalendar.set(Calendar.MINUTE, startMinutes);
            startCalendar.set(Calendar.SECOND, 0);
            startCalendar.set(Calendar.MILLISECOND, 0);

            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(fechaReserva);
            long endSeconds = reserva.getHora().getHoraFin();
            int endHours = (int) (endSeconds / 3600);
            int endMinutes = (int) ((endSeconds % 3600) / 60);
            endCalendar.set(Calendar.HOUR_OF_DAY, endHours);
            endCalendar.set(Calendar.MINUTE, endMinutes);
            endCalendar.set(Calendar.SECOND, 0);
            endCalendar.set(Calendar.MILLISECOND, 0);

            Calendar startReminderTime = (Calendar) startCalendar.clone();
            startReminderTime.add(Calendar.MINUTE, -30);

            Calendar endReminderTime = (Calendar) endCalendar.clone();
            endReminderTime.add(Calendar.MINUTE, -15);

            Calendar now = Calendar.getInstance();

            if (startReminderTime.after(now)) {
                scheduleStartReminder(reserva, startReminderTime.getTimeInMillis());
            } else {
                Log.d(TAG, "No se programa notificación de inicio para reserva " + reserva.getId() + " - tiempo ya pasado");
            }

            if (endReminderTime.after(now)) {
                scheduleEndReminder(reserva, endReminderTime.getTimeInMillis());
            } else {
                Log.d(TAG, "No se programa notificación de fin para reserva " + reserva.getId() + " - tiempo ya pasado");
            }

        } catch (ParseException e) {
            Log.e(TAG, "Error al parsear fecha: " + e.getMessage());
        }
    }

    private boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    public void updateNotifications(Reserva reserva) {
        Log.d(TAG, "Actualizando notificaciones para reserva: " + reserva.getId());
        cancelNotifications(reserva.getId());
        scheduleNotifications(reserva);
    }

    private void scheduleStartReminder(Reserva reserva, long triggerTime) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.EXTRA_NOTIFICATION_TYPE, NotificationReceiver.TYPE_START_REMINDER);
        intent.putExtra(NotificationReceiver.EXTRA_RESERVATION_ID, reserva.getId());
        intent.putExtra(NotificationReceiver.EXTRA_TITLE, "Reserva próxima a comenzar");
        intent.putExtra(NotificationReceiver.EXTRA_MESSAGE,
                "Tu reserva comenzará en 30 minutos. ¡No olvides dirigirte al parking!");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (reserva.getId() + "_start").hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        scheduleAlarm(triggerTime, pendingIntent);
        Log.d(TAG, "Programada notificación de inicio para reserva: " + reserva.getId());
    }

    private void scheduleEndReminder(Reserva reserva, long triggerTime) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.EXTRA_NOTIFICATION_TYPE, NotificationReceiver.TYPE_END_REMINDER);
        intent.putExtra(NotificationReceiver.EXTRA_RESERVATION_ID, reserva.getId());
        intent.putExtra(NotificationReceiver.EXTRA_TITLE, "Reserva próxima a finalizar");
        intent.putExtra(NotificationReceiver.EXTRA_MESSAGE,
                "Tu reserva finalizará en 15 minutos. ¡Prepárate para liberar la plaza!");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (reserva.getId() + "_end").hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        scheduleAlarm(triggerTime, pendingIntent);
        Log.d(TAG, "Programada notificación de fin para reserva: " + reserva.getId());
    }

    private void scheduleAlarm(long triggerTime, PendingIntent pendingIntent) {
        if (alarmManager != null && canScheduleExactAlarms()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                }
                Log.d(TAG, "Alarma programada exitosamente");
            } catch (SecurityException e) {
                Log.e(TAG, "Error de permisos al programar alarma: " + e.getMessage());
            }
        } else {
            Log.w(TAG, "No se puede programar alarma exacta - permisos insuficientes");
        }
    }

    public void cancelNotifications(String reservaId) {
        cancelNotification(reservaId + "_start");
        cancelNotification(reservaId + "_end");
        Log.d(TAG, "Canceladas notificaciones para reserva: " + reservaId);
    }

    private void cancelNotification(String identifier) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                identifier.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}