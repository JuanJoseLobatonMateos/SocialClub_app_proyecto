package com.jlobatonm.socialclub_app.ui.mis_reservas;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.jlobatonm.socialclub_app.R;
import com.jlobatonm.socialclub_app.database.InstalacionDao;
import com.jlobatonm.socialclub_app.database.MySQLConnection;
import com.jlobatonm.socialclub_app.database.ReservaDao;
import com.jlobatonm.socialclub_app.database.SocioDao;
import com.jlobatonm.socialclub_app.databinding.CardReservaBinding;
import com.jlobatonm.socialclub_app.databinding.FragmentMisReservasBinding;
import com.jlobatonm.socialclub_app.model.Instalacion;
import com.jlobatonm.socialclub_app.model.Reserva;
import com.jlobatonm.socialclub_app.model.Socio;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragmento que muestra las reservas activas del socio actual.
 * Permite visualizar y cancelar las reservas pendientes.
 */
public class MisReservasFragment extends Fragment {

    private static final String TAG = "MisReservasFragment";
    private FragmentMisReservasBinding binding;
    private MisReservasViewModel viewModel;
    private boolean isFragmentActive = true;

    /**
     * Crea y configura la vista del fragmento.
     *
     * @param inflater           El inflador de layouts
     * @param container          El contenedor padre
     * @param savedInstanceState Estado guardado del fragmento
     * @return La vista raíz del fragmento
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMisReservasBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(MisReservasViewModel.class);

        binding.contenedorReservas.removeAllViews();

        viewModel.getReservas().observe(getViewLifecycleOwner(), reservas -> {
            if (reservas != null) {
                if (reservas.isEmpty()) {
                    mostrarMensajeSinReservas();
                } else {
                    mostrarReservas(reservas);
                }
            }
        });

        viewModel.getCargando().observe(getViewLifecycleOwner(), estaCargando -> {
            if (estaCargando) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.contenedorReservas.setVisibility(View.GONE);
                binding.tvSinReservas.setVisibility(View.GONE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty() && isAdded()) {
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                mostrarMensajeSinReservas();
            }
        });

        cargarReservas();

        return root;
    }

    /**
     * Se ejecuta cuando el fragmento se reanuda.
     * Marca el fragmento como activo.
     */
    @Override
    public void onResume() {
        super.onResume();
        isFragmentActive = true;
    }

    /**
     * Se ejecuta cuando el fragmento se pausa.
     * Marca el fragmento como inactivo.
     */
    @Override
    public void onPause() {
        super.onPause();
        isFragmentActive = false;
    }

    /**
     * Carga las reservas del socio desde la base de datos.
     * Primero obtiene el socio por email y luego sus reservas.
     */
    private void cargarReservas() {
        if (!isAdded()) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String email = prefs.getString("email", "");

        if (email.isEmpty()) {
            viewModel.setError("No se pudo identificar al usuario");
            return;
        }

        viewModel.setCargando(true);

        MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
            @Override
            public void onSuccess(Connection connection) {
                SocioDao socioDao = new SocioDao();
                socioDao.getSocioByEmail(connection, email, new SocioDao.GetSocioCallback() {
                    @Override
                    public void onResult(Socio socio) {
                        if (socio != null) {
                            String numeroSocio = socio.getNumeroSocio();
                            Log.d(TAG, "Número de socio obtenido: " + numeroSocio);

                            ReservaDao reservaDao = new ReservaDao();
                            reservaDao.getReservasBySocio(connection, numeroSocio, new ReservaDao.GetReservasBySocioCallback() {
                                @Override
                                public void onSuccess(List<Reserva> reservas) {
                                    viewModel.setReservas(reservas);
                                    viewModel.setCargando(false);
                                }

                                @Override
                                public void onError(Exception exception) {
                                    Log.e(TAG, "Error al cargar reservas: " + exception.getMessage());
                                    viewModel.setError("Error al cargar reservas: " + exception.getMessage());
                                    viewModel.setCargando(false);
                                }
                            });
                        } else {
                            Log.e(TAG, "No se encontró socio con el email: " + email);
                            viewModel.setError("No se encontró socio con el email: " + email);
                            viewModel.setCargando(false);
                        }
                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e(TAG, "Error al obtener socio: " + exception.getMessage());
                        viewModel.setError("Error al obtener socio: " + exception.getMessage());
                        viewModel.setCargando(false);
                    }
                });
            }

            @Override
            public void onFailure(SQLException exception) {
                Log.e(TAG, "Error de conexión: " + exception.getMessage());
                viewModel.setError("Error de conexión: " + exception.getMessage());
                viewModel.setCargando(false);
            }
        });
    }

    /**
     * Muestra el mensaje de que no hay reservas activas.
     */
    private void mostrarMensajeSinReservas() {
        if (!isAdded()) return;

        binding.contenedorReservas.setVisibility(View.GONE);
        binding.tvSinReservas.setVisibility(View.VISIBLE);
    }

    /**
     * Muestra las reservas activas en la interfaz de usuario.
     * Filtra las reservas pasadas y muestra solo las activas.
     *
     * @param reservas Lista completa de reservas a procesar
     */
    private void mostrarReservas(List<Reserva> reservas) {
        if (!isAdded()) return;

        binding.contenedorReservas.setVisibility(View.VISIBLE);
        binding.tvSinReservas.setVisibility(View.GONE);

        binding.contenedorReservas.removeAllViews();

        Date fechaActual = new Date();
        List<Reserva> reservasActivas = new ArrayList<>();

        for (Reserva reserva : reservas) {
            Date fechaReserva = reserva.getFecha();

            if (fechaReserva.compareTo(fechaActual) > 0) {
                reservasActivas.add(reserva);
            } else if (isSameDay(fechaReserva, fechaActual)) {
                try {
                    String horaReserva = reserva.getHora();
                    if (horaReserva != null && !horaReserva.isEmpty()) {
                        if (!horaReserva.contains(":")) {
                            try {
                                int horaInt = Integer.parseInt(horaReserva);
                                horaReserva = String.format(Locale.getDefault(), "%02d:00", horaInt);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error al procesar hora: " + e.getMessage());
                            }
                        }

                        String[] partes = horaReserva.split(":");
                        if (partes.length >= 2) {
                            int horaInt = Integer.parseInt(partes[0]);
                            int minutoInt = Integer.parseInt(partes[1]);

                            Calendar calActual = Calendar.getInstance();
                            int horaActual = calActual.get(Calendar.HOUR_OF_DAY);
                            int minutoActual = calActual.get(Calendar.MINUTE);

                            if (horaInt > horaActual || (horaInt == horaActual && minutoInt > minutoActual)) {
                                reservasActivas.add(reserva);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al procesar hora: " + e.getMessage());
                }
            }
        }

        if (reservasActivas.isEmpty()) {
            mostrarMensajeSinReservas();
            return;
        }

        reservasActivas.sort(Comparator.comparing(Reserva::getFecha));

        for (Reserva reserva : reservasActivas) {
            View cardView = crearTarjetaReserva(reserva);
            binding.contenedorReservas.addView(cardView);
        }
    }

    /**
     * Verifica si dos fechas corresponden al mismo día calendario.
     *
     * @param date1 Primera fecha a comparar
     * @param date2 Segunda fecha a comparar
     * @return true si ambas fechas son del mismo día, false en caso contrario
     */
    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Crea una tarjeta de vista para mostrar una reserva.
     *
     * @param reserva La reserva a mostrar
     * @return Una vista que representa la tarjeta de la reserva
     */
    private View crearTarjetaReserva(Reserva reserva) {
        if (!isAdded()) return null;

        CardReservaBinding cardBinding = CardReservaBinding.inflate(
                LayoutInflater.from(requireContext()), binding.contenedorReservas, false);

        int imagenResourceId = obtenerImagenSegunInstalacion(reserva.getIdInstalacion());
        cardBinding.imagenInstalacion.setImageResource(imagenResourceId);

        obtenerNombreInstalacion(reserva.getIdInstalacion(), cardBinding.nombreInstalacion);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaFormateada = dateFormat.format(reserva.getFecha());
        cardBinding.fechaReserva.setText(getString(R.string.fecha_formato, fechaFormateada));

        String hora = reserva.getHora();
        if (hora != null && !hora.isEmpty()) {
            if (!hora.contains(":") || hora.split(":").length < 2) {
                try {
                    int horaInt = Integer.parseInt(hora);
                    hora = String.format(Locale.getDefault(), "%02d:00", horaInt);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error al procesar hora: " + e.getMessage());
                }
            } else {
                String[] partes = hora.split(":");
                if (partes.length >= 2) {
                    hora = String.format(Locale.getDefault(), "%02d:%02d",
                            Integer.parseInt(partes[0]), Integer.parseInt(partes[1]));
                }
            }
        }
        cardBinding.horaReserva.setText(getString(R.string.hora_formato, hora));
        cardBinding.btnCancelarReserva.setOnClickListener(v -> mostrarDialogoCancelarReserva(reserva));

        return cardBinding.getRoot();
    }

    /**
     * Determina qué imagen mostrar según el tipo de instalación.
     *
     * @param instalacionId El ID de la instalación
     * @return El recurso de imagen a mostrar
     */
    private int obtenerImagenSegunInstalacion(int instalacionId) {
        return switch (instalacionId % 5) {
            case 0 -> R.drawable.padel;
            case 1 -> R.drawable.padel;
            case 3 -> R.drawable.futbol;
            case 4 -> R.drawable.salon;
            default -> R.drawable.logo_club_social;
        };
    }

    /**
     * Obtiene y muestra el nombre de la instalación.
     *
     * @param instalacionId El ID de la instalación
     * @param tvNombre      El TextView donde mostrar el nombre
     */
    private void obtenerNombreInstalacion(int instalacionId, final TextView tvNombre) {
        if (!isAdded()) return;

        MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
            @Override
            public void onSuccess(Connection connection) {
                InstalacionDao instalacionDao = new InstalacionDao();
                instalacionDao.getInstalacionById(connection, instalacionId, new InstalacionDao.GetInstalacionCallback() {
                    @Override
                    public void onResult(Instalacion instalacion) {
                        if (instalacion != null && isAdded() && isFragmentActive) {
                            requireActivity().runOnUiThread(() ->
                                    tvNombre.setText(instalacion.getNombre()));
                        }
                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e(TAG, "Error al obtener instalación: " + exception.getMessage());
                        if (isAdded() && isFragmentActive) {
                            requireActivity().runOnUiThread(() ->
                                    tvNombre.setText(getString(R.string.instalacion_id_format, instalacionId))
                            );
                        }
                    }
                });
            }

            @Override
            public void onFailure(SQLException exception) {
                Log.e(TAG, "Error de conexión: " + exception.getMessage());
                if (isAdded() && isFragmentActive) {
                    requireActivity().runOnUiThread(() ->
                            tvNombre.setText(getString(R.string.instalacion_id_format, instalacionId)));
                }
            }
        });
    }

    /**
     * Muestra un diálogo de confirmación para cancelar una reserva.
     *
     * @param reserva La reserva a cancelar
     */
    private void mostrarDialogoCancelarReserva(Reserva reserva) {
        if (!isAdded()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Cancelar reserva")
                .setMessage("¿Estás seguro de que deseas cancelar esta reserva?")
                .setPositiveButton("Sí", (dialog, which) -> cancelarReserva(reserva))
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Cancela una reserva en la base de datos.
     *
     * @param reserva La reserva a cancelar
     */
    private void cancelarReserva(Reserva reserva) {
        if (!isAdded()) return;

        viewModel.setCargando(true);

        ReservaDao reservaDao = new ReservaDao();

        MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
            @Override
            public void onSuccess(Connection connection) {
                reservaDao.eliminarReserva(connection, reserva.getIdReserva(), new ReservaDao.EliminarReservaCallback() {
                    @Override
                    public void onSuccess() {
                        if (isAdded() && isFragmentActive) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Reserva cancelada correctamente", Toast.LENGTH_SHORT).show();
                                cargarReservas();
                            });
                        } else {
                            viewModel.setCargando(false);
                        }
                    }

                    @Override
                    public void onError(Exception exception) {
                        viewModel.setError("Error al cancelar reserva: " + exception.getMessage());
                        viewModel.setCargando(false);
                    }
                });
            }

            @Override
            public void onFailure(SQLException exception) {
                viewModel.setError("Error de conexión: " + exception.getMessage());
                viewModel.setCargando(false);
            }
        });
    }

    /**
     * Libera recursos cuando se destruye la vista.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Libera recursos y marca el fragmento como inactivo cuando se destruye.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        isFragmentActive = false;
    }
}