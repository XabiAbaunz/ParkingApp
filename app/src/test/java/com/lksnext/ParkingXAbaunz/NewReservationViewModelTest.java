package com.lksnext.ParkingXAbaunz;

import android.app.Application;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.domain.CallbackWithData;
import com.lksnext.ParkingXAbaunz.domain.Coche;
import com.lksnext.ParkingXAbaunz.domain.Reserva;
import com.lksnext.ParkingXAbaunz.notifications.ReservationNotificationManager;
import com.lksnext.ParkingXAbaunz.viewmodel.NewReservationViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NewReservationViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application mockApplication;

    @Mock
    private DataRepository mockRepository;

    @Mock
    private ReservationNotificationManager mockNotificationManager;

    private NewReservationViewModel viewModel;
    private MockedStatic<DataRepository> mockedDataRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock del método estático getInstance
        mockedDataRepository = mockStatic(DataRepository.class);
        mockedDataRepository.when(DataRepository::getInstance).thenReturn(mockRepository);

        viewModel = new NewReservationViewModel(mockApplication);
    }

    @After
    public void tearDown() {
        if (mockedDataRepository != null) {
            mockedDataRepository.close();
        }
    }

    @Test
    public void saveReservation_parametrosValidos_guardaExitosamente() throws InterruptedException {
        // Preparación
        String fecha = getTodayDateString();
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 1000L;
        long horaFin = 2000L;

        // Mock del check de conflictos
        doAnswer(invocation -> {
            CallbackWithData<Boolean> callback = invocation.getArgument(3);
            callback.onSuccess(false); // No hay conflictos
            return null;
        }).when(mockRepository).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));

        // Mock del guardado
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockRepository).saveReservation(any(Reserva.class), any(Callback.class));

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        Boolean isLoading = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        String successMessage = LiveDataTestUtil.getValue(viewModel.getSuccessLiveData());

        assertFalse("El loading debe ser false después de guardar exitosamente", isLoading);
        assertEquals("El mensaje de éxito debe coincidir", "Reserva guardada con éxito", successMessage);

        // Verificar que el repositorio fue llamado
        verify(mockRepository).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));
        verify(mockRepository).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_cocheNulo_estableceMensajeError() throws InterruptedException {
        // Preparación
        String fecha = getTodayDateString();
        Coche coche = null;
        String tipoPlaza = "STANDARD";
        long horaInicio = 1000L;
        long horaFin = 2000L;

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String errorMessage = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("Debe mostrar error por coche faltante", "Debes seleccionar un coche", errorMessage);

        verify(mockRepository, never()).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));
        verify(mockRepository, never()).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_fechaNula_estableceMensajeError() throws InterruptedException {
        // Preparación
        String fecha = null;
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 1000L;
        long horaFin = 2000L;

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String errorMessage = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("Debe mostrar error por fecha faltante", "Debes seleccionar una fecha", errorMessage);

        verify(mockRepository, never()).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));
        verify(mockRepository, never()).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_fechaVacia_estableceMensajeError() throws InterruptedException {
        // Preparación
        String fecha = "";
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 1000L;
        long horaFin = 2000L;

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String errorMessage = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("Debe mostrar error por fecha vacía", "Debes seleccionar una fecha", errorMessage);

        verify(mockRepository, never()).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));
        verify(mockRepository, never()).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_tipoPlazaNulo_estableceMensajeError() throws InterruptedException {
        // Preparación
        String fecha = getTodayDateString();
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = null;
        long horaInicio = 1000L;
        long horaFin = 2000L;

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String errorMessage = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("Debe mostrar error por tipo de plaza faltante", "Debes seleccionar un tipo de plaza", errorMessage);

        verify(mockRepository, never()).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));
        verify(mockRepository, never()).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_tipoPlazaVacio_estableceMensajeError() throws InterruptedException {
        // Preparación
        String fecha = getTodayDateString();
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "";
        long horaInicio = 1000L;
        long horaFin = 2000L;

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String errorMessage = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("Debe mostrar error por tipo de plaza vacío", "Debes seleccionar un tipo de plaza", errorMessage);

        verify(mockRepository, never()).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));
        verify(mockRepository, never()).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_horaInicioIgualHoraFin_estableceMensajeError() throws InterruptedException {
        // Preparación
        String fecha = getTodayDateString();
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 1000L;
        long horaFin = 1000L;

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String errorMessage = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("Debe mostrar error por rango de tiempo inválido",
                "La hora de fin debe ser posterior a la hora de inicio", errorMessage);

        verify(mockRepository, never()).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));
        verify(mockRepository, never()).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_horaInicioMayorQueHoraFin_estableceMensajeError() throws InterruptedException {
        // Preparación
        String fecha = getTodayDateString();
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 2000L;
        long horaFin = 1000L;

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String errorMessage = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("Debe mostrar error por rango de tiempo inválido",
                "La hora de fin debe ser posterior a la hora de inicio", errorMessage);

        verify(mockRepository, never()).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));
        verify(mockRepository, never()).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_fechaInvalida_estableceMensajeError() throws InterruptedException {
        // Preparación - fecha en el pasado
        String fecha = "2020-01-01";
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 1000L;
        long horaFin = 2000L;

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String errorMessage = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("Debe mostrar error por fecha inválida",
                "Solo puedes hacer reservas desde hoy hasta 7 días naturales", errorMessage);

        verify(mockRepository, never()).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));
        verify(mockRepository, never()).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_duracionExcesiva_estableceMensajeError() throws InterruptedException {
        // Preparación - más de 8 horas (8 horas = 28800 segundos)
        String fecha = getTodayDateString();
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 0L;
        long horaFin = 32400L; // 9 horas en segundos

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String errorMessage = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("Debe mostrar error por duración excesiva",
                "La reserva no puede exceder las 8 horas", errorMessage);

        verify(mockRepository, never()).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));
        verify(mockRepository, never()).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_conflictoExistente_estableceMensajeError() throws InterruptedException {
        // Preparación
        String fecha = getTodayDateString();
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 1000L;
        long horaFin = 2000L;

        // Mock del check de conflictos - hay conflicto
        doAnswer(invocation -> {
            CallbackWithData<Boolean> callback = invocation.getArgument(3);
            callback.onSuccess(true); // Hay conflicto
            return null;
        }).when(mockRepository).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String errorMessage = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        Boolean isLoading = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());

        assertFalse("El loading debe ser false después del conflicto", isLoading);
        assertEquals("Debe mostrar error por conflicto",
                "Ya tienes una reserva que coincide con este horario", errorMessage);

        verify(mockRepository).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));
        verify(mockRepository, never()).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_errorVerificacionConflicto_estableceMensajeError() throws InterruptedException {
        // Preparación
        String fecha = getTodayDateString();
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 1000L;
        long horaFin = 2000L;
        String errorConflicto = "Error de red";

        // Mock del check de conflictos - error
        doAnswer(invocation -> {
            CallbackWithData<Boolean> callback = invocation.getArgument(3);
            callback.onFailure(errorConflicto);
            return null;
        }).when(mockRepository).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String errorMessage = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        Boolean isLoading = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());

        assertFalse("El loading debe ser false después del error", isLoading);
        assertEquals("Debe mostrar error de verificación",
                "Error al verificar conflictos: " + errorConflicto, errorMessage);

        verify(mockRepository).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));
        verify(mockRepository, never()).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_falloRepositorio_estableceMensajeError() throws InterruptedException {
        // Preparación
        String fecha = getTodayDateString();
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 1000L;
        long horaFin = 2000L;
        String repositoryError = "Error de red";

        // Mock del check de conflictos - sin conflicto
        doAnswer(invocation -> {
            CallbackWithData<Boolean> callback = invocation.getArgument(3);
            callback.onSuccess(false);
            return null;
        }).when(mockRepository).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));

        // Mock del guardado - fallo
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onFailure(repositoryError);
            return null;
        }).when(mockRepository).saveReservation(any(Reserva.class), any(Callback.class));

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        Boolean isLoading = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        String errorMessage = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());

        assertFalse("El loading debe ser false después del fallo", isLoading);
        assertEquals("El mensaje de error debe coincidir", repositoryError, errorMessage);

        verify(mockRepository).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));
        verify(mockRepository).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_parametrosValidos_estableceEstadoLoadingCorrectamente() throws InterruptedException {
        // Preparación
        String fecha = getTodayDateString();
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 1000L;
        long horaFin = 2000L;

        // Mock del check de conflictos - no responder inmediatamente
        doAnswer(invocation -> {
            // No llamar callback inmediatamente para probar estado de carga
            return null;
        }).when(mockRepository).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        Boolean isLoading = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        assertTrue("El loading debe ser true durante la operación", isLoading);
    }

    @Test
    public void saveReservation_rangoTiempoMinimoValido_guardaExitosamente() throws InterruptedException {
        // Preparación
        String fecha = getTodayDateString();
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 0L;
        long horaFin = 1L;

        // Mock del check de conflictos
        doAnswer(invocation -> {
            CallbackWithData<Boolean> callback = invocation.getArgument(3);
            callback.onSuccess(false);
            return null;
        }).when(mockRepository).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));

        // Mock del guardado
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockRepository).saveReservation(any(Reserva.class), any(Callback.class));

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String successMessage = LiveDataTestUtil.getValue(viewModel.getSuccessLiveData());
        assertEquals("Debe guardar exitosamente con rango mínimo",
                "Reserva guardada con éxito", successMessage);

        verify(mockRepository).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));
        verify(mockRepository).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_duracionMaximaPermitida_guardaExitosamente() throws InterruptedException {
        // Preparación - exactamente 8 horas (28800 segundos)
        String fecha = getTodayDateString();
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 0L;
        long horaFin = 28800L; // 8 horas exactas

        // Mock del check de conflictos
        doAnswer(invocation -> {
            CallbackWithData<Boolean> callback = invocation.getArgument(3);
            callback.onSuccess(false);
            return null;
        }).when(mockRepository).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));

        // Mock del guardado
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockRepository).saveReservation(any(Reserva.class), any(Callback.class));

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String successMessage = LiveDataTestUtil.getValue(viewModel.getSuccessLiveData());
        assertEquals("Debe guardar exitosamente con duración máxima",
                "Reserva guardada con éxito", successMessage);

        verify(mockRepository).checkReservationConflict(anyString(), anyLong(), anyLong(), any(CallbackWithData.class));
        verify(mockRepository).saveReservation(any(Reserva.class), any(Callback.class));
    }

    // Método helper para obtener la fecha de hoy en formato String
    private String getTodayDateString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(new Date());
    }

    // Clase utilitaria para testing de LiveData
    public static class LiveDataTestUtil {

        public static <T> T getValue(LiveData<T> liveData) throws InterruptedException {
            final Object[] data = new Object[1];
            final CountDownLatch latch = new CountDownLatch(1);

            Observer<T> observer = new Observer<T>() {
                @Override
                public void onChanged(T o) {
                    data[0] = o;
                    latch.countDown();
                }
            };

            liveData.observeForever(observer);

            try {
                latch.await(2, TimeUnit.SECONDS);
            } finally {
                liveData.removeObserver(observer);
            }

            @SuppressWarnings("unchecked")
            T result = (T) data[0];
            return result;
        }
    }
}