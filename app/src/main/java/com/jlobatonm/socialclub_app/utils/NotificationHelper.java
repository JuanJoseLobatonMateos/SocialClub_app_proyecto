package com.jlobatonm.socialclub_app.utils;

      import android.app.Notification;
      import android.app.NotificationChannel;
      import android.app.NotificationManager;
      import android.app.PendingIntent;
      import android.content.Context;
      import android.content.Intent;

      import androidx.core.app.NotificationCompat;

      import com.jlobatonm.socialclub_app.ui.MainActivity;
      import com.jlobatonm.socialclub_app.R;

      /**
       * Clase utilitaria para la creación y gestión de notificaciones en la aplicación.
       * Proporciona métodos para crear canales de notificación y construir notificaciones
       * con una configuración predeterminada.
       */
      public class NotificationHelper {

          private static final String CHANNEL_ID = "reservas_channel";
          private static final String CHANNEL_NAME = "Recordatorios de Reservas";
          private static final String CHANNEL_DESCRIPTION = "Notificaciones para recordar las reservas programadas";

          /**
           * Crea un canal de notificaciones para dispositivos con Android 8.0 (API 26) o superior.
           * Este método debe ser llamado al inicio de la aplicación, antes de mostrar cualquier notificación.
           *
           * @param context El contexto de la aplicación utilizado para acceder al NotificationManager.
           */
          public static void createNotificationChannel(Context context) {
              NotificationChannel channel = new NotificationChannel(
                      CHANNEL_ID,
                      CHANNEL_NAME,
                      NotificationManager.IMPORTANCE_DEFAULT);

              channel.setDescription(CHANNEL_DESCRIPTION);

              NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
              notificationManager.createNotificationChannel(channel);
          }

          /**
           * Construye y devuelve un objeto Notification configurado con los parámetros proporcionados.
           * La notificación incluye un intent pendiente que abre la MainActivity cuando se toca.
           *
           * @param context El contexto de la aplicación para crear la notificación.
           * @param title Título que se mostrará en la notificación.
           * @param message Contenido principal de la notificación.
           * @param notificationId Identificador único para la notificación, utilizado para el PendingIntent.
           * @return Un objeto Notification completamente configurado, listo para ser mostrado.
           */
          public static Notification buildNotification(Context context, String title, String message, int notificationId) {
              Intent intent = new Intent(context, MainActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

              PendingIntent pendingIntent = PendingIntent.getActivity(
                      context,
                      notificationId,
                      intent,
                      PendingIntent.FLAG_IMMUTABLE);

              return new NotificationCompat.Builder(context, CHANNEL_ID)
                      .setSmallIcon(R.drawable.ic_stat_name)
                      .setContentTitle(title)
                      .setContentText(message)
                      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                      .setContentIntent(pendingIntent)
                      .setAutoCancel(true)
                      .build();
          }
      }