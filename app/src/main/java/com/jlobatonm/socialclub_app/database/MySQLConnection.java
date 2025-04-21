package com.jlobatonm.socialclub_app.database;

        import android.os.Handler;
        import android.os.Looper;
        import android.util.Log;

        import java.sql.Connection;
        import java.sql.DriverManager;
        import java.sql.SQLException;
        import java.util.concurrent.Executor;
        import java.util.concurrent.Executors;

        /**
         * Clase encargada de gestionar las conexiones a la base de datos MySQL.
         * Implementa un patrón asíncrono utilizando Executor y Handler para evitar
         * bloquear el hilo principal durante las operaciones de conexión a la base de datos.
         */
        public class MySQLConnection {

            private static final String TAG = "MySQLConnection";
            private static final String URL = "jdbc:mysql://192.168.1.25:3306/clubsocial?useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=5000";
            private static final String USER = "root";
            private static final String PASSWORD = "root";

            private static final Executor executor = Executors.newSingleThreadExecutor();
            private static final Handler mainHandler = new Handler(Looper.getMainLooper());

            /**
             * Establece una conexión asíncrona con la base de datos MySQL.
             * La operación se ejecuta en un hilo secundario y notifica el resultado
             * en el hilo principal a través del callback.
             *
             * @param callback Interfaz que maneja los resultados de la conexión, ya sea
             *                 exitosa o fallida.
             */
            public static void getConnectionAsync(ConnectionCallback callback) {
                executor.execute(() -> {
                    try {
                        Log.d(TAG, "Attempting to connect to the database...");
                        Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                        Log.d(TAG, "Connection successful!");

                        mainHandler.post(() -> callback.onSuccess(connection));
                    } catch (SQLException e) {
                        Log.e(TAG, "Connection failed: " + e.getMessage(), e);
                        mainHandler.post(() -> callback.onFailure(e));
                    }
                });
            }

            /**
             * Interfaz de callback para manejar los resultados de las operaciones
             * de conexión a la base de datos.
             */
            public interface ConnectionCallback {
                /**
                 * Se invoca cuando la conexión a la base de datos se ha establecido correctamente.
                 *
                 * @param connection Objeto Connection que representa la conexión exitosa a la base de datos.
                 */
                void onSuccess(Connection connection);

                /**
                 * Se invoca cuando ha ocurrido un error al intentar establecer la conexión.
                 *
                 * @param exception Excepción SQLException que contiene los detalles del error.
                 */
                void onFailure(SQLException exception);
            }
        }