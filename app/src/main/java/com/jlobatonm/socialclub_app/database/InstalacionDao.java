package com.jlobatonm.socialclub_app.database;

import android.util.Log;

import com.jlobatonm.socialclub_app.model.Instalacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO (Data Access Object) para gestionar las operaciones de base de datos
 * relacionadas con las instalaciones del club social.
 */
public class InstalacionDao {
    private static final String TAG = "InstalacionDao";

    /**
     * Interfaz de callback para obtener la lista completa de instalaciones.
     */
    public interface GetAllInstalacionesCallback {
        /**
         * Método llamado cuando la consulta es exitosa.
         *
         * @param instalaciones Lista de instalaciones obtenidas de la base de datos.
         */
        void onResult(List<Instalacion> instalaciones);

        /**
         * Método llamado cuando ocurre un error durante la consulta.
         *
         * @param exception Excepción que contiene los detalles del error.
         */
        void onError(Exception exception);
    }

    /**
     * Obtiene todas las instalaciones almacenadas en la base de datos.
     * La consulta se ejecuta en un hilo separado y los resultados se devuelven a través del callback.
     *
     * @param connection Conexión a la base de datos.
     * @param callback   Interfaz de callback para manejar los resultados o errores.
     */
    public void getAllInstalaciones(Connection connection, GetAllInstalacionesCallback callback) {
        new Thread(() -> {
            try {
                String query = "SELECT * FROM instalacion";
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery();

                List<Instalacion> instalaciones = new ArrayList<>();
                while (resultSet.next()) {
                    Instalacion instalacion = new Instalacion();
                    instalacion.setId(resultSet.getInt("id"));
                    instalacion.setTipo(resultSet.getString("tipo"));
                    instalacion.setNombre(resultSet.getString("nombre"));
                    instalacion.setCapacidad(resultSet.getInt("capacidad"));
                    instalacion.setPrecioAlquiler(resultSet.getDouble("precio_alquiler"));
                    instalacion.setDuracion(resultSet.getInt("duracion"));
                    instalacion.setHoraIni(resultSet.getString("hora_ini"));
                    instalacion.setHoraFin(resultSet.getString("hora_fin"));
                    instalacion.setDisponibilidad(resultSet.getBoolean("disponibilidad"));
                    instalacion.setIdEmpleado(resultSet.getInt("id_empleado"));
                    instalacion.setImagen(resultSet.getBytes("imagen"));
                    instalaciones.add(instalacion);
                }

                resultSet.close();
                statement.close();

                final List<Instalacion> finalInstalaciones = instalaciones;
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onResult(finalInstalaciones));

            } catch (SQLException e) {
                Log.e(TAG, "Error getting instalaciones: " + e.getMessage(), e);
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onError(e));
            }
        }).start();
    }

    /**
     * Interfaz de callback para obtener una instalación específica por su ID.
     */
    public interface GetInstalacionCallback {
        /**
         * Método llamado cuando la consulta es exitosa.
         *
         * @param instalacion Instalación encontrada en la base de datos.
         */
        void onResult(Instalacion instalacion);

        /**
         * Método llamado cuando ocurre un error durante la consulta.
         *
         * @param exception Excepción que contiene los detalles del error.
         */
        void onError(Exception exception);
    }

    /**
     * Busca y recupera una instalación específica por su ID.
     * La consulta se ejecuta en un hilo separado y el resultado se devuelve a través del callback.
     *
     * @param connection    Conexión a la base de datos.
     * @param instalacionId ID de la instalación que se desea buscar.
     * @param callback      Interfaz de callback para manejar el resultado o error.
     */
    public void getInstalacionById(Connection connection, int instalacionId, GetInstalacionCallback callback) {
        new Thread(() -> {
            try {
                String query = "SELECT * FROM instalacion WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, instalacionId);
                ResultSet resultSet = statement.executeQuery();

                Instalacion instalacion = null;
                if (resultSet.next()) {
                    instalacion = new Instalacion();
                    instalacion.setId(resultSet.getInt("id"));
                    instalacion.setTipo(resultSet.getString("tipo"));
                    instalacion.setNombre(resultSet.getString("nombre"));
                    instalacion.setCapacidad(resultSet.getInt("capacidad"));
                    instalacion.setPrecioAlquiler(resultSet.getDouble("precio_alquiler"));
                    instalacion.setDuracion(resultSet.getInt("duracion"));
                    instalacion.setHoraIni(resultSet.getString("hora_ini"));
                    instalacion.setHoraFin(resultSet.getString("hora_fin"));
                    instalacion.setDisponibilidad(resultSet.getBoolean("disponibilidad"));
                    instalacion.setIdEmpleado(resultSet.getInt("id_empleado"));
                    instalacion.setImagen(resultSet.getBytes("imagen"));
                }

                resultSet.close();
                statement.close();

                final Instalacion finalInstalacion = instalacion;
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onResult(finalInstalacion));

            } catch (SQLException e) {
                Log.e(TAG, "Error getting instalacion: " + e.getMessage(), e);
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onError(e));
            }
        }).start();
    }
}