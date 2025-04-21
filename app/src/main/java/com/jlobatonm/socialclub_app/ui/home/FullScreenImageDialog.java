package com.jlobatonm.socialclub_app.ui.home;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jlobatonm.socialclub_app.R;

import java.util.Objects;

/**
 * Diálogo de pantalla completa para mostrar una imagen en tamaño completo.
 * Muestra una imagen en un diálogo modal que ocupa toda la pantalla.
 */
public class FullScreenImageDialog extends DialogFragment {

    private static final String ARG_IMAGE = "image";

    /**
     * Crea una nueva instancia del diálogo de imagen a pantalla completa.
     *
     * @param image Bitmap de la imagen que se mostrará
     * @return Una nueva instancia del diálogo configurada con la imagen
     */
    public static FullScreenImageDialog newInstance(Bitmap image) {
        FullScreenImageDialog dialog = new FullScreenImageDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IMAGE, image);
        dialog.setArguments(args);
        return dialog;
    }

    /**
     * Crea y configura la vista del diálogo.
     *
     * @param inflater           El LayoutInflater utilizado para inflar la vista
     * @param container          El ViewGroup contenedor de la vista
     * @param savedInstanceState Estado guardado de la instancia
     * @return La vista raíz del diálogo
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_full_screen_image, container, false);
        ImageView imageView = view.findViewById(R.id.fullScreenImageView);

        if (getArguments() != null) {
            Bitmap image = getArguments().getParcelable(ARG_IMAGE);
            imageView.setImageBitmap(image);
        }

        imageView.setOnClickListener(v -> dismiss());

        return view;
    }

    /**
     * Configura el diálogo para que ocupe toda la pantalla cuando se muestra.
     * Este método se llama después de {@link #onCreateView}.
     */
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}