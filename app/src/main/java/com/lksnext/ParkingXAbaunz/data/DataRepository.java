package com.lksnext.ParkingXAbaunz.data;

import androidx.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseUser;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.domain.CallbackWithData;
import com.lksnext.ParkingXAbaunz.domain.Coche;
import com.lksnext.ParkingXAbaunz.domain.Reserva;
import com.lksnext.ParkingXAbaunz.domain.Usuario;

import java.util.ArrayList;
import java.util.List;

public class DataRepository {

    private static final String TAG = "DataRepository";
    private static DataRepository instance;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private DataRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized DataRepository getInstance() {
        if (instance == null) {
            instance = new DataRepository();
        }
        return instance;
    }

    public void login(String email, String pass, Callback callback) {
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            String errorMessage = "Error en el inicio de sesión. Inténtalo de nuevo.";
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = "La contraseña es incorrecta o el email no es válido.";
                            } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                errorMessage = "No hay usuario registrado con este correo electrónico.";
                            } else if (task.getException() != null) {
                                errorMessage = task.getException().getLocalizedMessage();
                            }
                            callback.onFailure(errorMessage);
                        }
                    }
                });
    }

    public void register(String email, String password, Callback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            Usuario usuario = new Usuario(email);
                            FirebaseFirestore.getInstance()
                                    .collection("usuarios")
                                    .document(firebaseUser.getUid())
                                    .set(usuario)
                                    .addOnSuccessListener(aVoid -> {
                                        callback.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        callback.onFailure("Registro creado, pero error al guardar usuario: " + e.getMessage());
                                    });
                        } else {
                            callback.onFailure("Error inesperado: el usuario es null.");
                        }
                    } else {
                        String errorMessage = "Error en el registro. Inténtalo de nuevo.";
                        if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                            errorMessage = "La contraseña es demasiado débil. Debe tener al menos 6 caracteres y ser más compleja.";
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "El formato del email no es válido.";
                        } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            errorMessage = "Ya existe una cuenta con este correo electrónico.";
                        } else if (task.getException() != null) {
                            errorMessage = task.getException().getLocalizedMessage();
                        }
                        callback.onFailure(errorMessage);
                    }
                });
    }

    public void sendPasswordResetEmail(String email, Callback callback) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            String errorMessage = "Error al enviar el email de restablecimiento.";
                            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                errorMessage = "No existe un usuario con este correo electrónico.";
                            } else if (task.getException() != null) {
                                errorMessage = task.getException().getLocalizedMessage();
                            }
                            callback.onFailure(errorMessage);
                        }
                    }
                });
    }

    public void getCoches(CallbackWithData<List<Coche>> callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Usuario no autenticado");
            return;
        }

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("coches")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Coche> coches = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Coche coche = document.toObject(Coche.class);
                            coches.add(coche);
                        }
                        callback.onSuccess(coches);
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getLocalizedMessage() :
                                "Error al cargar los coches";
                        callback.onFailure(errorMessage);
                    }
                });
    }

    public void addCoche(Coche coche, Callback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Usuario no autenticado");
            return;
        }

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("coches")
                .document(coche.getMatricula())
                .set(coche)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    String errorMessage = e.getLocalizedMessage() != null ?
                            e.getLocalizedMessage() :
                            "Error al añadir el coche";
                    callback.onFailure(errorMessage);
                });
    }

    public void deleteCoche(String matricula, Callback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Usuario no autenticado");
            return;
        }

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("coches")
                .document(matricula)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    String errorMessage = e.getLocalizedMessage() != null ?
                            e.getLocalizedMessage() :
                            "Error al eliminar el coche";
                    callback.onFailure(errorMessage);
                });
    }

    public void checkReservationConflict(String fecha, long horaInicio, long horaFin, CallbackWithData<Boolean> callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Usuario no autenticado");
            return;
        }

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("reservas")
                .whereEqualTo("fecha", fecha)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean hasConflict = false;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Reserva reserva = document.toObject(Reserva.class);
                                if (reserva.getHora() != null) {
                                    long existingStart = reserva.getHora().getHoraInicio();
                                    long existingEnd = reserva.getHora().getHoraFin();

                                    if (hasTimeOverlap(horaInicio, horaFin, existingStart, existingEnd)) {
                                        hasConflict = true;
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error al parsear reserva: " + e.getMessage());
                            }
                        }

                        callback.onSuccess(hasConflict);
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getLocalizedMessage() :
                                "Error al verificar conflictos";
                        callback.onFailure(errorMessage);
                    }
                });
    }

    public void checkReservationConflictForUpdate(String reservaIdToExclude, String fecha, long horaInicio, long horaFin, CallbackWithData<Boolean> callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Usuario no autenticado");
            return;
        }

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("reservas")
                .whereEqualTo("fecha", fecha)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean hasConflict = false;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Reserva reserva = document.toObject(Reserva.class);
                                if (reserva.getHora() != null && !reserva.getId().equals(reservaIdToExclude)) {
                                    long existingStart = reserva.getHora().getHoraInicio();
                                    long existingEnd = reserva.getHora().getHoraFin();

                                    if (hasTimeOverlap(horaInicio, horaFin, existingStart, existingEnd)) {
                                        hasConflict = true;
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error al parsear reserva: " + e.getMessage());
                            }
                        }

                        callback.onSuccess(hasConflict);
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getLocalizedMessage() :
                                "Error al verificar conflictos";
                        callback.onFailure(errorMessage);
                    }
                });
    }

    private boolean hasTimeOverlap(long start1, long end1, long start2, long end2) {
        return start1 < end2 && start2 < end1;
    }

    public void getReservationById(String reservaId, CallbackWithData<Reserva> callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Usuario no autenticado");
            return;
        }

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("reservas")
                .document(reservaId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            try {
                                Reserva reserva = document.toObject(Reserva.class);
                                callback.onSuccess(reserva);
                            } catch (Exception e) {
                                Log.e(TAG, "Error al parsear reserva: " + e.getMessage());
                                callback.onFailure("Error al cargar la reserva");
                            }
                        } else {
                            callback.onFailure("Reserva no encontrada");
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getLocalizedMessage() :
                                "Error al cargar la reserva";
                        callback.onFailure(errorMessage);
                    }
                });
    }

    public void saveReservation(Reserva reserva, Callback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Usuario no autenticado");
            return;
        }

        String reservaId = reserva.getId();
        if (reservaId == null || reservaId.startsWith("temp-")) {
            reservaId = db.collection("usuarios").document(currentUser.getUid())
                    .collection("reservas").document().getId();
            reserva.setId(reservaId);
        }

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("reservas")
                .document(reservaId)
                .set(reserva)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    String errorMessage = e.getLocalizedMessage() != null ?
                            e.getLocalizedMessage() :
                            "Error al guardar la reserva";
                    callback.onFailure(errorMessage);
                });
    }

    public void getReservations(CallbackWithData<List<Reserva>> callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Usuario no autenticado");
            return;
        }

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("reservas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Reserva> reservas = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Reserva reserva = document.toObject(Reserva.class);

                                if (reserva.getCoche() != null &&
                                        (reserva.getCoche().getMarca() == null ||
                                                reserva.getCoche().getModelo() == null ||
                                                reserva.getCoche().getMarca().isEmpty() ||
                                                reserva.getCoche().getModelo().isEmpty())) {

                                    String matricula = reserva.getCoche().getMatricula();
                                    if (matricula != null && !matricula.isEmpty()) {
                                        getCocheByMatricula(matricula, new CallbackWithData<Coche>() {
                                            @Override
                                            public void onSuccess(Coche coche) {
                                                reserva.setCoche(coche);
                                                updateReservation(reserva, new Callback() {
                                                    @Override
                                                    public void onSuccess() {
                                                    }

                                                    @Override
                                                    public void onFailure(String error) {
                                                        Log.e(TAG, "Error al actualizar reserva: " + error);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(String error) {
                                                Log.e(TAG, "Error al obtener coche: " + error);
                                            }
                                        });
                                    }
                                }

                                reservas.add(reserva);
                            } catch (Exception e) {
                                Log.e(TAG, "Error al parsear reserva: " + e.getMessage());
                            }
                        }
                        callback.onSuccess(reservas);
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getLocalizedMessage() :
                                "Error al cargar las reservas";
                        callback.onFailure(errorMessage);
                    }
                });
    }

    private void getCocheByMatricula(String matricula, CallbackWithData<Coche> callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || matricula == null) {
            callback.onFailure("Usuario no autenticado o matrícula inválida");
            return;
        }

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("coches")
                .document(matricula)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        Coche coche = task.getResult().toObject(Coche.class);
                        callback.onSuccess(coche);
                    } else {
                        callback.onFailure("No se encontró el coche");
                    }
                });
    }

    public void deleteReservation(String reservaId, Callback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Usuario no autenticado");
            return;
        }

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("reservas")
                .document(reservaId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    String errorMessage = e.getLocalizedMessage() != null ?
                            e.getLocalizedMessage() :
                            "Error al eliminar la reserva";
                    callback.onFailure(errorMessage);
                });
    }

    public void updateReservation(Reserva reserva, Callback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Usuario no autenticado");
            return;
        }

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("reservas")
                .document(reserva.getId())
                .set(reserva)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    String errorMessage = e.getLocalizedMessage() != null ?
                            e.getLocalizedMessage() :
                            "Error al actualizar la reserva";
                    callback.onFailure(errorMessage);
                });
    }
}