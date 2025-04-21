package com.jlobatonm.socialclub_app.model;

import java.util.Date;

/**
 * Clase modelo que representa un socio del club social.
 * Contiene información completa sobre cada socio, incluyendo sus datos personales,
 * información de membresía y credenciales de acceso.
 */
public class Socio {
    private String numeroSocio;
    private int idSocio;
    private int numFamilia;
    private String nombre;
    private String apellidos;
    private String telefono;
    private String dni;
    private String email;
    private Date fechaNacimiento;
    private byte[] foto;
    private byte[] huella;
    private String titularidad;
    private boolean dentroInstalacion;
    private Date fechaAlta;
    private String password;

    /**
     * Constructor predeterminado sin parámetros.
     */
    public Socio() {
    }

    /**
     * Obtiene el número identificador único del socio.
     *
     * @return Cadena de texto con el número de socio.
     */
    public String getNumeroSocio() {
        return numeroSocio;
    }

    /**
     * Establece el número identificador único del socio.
     *
     * @param numeroSocio Cadena de texto con el número de socio.
     */
    public void setNumeroSocio(String numeroSocio) {
        this.numeroSocio = numeroSocio;
    }

    /**
     * Obtiene el identificador numérico interno del socio.
     *
     * @return Identificador numérico del socio.
     */
    public int getIdSocio() {
        return idSocio;
    }

    /**
     * Establece el identificador numérico interno del socio.
     *
     * @param idSocio Identificador numérico del socio.
     */
    public void setIdSocio(int idSocio) {
        this.idSocio = idSocio;
    }

    /**
     * Obtiene el número de familia al que pertenece el socio.
     *
     * @return Número entero que representa la familia del socio.
     */
    public int getNumFamilia() {
        return numFamilia;
    }

    /**
     * Establece el número de familia al que pertenece el socio.
     *
     * @param numFamilia Número entero que representa la familia del socio.
     */
    public void setNumFamilia(int numFamilia) {
        this.numFamilia = numFamilia;
    }

    /**
     * Obtiene el nombre del socio.
     *
     * @return Cadena de texto con el nombre del socio.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del socio.
     *
     * @param nombre Cadena de texto con el nombre del socio.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene los apellidos del socio.
     *
     * @return Cadena de texto con los apellidos del socio.
     */
    public String getApellidos() {
        return apellidos;
    }

    /**
     * Establece los apellidos del socio.
     *
     * @param apellidos Cadena de texto con los apellidos del socio.
     */
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    /**
     * Obtiene el número de teléfono del socio.
     *
     * @return Cadena de texto con el número de teléfono.
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * Establece el número de teléfono del socio.
     *
     * @param telefono Cadena de texto con el número de teléfono.
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * Obtiene el número de DNI del socio.
     *
     * @return Cadena de texto con el DNI del socio.
     */
    public String getDni() {
        return dni;
    }

    /**
     * Establece el número de DNI del socio.
     *
     * @param dni Cadena de texto con el DNI del socio.
     */
    public void setDni(String dni) {
        this.dni = dni;
    }

    /**
     * Obtiene la dirección de correo electrónico del socio.
     *
     * @return Cadena de texto con el email del socio.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece la dirección de correo electrónico del socio.
     *
     * @param email Cadena de texto con el email del socio.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene la fecha de nacimiento del socio.
     *
     * @return Objeto Date con la fecha de nacimiento.
     */
    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    /**
     * Establece la fecha de nacimiento del socio.
     *
     * @param fechaNacimiento Objeto Date con la fecha de nacimiento.
     */
    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * Obtiene la fotografía del socio.
     *
     * @return Array de bytes que representa la fotografía del socio.
     */
    public byte[] getFoto() {
        return foto;
    }

    /**
     * Establece la fotografía del socio.
     *
     * @param foto Array de bytes que representa la fotografía del socio.
     */
    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    /**
     * Obtiene los datos de la huella dactilar del socio.
     *
     * @return Array de bytes que representa la huella dactilar.
     */
    public byte[] getHuella() {
        return huella;
    }

    /**
     * Establece los datos de la huella dactilar del socio.
     *
     * @param huella Array de bytes que representa la huella dactilar.
     */
    public void setHuella(byte[] huella) {
        this.huella = huella;
    }

    /**
     * Obtiene el tipo de titularidad del socio.
     *
     * @return Cadena de texto con la titularidad del socio.
     */
    public String getTitularidad() {
        return titularidad;
    }

    /**
     * Establece el tipo de titularidad del socio.
     *
     * @param titularidad Cadena de texto con la titularidad del socio.
     */
    public void setTitularidad(String titularidad) {
        this.titularidad = titularidad;
    }

    /**
     * Verifica si el socio se encuentra actualmente dentro de las instalaciones.
     *
     * @return true si el socio está dentro de las instalaciones, false en caso contrario.
     */
    public boolean isDentroInstalacion() {
        return dentroInstalacion;
    }

    /**
     * Establece el estado de presencia del socio en las instalaciones.
     *
     * @param dentroInstalacion true si el socio está dentro de las instalaciones, false en caso contrario.
     */
    public void setDentroInstalacion(boolean dentroInstalacion) {
        this.dentroInstalacion = dentroInstalacion;
    }

    /**
     * Obtiene la fecha de alta del socio en el club.
     *
     * @return Objeto Date con la fecha de alta.
     */
    public Date getFechaAlta() {
        return fechaAlta;
    }

    /**
     * Establece la fecha de alta del socio en el club.
     *
     * @param fechaAlta Objeto Date con la fecha de alta.
     */
    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    /**
     * Obtiene la contraseña encriptada del socio.
     *
     * @return Cadena de texto con la contraseña encriptada.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contraseña del socio.
     *
     * @param password Cadena de texto con la contraseña.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}