package com.jlobatonm.socialclub_app.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jlobatonm.socialclub_app.R;
import com.jlobatonm.socialclub_app.services.EventoService;

/**
 * Actividad principal que sirve como punto de entrada a la aplicación del club social.
 * <p>
 * Esta actividad implementa la navegación inferior para acceder a las diferentes secciones
 * de la aplicación, incluyendo reservas, perfil de usuario y gestión de reservas existentes.
 * También se encarga de solicitar y gestionar los permisos necesarios para las notificaciones.
 * </p>
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Código de solicitud para el permiso de notificaciones.
     */
    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    /**
     * Lanzador para solicitar el permiso de notificaciones de manera moderna.
     * Utiliza la API ActivityResultContracts para manejar la respuesta del usuario
     * a la solicitud de permiso.
     */
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Las notificaciones de reservas no funcionarán sin este permiso", Toast.LENGTH_LONG).show();
                }
            });

    /**
     * Método llamado cuando se crea la actividad.
     * <p>
     * Configura la interfaz de usuario, inicializa la barra de herramientas,
     * verifica los permisos de notificación, inicia el servicio de eventos
     * y configura la navegación inferior.
     * </p>
     *
     * @param savedInstanceState Si la actividad está siendo recreada después de
     *                           haber sido previamente destruida, contiene los datos
     *                           que estaban en el Bundle anteriormente guardado.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        checkNotificationPermission();

        Intent serviceIntent = new Intent(this, EventoService.class);
        startService(serviceIntent);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_mis_reservas, R.id.navigation_profile, R.id.navigation_reservas)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    /**
     * Verifica y solicita el permiso de notificaciones si es necesario.
     * <p>
     * En Android 13 (TIRAMISU) y superior, se requiere un permiso explícito
     * para mostrar notificaciones. Este método verifica si el permiso ya ha sido
     * concedido y, de lo contrario, lo solicita utilizando el ActivityResultLauncher.
     * </p>
     */
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    /**
     * Método llamado cuando el usuario responde a una solicitud de permiso.
     * <p>
     * Procesa el resultado de la solicitud de permisos y muestra un mensaje
     * apropiado según la respuesta del usuario. Este método se utiliza como respaldo
     * para la gestión de permisos en caso de que se use el método tradicional.
     * </p>
     *
     * @param requestCode  Código de solicitud pasado a requestPermissions()
     * @param permissions  Los permisos solicitados
     * @param grantResults Los resultados de la solicitud (PERMISSION_GRANTED o PERMISSION_DENIED)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Las notificaciones de reservas no funcionarán sin este permiso", Toast.LENGTH_LONG).show();
            }
        }
    }
}