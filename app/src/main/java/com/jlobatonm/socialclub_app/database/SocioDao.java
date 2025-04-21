package com.jlobatonm.socialclub_app.database;

        import android.util.Log;

        import com.jlobatonm.socialclub_app.model.Socio;

        import java.sql.Connection;
        import java.sql.PreparedStatement;
        import java.sql.ResultSet;
        import java.sql.SQLException;

        import org.mindrot.jbcrypt.BCrypt;

        /**
         * Clase de acceso a datos para las operaciones relacionadas con los socios en la base de datos.
         * Proporciona métodos para consultar y verificar información de los socios de manera asíncrona.
         */
        public class SocioDao {

            private static final String TAG = "SocioDao";

            /**
             * Interfaz de callback para manejar el resultado de obtener un socio.
             */
            public interface GetSocioCallback {
                /**
                 * Se invoca cuando la consulta del socio es exitosa.
                 *
                 * @param socio Objeto Socio con los datos encontrados, o null si no existe.
                 */
                void onResult(Socio socio);

                /**
                 * Se invoca cuando ocurre un error al obtener el socio.
                 *
                 * @param exception Excepción que contiene los detalles del error.
                 */
                void onError(Exception exception);
            }

            /**
             * Interfaz de callback para manejar el resultado de verificar las credenciales de un socio.
             */
            public interface CheckSocioCallback {
                /**
                 * Se invoca cuando la verificación del socio es exitosa.
                 *
                 * @param isValid Booleano que indica si las credenciales son válidas.
                 */
                void onResult(boolean isValid);

                /**
                 * Se invoca cuando ocurre un error al verificar el socio.
                 *
                 * @param exception Excepción que contiene los detalles del error.
                 */
                void onError(Exception exception);
            }

            /**
             * Obtiene un socio por su dirección de correo electrónico.
             *
             * @param connection Conexión a la base de datos.
             * @param email      Dirección de correo electrónico del socio.
             * @param callback   Interfaz de callback para notificar el resultado.
             */
            public void getSocioByEmail(Connection connection, String email, GetSocioCallback callback) {
                new Thread(() -> {
                    try {
                        String query = "SELECT numero_socio, id_socio, num_familia, nombre, apellidos, " +
                                "telefono, dni, email, fecha_nacimiento, foto, huella, titularidad, " +
                                "dentro_instalacion, fecha_alta, contrasenia FROM socio WHERE email = ?";
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setString(1, email);
                        ResultSet resultSet = statement.executeQuery();

                        Socio socio = null;
                        if (resultSet.next()) {
                            socio = new Socio();
                            socio.setNumeroSocio(resultSet.getString("numero_socio"));
                            socio.setIdSocio(resultSet.getInt("id_socio"));
                            socio.setNumFamilia(resultSet.getInt("num_familia"));
                            socio.setNombre(resultSet.getString("nombre"));
                            socio.setApellidos(resultSet.getString("apellidos"));
                            socio.setTelefono(resultSet.getString("telefono"));
                            socio.setDni(resultSet.getString("dni"));
                            socio.setEmail(resultSet.getString("email"));
                            socio.setFechaNacimiento(resultSet.getDate("fecha_nacimiento"));
                            socio.setFoto(resultSet.getBytes("foto"));
                            socio.setHuella(resultSet.getBytes("huella"));
                            socio.setTitularidad(resultSet.getString("titularidad"));
                            socio.setDentroInstalacion(resultSet.getBoolean("dentro_instalacion"));
                            socio.setFechaAlta(resultSet.getDate("fecha_alta"));
                            socio.setPassword(resultSet.getString("contrasenia"));
                        }

                        resultSet.close();
                        statement.close();

                        final Socio finalSocio = socio;
                        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                        mainHandler.post(() -> callback.onResult(finalSocio));

                    } catch (SQLException e) {
                        Log.e(TAG, "Error getting socio: " + e.getMessage(), e);
                        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                        mainHandler.post(() -> callback.onError(e));
                    }
                }).start();
            }

            /**
             * Verifica las credenciales de un socio comparando la contraseña proporcionada.
             *
             * @param connection   Conexión a la base de datos.
             * @param email        Dirección de correo electrónico del socio.
             * @param plainPassword Contraseña en texto plano para verificar.
             * @param callback     Interfaz de callback para notificar el resultado.
             */
            public void checkSocio(Connection connection, String email, String plainPassword, CheckSocioCallback callback) {
                new Thread(() -> {
                    try {
                        String query = "SELECT contrasenia FROM socio WHERE email = ?";
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setString(1, email);
                        ResultSet resultSet = statement.executeQuery();

                        boolean isValid = false;
                        if (resultSet.next()) {
                            String hashedPassword = resultSet.getString("contrasenia");
                            isValid = verifyPassword(plainPassword, hashedPassword);
                        }

                        resultSet.close();
                        statement.close();

                        final boolean finalIsValid = isValid;
                        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                        mainHandler.post(() -> callback.onResult(finalIsValid));

                    } catch (SQLException e) {
                        Log.e(TAG, "Error checking socio: " + e.getMessage(), e);
                        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                        mainHandler.post(() -> callback.onError(e));
                    }
                }).start();
            }

            /**
             * Verifica si una contraseña en texto plano coincide con su versión encriptada con BCrypt.
             *
             * @param plainPassword  Contraseña en texto plano para verificar.
             * @param hashedPassword Versión encriptada de la contraseña almacenada en la base de datos.
             * @return true si la contraseña coincide, false en caso contrario.
             */
            public static boolean verifyPassword(String plainPassword, String hashedPassword) {
                try {
                    Log.d(TAG, "Verificando contraseña con BCrypt");
                    boolean result = BCrypt.checkpw(plainPassword, hashedPassword);
                    Log.d(TAG, "Resultado de verificación: " + result);
                    return result;
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error verificando contraseña: " + e.getMessage(), e);
                    return false;
                }
            }
        }