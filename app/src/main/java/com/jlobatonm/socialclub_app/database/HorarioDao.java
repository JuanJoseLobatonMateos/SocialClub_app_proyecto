package com.jlobatonm.socialclub_app.database;

import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Clase DAO (Data Access Object) para gestionar las operaciones relacionadas
 * con los horarios y reservas en la base de datos.
 */
public class HorarioDao {
    private static final String TAG = "HorarioDao";

    /**
     * Interfaz de callback para obtener las horas reservadas.
     */
    public interface GetHorasReservadasCallback {
        /**
         * Método llamado cuando la consulta es exitosa.
         * @param horasReservadas Lista de horas que ya están reservadas.
         */
        void onResult(List<String> horasReservadas);

        /**
         * Método llamado cuando ocurre un error durante la consulta.
         * @param exception Excepción que contiene los detalles del error.
         */
        void onError(Exception exception);
    }

    /**
     * Obtiene las horas ya reservadas para una instalación específica en una fecha determinada.
     * La consulta se ejecuta en un hilo separado y los resultados se devuelven a través del callback.
     *
     * @param connection Conexión a la base de datos.
     * @param idInstalacion Identificador de la instalación para la cual se buscan las reservas.
     * @param fecha Fecha para la cual se desean conocer las horas reservadas.
     * @param callback Interfaz de callback para manejar los resultados o errores.
     */
    public void getHorasReservadas(Connection connection, int idInstalacion, Date fecha, GetHorasReservadasCallback callback) {
        new Thread(() -> {
            try {
                String query = "SELECT hora FROM reserva WHERE id_instalacion = ? AND fecha = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, idInstalacion);
                statement.setDate(2, new java.sql.Date(fecha.getTime()));
                ResultSet resultSet = statement.executeQuery();

                List<String> horasReservadas = new ArrayList<>();
                while (resultSet.next()) {
                    horasReservadas.add(resultSet.getString("hora"));
                }

                resultSet.close();
                statement.close();

                final List<String> finalHorasReservadas = horasReservadas;
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onResult(finalHorasReservadas));

            } catch (SQLException e) {
                Log.e(TAG, "Error getting reserved hours: " + e.getMessage(), e);
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onError(e));
            }
        }).start();
    }
}