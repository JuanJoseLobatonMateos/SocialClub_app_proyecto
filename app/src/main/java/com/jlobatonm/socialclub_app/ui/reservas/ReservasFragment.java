package com.jlobatonm.socialclub_app.ui.reservas;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.jlobatonm.socialclub_app.R;
import com.jlobatonm.socialclub_app.database.InstalacionDao;
import com.jlobatonm.socialclub_app.database.MySQLConnection;
import com.jlobatonm.socialclub_app.database.ReservaDao;
import com.jlobatonm.socialclub_app.databinding.FragmentReservasBinding;
import com.jlobatonm.socialclub_app.model.Instalacion;
import com.jlobatonm.socialclub_app.utils.NotificationHelper;
import com.jlobatonm.socialclub_app.utils.ReservationAlarmReceiver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragmento para gestionar la reserva de instalaciones del club social.
 * <p>
 * Permite a los usuarios seleccionar una instalación, una fecha y un horario disponible
 * para realizar una reserva. Gestiona la disponibilidad en tiempo real y programa
 * notificaciones para recordar al usuario su reserva.
 * </p>
 */
public class ReservasFragment extends Fragment {

    private FragmentReservasBinding binding;
    private List<Instalacion> instalacionesList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private int selectedInstalacionId;
    private Instalacion selectedInstalacion;
    private Date selectedDate;
    private static final String TAG = "ReservasFragment";
    private ReservasViewModel reservasViewModel;
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_EMAIL = "email";

    /**
     * Crea y configura la vista del fragmento de reservas.
     *
     * @param inflater           Inflador para cargar el layout del fragmento
     * @param container          Contenedor del fragmento
     * @param savedInstanceState Estado guardado del fragmento
     * @return Vista raíz del fragmento
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        reservasViewModel = new ViewModelProvider(this).get(ReservasViewModel.class);

        binding = FragmentReservasBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Spinner spinnerInstalacion = binding.spinnerInstalacion;
        spinnerAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, new ArrayList<>()) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(Color.WHITE);
                return view;
            }
        };

        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerInstalacion.setAdapter(spinnerAdapter);

        spinnerInstalacion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.WHITE);
                }

                if (position >= 0 && position < instalacionesList.size()) {
                    selectedInstalacion = instalacionesList.get(position);
                    selectedInstalacionId = selectedInstalacion.getId();
                    Log.d(TAG, "Instalación seleccionada: " + selectedInstalacion.getNombre() + " (ID: " + selectedInstalacionId + ")");

                    if (selectedDate != null) {
                        cargarHorariosDisponibles();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedInstalacion = null;
                selectedInstalacionId = 0;
            }
        });

        CalendarView calendarView = binding.calendarView;

        Calendar calendar = Calendar.getInstance();
        long today = calendar.getTimeInMillis();
        selectedDate = calendar.getTime();
        calendarView.setMinDate(today);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            selectedDate = calendar.getTime();
            if (selectedInstalacion != null) {
                cargarHorariosDisponibles();
            }
        });

        reservasViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        reservasViewModel.getReservaRealizada().observe(getViewLifecycleOwner(), realizada -> {
            if (realizada) {
                Toast.makeText(requireContext(), "Reserva realizada con éxito", Toast.LENGTH_SHORT).show();
                reservasViewModel.resetReservaRealizada();
                cargarHorariosDisponibles();
            }
        });

        cargarInstalaciones();

        return root;
    }

    /**
     * Carga la lista de instalaciones desde la base de datos.
     * Actualiza el spinner con los nombres de las instalaciones disponibles.
     */
    private void cargarInstalaciones() {
        MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
            @Override
            public void onSuccess(Connection connection) {
                InstalacionDao instalacionDao = new InstalacionDao();
                instalacionDao.getAllInstalaciones(connection, new InstalacionDao.GetAllInstalacionesCallback() {
                    @Override
                    public void onResult(List<Instalacion> instalaciones) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                instalacionesList = instalaciones;
                                List<String> nombresInstalaciones = new ArrayList<>();
                                for (Instalacion instalacion : instalaciones) {
                                    nombresInstalaciones.add(instalacion.getNombre());
                                }
                                spinnerAdapter.clear();
                                spinnerAdapter.addAll(nombresInstalaciones);
                                spinnerAdapter.notifyDataSetChanged();

                                if (selectedInstalacionId != 0 && selectedDate != null) {
                                    cargarHorariosDisponibles();
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(Exception exception) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Error cargando instalaciones: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                                Log.e(TAG, "Error cargando instalaciones: " + exception.getMessage(), exception);
                            });
                        }
                    }
                });
            }

            @Override
            public void onFailure(SQLException exception) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error de conexión: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error de conexión: " + exception.getMessage(), exception);
                    });
                }
            }
        });
    }

    /**
     * Carga los horarios disponibles para la instalación y fecha seleccionadas.
     * Consulta la base de datos para obtener las horas ya reservadas.
     */
    private void cargarHorariosDisponibles() {
        if (selectedInstalacionId == 0 || selectedDate == null) {
            Log.d(TAG, "No se pueden cargar horarios: instalación o fecha no seleccionadas");
            return;
        }

        Log.d(TAG, "Cargando horarios para instalación ID: " + selectedInstalacionId +
                " y fecha: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate));

        if (binding != null) {
            GridLayout layoutHorarios = binding.layoutHorarios;
            requireActivity().runOnUiThread(() -> {
                layoutHorarios.removeAllViews();
                TextView loadingText = new TextView(requireContext());
                loadingText.setText(getString(R.string.loading_schedules));
                loadingText.setTextColor(Color.WHITE);
                layoutHorarios.addView(loadingText);
            });
        }

        MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
            @Override
            public void onSuccess(Connection connection) {
                ReservaDao reservaDao = new ReservaDao();
                reservaDao.obtenerHorasReservadas(connection, selectedInstalacionId, selectedDate,
                        new ReservaDao.GetHorasReservadasCallback() {
                            @Override
                            public void onResult(List<String> horasReservadas) {
                                if (isAdded()) {
                                    requireActivity().runOnUiThread(() -> {
                                        for (String hora : horasReservadas) {
                                            Log.d(TAG, "Hora reservada: '" + hora + "'");
                                        }
                                        mostrarHorarios(horasReservadas);
                                    });
                                }
                            }

                            @Override
                            public void onError(Exception exception) {
                                if (isAdded()) {
                                    requireActivity().runOnUiThread(() -> {
                                        Log.e(TAG, "Error obteniendo horas reservadas: " + exception.getMessage(), exception);
                                        Toast.makeText(getContext(), "Error cargando horarios: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                                }
                            }
                        });
            }

            @Override
            public void onFailure(SQLException exception) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error de conexión: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error de conexión: " + exception.getMessage(), exception);
                    });
                }
            }
        });
    }

    /**
     * Muestra los horarios disponibles en la interfaz de usuario basados en la lista de horas ya reservadas.
     * Genera botones para cada slot horario calculado según la configuración de la instalación.
     *
     * @param horasReservadas Lista de horas que ya han sido reservadas y no están disponibles
     */
    private void mostrarHorarios(List<String> horasReservadas) {
        if (binding == null) return;

        GridLayout layoutHorarios = binding.layoutHorarios;
        layoutHorarios.removeAllViews();

        if (selectedInstalacion == null) return;

        Log.d(TAG, "Mostrando horarios para " + selectedInstalacion.getNombre());
        Log.d(TAG, "Horas reservadas recibidas: " + horasReservadas);

        String horaIni = selectedInstalacion.getHoraIni();
        String horaFin = selectedInstalacion.getHoraFin();
        int duracion = selectedInstalacion.getDuracion();

        Log.d(TAG, "Configuración de instalación: " + selectedInstalacion.getNombre()
                + " - horaIni=" + horaIni
                + ", horaFin=" + horaFin
                + ", duracion=" + duracion + " minutos");

        try {
            int horaIniInt = convertirHoraAMinutos(horaIni);
            int horaFinInt = convertirHoraAMinutos(horaFin);
            Log.d(TAG, "Minutos: Inicio=" + horaIniInt + ", Fin=" + horaFinInt + ", Diferencia=" + (horaFinInt - horaIniInt));

            boolean esHoy = esHoyLaFechaSeleccionada();
            int horaActualMinutos = 0;

            if (esHoy) {
                Calendar cal = Calendar.getInstance();
                horaActualMinutos = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
            }

            if (horaIniInt >= horaFinInt || duracion <= 0) {
                TextView msgText = new TextView(requireContext());
                msgText.setText(getString(R.string.no_schedule_configuration));
                msgText.setTextColor(Color.WHITE);
                layoutHorarios.addView(msgText);
                return;
            }

            List<String> horariosDisponibles = new ArrayList<>();

            if (duracion >= (horaFinInt - horaIniInt)) {
                horariosDisponibles.add(convertirMinutosAHora(horaIniInt));
            } else {
                int numHorarios = (horaFinInt - horaIniInt) / duracion;

                for (int i = 0; i < numHorarios; i++) {
                    int minutos = horaIniInt + (i * duracion);
                    if (minutos + duracion <= horaFinInt) {
                        horariosDisponibles.add(convertirMinutosAHora(minutos));
                    }
                }
            }

            Log.d(TAG, "Total horarios generados: " + horariosDisponibles.size() + " - " + horariosDisponibles);

            int totalHorarios = horariosDisponibles.size();
            int columnCount = Math.min(3, Math.max(1, totalHorarios));
            layoutHorarios.setColumnCount(columnCount);

            int column = 0;
            int row = 0;

            for (String horaInicio : horariosDisponibles) {
                Button button = new Button(requireContext());
                button.setText(horaInicio);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                params.setMargins(8, 8, 8, 8);
                params.columnSpec = GridLayout.spec(column, 1, 1f);
                params.rowSpec = GridLayout.spec(row);
                button.setLayoutParams(params);

                boolean estaReservada = horasReservadas.contains(horaInicio);
                boolean yaPaso = esHoy && convertirHoraAMinutos(horaInicio) < horaActualMinutos;

                if (estaReservada || yaPaso) {
                    button.setEnabled(false);
                    button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.rounded_button_disabled, null));
                    button.setTextColor(Color.DKGRAY);
                } else {
                    button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.rounded_button, null));
                    button.setTextColor(Color.WHITE);
                    button.setOnClickListener(v -> seleccionarHorario(horaInicio));
                }

                layoutHorarios.addView(button);

                column++;
                if (column >= columnCount) {
                    column = 0;
                    row++;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar horarios: " + e.getMessage(), e);
            TextView errorText = new TextView(requireContext());
            errorText.setText(getString(R.string.error_show_schedules, e.getMessage()));
            errorText.setTextColor(Color.WHITE);
            layoutHorarios.addView(errorText);
        }
    }

    /**
     * Convierte una hora en formato HH:MM a minutos desde el inicio del día.
     *
     * @param hora Hora en formato HH:MM
     * @return Total de minutos desde el inicio del día
     */
    private int convertirHoraAMinutos(String hora) {
        try {
            if (hora == null || hora.isEmpty()) {
                Log.e(TAG, "La hora está vacía o es null");
                return 0;
            }

            Log.d(TAG, "Convirtiendo hora a minutos: " + hora);

            String[] partes = hora.split(":");
            if (partes.length < 2) {
                Log.e(TAG, "Formato de hora inválido: " + hora);
                return 0;
            }

            int horas = Integer.parseInt(partes[0]);
            int minutos = Integer.parseInt(partes[1]);
            return horas * 60 + minutos;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error al convertir hora a minutos: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Convierte minutos desde el inicio del día a formato de hora HH:MM.
     *
     * @param minutos Total de minutos desde el inicio del día
     * @return Hora en formato HH:MM
     */
    private String convertirMinutosAHora(int minutos) {
        int horas = minutos / 60;
        int mins = minutos % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", horas, mins);
    }

    /**
     * Verifica si la fecha seleccionada corresponde al día actual.
     *
     * @return true si la fecha seleccionada es hoy, false en caso contrario
     */
    private boolean esHoyLaFechaSeleccionada() {
        Calendar hoy = Calendar.getInstance();
        Calendar fechaSeleccionada = Calendar.getInstance();
        fechaSeleccionada.setTime(selectedDate);

        return hoy.get(Calendar.YEAR) == fechaSeleccionada.get(Calendar.YEAR) &&
                hoy.get(Calendar.MONTH) == fechaSeleccionada.get(Calendar.MONTH) &&
                hoy.get(Calendar.DAY_OF_MONTH) == fechaSeleccionada.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Verifica la disponibilidad en tiempo real del horario seleccionado
     * y muestra un diálogo de confirmación para realizar la reserva.
     *
     * @param hora Hora seleccionada en formato HH:MM
     */
    private void seleccionarHorario(String hora) {
        MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
            @Override
            public void onSuccess(Connection connection) {
                ReservaDao reservaDao = new ReservaDao();
                reservaDao.obtenerHorasReservadas(connection, selectedInstalacionId, selectedDate,
                        new ReservaDao.GetHorasReservadasCallback() {
                            @Override
                            public void onResult(List<String> horasReservadas) {
                                if (isAdded()) {
                                    requireActivity().runOnUiThread(() -> {
                                        if (horasReservadas.contains(hora)) {
                                            mostrarAlerta("Hora no disponible",
                                                    "Esta hora ya ha sido reservada. Por favor seleccione otra hora.");
                                            cargarHorariosDisponibles();
                                        } else {
                                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
                                            builder.setTitle("Confirmar Reserva");
                                            builder.setMessage("¿Desea reservar la instalación " + selectedInstalacion.getNombre() + " el " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate) + " a las " + hora + "?");

                                            builder.setPositiveButton("Confirmar", (dialog, which) -> {
                                                String email = obtenerEmailUsuario();
                                                if (email != null && !email.isEmpty()) {
                                                    buscarNumeroSocioPorEmail(email, numeroSocio -> {
                                                        if (numeroSocio != null) {
                                                            realizarReserva(numeroSocio, hora);
                                                        } else {
                                                            mostrarAlerta("Error", "No se encontró información de socio para el usuario actual.");
                                                        }
                                                    });
                                                } else {
                                                    mostrarAlerta("Error", "No se pudo identificar al usuario. Por favor inicie sesión nuevamente.");
                                                }
                                            });

                                            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

                                            builder.create().show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onError(Exception exception) {
                                if (isAdded()) {
                                    requireActivity().runOnUiThread(() -> mostrarAlerta("Error", "No se pudo verificar la disponibilidad: " + exception.getMessage()));
                                }
                            }
                        });
            }

            @Override
            public void onFailure(SQLException exception) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> mostrarAlerta("Error de conexión", exception.getMessage()));
                }
            }
        });
    }

    /**
     * Obtiene el email del usuario actualmente logueado desde SharedPreferences.
     *
     * @return Email del usuario o cadena vacía si no está disponible
     */
    private String obtenerEmailUsuario() {
        SharedPreferences preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_EMAIL, "");
    }

    /**
     * Interfaz para gestionar el callback de la búsqueda de socio.
     */
    interface BuscarSocioCallback {
        void onResult(String numeroSocio);
    }

    /**
     * Busca el número de socio asociado a un email en la base de datos.
     *
     * @param email    Email del socio a buscar
     * @param callback Callback para recibir el resultado de la búsqueda
     */
    private void buscarNumeroSocioPorEmail(String email, BuscarSocioCallback callback) {
        MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
            @Override
            public void onSuccess(Connection connection) {
                new Thread(() -> {
                    try {
                        String query = "SELECT numero_socio FROM socio WHERE email = ?";
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setString(1, email);
                        ResultSet resultSet = statement.executeQuery();

                        String numeroSocio = null;
                        if (resultSet.next()) {
                            numeroSocio = resultSet.getString("numero_socio");
                        }
                        resultSet.close();
                        statement.close();

                        String finalNumeroSocio = numeroSocio;
                        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                        mainHandler.post(() -> callback.onResult(finalNumeroSocio));

                    } catch (SQLException e) {
                        Log.e(TAG, "Error buscando número de socio: " + e.getMessage(), e);
                        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                        mainHandler.post(() -> callback.onResult(null));
                    }
                }).start();
            }

            @Override
            public void onFailure(SQLException exception) {
                Log.e(TAG, "Error de conexión: " + exception.getMessage(), exception);
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onResult(null));
            }
        });
    }

    /**
     * Realiza una reserva en la base de datos después de verificar nuevamente la disponibilidad.
     * También programa una notificación de recordatorio para la reserva.
     *
     * @param numeroSocio Número del socio que realiza la reserva
     * @param hora        Hora seleccionada para la reserva
     */
    private void realizarReserva(String numeroSocio, String hora) {
        Log.d(TAG, "Realizando reserva con número de socio: " + numeroSocio);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Procesando");
        builder.setMessage("Verificando disponibilidad y realizando la reserva...");
        builder.setCancelable(false);

        android.widget.ProgressBar progressBar = new android.widget.ProgressBar(requireContext());
        progressBar.setIndeterminate(true);
        builder.setView(progressBar);

        android.app.AlertDialog progressDialog = builder.create();
        progressDialog.show();

        MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
            @Override
            public void onSuccess(Connection connection) {
                ReservaDao reservaDao = new ReservaDao();
                reservaDao.obtenerHorasReservadas(connection, selectedInstalacionId, selectedDate,
                        new ReservaDao.GetHorasReservadasCallback() {
                            @Override
                            public void onResult(List<String> horasReservadas) {
                                if (isAdded()) {
                                    if (horasReservadas.contains(hora)) {
                                        progressDialog.dismiss();
                                        mostrarAlerta("Hora no disponible", "Esta hora ya ha sido reservada por otro socio. Por favor, seleccione otra hora.");
                                        cargarHorariosDisponibles();
                                    } else {
                                        reservasViewModel.realizarReserva(numeroSocio, selectedInstalacionId, selectedDate, hora);
                                        programarNotificacionReserva(hora);
                                        progressDialog.dismiss();
                                    }
                                }
                            }

                            @Override
                            public void onError(Exception exception) {
                                if (isAdded()) {
                                    progressDialog.dismiss();
                                    mostrarAlerta("Error", "Error al verificar disponibilidad: " + exception.getMessage());
                                }
                            }
                        });
            }

            @Override
            public void onFailure(SQLException exception) {
                progressDialog.dismiss();
                mostrarAlerta("Error de conexión", exception.getMessage());
            }
        });
    }

    /**
     * Programa una notificación para recordar al usuario su reserva una hora antes del horario seleccionado.
     *
     * @param hora Hora de la reserva en formato HH:MM
     */
    private void programarNotificacionReserva(String hora) {
        try {
            NotificationHelper.createNotificationChannel(requireContext());

            Calendar fechaReserva = Calendar.getInstance();
            fechaReserva.setTime(selectedDate);

            String[] partes = hora.split(":");
            int horas = Integer.parseInt(partes[0]);
            int minutos = Integer.parseInt(partes[1]);

            fechaReserva.set(Calendar.HOUR_OF_DAY, horas);
            fechaReserva.set(Calendar.MINUTE, minutos);
            fechaReserva.set(Calendar.SECOND, 0);

            fechaReserva.add(Calendar.HOUR_OF_DAY, -1);

            Calendar ahora = Calendar.getInstance();
            if (fechaReserva.before(ahora)) {
                Log.d(TAG, "La hora de notificación ya ha pasado, no se programará");
                return;
            }

            int notificationId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

            Intent intent = ReservationAlarmReceiver.createIntent(
                    requireContext(),
                    notificationId,
                    selectedInstalacionId,
                    hora,
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate));

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    requireContext(),
                    notificationId,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE);

            android.app.AlarmManager alarmManager =
                    (android.app.AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                if (alarmManager.canScheduleExactAlarms()) {
                    programarAlarmaExacta(alarmManager, fechaReserva, pendingIntent);
                } else {
                    mostrarDialogoPermisoAlarma();
                    alarmManager.set(
                            android.app.AlarmManager.RTC_WAKEUP,
                            fechaReserva.getTimeInMillis(),
                            pendingIntent);
                }

                Log.d(TAG, "Notificación programada para: " + fechaReserva.getTime());

                Toast.makeText(requireContext(),
                        "Se te notificará una hora antes de tu reserva",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException se) {
            Log.e(TAG, "Error de permisos al programar alarma exacta: " + se.getMessage(), se);
            Toast.makeText(requireContext(),
                    "No tienes permisos para programar alarmas exactas",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error al programar notificación: " + e.getMessage(), e);
        }
    }

    /**
     * Programa una alarma exacta para la notificación de reserva.
     * Utiliza el método setExactAndAllowWhileIdle para asegurar que la notificación
     * se muestre incluso si el dispositivo está en modo de ahorro de energía.
     *
     * @param alarmManager  Gestor de alarmas del sistema
     * @param fechaReserva  Fecha y hora programada para la notificación
     * @param pendingIntent Intent pendiente con la información de la reserva
     */
    private void programarAlarmaExacta(android.app.AlarmManager alarmManager,
                                       Calendar fechaReserva,
                                       PendingIntent pendingIntent) {
        alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                fechaReserva.getTimeInMillis(),
                pendingIntent);
    }

    /**
     * Muestra un diálogo para solicitar al usuario que conceda permisos para programar alarmas exactas.
     * El diálogo ofrece la opción de ir directamente a la configuración del sistema para activar el permiso.
     */
    private void mostrarDialogoPermisoAlarma() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Permiso necesario")
                .setMessage("Para recibir notificaciones antes de tu reserva, necesitamos permiso para programar alarmas exactas. Por favor, habilita esta opción en la configuración.")
                .setPositiveButton("Ir a Configuración", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Muestra un diálogo de alerta con un título, mensaje y botón de aceptar.
     * Verifica que el fragmento esté adjunto a una actividad antes de mostrar el diálogo.
     *
     * @param titulo  Título del diálogo de alerta
     * @param mensaje Mensaje a mostrar en el diálogo
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        if (isAdded()) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
            builder.setTitle(titulo)
                    .setMessage(mensaje)
                    .setPositiveButton("Aceptar", null)
                    .show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}