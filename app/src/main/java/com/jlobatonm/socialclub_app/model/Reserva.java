package com.jlobatonm.socialclub_app.model;

    import java.util.Date;

    /**
     * Clase modelo que representa una reserva de instalación en el club social.
     * Contiene información sobre cada reserva, incluyendo su identificador, fecha,
     * hora, instalación asociada, socio que la realiza y empleado que la gestiona.
     */
    public class Reserva {
        private int idReserva;
        private Date fecha;
        private String hora;
        private int idInstalacion;
        private String numeroSocio;
        private Integer idEmpleado;

        /**
         * Obtiene el identificador único de la reserva.
         *
         * @return Identificador numérico de la reserva.
         */
        public int getIdReserva() {
            return idReserva;
        }

        /**
         * Establece el identificador único de la reserva.
         *
         * @param idReserva Identificador numérico de la reserva.
         */
        public void setIdReserva(int idReserva) {
            this.idReserva = idReserva;
        }

        /**
         * Obtiene la fecha para la que se ha realizado la reserva.
         *
         * @return Objeto Date con la fecha de la reserva.
         */
        public Date getFecha() {
            return fecha;
        }

        /**
         * Establece la fecha para la que se realiza la reserva.
         *
         * @param fecha Objeto Date con la fecha de la reserva.
         */
        public void setFecha(Date fecha) {
            this.fecha = fecha;
        }

        /**
         * Obtiene la hora para la que se ha realizado la reserva.
         *
         * @return Cadena de texto que representa la hora de la reserva.
         */
        public String getHora() {
            return hora;
        }

        /**
         * Establece la hora para la que se realiza la reserva.
         *
         * @param hora Cadena de texto que representa la hora de la reserva.
         */
        public void setHora(String hora) {
            this.hora = hora;
        }

        /**
         * Obtiene el identificador de la instalación reservada.
         *
         * @return Identificador numérico de la instalación.
         */
        public int getIdInstalacion() {
            return idInstalacion;
        }

        /**
         * Establece el identificador de la instalación a reservar.
         *
         * @param idInstalacion Identificador numérico de la instalación.
         */
        public void setIdInstalacion(int idInstalacion) {
            this.idInstalacion = idInstalacion;
        }

        /**
         * Obtiene el número identificador del socio que realiza la reserva.
         *
         * @return Cadena de texto con el número de socio.
         */
        public String getNumeroSocio() {
            return numeroSocio;
        }

        /**
         * Establece el número identificador del socio que realiza la reserva.
         *
         * @param numeroSocio Cadena de texto con el número de socio.
         */
        public void setNumeroSocio(String numeroSocio) {
            this.numeroSocio = numeroSocio;
        }

        /**
         * Obtiene el identificador del empleado que gestiona la reserva, si existe.
         *
         * @return Identificador numérico del empleado o null si no hay empleado asociado.
         */
        public Integer getIdEmpleado() {
            return idEmpleado;
        }

        /**
         * Establece el identificador del empleado que gestiona la reserva.
         *
         * @param idEmpleado Identificador numérico del empleado o null si no hay empleado asociado.
         */
        public void setIdEmpleado(Integer idEmpleado) {
            this.idEmpleado = idEmpleado;
        }
    }