package com.jlobatonm.socialclub_app.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.jlobatonm.socialclub_app.R;
import com.jlobatonm.socialclub_app.database.MySQLConnection;
import com.jlobatonm.socialclub_app.databinding.FragmentHomeBinding;
import com.jlobatonm.socialclub_app.model.Evento;
import com.jlobatonm.socialclub_app.model.Socio;
import com.jlobatonm.socialclub_app.ui.login.LoginActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragmento que muestra la pantalla principal de la aplicación.
 * Contiene la información del socio, su carnet y los eventos próximos.
 */
public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private static final String TAG = "HomeFragment";

    /**
     * Crea la vista del fragmento y configura los elementos de la UI.
     *
     * @param inflater           El LayoutInflater utilizado para inflar la vista
     * @param container          El contenedor donde se va a añadir la vista del fragmento
     * @param savedInstanceState Estado guardado de la instancia
     * @return La vista root del fragmento
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textNumeroSocio = binding.textNumeroSocio;
        final TextView textNombre = binding.textNombre;
        final TextView textFechaNacimiento = binding.textFechaNacimiento;
        final TextView textFecha = binding.textFecha;
        final ImageView imageViewFoto = binding.imageViewFoto;
        final ImageView imageViewQrCode = binding.imageViewQrCode;

        binding.carnet.setOnClickListener(v -> {
            Socio socio = homeViewModel.getSocioData().getValue();
            if (socio != null) {
                DialogFragment dialog = FullScreenCarnetDialog.newInstance(socio);
                dialog.show(getParentFragmentManager(), "FullScreenCarnetDialog");
            }
        });

        homeViewModel.getSocioData().observe(getViewLifecycleOwner(), socio -> {
            if (socio != null) {
                textNumeroSocio.setText(String.valueOf(socio.getNumeroSocio()));
                String fullName = socio.getNombre() + " " + socio.getApellidos();
                textNombre.setText(fullName);
                String categoriaEdad = getCategoriaEdad(socio.getFechaNacimiento());
                textFechaNacimiento.setText(categoriaEdad);

                byte[] fotoBlob = socio.getFoto();
                if (fotoBlob != null && fotoBlob.length > 0) {
                    Log.d(TAG, "Foto BLOB length: " + fotoBlob.length);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(fotoBlob, 0, fotoBlob.length);
                    if (bitmap != null) {
                        imageViewFoto.setImageBitmap(bitmap);
                        Log.d(TAG, "Bitmap set successfully");
                    } else {
                        Log.d(TAG, "Failed to decode bitmap");
                        saveBlobToFile(fotoBlob);
                    }
                } else {
                    Log.d(TAG, "Foto BLOB is null or empty");
                }

                if (socio.getNumeroSocio() != null && !socio.getNumeroSocio().isEmpty()) {
                    try {
                        Bitmap qrCodeBitmap = generateQRCode(socio.getNumeroSocio());
                        imageViewQrCode.setImageBitmap(qrCodeBitmap);
                    } catch (WriterException e) {
                        Log.e(TAG, "Error generating QR code", e);
                    }
                }

                AppCompatActivity activity = (AppCompatActivity) getActivity();
                if (activity != null) {
                    Toolbar toolbar = activity.findViewById(R.id.toolbar);
                    activity.setSupportActionBar(toolbar);
                    if (activity.getSupportActionBar() != null) {
                        activity.getSupportActionBar().setTitle(fullName);
                        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    }
                }
            }
        });

        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        homeViewModel.getEventosData().observe(getViewLifecycleOwner(), eventos -> {
            LinearLayout eventosLayout = root.findViewById(R.id.eventosLinearLayout);
            eventosLayout.removeAllViews();

            if (eventos == null || eventos.isEmpty() || !hayEventosFuturos(eventos)) {
                TextView noEventosTextView = new TextView(getContext());
                noEventosTextView.setText(R.string.no_hay_eventos);
                noEventosTextView.setTextSize(18);
                noEventosTextView.setTextColor(getResources().getColor(android.R.color.white, null));
                noEventosTextView.setGravity(android.view.Gravity.CENTER);
                noEventosTextView.setPadding(0, 32, 0, 0);

                eventosLayout.addView(noEventosTextView);
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
                Date today = new Date();

                for (Evento evento : eventos) {
                    if (evento.getFecha().compareTo(today) >= 0) {
                        View eventoView = LayoutInflater.from(getContext()).inflate(R.layout.item_evento, eventosLayout, false);

                        TextView textEventoNombre = eventoView.findViewById(R.id.textEventoNombre);
                        TextView textEventoFecha = eventoView.findViewById(R.id.textEventoFecha);
                        ImageView imageViewEvento = eventoView.findViewById(R.id.imageViewEvento);

                        textEventoNombre.setText(evento.getNombre());
                        String formattedDate = dateFormat.format(evento.getFecha());
                        textEventoFecha.setText(formattedDate);

                        byte[] imagenBlob = evento.getImagen();
                        if (imagenBlob != null && imagenBlob.length > 0) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenBlob, 0, imagenBlob.length);
                            if (bitmap != null) {
                                imageViewEvento.setImageBitmap(bitmap);

                                final Bitmap finalBitmap = bitmap;
                                eventoView.setOnClickListener(v -> {
                                    DialogFragment dialog = FullScreenImageDialog.newInstance(finalBitmap);
                                    dialog.show(getParentFragmentManager(), "FullScreenImageDialog");
                                });
                            } else {
                                Log.d(TAG, "Failed to decode bitmap");
                            }
                        } else {
                            Log.d(TAG, "Imagen BLOB is null or empty");
                        }

                        eventosLayout.addView(eventoView);
                    }
                }
            }
        });

        MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
            @Override
            public void onSuccess(Connection connection) {
                String userEmail = getUserEmail();
                homeViewModel.fetchSocioData(connection, userEmail);
                homeViewModel.fetchEventosData(connection);
            }

            private String getUserEmail() {
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                return sharedPreferences.getString("email", "");
            }

            @Override
            public void onFailure(SQLException exception) {
                homeViewModel.setErrorMessage(exception.getMessage());
            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        String formattedDate = dateFormat.format(new Date());
        textFecha.setText(formattedDate);

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_home, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_logout) {
                    logout();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        return root;
    }

    /**
     * Cierra la sesión del usuario actual.
     * Muestra un diálogo de confirmación y, si el usuario confirma,
     * borra los datos guardados y redirige a la pantalla de login.
     */
    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Genera un código QR a partir del texto proporcionado.
     *
     * @param content Texto a codificar en el QR
     * @return Bitmap con el código QR generado
     * @throws WriterException Si hay error en la generación del código
     */
    private Bitmap generateQRCode(String content) throws WriterException {
        final int QR_WIDTH = 300;
        final int QR_HEIGHT = 300;
        final int WHITE = 0xFFFFFFFF;
        final int BLACK = 0xFF000000;

        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return bitmap;
    }

    /**
     * Guarda los datos del BLOB en un archivo para diagnóstico.
     * Se utiliza cuando hay problemas al decodificar la imagen.
     *
     * @param blob Datos binarios a guardar en el archivo
     */
    private void saveBlobToFile(byte[] blob) {
        File file = new File(requireContext().getExternalFilesDir(null), "socio_foto_blob");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(blob);
            Log.d(TAG, "BLOB saved to file: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Failed to save BLOB to file", e);
        }
    }

    /**
     * Limpia los recursos cuando se destruye la vista.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Determina la categoría de edad del socio basado en su fecha de nacimiento.
     *
     * @param fechaNacimiento Fecha de nacimiento del socio
     * @return Categoría correspondiente a la edad (Bebe, Infantil, Juvenil, Adulto)
     */
    private String getCategoriaEdad(Date fechaNacimiento) {
        int edad = calcularEdad(fechaNacimiento);
        if (edad <= 2) {
            return "Bebe";
        } else if (edad <= 12) {
            return "Infantil";
        } else if (edad <= 18) {
            return "Juvenil";
        } else {
            return "Adulto";
        }
    }

    /**
     * Calcula la edad en años a partir de la fecha de nacimiento.
     *
     * @param fechaNacimiento Fecha de nacimiento
     * @return Edad en años
     */
    private int calcularEdad(Date fechaNacimiento) {
        Calendar nacimiento = Calendar.getInstance();
        nacimiento.setTime(fechaNacimiento);
        Calendar hoy = Calendar.getInstance();
        int edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR);
        if (hoy.get(Calendar.DAY_OF_YEAR) < nacimiento.get(Calendar.DAY_OF_YEAR)) {
            edad--;
        }
        return edad;
    }

    /**
     * Verifica si hay eventos futuros en la lista de eventos.
     *
     * @param eventos Lista de eventos a verificar
     * @return true si hay al menos un evento futuro, false en caso contrario
     */
    private boolean hayEventosFuturos(List<Evento> eventos) {
        Date today = new Date();
        for (Evento evento : eventos) {
            if (evento.getFecha().compareTo(today) >= 0) {
                return true;
            }
        }
        return false;
    }
}