package com.jlobatonm.socialclub_app.model;

/**
 * Clase modelo que representa una instalación del club social.
 * Contiene información sobre cada instalación, incluyendo su identificador,
 * tipo, nombre, capacidad, precios, horarios, disponibilidad e imagen.
 */
public class Instalacion {
    private int id;
    private String tipo;
    private String nombre;
    private int capacidad;
    private double precioAlquiler;
    private int duracion;
    private String horaIni;
    private String horaFin;
    private boolean disponibilidad;
    private int idEmpleado;
    private byte[] imagen;

    /**
     * Obtiene el identificador único de la instalación.
     *
     * @return Identificador numérico de la instalación.
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador único de la instalación.
     *
     * @param id Identificador numérico de la instalación.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el tipo de instalación.
     *
     * @return Cadena de texto con el tipo de instalación.
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Establece el tipo de instalación.
     *
     * @param tipo Cadena de texto con el tipo de instalación.
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * Obtiene el nombre de la instalación.
     *
     * @return Cadena de texto con el nombre de la instalación.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre de la instalación.
     *
     * @param nombre Cadena de texto con el nombre de la instalación.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene la capacidad máxima de personas de la instalación.
     *
     * @return Número entero que representa la capacidad.
     */
    public int getCapacidad() {
        return capacidad;
    }

    /**
     * Establece la capacidad máxima de personas de la instalación.
     *
     * @param capacidad Número entero que representa la capacidad.
     */
    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    /**
     * Obtiene el precio de alquiler de la instalación.
     *
     * @return Valor decimal que representa el precio de alquiler.
     */
    public double getPrecioAlquiler() {
        return precioAlquiler;
    }

    /**
     * Establece el precio de alquiler de la instalación.
     *
     * @param precioAlquiler Valor decimal que representa el precio de alquiler.
     */
    public void setPrecioAlquiler(double precioAlquiler) {
        this.precioAlquiler = precioAlquiler;
    }

    /**
     * Obtiene la duración estándar de uso de la instalación en minutos.
     *
     * @return Número entero que representa la duración en minutos.
     */
    public int getDuracion() {
        return duracion;
    }

    /**
     * Establece la duración estándar de uso de la instalación en minutos.
     *
     * @param duracion Número entero que representa la duración en minutos.
     */
    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    /**
     * Obtiene la hora de inicio de disponibilidad de la instalación.
     *
     * @return Cadena de texto que representa la hora de inicio.
     */
    public String getHoraIni() {
        return horaIni;
    }

    /**
     * Establece la hora de inicio de disponibilidad de la instalación.
     *
     * @param horaIni Cadena de texto que representa la hora de inicio.
     */
    public void setHoraIni(String horaIni) {
        this.horaIni = horaIni;
    }

    /**
     * Obtiene la hora de fin de disponibilidad de la instalación.
     *
     * @return Cadena de texto que representa la hora de fin.
     */
    public String getHoraFin() {
        return horaFin;
    }

    /**
     * Establece la hora de fin de disponibilidad de la instalación.
     *
     * @param horaFin Cadena de texto que representa la hora de fin.
     */
    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    /**
     * Verifica si la instalación está disponible para su uso.
     *
     * @return true si la instalación está disponible, false en caso contrario.
     */
    public boolean isDisponibilidad() {
        return disponibilidad;
    }

    /**
     * Establece la disponibilidad de la instalación.
     *
     * @param disponibilidad true si la instalación está disponible, false en caso contrario.
     */
    public void setDisponibilidad(boolean disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    /**
     * Obtiene el identificador del empleado responsable de la instalación.
     *
     * @return Identificador numérico del empleado.
     */
    public int getIdEmpleado() {
        return idEmpleado;
    }

    /**
     * Establece el identificador del empleado responsable de la instalación.
     *
     * @param idEmpleado Identificador numérico del empleado.
     */
    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    /**
     * Obtiene la imagen asociada a la instalación.
     *
     * @return Array de bytes que representa la imagen de la instalación.
     */
    public byte[] getImagen() {
        return imagen;
    }

    /**
     * Establece la imagen asociada a la instalación.
     *
     * @param imagen Array de bytes que representa la imagen de la instalación.
     */
    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }
}