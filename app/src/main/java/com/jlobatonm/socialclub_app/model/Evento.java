package com.jlobatonm.socialclub_app.model;

import java.util.Date;

/**
 * Clase modelo que representa un evento del club social.
 * Contiene información básica sobre cada evento, incluyendo su identificador,
 * nombre, imagen asociada y fecha de realización.
 */
public class Evento {
    private int idEvento;
    private String nombre;
    private byte[] imagen;
    private Date fecha;

    /**
     * Constructor predeterminado sin parámetros.
     */
    public Evento() {}

    /**
     * Obtiene el identificador único del evento.
     *
     * @return Identificador numérico del evento.
     */
    public int getIdEvento() {
        return idEvento;
    }

    /**
     * Establece el identificador único del evento.
     *
     * @param idEvento Identificador numérico del evento.
     */
    public void setIdEvento(int idEvento) {
        this.idEvento = idEvento;
    }

    /**
     * Obtiene el nombre o título del evento.
     *
     * @return Cadena de texto con el nombre del evento.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre o título del evento.
     *
     * @param nombre Cadena de texto con el nombre del evento.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene la imagen asociada al evento.
     *
     * @return Array de bytes que representa la imagen del evento.
     */
    public byte[] getImagen() {
        return imagen;
    }

    /**
     * Establece la imagen asociada al evento.
     *
     * @param imagen Array de bytes que representa la imagen del evento.
     */
    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    /**
     * Obtiene la fecha de realización del evento.
     *
     * @return Objeto Date con la fecha del evento.
     */
    public Date getFecha() {
        return fecha;
    }

    /**
     * Establece la fecha de realización del evento.
     *
     * @param fecha Objeto Date con la fecha del evento.
     */
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}