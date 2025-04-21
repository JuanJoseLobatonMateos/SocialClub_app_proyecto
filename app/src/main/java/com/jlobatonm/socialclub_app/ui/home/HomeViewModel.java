package com.jlobatonm.socialclub_app.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jlobatonm.socialclub_app.database.EventoDao;
import com.jlobatonm.socialclub_app.database.SocioDao;
import com.jlobatonm.socialclub_app.model.Evento;
import com.jlobatonm.socialclub_app.model.Socio;

import java.sql.Connection;
import java.util.List;

/**
 * ViewModel para la pantalla principal de la aplicación.
 * Gestiona la obtención y almacenamiento de datos relacionados con eventos y socios.
 */
public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<Evento>> eventosData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Socio> socioData = new MutableLiveData<>();

    /**
     * Obtiene los datos de eventos como LiveData.
     *
     * @return LiveData con la lista de eventos
     */
    public LiveData<List<Evento>> getEventosData() {
        return eventosData;
    }

    /**
     * Obtiene los mensajes de error como LiveData.
     *
     * @return LiveData con el mensaje de error actual
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Obtiene los datos del socio como LiveData.
     *
     * @return LiveData con los datos del socio actual
     */
    public LiveData<Socio> getSocioData() {
        return socioData;
    }

    /**
     * Obtiene todos los eventos de la base de datos y actualiza el LiveData correspondiente.
     *
     * @param connection Conexión a la base de datos
     */
    public void fetchEventosData(Connection connection) {
        EventoDao eventoDao = new EventoDao();
        eventoDao.getAllEventos(connection, new EventoDao.GetAllEventosCallback() {
            @Override
            public void onResult(List<Evento> eventos) {
                eventosData.postValue(eventos);
            }

            @Override
            public void onError(Exception exception) {
                errorMessage.postValue(exception.getMessage());
            }
        });
    }

    /**
     * Obtiene los datos de un socio por su email y actualiza el LiveData correspondiente.
     *
     * @param connection Conexión a la base de datos
     * @param email      Email del socio a buscar
     */
    public void fetchSocioData(Connection connection, String email) {
        SocioDao socioDao = new SocioDao();
        socioDao.getSocioByEmail(connection, email, new SocioDao.GetSocioCallback() {
            @Override
            public void onResult(Socio socio) {
                socioData.postValue(socio);
            }

            @Override
            public void onError(Exception exception) {
                errorMessage.postValue(exception.getMessage());
            }
        });
    }

    /**
     * Establece un mensaje de error y lo publica en el LiveData correspondiente.
     *
     * @param message Mensaje de error a mostrar
     */
    public void setErrorMessage(String message) {
        errorMessage.postValue(message);
    }
}