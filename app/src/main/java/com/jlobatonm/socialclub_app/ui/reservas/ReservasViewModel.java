package com.jlobatonm.socialclub_app.ui.reservas;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jlobatonm.socialclub_app.database.HorarioDao;
import com.jlobatonm.socialclub_app.database.InstalacionDao;
import com.jlobatonm.socialclub_app.database.MySQLConnection;
import com.jlobatonm.socialclub_app.database.ReservaDao;
import com.jlobatonm.socialclub_app.model.Instalacion;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * ViewModel para la gestión de reservas de instalaciones.
 * Proporciona datos y funcionalidades para la vista de reservas.
 */
public class ReservasViewModel extends ViewModel {

    private static final String TAG = "ReservasViewModel";

    private final MutableLiveData<List<Instalacion>> instalaciones = new MutableLiveData<>();
    private final MutableLiveData<List<String>> horasReservadas = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> reservaRealizada = new MutableLiveData<>(false);

    /**
     * Constructor que inicializa el ViewModel y carga la lista de instalaciones.
     */
    public ReservasViewModel() {
        cargarInstalaciones();
    }

    /**
     * Obtiene los mensajes de error que pueden ocurrir durante las operaciones.
     *
     * @return LiveData con el mensaje de error actual
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Obtiene el estado de la operación de reserva.
     *
     * @return LiveData que indica si la reserva fue realizada exitosamente
     */
    public LiveData<Boolean> getReservaRealizada() {
        return reservaRealizada;
    }

    /**
     * Carga la lista de instalaciones disponibles desde la base de datos.
     * Actualiza el LiveData de instalaciones y el estado de carga.
     */
    public void cargarInstalaciones() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
            @Override
            public void onSuccess(Connection connection) {
                InstalacionDao instalacionDao = new InstalacionDao();
                instalacionDao.getAllInstalaciones(connection, new InstalacionDao.GetAllInstalacionesCallback() {
                    @Override
                    public void onResult(List<Instalacion> resultado) {
                        instalaciones.postValue(resultado);
                        isLoading.postValue(false);
                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e(TAG, "Error al cargar instalaciones", exception);
                        errorMessage.postValue("Error al cargar instalaciones: " + exception.getMessage());
                        isLoading.postValue(false);
                    }
                });
            }

            @Override
            public void onFailure(SQLException exception) {
                Log.e(TAG, "Error de conexión a la base de datos", exception);
                errorMessage.postValue("Error de conexión: " + exception.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Carga los horarios que ya están reservados para una instalación en una fecha específica.
     *
     * @param idInstalacion ID de la instalación seleccionada
     * @param fecha         Fecha para la cual se consultan las reservas
     */
    public void cargarHorariosReservados(int idInstalacion, Date fecha) {
        if (idInstalacion == 0 || fecha == null) {
            Log.e(TAG, "No se pueden cargar horarios: instalación o fecha no válidas");
            errorMessage.postValue("Instalación o fecha no válidas");
            return;
        }

        isLoading.postValue(true);
        errorMessage.postValue(null);

        MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
            @Override
            public void onSuccess(Connection connection) {
                HorarioDao horarioDao = new HorarioDao();
                horarioDao.getHorasReservadas(connection, idInstalacion, fecha, new HorarioDao.GetHorasReservadasCallback() {
                    @Override
                    public void onResult(List<String> resultado) {
                        horasReservadas.postValue(resultado);
                        isLoading.postValue(false);
                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e(TAG, "Error al cargar horarios reservados", exception);
                        errorMessage.postValue("Error al cargar horarios: " + exception.getMessage());
                        isLoading.postValue(false);
                    }
                });
            }

            @Override
            public void onFailure(SQLException exception) {
                Log.e(TAG, "Error de conexión a la base de datos", exception);
                errorMessage.postValue("Error de conexión: " + exception.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Realiza una nueva reserva de instalación para un socio.
     * Valida los parámetros, registra la reserva en la base de datos y actualiza el estado.
     *
     * @param numeroSocio   Número del socio que realiza la reserva
     * @param idInstalacion ID de la instalación a reservar
     * @param fecha         Fecha de la reserva
     * @param hora          Hora de la reserva en formato HH:MM
     */
    public void realizarReserva(String numeroSocio, int idInstalacion, Date fecha, String hora) {
        if (numeroSocio == null || numeroSocio.isEmpty() || idInstalacion <= 0 || fecha == null || hora == null || hora.isEmpty()) {
            errorMessage.setValue("Datos de reserva incompletos o inválidos");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);
        reservaRealizada.setValue(false);

        MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
            @Override
            public void onSuccess(Connection connection) {
                ReservaDao reservaDao = new ReservaDao();
                reservaDao.guardarReserva(connection, numeroSocio, idInstalacion, fecha, hora, new ReservaDao.SaveReservaCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Reserva realizada correctamente para socio: " + numeroSocio +
                                " en instalación: " + idInstalacion +
                                " fecha: " + fecha +
                                " hora: " + hora);

                        reservaRealizada.postValue(true);
                        isLoading.postValue(false);

                        cargarHorariosReservados(idInstalacion, fecha);
                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e(TAG, "Error al realizar la reserva", exception);
                        errorMessage.postValue("Error al realizar la reserva: " + exception.getMessage());
                        isLoading.postValue(false);
                    }
                });
            }

            @Override
            public void onFailure(SQLException exception) {
                Log.e(TAG, "Error de conexión a la base de datos", exception);
                errorMessage.postValue("Error de conexión: " + exception.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Reinicia el estado de la reserva después de mostrar un mensaje de confirmación.
     * Debe llamarse después de procesar la notificación de reserva exitosa.
     */
    public void resetReservaRealizada() {
        reservaRealizada.setValue(false);
    }
}