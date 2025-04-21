package com.jlobatonm.socialclub_app.database;

                    import android.util.Log;
                    import com.jlobatonm.socialclub_app.model.Evento;
                    import java.sql.Connection;
                    import java.sql.PreparedStatement;
                    import java.sql.ResultSet;
                    import java.sql.SQLException;
                    import java.util.ArrayList;
                    import java.util.List;

                    /**
                     * Clase DAO (Data Access Object) para gestionar las operaciones de base de datos
                     * relacionadas con eventos del club social.
                     */
                    public class EventoDao {

                        private static final String TAG = "EventoDao";

                        /**
                         * Interfaz de callback para obtener la lista de eventos.
                         */
                        public interface GetAllEventosCallback {
                            /**
                             * Método llamado cuando la consulta es exitosa.
                             * @param eventos Lista de eventos obtenidos de la base de datos.
                             */
                            void onResult(List<Evento> eventos);

                            /**
                             * Método llamado cuando ocurre un error durante la consulta.
                             * @param exception Excepción que contiene los detalles del error.
                             */
                            void onError(Exception exception);
                        }

                        /**
                         * Obtiene todos los eventos almacenados en la base de datos.
                         * La consulta se ejecuta en un hilo separado y los resultados se devuelven a través del callback.
                         *
                         * @param connection Conexión a la base de datos.
                         * @param callback Interfaz de callback para manejar los resultados o errores.
                         */
                        public void getAllEventos(Connection connection, GetAllEventosCallback callback) {
                            new Thread(() -> {
                                try {
                                    String query = "SELECT idevento, nombre, imagen, fecha FROM evento";
                                    PreparedStatement statement = connection.prepareStatement(query);
                                    ResultSet resultSet = statement.executeQuery();

                                    List<Evento> eventos = new ArrayList<>();
                                    while (resultSet.next()) {
                                        Evento evento = new Evento();
                                        evento.setIdEvento(resultSet.getInt("idevento"));
                                        evento.setNombre(resultSet.getString("nombre"));
                                        evento.setImagen(resultSet.getBytes("imagen"));
                                        evento.setFecha(resultSet.getDate("fecha"));
                                        eventos.add(evento);
                                    }

                                    resultSet.close();
                                    statement.close();

                                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                                    mainHandler.post(() -> callback.onResult(eventos));

                                } catch (SQLException e) {
                                    Log.e(TAG, "Error getting eventos: " + e.getMessage(), e);
                                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                                    mainHandler.post(() -> callback.onError(e));
                                }
                            }).start();
                        }
                    }