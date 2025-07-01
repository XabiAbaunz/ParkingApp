package com.lksnext.ParkingXAbaunz.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.domain.Usuario;

public class DataRepository {

    private static DataRepository instance;
    private FirebaseAuth mAuth;

    private DataRepository() {
        mAuth = FirebaseAuth.getInstance();
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
}

