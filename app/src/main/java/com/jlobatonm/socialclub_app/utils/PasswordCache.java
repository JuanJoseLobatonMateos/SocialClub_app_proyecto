package com.jlobatonm.socialclub_app.utils;

    /**
     * Caché singleton para almacenar temporalmente la contraseña del usuario en memoria.
     * <p>
     * Esta clase implementa el patrón Singleton para garantizar que solo exista una instancia
     * durante la ejecución de la aplicación. Se utiliza para mantener la contraseña del usuario
     * disponible en distintas partes de la aplicación sin necesidad de pedir al usuario que
     * la introduzca repetidamente.
     * </p>
     * <p>
     * Nota de seguridad: Esta clase debe usarse con precaución ya que almacena contraseñas en texto plano
     * en memoria. Para aplicaciones con requisitos de seguridad elevados, considere utilizar
     * soluciones más seguras como EncryptedSharedPreferences o Android Keystore.
     * </p>
     */
    public class PasswordCache {
        private static PasswordCache instance;
        private String password;

        /**
         * Constructor privado para prevenir la instanciación directa.
         * Este constructor es parte del patrón Singleton.
         */
        private PasswordCache() {
        }

        /**
         * Obtiene la instancia única de PasswordCache.
         * Si la instancia no existe, se crea una nueva.
         *
         * @return La instancia única de PasswordCache
         */
        public static synchronized PasswordCache getInstance() {
            if (instance == null) {
                instance = new PasswordCache();
            }
            return instance;
        }

        /**
         * Almacena la contraseña del usuario en la caché.
         *
         * @param password La contraseña a almacenar
         */
        public void setPassword(String password) {
            this.password = password;
        }

        /**
         * Recupera la contraseña almacenada en la caché.
         *
         * @return La contraseña almacenada, o null si no se ha establecido
         */
        public String getPassword() {
            return password;
        }
    }