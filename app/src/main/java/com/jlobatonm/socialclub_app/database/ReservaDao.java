package com.jlobatonm.socialclub_app.database;

            import android.util.Log;

            import com.jlobatonm.socialclub_app.model.Reserva;

            import java.sql.Connection;
            import java.sql.PreparedStatement;
            import java.sql.ResultSet;
            import java.sql.SQLException;
            import java.util.ArrayList;
            import java.util.Date;
            import java.util.List;

            /**
             * Clase de acceso a datos para las operaciones relacionadas con las reservas en la base de datos.
             * Proporciona métodos para crear, consultar y eliminar reservas de manera asíncrona.
             */
            public class ReservaDao {
                private static final String TAG = "ReservaDao";

                /**
                 * Interfaz de callback para manejar el resultado de guardar una reserva.
                 */
                public interface SaveReservaCallback {
                    /**
                     * Se invoca cuando la operación de guardar la reserva es exitosa.
                     */
                    void onSuccess();

                    /**
                     * Se invoca cuando ocurre un error al guardar la reserva.
                     *
                     * @param exception Excepción que contiene los detalles del error.
                     */
                    void onError(Exception exception);
                }

                /**
                 * Guarda una nueva reserva en la base de datos.
                 *
                 * @param connection    Conexión a la base de datos.
                 * @param numeroSocio   Número identificador del socio que realiza la reserva.
                 * @param idInstalacion Identificador de la instalación a reservar.
                 * @param fecha         Fecha de la reserva.
                 * @param hora          Hora de la reserva.
                 * @param callback      Interfaz de callback para notificar el resultado.
                 */
                public void guardarReserva(Connection connection, String numeroSocio, int idInstalacion,
                                           Date fecha, String hora, SaveReservaCallback callback) {
                    new Thread(() -> {
                        try {
                            String query = "INSERT INTO reserva (fecha, hora, id_instalacion, numero_socio) VALUES (?, ?, ?, ?)";
                            PreparedStatement statement = connection.prepareStatement(query);
                            statement.setDate(1, new java.sql.Date(fecha.getTime()));
                            statement.setString(2, hora);
                            statement.setInt(3, idInstalacion);
                            statement.setString(4, numeroSocio);

                            int rowsInserted = statement.executeUpdate();
                            statement.close();

                            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                            if (rowsInserted > 0) {
                                mainHandler.post(callback::onSuccess);
                            } else {
                                mainHandler.post(() -> callback.onError(new Exception("No se pudo guardar la reserva")));
                            }

                        } catch (SQLException e) {
                            Log.e(TAG, "Error guardando reserva: " + e.getMessage(), e);
                            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                            mainHandler.post(() -> callback.onError(e));
                        }
                    }).start();
                }

                /**
                 * Interfaz de callback para manejar el resultado de obtener las horas reservadas.
                 */
                public interface GetHorasReservadasCallback {
                    /**
                     * Se invoca cuando la consulta de horas reservadas es exitosa.
                     *
                     * @param horasReservadas Lista de horas que ya están reservadas.
                     */
                    void onResult(List<String> horasReservadas);

                    /**
                     * Se invoca cuando ocurre un error al obtener las horas reservadas.
                     *
                     * @param exception Excepción que contiene los detalles del error.
                     */
                    void onError(Exception exception);
                }

                /**
                 * Obtiene las horas ya reservadas para una instalación en una fecha específica.
                 *
                 * @param connection    Conexión a la base de datos.
                 * @param idInstalacion Identificador de la instalación.
                 * @param fecha         Fecha para la que se consultan las reservas.
                 * @param callback      Interfaz de callback para notificar el resultado.
                 */
                public void obtenerHorasReservadas(Connection connection, int idInstalacion, Date fecha, GetHorasReservadasCallback callback) {
                    new Thread(() -> {
                        try {
                            String query = "SELECT hora FROM reserva WHERE id_instalacion = ? AND fecha = ?";
                            PreparedStatement statement = connection.prepareStatement(query);
                            statement.setInt(1, idInstalacion);
                            statement.setDate(2, new java.sql.Date(fecha.getTime()));

                            ResultSet resultSet = statement.executeQuery();

                            List<String> horasReservadas = new ArrayList<>();
                            while (resultSet.next()) {
                                String horaCompleta = resultSet.getString("hora").trim();
                                String horaSinSegundos = horaCompleta;
                                if (horaCompleta.length() > 5) {
                                    horaSinSegundos = horaCompleta.substring(0, 5);
                                }
                                horasReservadas.add(horaSinSegundos);
                                Log.d(TAG, "Hora reservada encontrada: '" + horaSinSegundos + "' (original: '" + horaCompleta + "')");
                            }

                            resultSet.close();
                            statement.close();

                            final List<String> finalHorasReservadas = horasReservadas;
                            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                            mainHandler.post(() -> callback.onResult(finalHorasReservadas));

                        } catch (SQLException e) {
                            Log.e(TAG, "Error obteniendo horas reservadas: " + e.getMessage(), e);
                            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                            mainHandler.post(() -> callback.onError(e));
                        }
                    }).start();
                }

                /**
                 * Interfaz de callback para manejar el resultado de obtener las reservas de un socio.
                 */
                public interface GetReservasBySocioCallback {
                    /**
                     * Se invoca cuando la consulta de reservas es exitosa.
                     *
                     * @param reservas Lista de reservas encontradas para el socio.
                     */
                    void onSuccess(List<Reserva> reservas);

                    /**
                     * Se invoca cuando ocurre un error al obtener las reservas.
                     *
                     * @param exception Excepción que contiene los detalles del error.
                     */
                    void onError(Exception exception);
                }

                /**
                 * Obtiene todas las reservas realizadas por un socio específico.
                 *
                 * @param connection  Conexión a la base de datos.
                 * @param numeroSocio Número identificador del socio.
                 * @param callback    Interfaz de callback para notificar el resultado.
                 */
                public void getReservasBySocio(Connection connection, String numeroSocio, GetReservasBySocioCallback callback) {
                    new Thread(() -> {
                        try {
                            Log.d(TAG, "Buscando reservas para socio: " + numeroSocio);

                            String query = "SELECT * FROM reserva WHERE numero_socio = ? ORDER BY fecha DESC, hora ASC";
                            PreparedStatement statement = connection.prepareStatement(query);
                            statement.setString(1, numeroSocio);
                            ResultSet resultSet = statement.executeQuery();

                            List<Reserva> reservas = new ArrayList<>();
                            while (resultSet.next()) {
                                Reserva reserva = new Reserva();
                                reserva.setIdReserva(resultSet.getInt("id_reserva"));
                                reserva.setFecha(resultSet.getDate("fecha"));
                                reserva.setHora(resultSet.getString("hora"));
                                reserva.setIdInstalacion(resultSet.getInt("id_instalacion"));
                                reserva.setNumeroSocio(resultSet.getString("numero_socio"));

                                Object idEmpleadoObj = resultSet.getObject("id_empleado");
                                if (idEmpleadoObj != null && !resultSet.wasNull()) {
                                    reserva.setIdEmpleado(resultSet.getInt("id_empleado"));
                                } else {
                                    reserva.setIdEmpleado(0);
                                }

                                reservas.add(reserva);
                            }

                            resultSet.close();
                            statement.close();

                            Log.d(TAG, "Reservas encontradas: " + reservas.size());

                            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                            mainHandler.post(() -> callback.onSuccess(reservas));

                        } catch (SQLException e) {
                            Log.e(TAG, "Error obteniendo reservas: " + e.getMessage(), e);
                            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                            mainHandler.post(() -> callback.onError(e));
                        }
                    }).start();
                }

                /**
                 * Interfaz de callback para manejar el resultado de eliminar una reserva.
                 */
                public interface EliminarReservaCallback {
                    /**
                     * Se invoca cuando la operación de eliminar la reserva es exitosa.
                     */
                    void onSuccess();

                    /**
                     * Se invoca cuando ocurre un error al eliminar la reserva.
                     *
                     * @param exception Excepción que contiene los detalles del error.
                     */
                    void onError(Exception exception);
                }

                /**
                 * Elimina una reserva específica de la base de datos.
                 *
                 * @param connection Conexión a la base de datos.
                 * @param idReserva  Identificador de la reserva a eliminar.
                 * @param callback   Interfaz de callback para notificar el resultado.
                 */
                public void eliminarReserva(Connection connection, int idReserva, EliminarReservaCallback callback) {
                    new Thread(() -> {
                        try {
                            String query = "DELETE FROM reserva WHERE id_reserva = ?";
                            PreparedStatement statement = connection.prepareStatement(query);
                            statement.setInt(1, idReserva);

                            int rowsDeleted = statement.executeUpdate();
                            statement.close();

                            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                            if (rowsDeleted > 0) {
                                mainHandler.post(callback::onSuccess);
                            } else {
                                mainHandler.post(() -> callback.onError(new Exception("No se encontró la reserva a eliminar")));
                            }

                        } catch (SQLException e) {
                            Log.e(TAG, "Error eliminando reserva: " + e.getMessage(), e);
                            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                            mainHandler.post(() -> callback.onError(e));
                        }
                    }).start();
                }
            }