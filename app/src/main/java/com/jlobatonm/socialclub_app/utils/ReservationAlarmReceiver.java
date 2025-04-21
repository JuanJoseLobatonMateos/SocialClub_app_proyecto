package com.jlobatonm.socialclub_app.utils;

    import android.Manifest;
    import android.app.Notification;
    import android.content.BroadcastReceiver;
    import android.content.Context;
    import android.content.Intent;
    import android.util.Log;

    import androidx.annotation.RequiresPermission;
    import androidx.core.app.NotificationManagerCompat;

    import com.jlobatonm.socialclub_app.model.Instalacion;
    import com.jlobatonm.socialclub_app.database.InstalacionDao;
    import com.jlobatonm.socialclub_app.database.MySQLConnection;

    import java.sql.Connection;
    import java.sql.SQLException;

    /**
     * Receptor de emisiones que maneja recordatorios de reservas de instalaciones.
     * <p>
     * Este BroadcastReceiver se encarga de mostrar notificaciones para recordar a los usuarios
     * sobre sus reservas programadas. Verifica la acción del Intent recibido para asegurar
     * que solo procesa intents legítimos del sistema.
     * </p>
     * <p>
     * Para programar un recordatorio, use el método {@link #createIntent} para crear el Intent
     * y regístrelo con AlarmManager o WorkManager.
     * </p>
     */
    public class ReservationAlarmReceiver extends BroadcastReceiver {
        private static final String TAG = "ReservationAlarmReceiver";
        private static final String EXTRA_NOTIFICATION_ID = "notification_id";
        private static final String EXTRA_INSTALACION_ID = "instalacion_id";
        private static final String EXTRA_HORA = "hora";
        private static final String EXTRA_FECHA = "fecha";

        /**
         * Acción específica para este receiver. Solo intents con esta acción serán procesados.
         * Esta acción es protegida y solo debe ser utilizada por esta aplicación.
         */
        public static final String ACTION_RESERVATION_REMINDER =
                "com.jlobatonm.socialclub_app.ACTION_RESERVATION_REMINDER";

        /**
         * Procesa los intents recibidos para mostrar notificaciones de recordatorio.
         * <p>
         * Este método verifica primero que el intent tenga la acción esperada para prevenir
         * que intents maliciosos activen el receptor. Luego, recupera la información de la
         * instalación reservada y muestra una notificación de recordatorio al usuario.
         * </p>
         *
         * @param context El Context en el cual se está ejecutando el receiver
         * @param intent El Intent recibido que contiene datos de la reserva
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            // Verificar que la acción del intent sea la esperada
            String action = intent.getAction();
            if (action == null || !action.equals(ACTION_RESERVATION_REMINDER)) {
                Log.w(TAG, "Intent recibido con acción incorrecta o nula: " + action);
                return;
            }

            int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
            int instalacionId = intent.getIntExtra(EXTRA_INSTALACION_ID, 0);
            String hora = intent.getStringExtra(EXTRA_HORA);

            Log.d(TAG, "Recibida alarma para recordatorio de reserva: ID=" + notificationId);

            MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
                @Override
                public void onSuccess(Connection connection) {
                    InstalacionDao instalacionDao = new InstalacionDao();
                    // Obtener el nombre de la instalación para la notificación
                    instalacionDao.getInstalacionById(connection, instalacionId, new InstalacionDao.GetInstalacionCallback() {
                        @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
                        @Override
                        public void onResult(Instalacion instalacion) {
                            if (instalacion != null) {
                                String title = "Recordatorio de Reserva";
                                String message = "Tienes una reserva para " + instalacion.getNombre() +
                                        " hoy a las " + hora;

                                // Crear y mostrar notificación
                                Notification notification = NotificationHelper.buildNotification(
                                        context, title, message, notificationId);

                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                                try {
                                    notificationManager.notify(notificationId, notification);
                                    Log.d(TAG, "Notificación mostrada: " + message);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error al mostrar notificación: " + e.getMessage());
                                }
                            }
                        }

                        @Override
                        public void onError(Exception exception) {
                            Log.e(TAG, "Error al obtener información de la instalación: " + exception.getMessage());
                        }
                    });
                }

                @Override
                public void onFailure(SQLException exception) {
                    Log.e(TAG, "Error de conexión al programar notificación: " + exception.getMessage());
                }
            });
        }

        /**
         * Crea un Intent para programar un recordatorio de reserva.
         * <p>
         * Este método configura un Intent con todos los datos necesarios para mostrar
         * un recordatorio de reserva cuando es recibido por este BroadcastReceiver.
         * </p>
         *
         * @param context El Context de la aplicación
         * @param notificationId ID único para la notificación
         * @param instalacionId ID de la instalación reservada
         * @param hora Hora de la reserva en formato legible (HH:mm)
         * @param fecha Fecha de la reserva en formato legible
         * @return Intent configurado para ser usado con AlarmManager o WorkManager
         */
        public static Intent createIntent(Context context, int notificationId,
                                         int instalacionId, String hora, String fecha) {
            Intent intent = new Intent(context, ReservationAlarmReceiver.class);
            // Establecer la acción específica
            intent.setAction(ACTION_RESERVATION_REMINDER);
            intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
            intent.putExtra(EXTRA_INSTALACION_ID, instalacionId);
            intent.putExtra(EXTRA_HORA, hora);
            intent.putExtra(EXTRA_FECHA, fecha);
            return intent;
        }
    }