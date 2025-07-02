package com.lksnext.ParkingXAbaunz;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.domain.Coche;
import com.lksnext.ParkingXAbaunz.domain.Reserva;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NewReservationViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private DataRepository mockRepository;

    private NewReservationViewModel viewModel;
    private MockedStatic<DataRepository> mockedDataRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock del método estático getInstance
        mockedDataRepository = mockStatic(DataRepository.class);
        mockedDataRepository.when(DataRepository::getInstance).thenReturn(mockRepository);

        viewModel = new NewReservationViewModel();
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
        String fecha = "2024-01-15";
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 1000L;
        long horaFin = 2000L;

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

        // Verificar que el repositorio fue llamado con los parámetros correctos
        ArgumentCaptor<Reserva> reservaCaptor = ArgumentCaptor.forClass(Reserva.class);
        verify(mockRepository).saveReservation(reservaCaptor.capture(), any(Callback.class));

        Reserva capturedReserva = reservaCaptor.getValue();
        assertEquals("La fecha debe coincidir", fecha, capturedReserva.getFecha());
        assertEquals("El coche debe coincidir", coche, capturedReserva.getCoche());
        assertEquals("El tipo de plaza debe coincidir", tipoPlaza, capturedReserva.getPlazaId().getTipo());
        assertEquals("La hora de inicio debe coincidir", horaInicio, capturedReserva.getHora().getHoraInicio());
        assertEquals("La hora de fin debe coincidir", horaFin, capturedReserva.getHora().getHoraFin());
    }

    @Test
    public void saveReservation_cocheNulo_estableceMensajeError() throws InterruptedException {
        // Preparación
        String fecha = "2024-01-15";
        Coche coche = null;
        String tipoPlaza = "STANDARD";
        long horaInicio = 1000L;
        long horaFin = 2000L;

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String errorMessage = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("Debe mostrar error por coche faltante", "Debes seleccionar un coche", errorMessage);

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

        verify(mockRepository, never()).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_tipoPlazaNulo_estableceMensajeError() throws InterruptedException {
        // Preparación
        String fecha = "2024-01-15";
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = null;
        long horaInicio = 1000L;
        long horaFin = 2000L;

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String errorMessage = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("Debe mostrar error por tipo de plaza faltante", "Debes seleccionar un tipo de plaza", errorMessage);

        verify(mockRepository, never()).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_tipoPlazaVacio_estableceMensajeError() throws InterruptedException {
        // Preparación
        String fecha = "2024-01-15";
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "";
        long horaInicio = 1000L;
        long horaFin = 2000L;

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String errorMessage = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("Debe mostrar error por tipo de plaza vacío", "Debes seleccionar un tipo de plaza", errorMessage);

        verify(mockRepository, never()).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_horaInicioIgualHoraFin_estableceMensajeError() throws InterruptedException {
        // Preparación
        String fecha = "2024-01-15";
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

        verify(mockRepository, never()).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_horaInicioMayorQueHoraFin_estableceMensajeError() throws InterruptedException {
        // Preparación
        String fecha = "2024-01-15";
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

        verify(mockRepository, never()).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_falloRepositorio_estableceMensajeError() throws InterruptedException {
        // Preparación
        String fecha = "2024-01-15";
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 1000L;
        long horaFin = 2000L;
        String repositoryError = "Error de red";

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

        verify(mockRepository).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_parametrosValidos_estableceEstadoLoadingCorrectamente() throws InterruptedException {
        // Preparación
        String fecha = "2024-01-15";
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 1000L;
        long horaFin = 2000L;

        doAnswer(invocation -> {
            // No llamar callback inmediatamente para probar estado de carga
            return null;
        }).when(mockRepository).saveReservation(any(Reserva.class), any(Callback.class));

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        Boolean isLoading = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        assertTrue("El loading debe ser true durante la operación", isLoading);
    }

    @Test
    public void saveReservation_rangoTiempoMinimoValido_guardaExitosamente() throws InterruptedException {
        // Preparación
        String fecha = "2024-01-15";
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = 0L;
        long horaFin = 1L;

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

        verify(mockRepository).saveReservation(any(Reserva.class), any(Callback.class));
    }

    @Test
    public void saveReservation_valoresTiempoGrandes_guardaExitosamente() throws InterruptedException {
        // Preparación
        String fecha = "2024-01-15";
        Coche coche = new Coche("ABC123", "Toyota", "Camry");
        String tipoPlaza = "STANDARD";
        long horaInicio = Long.MAX_VALUE - 1000L;
        long horaFin = Long.MAX_VALUE;

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockRepository).saveReservation(any(Reserva.class), any(Callback.class));

        // Ejecución
        viewModel.saveReservation(fecha, coche, tipoPlaza, horaInicio, horaFin);

        // Verificación
        String successMessage = LiveDataTestUtil.getValue(viewModel.getSuccessLiveData());
        assertEquals("Debe guardar exitosamente con valores grandes",
                "Reserva guardada con éxito", successMessage);

        verify(mockRepository).saveReservation(any(Reserva.class), any(Callback.class));
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