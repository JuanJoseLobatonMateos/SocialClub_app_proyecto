package com.jlobatonm.socialclub_app.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.jlobatonm.socialclub_app.R;
import com.jlobatonm.socialclub_app.database.MySQLConnection;
import com.jlobatonm.socialclub_app.database.SocioDao;
import com.jlobatonm.socialclub_app.databinding.FragmentProfileBinding;
import com.jlobatonm.socialclub_app.model.Socio;
import com.jlobatonm.socialclub_app.utils.PasswordCache;

import org.mindrot.jbcrypt.BCrypt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragmento que gestiona la visualización y edición del perfil de usuario.
 * Permite al socio modificar sus datos personales, incluida la foto de perfil.
 */
public class ProfileFragment extends Fragment {
    private Uri photoURI;
    private ProfileViewModel profileViewModel;
    private FragmentProfileBinding binding;
    private static final String TAG = "ProfileFragment";
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Socio currentSocio;
    private final SimpleDateFormat dateFormatDisplay = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
    private boolean pendingCameraAction = false;
    private boolean pendingGalleryAction = false;
    private boolean fotoChanged = false;

    /**
     * Crea y configura la vista del fragmento.
     *
     * @param inflater           El inflador de layouts.
     * @param container          El contenedor padre.
     * @param savedInstanceState Estado guardado del fragmento.
     * @return La vista raíz del fragmento.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.imageViewFoto.setOnClickListener(v -> mostrarOpcionesFoto());

        binding.textNumeroSocio.setEnabled(false);

        binding.textFechaNacimiento.setFocusable(false);
        binding.textFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());

        profileViewModel.getSocioData().observe(getViewLifecycleOwner(), socio -> {
            if (socio != null) {
                currentSocio = socio;

                binding.textNumeroSocio.setText(socio.getNumeroSocio());
                binding.textNombre.setText(socio.getNombre());
                binding.textApellidos.setText(socio.getApellidos());
                binding.textTelefono.setText(socio.getTelefono());
                binding.textDni.setText(socio.getDni());
                binding.textEmail.setText(socio.getEmail());

                if (socio.getFechaNacimiento() != null) {
                    String formattedDate = dateFormatDisplay.format(socio.getFechaNacimiento());
                    binding.textFechaNacimiento.setText(formattedDate);
                }

                String plainPassword = PasswordCache.getInstance().getPassword();
                if (plainPassword != null && !plainPassword.isEmpty() &&
                        SocioDao.verifyPassword(plainPassword, socio.getPassword())) {
                    binding.textContrasenia.setText(plainPassword);
                } else {
                    binding.textContrasenia.setText(R.string.contrase_a_cifrada);
                }

                byte[] fotoBlob = socio.getFoto();
                if (fotoBlob != null && fotoBlob.length > 0) {
                    Log.d(TAG, "Foto BLOB length: " + fotoBlob.length);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(fotoBlob, 0, fotoBlob.length);
                    if (bitmap != null) {
                        binding.imageViewFoto.setImageBitmap(bitmap);
                    } else {
                        Log.d(TAG, "Failed to decode bitmap");
                        saveBlobToFile(fotoBlob);
                    }
                }
            }
        });

        binding.btnGuardar.setOnClickListener(v -> mostrarDialogoConfirmacion());

        cargarDatosSocio();

        return root;
    }

    /**
     * Muestra un diálogo con opciones para cambiar la foto de perfil.
     */
    private void mostrarOpcionesFoto() {
        CharSequence[] options = new CharSequence[]{"Tomar foto", "Elegir de la galería", "Cancelar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Cambiar foto de perfil")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            checkCameraPermissions(true);
                            break;
                        case 1:
                            checkCameraPermissions(false);
                            break;
                        case 2:
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }

    /**
     * Verifica los permisos necesarios para acceder a la cámara o galería.
     *
     * @param isCamera true si se desea acceder a la cámara, false para la galería.
     */
    private void checkCameraPermissions(boolean isCamera) {
        List<String> permissionsNeeded = new ArrayList<>();

        if (isCamera) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.CAMERA);
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (permissionsNeeded.isEmpty()) {
            if (isCamera) {
                dispatchTakePictureIntent();
            } else {
                dispatchGalleryIntent();
            }
        } else {
            pendingCameraAction = isCamera;
            pendingGalleryAction = !isCamera;
            requestPermissionLauncher.launch(permissionsNeeded.toArray(new String[0]));
        }
    }

    /**
     * Lanza la intención para tomar una foto con la cámara.
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(requireContext(), "Error al crear archivo de imagen",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        photoURI = FileProvider.getUriForFile(requireContext(),
                "com.jlobatonm.socialclub_app.fileprovider",
                photoFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

        try {
            takePictureLauncher.launch(takePictureIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "No hay aplicación de cámara disponible",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Lanza la intención para seleccionar una imagen de la galería.
     */
    private void dispatchGalleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(galleryIntent);
    }

    /**
     * Crea un archivo temporal para almacenar la imagen capturada.
     *
     * @return El archivo creado.
     * @throws IOException Si hay error al crear el archivo.
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /**
     * Muestra un diálogo para seleccionar fecha de nacimiento.
     */
    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        if (currentSocio != null && currentSocio.getFechaNacimiento() != null) {
            calendar.setTime(currentSocio.getFechaNacimiento());
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        final Calendar fechaActual = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(Calendar.YEAR, selectedYear);
                    selectedDate.set(Calendar.MONTH, selectedMonth);
                    selectedDate.set(Calendar.DAY_OF_MONTH, selectedDay);

                    if (selectedDate.after(fechaActual)) {
                        Toast.makeText(requireContext(),
                                "La fecha de nacimiento no puede ser posterior a la fecha actual",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                    String formattedDate = dateFormatDisplay.format(calendar.getTime());
                    binding.textFechaNacimiento.setText(formattedDate);
                },
                year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(fechaActual.getTimeInMillis());
        datePickerDialog.show();
    }

    /**
     * Muestra un diálogo de confirmación para guardar los cambios.
     */
    private void mostrarDialogoConfirmacion() {
        if (currentSocio == null) {
            Toast.makeText(getContext(), "No hay datos para guardar", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hayModificaciones = verificarSiHayCambios();

        if (!hayModificaciones) {
            Toast.makeText(getContext(), "No hay cambios para guardar", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirmar cambios")
                .setMessage("¿Está seguro que desea guardar los cambios?")
                .setPositiveButton("Sí", (dialog, which) -> guardarCambios())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Verifica si algún campo ha sido modificado.
     *
     * @return true si hay cambios, false en caso contrario.
     */
    private boolean verificarSiHayCambios() {
        String nombreOriginal = currentSocio.getNombre();
        String apellidosOriginal = currentSocio.getApellidos();
        String telefonoOriginal = currentSocio.getTelefono();
        String dniOriginal = currentSocio.getDni();
        String emailOriginal = currentSocio.getEmail();
        String fechaOriginal = "";
        if (currentSocio.getFechaNacimiento() != null) {
            fechaOriginal = dateFormatDisplay.format(currentSocio.getFechaNacimiento());
        }

        String newPassword = binding.textContrasenia.getText().toString();
        String cachedPassword = PasswordCache.getInstance().getPassword();
        boolean passwordChanged = !newPassword.equals("(Contraseña cifrada)") &&
                (!newPassword.equals(cachedPassword));

        boolean hayFotoActual = binding.imageViewFoto.getDrawable() != null;
        boolean hayFotoOriginal = currentSocio.getFoto() != null && currentSocio.getFoto().length > 0;

        if (hayFotoActual != hayFotoOriginal) {
            fotoChanged = true;
            Log.d(TAG, "Cambio de foto detectado: presencia de foto diferente");
        } else if (hayFotoActual) {
            Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable) binding.imageViewFoto.getDrawable()).getBitmap();
            String hashActual = calcularHashImagen(bitmap);
            String hashOriginal = calcularHashImagen(currentSocio.getFoto());

            if (!hashActual.equals(hashOriginal)) {
                fotoChanged = true;
                Log.d(TAG, "Cambio de foto detectado: hash diferente");
            }
        }

        return !binding.textNombre.getText().toString().equals(nombreOriginal) ||
                !binding.textApellidos.getText().toString().equals(apellidosOriginal) ||
                !binding.textTelefono.getText().toString().equals(telefonoOriginal) ||
                !binding.textDni.getText().toString().equals(dniOriginal) ||
                !binding.textEmail.getText().toString().equals(emailOriginal) ||
                !binding.textFechaNacimiento.getText().toString().equals(fechaOriginal) ||
                passwordChanged ||
                fotoChanged;
    }

    /**
     * Calcula un hash simple para una imagen bitmap.
     *
     * @param bitmap La imagen para calcular su hash.
     * @return Un string que representa el hash de la imagen.
     */
    private String calcularHashImagen(Bitmap bitmap) {
        Bitmap smallBitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, true);

        StringBuilder hash = new StringBuilder();
        for (int x = 0; x < smallBitmap.getWidth(); x += 10) {
            for (int y = 0; y < smallBitmap.getHeight(); y += 10) {
                hash.append(smallBitmap.getPixel(x, y) & 0xFFFFFF);
            }
        }
        return hash.toString();
    }

    /**
     * Calcula un hash simple para una imagen en formato de bytes.
     *
     * @param imagenBytes Los bytes de la imagen.
     * @return Un string que representa el hash de la imagen.
     */
    private String calcularHashImagen(byte[] imagenBytes) {
        if (imagenBytes == null || imagenBytes.length == 0) {
            return "";
        }

        Bitmap bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
        if (bitmap == null) {
            return "";
        }

        return calcularHashImagen(bitmap);
    }

    /**
     * Carga los datos del socio desde la base de datos.
     */
    private void cargarDatosSocio() {
        MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
            @Override
            public void onSuccess(Connection connection) {
                String email = getUserEmail();
                SocioDao socioDao = new SocioDao();

                socioDao.getSocioByEmail(connection, email, new SocioDao.GetSocioCallback() {
                    @Override
                    public void onResult(Socio socio) {
                        profileViewModel.setSocioData(socio);
                    }

                    @Override
                    public void onError(Exception exception) {
                        Toast.makeText(getContext(), "Error al cargar datos: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(SQLException exception) {
                Toast.makeText(getContext(), "Error de conexión: " + exception.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Guarda los cambios realizados en el perfil.
     */
    private void guardarCambios() {
        try {
            currentSocio.setNombre(binding.textNombre.getText().toString());
            currentSocio.setApellidos(binding.textApellidos.getText().toString());
            currentSocio.setTelefono(binding.textTelefono.getText().toString());
            currentSocio.setDni(binding.textDni.getText().toString());
            currentSocio.setEmail(binding.textEmail.getText().toString());

            String fechaStr = binding.textFechaNacimiento.getText().toString();
            try {
                Date fechaNacimiento = dateFormatDisplay.parse(fechaStr);
                currentSocio.setFechaNacimiento(fechaNacimiento);
            } catch (ParseException e) {
                Toast.makeText(getContext(), "Error al procesar la fecha de nacimiento",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String newPassword = binding.textContrasenia.getText().toString();
            String cachedPassword = PasswordCache.getInstance().getPassword();
            boolean passwordChanged = !newPassword.equals("(Contraseña cifrada)") &&
                    (!newPassword.equals(cachedPassword));

            if (passwordChanged) {
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                currentSocio.setPassword(hashedPassword);
                PasswordCache.getInstance().setPassword(newPassword);
            }

            if (binding.imageViewFoto.getDrawable() != null) {
                Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable) binding.imageViewFoto.getDrawable()).getBitmap();
                currentSocio.setFoto(bitmapToByteArray(bitmap));
            } else {
                currentSocio.setFoto(null);
            }

            actualizarDatosSocio(currentSocio);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                fotoChanged = false;
                cargarDatosSocio();
            }, 500);

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al guardar cambios: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error al guardar cambios", e);
        }
    }

    /**
     * Actualiza los datos del socio en la base de datos.
     *
     * @param socio El objeto Socio con los datos actualizados.
     */
    private void actualizarDatosSocio(final Socio socio) {
        MySQLConnection.getConnectionAsync(new MySQLConnection.ConnectionCallback() {
            @Override
            public void onSuccess(Connection connection) {
                new Thread(() -> {
                    try {
                        String query = "UPDATE socio SET nombre = ?, apellidos = ?, telefono = ?, " +
                                "dni = ?, email = ?, fecha_nacimiento = ?";

                        String cachedPassword = PasswordCache.getInstance().getPassword();
                        boolean passwordChanged = cachedPassword != null &&
                                !binding.textContrasenia.getText().toString().equals("(Contraseña cifrada)");

                        query += ", foto = ?";

                        if (passwordChanged) {
                            query += ", contrasenia = ?";
                        }

                        query += " WHERE numero_socio = ?";

                        PreparedStatement statement = connection.prepareStatement(query);

                        statement.setString(1, socio.getNombre());
                        statement.setString(2, socio.getApellidos());
                        statement.setString(3, socio.getTelefono());
                        statement.setString(4, socio.getDni());
                        statement.setString(5, socio.getEmail());
                        statement.setDate(6, new java.sql.Date(socio.getFechaNacimiento().getTime()));

                        int paramIndex = 7;
                        statement.setBytes(paramIndex++, socio.getFoto());

                        if (passwordChanged) {
                            statement.setString(paramIndex++, socio.getPassword());
                        }

                        statement.setString(paramIndex, socio.getNumeroSocio());

                        int rowsAffected = statement.executeUpdate();
                        statement.close();

                        requireActivity().runOnUiThread(() -> {
                            if (rowsAffected > 0) {
                                Toast.makeText(getContext(), "Datos actualizados correctamente",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "No se pudo actualizar los datos",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (SQLException e) {
                        Log.e(TAG, "Error al actualizar socio: " + e.getMessage(), e);
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(),
                                "Error al actualizar: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
                    }
                }).start();
            }

            @Override
            public void onFailure(SQLException exception) {
                Toast.makeText(getContext(), "Error de conexión: " + exception.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Obtiene el email del usuario actual desde las preferencias compartidas.
     *
     * @return El email del usuario.
     */
    private String getUserEmail() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("email", "");
    }

    /**
     * Guarda un blob de bytes en un archivo para debug.
     *
     * @param blob Los bytes a guardar.
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
     * Procesa la imagen capturada desde la cámara.
     */
    private void procesarImagenCapturada() {
        try {
            Bitmap bitmap = getBitmapFromUri(photoURI);
            if (bitmap != null) {
                bitmap = fixImageRotation(photoURI);
                bitmap = resizeBitmap(bitmap);
                binding.imageViewFoto.setImageBitmap(bitmap);

                if (currentSocio != null) {
                    currentSocio.setFoto(bitmapToByteArray(bitmap));
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error al procesar la imagen", e);
            Toast.makeText(requireContext(), "Error al procesar la imagen",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Procesa la imagen seleccionada desde la galería.
     *
     * @param selectedImage URI de la imagen seleccionada.
     */
    private void procesarImagenGaleria(Uri selectedImage) {
        try {
            Bitmap bitmap = getBitmapFromUri(selectedImage);
            if (bitmap != null) {
                bitmap = fixImageRotation(selectedImage);
                bitmap = resizeBitmap(bitmap);
                binding.imageViewFoto.setImageBitmap(bitmap);

                if (currentSocio != null) {
                    currentSocio.setFoto(bitmapToByteArray(bitmap));
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error al procesar la imagen", e);
            Toast.makeText(requireContext(), "Error al procesar la imagen",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Corrige la rotación de una imagen basada en sus datos EXIF.
     *
     * @param imageUri URI de la imagen a corregir.
     * @return Bitmap con la orientación corregida.
     * @throws IOException Si hay error al procesar la imagen.
     */
    private Bitmap fixImageRotation(Uri imageUri) throws IOException {
        Bitmap bitmap = getBitmapFromUri(imageUri);
        int orientation = getImageOrientation(imageUri);
        Log.d(TAG, "Image orientation: " + orientation);

        if (orientation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            if (bitmap != rotatedBitmap) {
                bitmap.recycle();
            }
            return rotatedBitmap;
        }
        return bitmap;
    }

    /**
     * Obtiene la orientación de una imagen desde sus datos EXIF.
     *
     * @param imageUri URI de la imagen.
     * @return Ángulo de rotación en grados.
     */
    private int getImageOrientation(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return 0;
            }

            androidx.exifinterface.media.ExifInterface exif =
                    new androidx.exifinterface.media.ExifInterface(inputStream);

            int orientation = exif.getAttributeInt(
                    androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_UNDEFINED);

            inputStream.close();

            return switch (orientation) {
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90;
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180;
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270;
                default -> 0;
            };
        } catch (Exception e) {
            Log.e(TAG, "Error getting image orientation", e);
            return 0;
        }
    }

    /**
     * Inicializa los launchers para permisos y actividades de captura.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = true;
                    for (Boolean isGranted : permissions.values()) {
                        if (!isGranted) {
                            allGranted = false;
                            break;
                        }
                    }

                    if (allGranted) {
                        if (pendingCameraAction) {
                            dispatchTakePictureIntent();
                        } else if (pendingGalleryAction) {
                            dispatchGalleryIntent();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Se requieren permisos para esta funcionalidad",
                                Toast.LENGTH_SHORT).show();
                    }

                    pendingCameraAction = false;
                    pendingGalleryAction = false;
                });

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        procesarImagenCapturada();
                    }
                });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        procesarImagenGaleria(result.getData().getData());
                    }
                });
    }

    /**
     * Obtiene un bitmap a partir de un URI.
     *
     * @param uri URI de la imagen.
     * @return El bitmap obtenido.
     * @throws IOException Si hay error al acceder al archivo.
     */
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                requireContext().getContentResolver().openFileDescriptor(uri, "r");
        assert parcelFileDescriptor != null;
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    /**
     * Redimensiona un bitmap manteniendo su relación de aspecto.
     * Si la imagen es horizontal (ancho > alto), el ancho será 500px.
     * Si la imagen es vertical (alto > ancho), el alto será 500px.
     *
     * @param bitmap El bitmap original a redimensionar
     * @return Un nuevo bitmap redimensionado
     */
    private Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = 500;
            height = (int) (width / bitmapRatio);
        } else {
            height = 500;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    /**
     * Convierte un bitmap en un array de bytes en formato JPEG con 70% de calidad.
     * Útil para almacenar imágenes en la base de datos.
     *
     * @param bitmap El bitmap a convertir
     * @return Array de bytes que representa la imagen en formato JPEG
     */
    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        return baos.toByteArray();
    }

    /**
     * Limpia los recursos cuando se destruye la vista del fragmento.
     * Libera la referencia al binding para evitar fugas de memoria.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}