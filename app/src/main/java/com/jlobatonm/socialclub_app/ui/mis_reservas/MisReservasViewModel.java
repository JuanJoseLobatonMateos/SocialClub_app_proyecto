package com.jlobatonm.socialclub_app.ui.mis_reservas;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jlobatonm.socialclub_app.model.Reserva;

import java.util.List;

/**
 * ViewModel que gestiona los datos relacionados con las reservas del usuario.
 * Proporciona LiveData para observar las reservas, estados de carga y errores.
 */
public class MisReservasViewModel extends ViewModel {

    private final MutableLiveData<List<Reserva>> reservas;
    private final MutableLiveData<Boolean> cargando;
    private final MutableLiveData<String> error;

    /**
     * Constructor que inicializa los LiveData para reservas, estado de carga y errores.
     */
    public MisReservasViewModel() {
        reservas = new MutableLiveData<>();
        cargando = new MutableLiveData<>(false);
        error = new MutableLiveData<>();
    }

    /**
     * Obtiene las reservas como LiveData para observación.
     *
     * @return LiveData con la lista de reservas
     */
    public LiveData<List<Reserva>> getReservas() {
        return reservas;
    }

    /**
     * Obtiene el estado de carga como LiveData para observación.
     *
     * @return LiveData con el estado de carga (true si está cargando)
     */
    public LiveData<Boolean> getCargando() {
        return cargando;
    }

    /**
     * Obtiene los mensajes de error como LiveData para observación.
     *
     * @return LiveData con el mensaje de error
     */
    public LiveData<String> getError() {
        return error;
    }

    /**
     * Establece la lista de reservas.
     *
     * @param listaReservas Lista de reservas del usuario
     */
    public void setReservas(List<Reserva> listaReservas) {
        reservas.setValue(listaReservas);
    }

    /**
     * Establece el estado de carga.
     *
     * @param estaCargando true si está en proceso de carga, false si no
     */
    public void setCargando(boolean estaCargando) {
        cargando.setValue(estaCargando);
    }

    /**
     * Establece un mensaje de error.
     *
     * @param mensaje El mensaje de error a mostrar
     */
    public void setError(String mensaje) {
        error.setValue(mensaje);
    }
}