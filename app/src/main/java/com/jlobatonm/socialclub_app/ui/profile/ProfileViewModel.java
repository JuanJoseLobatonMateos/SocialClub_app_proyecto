package com.jlobatonm.socialclub_app.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jlobatonm.socialclub_app.model.Socio;

/**
 * ViewModel para la pantalla de perfil.
 * Gestiona los datos del socio mostrado y los mensajes de error.
 */
public class ProfileViewModel extends ViewModel {
    private final MutableLiveData<Socio> socioData = new MutableLiveData<>();

    /**
     * Obtiene los datos del socio actual como LiveData.
     *
     * @return LiveData con los datos del socio
     */
    public LiveData<Socio> getSocioData() {
        return socioData;
    }

    /**
     * Actualiza los datos del socio en el ViewModel.
     *
     * @param socio Objeto Socio con los datos actualizados
     */
    public void setSocioData(Socio socio) {
        socioData.setValue(socio);
    }

}