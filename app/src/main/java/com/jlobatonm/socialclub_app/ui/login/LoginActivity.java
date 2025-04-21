package com.jlobatonm.socialclub_app.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.jlobatonm.socialclub_app.R;
import com.jlobatonm.socialclub_app.database.MySQLConnection;
import com.jlobatonm.socialclub_app.database.SocioDao;
import com.jlobatonm.socialclub_app.ui.MainActivity;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Actividad que gestiona el inicio de sesión de usuarios.
 * Permite a los usuarios autenticarse mediante un correo electrónico y contraseña,
 * y mantiene la sesión activa utilizando SharedPreferences.
 * Esta actividad también maneja la pantalla de inicio de Android utilizando SplashScreen API,
 * en lugar de proporcionar una pantalla de presentación propia mediante una actividad separada.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Tag para los mensajes de registro de la actividad
     */
    private static final String TAG = "LoginActivity";

    /**
     * Objeto de acceso a datos para la entidad Socio
     */
    private SocioDao socioDao;

    /**
     * Método que se ejecuta al crear la actividad.
     * Gestiona la pantalla de inicio del sistema y configura los elementos de la interfaz
     * y el manejador para el botón de inicio de sesión.
     *
     * @param savedInstanceState Estado guardado de la actividad si está siendo recreada
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Maneja la pantalla de inicio del sistema antes de inflar el layout
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Verificar si el usuario ya está logueado antes de mostrar la interfaz de login
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);

        if (email != null) {
            // Usuario ya logueado, ir a MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        socioDao = new SocioDao();

        EditText emailEditText = findViewById(R.id.username);
        EditText passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(v -> {
            String userEmail = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
                @Override
                public void onSuccess(Connection connection) {
                    socioDao.checkSocio(connection, userEmail, password, new SocioDao.CheckSocioCallback() {
                        @Override
                        public void onResult(boolean isValid) {
                            if (isValid) {
                                // Guardar email en SharedPreferences para mantener la sesión
                                SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("email", userEmail);
                                editor.apply();

                                // Navegar a MainActivity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show());
                                Log.i(TAG, "Credenciales incorrectas");
                            }
                        }

                        @Override
                        public void onError(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
                            Log.e(TAG, "Error de verificación: " + exception.getMessage(), exception);
                        }
                    });
                }

                @Override
                public void onFailure(SQLException exception) {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Error de conexión: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
                    Log.e(TAG, "Error de conexión: " + exception.getMessage(), exception);
                }
            });
        });
    }

    /**
     * Método que se ejecuta cuando la actividad se vuelve visible al usuario.
     * Nota: La verificación de sesión activa se ha movido al método onCreate para
     * evitar transiciones innecesarias tras el splash screen.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // La verificación de sesión ahora está en onCreate para mejor experiencia de usuario
    }
}