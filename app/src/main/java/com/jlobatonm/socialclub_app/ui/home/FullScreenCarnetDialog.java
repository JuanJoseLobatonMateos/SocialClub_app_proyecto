package com.jlobatonm.socialclub_app.ui.home;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.jlobatonm.socialclub_app.R;
import com.jlobatonm.socialclub_app.model.Socio;
import com.jlobatonm.socialclub_app.databinding.DialogFullScreenCarnetBinding;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Diálogo de pantalla completa que muestra el carnet de socio con información
 * personal, foto y código QR para identificación.
 */
public class FullScreenCarnetDialog extends DialogFragment {

    private DialogFullScreenCarnetBinding binding;
    private static final String ARG_SOCIO_NOMBRE = "socio_nombre";
    private static final String ARG_SOCIO_APELLIDOS = "socio_apellidos";
    private static final String ARG_SOCIO_NUMERO = "socio_numero";
    private static final String ARG_SOCIO_FECHA_NAC = "socio_fecha_nac";
    private static final String ARG_SOCIO_FOTO = "socio_foto";

    private static final int QR_WIDTH = 300;
    private static final int QR_HEIGHT = 300;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    /**
     * Crea una nueva instancia del diálogo con los datos del socio.
     *
     * @param socio Objeto Socio con la información a mostrar
     * @return Nueva instancia del diálogo configurada
     */
    public static FullScreenCarnetDialog newInstance(Socio socio) {
        FullScreenCarnetDialog fragment = new FullScreenCarnetDialog();
        Bundle args = new Bundle();
        args.putString(ARG_SOCIO_NOMBRE, socio.getNombre());
        args.putString(ARG_SOCIO_APELLIDOS, socio.getApellidos());
        args.putString(ARG_SOCIO_NUMERO, socio.getNumeroSocio());

        if (socio.getFechaNacimiento() != null) {
            args.putLong(ARG_SOCIO_FECHA_NAC, socio.getFechaNacimiento().getTime());
        }

        if (socio.getFoto() != null) {
            args.putByteArray(ARG_SOCIO_FOTO, socio.getFoto());
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogFullScreenCarnetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            String nombre = args.getString(ARG_SOCIO_NOMBRE, "");
            String apellidos = args.getString(ARG_SOCIO_APELLIDOS, "");
            String numeroSocio = args.getString(ARG_SOCIO_NUMERO, "");

            binding.textNombre.setText(getString(R.string.nombre_completo_format, nombre, apellidos));
            binding.textNumeroSocio.setText(numeroSocio);

            if (args.containsKey(ARG_SOCIO_FECHA_NAC)) {
                long timeInMillis = args.getLong(ARG_SOCIO_FECHA_NAC);
                Date fechaNacimiento = new Date(timeInMillis);
                String categoriaEdad = getCategoriaEdad(fechaNacimiento);
                binding.textFechaNacimiento.setText(categoriaEdad);
            }

            byte[] fotoBytes = args.getByteArray(ARG_SOCIO_FOTO);
            if (fotoBytes != null && fotoBytes.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(fotoBytes, 0, fotoBytes.length);
                if (bitmap != null) {
                    binding.imageViewFoto.setImageBitmap(bitmap);
                }
            }

            if (numeroSocio != null && !numeroSocio.isEmpty()) {
                try {
                    Bitmap qrCodeBitmap = generateQRCode(numeroSocio);
                    binding.imageViewQrCode.setImageBitmap(qrCodeBitmap);
                } catch (WriterException e) {
                    Log.e("FullScreenCarnetDialog", "Error al generar código QR", e);
                }
            }
        }

        view.setOnClickListener(v -> dismiss());
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
     * Genera un código QR a partir del texto proporcionado.
     *
     * @param content Texto a codificar en el QR
     * @return Bitmap con el código QR generado
     * @throws WriterException Si hay error en la generación del código
     */
    private Bitmap generateQRCode(String content) throws WriterException {
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

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            dialog.getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = dialog.getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }

            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        int originalOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        requireActivity().setRequestedOrientation(originalOrientation);
        binding = null;
    }
}