package com.jlobatonm.socialclub_app.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.jlobatonm.socialclub_app.R;
import com.jlobatonm.socialclub_app.database.EventoDao;
import com.jlobatonm.socialclub_app.database.MySQLConnection;
import com.jlobatonm.socialclub_app.model.Evento;
import com.jlobatonm.socialclub_app.ui.MainActivity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servicio encargado de monitorear nuevos eventos en el club social.
 * Verifica periódicamente la existencia de nuevos eventos y muestra
 * notificaciones al usuario cuando se detectan eventos no notificados previamente.
 */
public class EventoService extends Service {

    private static final String TAG = "EventoService";
    private static final String CHANNEL_ID = "EventosChannel";
    private Handler handler;
    private Runnable runnable;
    private final Set<Integer> eventosNotificados = new HashSet<>();

    /**
     * Inicializa el servicio, crea el canal de notificaciones y configura
     * la tarea periódica para verificar nuevos eventos.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                verificarEventosNuevos();
                handler.postDelayed(this, 60000); // Verificar cada 60 segundos
            }
        };
        handler.post(runnable);
    }

    /**
     * Maneja el inicio del servicio y define su comportamiento.
     *
     * @param intent  El Intent suministrado a {@link android.content.Context#startService},
     *                como se entrega, puede ser nulo si el servicio se reinicia
     * @param flags   Información adicional sobre la solicitud de inicio
     * @param startId Un identificador único para esta solicitud de inicio
     * @return Un valor indicando la política de reinicio {@link Service#START_STICKY}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Limpia los recursos cuando el servicio es destruido.
     * Elimina las tareas pendientes para evitar fugas de memoria.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    /**
     * Consulta la base de datos para obtener la lista de eventos
     * y muestra notificaciones para aquellos que no han sido
     * notificados previamente.
     */
    private void verificarEventosNuevos() {
        MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
            @Override
            public void onSuccess(Connection connection) {
                EventoDao eventoDao = new EventoDao();
                eventoDao.getAllEventos(connection, new EventoDao.GetAllEventosCallback() {
                    @Override
                    public void onResult(List<Evento> eventos) {
                        for (Evento evento : eventos) {
                            if (!eventosNotificados.contains(evento.getIdEvento())) {
                                mostrarNotificacion(evento.getNombre(), "Fecha: " + evento.getFecha(), evento.getImagen());
                                eventosNotificados.add(evento.getIdEvento());
                            }
                        }
                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e(TAG, "Error al obtener eventos: ", exception);
                    }
                });
            }

            @Override
            public void onFailure(SQLException exception) {
                Log.e(TAG, "Error de conexión a la base de datos: ", exception);
            }
        });
    }

    /**
     * Construye y muestra una notificación con la información del evento.
     *
     * @param titulo  El título que se mostrará en la notificación
     * @param mensaje El contenido textual de la notificación
     * @param imagen  Los bytes de la imagen que se mostrará en la notificación expandida,
     *                puede ser null si no hay imagen disponible
     */
    private void mostrarNotificacion(String titulo, String mensaje, byte[] imagen) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSubText(getString(R.string.app_name));

        if (imagen != null) {
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imagen, 0, imagen.length);
                builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon((Bitmap) null));
            } catch (Exception e) {
                Log.e(TAG, "Error al decodificar la imagen: ", e);
            }
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    /**
     * Crea el canal de notificaciones requerido para Android 8.0 (API 26) y superior.
     * Este canal se utiliza para todas las notificaciones de eventos del club.
     */
    private void createNotificationChannel() {
        CharSequence name = "Eventos Channel";
        String description = "Canal para notificaciones de nuevos eventos";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * Retorna la interfaz de comunicación con el cliente.
     * Este servicio no admite enlace, por lo que devuelve null.
     *
     * @param intent El Intent que se envió al {@link android.content.Context#bindService}
     * @return null porque este servicio no permite enlace
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}