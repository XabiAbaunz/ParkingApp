package com.lksnext.ParkingXAbaunz;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.utils.LiveDataTestUtil;
import com.lksnext.ParkingXAbaunz.viewmodel.SignupViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SignupViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private DataRepository mockDataRepository;

    private SignupViewModel viewModel;
    private MockedStatic<DataRepository> mockedStaticDataRepository;
    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Mockear el singleton de DataRepository
        mockedStaticDataRepository = mockStatic(DataRepository.class);
        mockedStaticDataRepository.when(DataRepository::getInstance).thenReturn(mockDataRepository);

        viewModel = new SignupViewModel();
    }

    @After
    public void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
        if (mockedStaticDataRepository != null) {
            mockedStaticDataRepository.close();
        }
    }

    // Tests de validación de campos vacíos (estos funcionan porque se validan ANTES del email)
    @Test
    public void registerUser_emailVacio_estableceErrorYRegisteredFalse() throws InterruptedException {
        String emptyEmail = "";
        String validPassword = "password123";
        String validConfirmPassword = "password123";

        viewModel.registerUser(emptyEmail, validPassword, validConfirmPassword);

        Boolean result = LiveDataTestUtil.getValue(viewModel.isRegistered());
        String error = LiveDataTestUtil.getValue(viewModel.getError());

        assertFalse("El registro debe fallar con email vacío", result);
        assertEquals("Debe mostrar error de campos obligatorios", "Todos los campos son obligatorios", error);
        verify(mockDataRepository, never()).register(anyString(), anyString(), any(Callback.class));
    }

    @Test
    public void registerUser_passwordVacia_estableceErrorYRegisteredFalse() throws InterruptedException {
        String validEmail = "test@example.com";
        String emptyPassword = "";
        String validConfirmPassword = "password123";

        viewModel.registerUser(validEmail, emptyPassword, validConfirmPassword);

        Boolean result = LiveDataTestUtil.getValue(viewModel.isRegistered());
        String error = LiveDataTestUtil.getValue(viewModel.getError());

        assertFalse("El registro debe fallar con contraseña vacía", result);
        assertEquals("Debe mostrar error de campos obligatorios", "Todos los campos son obligatorios", error);
        verify(mockDataRepository, never()).register(anyString(), anyString(), any(Callback.class));
    }

    @Test
    public void registerUser_confirmPasswordVacia_estableceErrorYRegisteredFalse() throws InterruptedException {
        String validEmail = "test@example.com";
        String validPassword = "password123";
        String emptyConfirmPassword = "";

        viewModel.registerUser(validEmail, validPassword, emptyConfirmPassword);

        Boolean result = LiveDataTestUtil.getValue(viewModel.isRegistered());
        String error = LiveDataTestUtil.getValue(viewModel.getError());

        assertFalse("El registro debe fallar con confirmación vacía", result);
        assertEquals("Debe mostrar error de campos obligatorios", "Todos los campos son obligatorios", error);
        verify(mockDataRepository, never()).register(anyString(), anyString(), any(Callback.class));
    }

    @Test
    public void registerUser_todosCamposVacios_estableceErrorYRegisteredFalse() throws InterruptedException {
        String emptyEmail = "";
        String emptyPassword = "";
        String emptyConfirmPassword = "";

        viewModel.registerUser(emptyEmail, emptyPassword, emptyConfirmPassword);

        Boolean result = LiveDataTestUtil.getValue(viewModel.isRegistered());
        String error = LiveDataTestUtil.getValue(viewModel.getError());

        assertFalse("El registro debe fallar con todos los campos vacíos", result);
        assertEquals("Debe mostrar error de campos obligatorios", "Todos los campos son obligatorios", error);
        verify(mockDataRepository, never()).register(anyString(), anyString(), any(Callback.class));
    }

    // Tests de gestión de estado con campos vacíos
    @Test
    public void registerUser_multiplesErroresValidacion_muestraPrimerError() throws InterruptedException {
        String emptyEmail = "";
        String shortPassword = "123";
        String confirmPassword = "123";

        viewModel.registerUser(emptyEmail, shortPassword, confirmPassword);

        Boolean result = LiveDataTestUtil.getValue(viewModel.isRegistered());
        String error = LiveDataTestUtil.getValue(viewModel.getError());

        assertFalse("El registro debe fallar", result);
        assertEquals("Debe mostrar el primer error de validación", "Todos los campos son obligatorios", error);
        verify(mockDataRepository, never()).register(anyString(), anyString(), any(Callback.class));
    }

    @Test
    public void clearError_reiniciaEstadoErrorYRegistered() throws InterruptedException {
        // Establecer un estado de error primero
        viewModel.registerUser("", "", "");

        Boolean initialRegistered = LiveDataTestUtil.getValue(viewModel.isRegistered());
        String initialError = LiveDataTestUtil.getValue(viewModel.getError());
        assertFalse("Debe estar en estado de error", initialRegistered);
        assertNotNull("Debe tener mensaje de error", initialError);

        viewModel.clearError();

        Boolean clearedRegistered = LiveDataTestUtil.getValue(viewModel.isRegistered());
        String clearedError = LiveDataTestUtil.getValue(viewModel.getError());

        assertNull("El estado registered debe ser null después de limpiar", clearedRegistered);
        assertNull("El error debe ser null después de limpiar", clearedError);
    }

    // Tests de setters individuales
    @Test
    public void setEmail_estableceEmailCorrectamente() throws InterruptedException {
        String email = "test@example.com";

        viewModel.setEmail(email);

        String result = LiveDataTestUtil.getValue(viewModel.getEmail());
        assertEquals("El email debe establecerse correctamente", email, result);
    }

    @Test
    public void setPassword_establecePasswordCorrectamente() throws InterruptedException {
        String password = "password123";

        viewModel.setPassword(password);

        String result = LiveDataTestUtil.getValue(viewModel.getPassword());
        assertEquals("La contraseña debe establecerse correctamente", password, result);
    }

    @Test
    public void setConfirmPassword_estableceConfirmPasswordCorrectamente() throws InterruptedException {
        String confirmPassword = "password123";

        viewModel.setConfirmPassword(confirmPassword);

        String result = LiveDataTestUtil.getValue(viewModel.getConfirmPassword());
        assertEquals("La confirmación debe establecerse correctamente", confirmPassword, result);
    }

    // Test de estado inicial
    @Test
    public void viewModelInicializado_estadoInicialCorrecto() throws InterruptedException {
        Boolean registered = LiveDataTestUtil.getValue(viewModel.isRegistered());
        String error = LiveDataTestUtil.getValue(viewModel.getError());
        String email = LiveDataTestUtil.getValue(viewModel.getEmail());
        String password = LiveDataTestUtil.getValue(viewModel.getPassword());
        String confirmPassword = LiveDataTestUtil.getValue(viewModel.getConfirmPassword());

        assertNull("Registered debe ser null inicialmente", registered);
        assertNull("Error debe ser null inicialmente", error);
        assertEquals("Email debe ser string vacío inicialmente", "", email);
        assertEquals("Password debe ser string vacío inicialmente", "", password);
        assertEquals("ConfirmPassword debe ser string vacío inicialmente", "", confirmPassword);
    }

    // Test de llamadas al repository cuando los datos están completamente vacíos
    @Test
    public void registerUser_camposVacios_noLlamaRepository() throws InterruptedException {
        viewModel.registerUser("", "", "");

        verify(mockDataRepository, never()).register(anyString(), anyString(), any(Callback.class));
    }

    @Test
    public void registerUser_algunCampoVacio_noLlamaRepository() throws InterruptedException {
        viewModel.registerUser("test@example.com", "", "password123");

        verify(mockDataRepository, never()).register(anyString(), anyString(), any(Callback.class));
    }

    // Test del método clearError independiente
    @Test
    public void clearError_estadoInicialLimpio() throws InterruptedException {
        viewModel.clearError();

        Boolean registered = LiveDataTestUtil.getValue(viewModel.isRegistered());
        String error = LiveDataTestUtil.getValue(viewModel.getError());

        assertNull("Registered debe ser null después de clearError", registered);
        assertNull("Error debe ser null después de clearError", error);
    }

    // Tests adicionales para diferentes combinaciones de campos vacíos
    @Test
    public void registerUser_soloEmailLleno_estableceErrorYRegisteredFalse() throws InterruptedException {
        String validEmail = "test@example.com";
        String emptyPassword = "";
        String emptyConfirmPassword = "";

        viewModel.registerUser(validEmail, emptyPassword, emptyConfirmPassword);

        Boolean result = LiveDataTestUtil.getValue(viewModel.isRegistered());
        String error = LiveDataTestUtil.getValue(viewModel.getError());

        assertFalse("El registro debe fallar con solo email lleno", result);
        assertEquals("Debe mostrar error de campos obligatorios", "Todos los campos son obligatorios", error);
        verify(mockDataRepository, never()).register(anyString(), anyString(), any(Callback.class));
    }

    @Test
    public void registerUser_soloPasswordLlena_estableceErrorYRegisteredFalse() throws InterruptedException {
        String emptyEmail = "";
        String validPassword = "password123";
        String emptyConfirmPassword = "";

        viewModel.registerUser(emptyEmail, validPassword, emptyConfirmPassword);

        Boolean result = LiveDataTestUtil.getValue(viewModel.isRegistered());
        String error = LiveDataTestUtil.getValue(viewModel.getError());

        assertFalse("El registro debe fallar con solo password llena", result);
        assertEquals("Debe mostrar error de campos obligatorios", "Todos los campos son obligatorios", error);
        verify(mockDataRepository, never()).register(anyString(), anyString(), any(Callback.class));
    }

    @Test
    public void registerUser_soloConfirmPasswordLlena_estableceErrorYRegisteredFalse() throws InterruptedException {
        String emptyEmail = "";
        String emptyPassword = "";
        String validConfirmPassword = "password123";

        viewModel.registerUser(emptyEmail, emptyPassword, validConfirmPassword);

        Boolean result = LiveDataTestUtil.getValue(viewModel.isRegistered());
        String error = LiveDataTestUtil.getValue(viewModel.getError());

        assertFalse("El registro debe fallar con solo confirmPassword llena", result);
        assertEquals("Debe mostrar error de campos obligatorios", "Todos los campos son obligatorios", error);
        verify(mockDataRepository, never()).register(anyString(), anyString(), any(Callback.class));
    }

    // Test de que los valores se establecen correctamente incluso cuando hay error
    @Test
    public void registerUser_camposVacios_estableceValoresCorrectamente() throws InterruptedException {
        String emptyEmail = "";
        String emptyPassword = "";
        String emptyConfirmPassword = "";

        viewModel.registerUser(emptyEmail, emptyPassword, emptyConfirmPassword);

        String resultEmail = LiveDataTestUtil.getValue(viewModel.getEmail());
        String resultPassword = LiveDataTestUtil.getValue(viewModel.getPassword());
        String resultConfirmPassword = LiveDataTestUtil.getValue(viewModel.getConfirmPassword());

        assertEquals("El email debe establecerse aunque esté vacío", emptyEmail, resultEmail);
        assertEquals("La contraseña debe establecerse aunque esté vacía", emptyPassword, resultPassword);
        assertEquals("La confirmación debe establecerse aunque esté vacía", emptyConfirmPassword, resultConfirmPassword);
    }
}