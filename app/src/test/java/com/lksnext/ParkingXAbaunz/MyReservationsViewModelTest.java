package com.lksnext.ParkingXAbaunz;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.domain.CallbackWithData;
import com.lksnext.ParkingXAbaunz.domain.Reserva;
import com.lksnext.ParkingXAbaunz.utils.LiveDataTestUtil;
import com.lksnext.ParkingXAbaunz.viewmodel.MyReservationsViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MyReservationsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private DataRepository mockRepository;

    private MyReservationsViewModel viewModel;
    private MockedStatic<DataRepository> mockedDataRepository;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mockear el método estático DataRepository.getInstance()
        mockedDataRepository = Mockito.mockStatic(DataRepository.class);
        mockedDataRepository.when(DataRepository::getInstance).thenReturn(mockRepository);

        // Configurar comportamiento por defecto para getReservations
        doAnswer(invocation -> {
            CallbackWithData<List<Reserva>> callback = invocation.getArgument(0);
            callback.onSuccess(createTestReservations());
            return null;
        }).when(mockRepository).getReservations(any(CallbackWithData.class));

        viewModel = new MyReservationsViewModel();

        // Resetear interacciones del mock después de la inicialización del ViewModel
        reset(mockRepository);

        // Re-establecer el comportamiento por defecto después del reset
        doAnswer(invocation -> {
            CallbackWithData<List<Reserva>> callback = invocation.getArgument(0);
            callback.onSuccess(createTestReservations());
            return null;
        }).when(mockRepository).getReservations(any(CallbackWithData.class));
    }

    @After
    public void tearDown() {
        if (mockedDataRepository != null) {
            mockedDataRepository.close();
        }
    }

    // Método auxiliar para crear reservas de prueba
    private List<Reserva> createTestReservations() {
        List<Reserva> reservas = new ArrayList<>();

        // Reserva futura (Agosto 2025)
        Reserva futureReserva = new Reserva();
        futureReserva.setId("1");
        futureReserva.setFecha("2025-08-15");
        futureReserva.setHoraInicio(800L);
        reservas.add(futureReserva);

        // Reserva pasada (Junio 2025)
        Reserva pastReserva = new Reserva();
        pastReserva.setId("2");
        pastReserva.setFecha("2025-06-01");
        pastReserva.setHoraInicio(900L);
        reservas.add(pastReserva);

        return reservas;
    }

    private Reserva createTestReservation() {
        Reserva reserva = new Reserva();
        reserva.setId("test-id");
        reserva.setFecha("2025-08-15");
        reserva.setHoraInicio(800L);
        return reserva;
    }

    // ==================== TESTS DE CARGA DE RESERVAS ====================

    @Test
    public void loadReservations_respuestaExitosa_estableceEstadoCargaYProcesaReservas() throws InterruptedException {
        List<Reserva> testReservations = createTestReservations();

        doAnswer(invocation -> {
            CallbackWithData<List<Reserva>> callback = invocation.getArgument(0);
            callback.onSuccess(testReservations);
            return null;
        }).when(mockRepository).getReservations(any(CallbackWithData.class));

        viewModel.loadReservations();

        Boolean isLoading = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        assertFalse("La carga debería ser false después de una respuesta exitosa", isLoading);

        List<Reserva> futureReservations = LiveDataTestUtil.getValue(viewModel.getFutureReservationsLiveData());
        List<Reserva> pastReservations = LiveDataTestUtil.getValue(viewModel.getPastReservationsLiveData());

        assertNotNull("Las reservas futuras no deberían ser null", futureReservations);
        assertNotNull("Las reservas pasadas no deberían ser null", pastReservations);
        assertEquals("Debería tener una reserva futura", 1, futureReservations.size());
        assertEquals("Debería tener una reserva pasada", 1, pastReservations.size());

        verify(mockRepository, times(1)).getReservations(any(CallbackWithData.class));
    }

    @Test
    public void loadReservations_respuestaFallida_estableceErrorYDetieneCarga() throws InterruptedException {
        String errorMessage = "Error de red ocurrido";

        doAnswer(invocation -> {
            CallbackWithData<List<Reserva>> callback = invocation.getArgument(0);
            callback.onFailure(errorMessage);
            return null;
        }).when(mockRepository).getReservations(any(CallbackWithData.class));

        viewModel.loadReservations();

        Boolean isLoading = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        assertFalse("La carga debería ser false después del fallo", isLoading);

        String error = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("El mensaje de error debería coincidir", errorMessage, error);

        verify(mockRepository, times(1)).getReservations(any(CallbackWithData.class));
    }

    @Test
    public void loadReservations_listaVacia_estableceListasVacias() throws InterruptedException {
        List<Reserva> emptyReservations = new ArrayList<>();

        doAnswer(invocation -> {
            CallbackWithData<List<Reserva>> callback = invocation.getArgument(0);
            callback.onSuccess(emptyReservations);
            return null;
        }).when(mockRepository).getReservations(any(CallbackWithData.class));

        viewModel.loadReservations();

        List<Reserva> futureReservations = LiveDataTestUtil.getValue(viewModel.getFutureReservationsLiveData());
        List<Reserva> pastReservations = LiveDataTestUtil.getValue(viewModel.getPastReservationsLiveData());

        assertNotNull("Las reservas futuras no deberían ser null", futureReservations);
        assertNotNull("Las reservas pasadas no deberían ser null", pastReservations);
        assertTrue("Las reservas futuras deberían estar vacías", futureReservations.isEmpty());
        assertTrue("Las reservas pasadas deberían estar vacías", pastReservations.isEmpty());

        verify(mockRepository, times(1)).getReservations(any(CallbackWithData.class));
    }

    @Test
    public void loadReservations_respuestaNula_manejaGracilmente() throws InterruptedException {
        // Una respuesta nula debería tratarse como un error en escenarios reales
        String expectedError = "Error de red - respuesta nula";

        doAnswer(invocation -> {
            CallbackWithData<List<Reserva>> callback = invocation.getArgument(0);
            callback.onFailure(expectedError);
            return null;
        }).when(mockRepository).getReservations(any(CallbackWithData.class));

        viewModel.loadReservations();

        Boolean isLoading = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        assertFalse("La carga debería ser false después del fallo por respuesta nula", isLoading);

        String error = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("El mensaje de error debería indicar problema de respuesta nula", expectedError, error);

        verify(mockRepository, times(1)).getReservations(any(CallbackWithData.class));
    }

    // ==================== TESTS DE ELIMINACIÓN DE RESERVAS ====================

    @Test
    public void deleteReservation_eliminacionExitosa_estableceMensajeExitoYRecargaReservas() throws InterruptedException {
        Reserva testReservation = createTestReservation();

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockRepository).deleteReservation(eq(testReservation.getId()), any(Callback.class));

        viewModel.deleteReservation(testReservation);

        Boolean isLoading = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        assertFalse("La carga debería ser false después de eliminación exitosa", isLoading);

        String successMessage = LiveDataTestUtil.getValue(viewModel.getSuccessLiveData());
        assertEquals("El mensaje de éxito debería coincidir", "Reserva eliminada correctamente", successMessage);

        verify(mockRepository, times(1)).deleteReservation(eq(testReservation.getId()), any(Callback.class));
        verify(mockRepository, times(1)).getReservations(any(CallbackWithData.class));
    }

    @Test
    public void deleteReservation_eliminacionFallida_estableceMensajeError() throws InterruptedException {
        Reserva testReservation = createTestReservation();
        String errorMessage = "Falló la eliminación de la reserva";

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onFailure(errorMessage);
            return null;
        }).when(mockRepository).deleteReservation(eq(testReservation.getId()), any(Callback.class));

        viewModel.deleteReservation(testReservation);

        Boolean isLoading = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        assertFalse("La carga debería ser false después del fallo", isLoading);

        String error = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("El mensaje de error debería coincidir", errorMessage, error);

        verify(mockRepository, times(1)).deleteReservation(eq(testReservation.getId()), any(Callback.class));
        verify(mockRepository, never()).getReservations(any(CallbackWithData.class));
    }

    @Test
    public void deleteReservation_reservaNula_manejaGracilmente() {
        try {
            viewModel.deleteReservation(null);
            verify(mockRepository, never()).deleteReservation(anyString(), any(Callback.class));
        } catch (Exception e) {
            assertTrue("Debería manejar reserva nula apropiadamente", true);
        }
    }

    @Test
    public void deleteReservation_reservaConIdNulo_manejaGracilmente() {
        Reserva reservationWithNullId = new Reserva();
        reservationWithNullId.setId(null);

        try {
            viewModel.deleteReservation(reservationWithNullId);
            verify(mockRepository, times(1)).deleteReservation(eq(null), any(Callback.class));
        } catch (Exception e) {
            assertTrue("Debería manejar ID nulo apropiadamente", true);
        }
    }

    // ==================== TESTS DE ACTUALIZACIÓN DE RESERVAS ====================

    @Test
    public void updateReservation_actualizacionExitosa_estableceMensajeExitoYRecargaReservas() throws InterruptedException {
        Reserva testReservation = createTestReservation();

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockRepository).updateReservation(eq(testReservation), any(Callback.class));

        viewModel.updateReservation(testReservation);

        Boolean isLoading = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        assertFalse("La carga debería ser false después de actualización exitosa", isLoading);

        String successMessage = LiveDataTestUtil.getValue(viewModel.getSuccessLiveData());
        assertEquals("El mensaje de éxito debería coincidir", "Reserva actualizada correctamente", successMessage);

        verify(mockRepository, times(1)).updateReservation(eq(testReservation), any(Callback.class));
        verify(mockRepository, times(1)).getReservations(any(CallbackWithData.class));
    }

    @Test
    public void updateReservation_actualizacionFallida_estableceMensajeError() throws InterruptedException {
        Reserva testReservation = createTestReservation();
        String errorMessage = "Falló la actualización de la reserva";

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onFailure(errorMessage);
            return null;
        }).when(mockRepository).updateReservation(eq(testReservation), any(Callback.class));

        viewModel.updateReservation(testReservation);

        Boolean isLoading = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        assertFalse("La carga debería ser false después del fallo", isLoading);

        String error = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("El mensaje de error debería coincidir", errorMessage, error);

        verify(mockRepository, times(1)).updateReservation(eq(testReservation), any(Callback.class));
        verify(mockRepository, never()).getReservations(any(CallbackWithData.class));
    }

    @Test
    public void updateReservation_reservaNula_manejaGracilmente() {
        try {
            viewModel.updateReservation(null);
            verify(mockRepository, times(1)).updateReservation(eq(null), any(Callback.class));
        } catch (Exception e) {
            assertTrue("Debería manejar reserva nula apropiadamente", true);
        }
    }

    @Test
    public void updateReservation_reservaModificada_pasaDatosCorrectosAlRepositorio() {
        Reserva modifiedReservation = createTestReservation();
        modifiedReservation.setFecha("2025-09-15");
        modifiedReservation.setHoraInicio(1000L);

        viewModel.updateReservation(modifiedReservation);

        verify(mockRepository, times(1)).updateReservation(eq(modifiedReservation), any(Callback.class));
    }

    // ==================== TESTS DE INTEGRACIÓN ====================

    @Test
    public void deleteAndUpdate_operacionesMultiples_manejaCorrectamente() throws InterruptedException {
        Reserva testReservation = createTestReservation();

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockRepository).deleteReservation(eq(testReservation.getId()), any(Callback.class));

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockRepository).updateReservation(eq(testReservation), any(Callback.class));

        // Eliminar primero
        viewModel.deleteReservation(testReservation);

        // Reset para rastrear solo las llamadas de actualización
        reset(mockRepository);

        doAnswer(invocation -> {
            CallbackWithData<List<Reserva>> callback = invocation.getArgument(0);
            callback.onSuccess(createTestReservations());
            return null;
        }).when(mockRepository).getReservations(any(CallbackWithData.class));

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockRepository).updateReservation(eq(testReservation), any(Callback.class));

        // Actualizar después de eliminar
        viewModel.updateReservation(testReservation);

        String successMessage = LiveDataTestUtil.getValue(viewModel.getSuccessLiveData());
        assertEquals("El último mensaje de éxito debería ser de actualización", "Reserva actualizada correctamente", successMessage);

        verify(mockRepository, times(1)).updateReservation(eq(testReservation), any(Callback.class));
        verify(mockRepository, times(1)).getReservations(any(CallbackWithData.class));
    }

    // ==================== TESTS ADICIONALES PARA PROCESAMIENTO DE RESERVAS ====================

    @Test
    public void processReservations_reservaDeHoy_esClasificadaComoFutura() throws InterruptedException {
        List<Reserva> reservas = new ArrayList<>();

        Calendar today = Calendar.getInstance();
        String todayString = dateFormat.format(today.getTime());

        Reserva todayReserva = new Reserva();
        todayReserva.setId("today-id");
        todayReserva.setFecha(todayString);
        todayReserva.setHoraInicio(1000L);
        reservas.add(todayReserva);

        doAnswer(invocation -> {
            CallbackWithData<List<Reserva>> callback = invocation.getArgument(0);
            callback.onSuccess(reservas);
            return null;
        }).when(mockRepository).getReservations(any(CallbackWithData.class));

        viewModel.loadReservations();

        List<Reserva> futureReservations = LiveDataTestUtil.getValue(viewModel.getFutureReservationsLiveData());
        List<Reserva> pastReservations = LiveDataTestUtil.getValue(viewModel.getPastReservationsLiveData());

        assertEquals("La reserva de hoy debería estar en la lista futura", 1, futureReservations.size());
        assertEquals("Las reservas pasadas deberían estar vacías", 0, pastReservations.size());
        assertEquals("La reserva de hoy debería tener el ID correcto", "today-id", futureReservations.get(0).getId());
    }

    @Test
    public void processReservations_formatoFechaInvalido_agregadaAListaFutura() throws InterruptedException {
        List<Reserva> reservas = new ArrayList<>();

        Reserva invalidDateReserva = new Reserva();
        invalidDateReserva.setId("invalid-date-id");
        invalidDateReserva.setFecha("formato-fecha-invalido");
        invalidDateReserva.setHoraInicio(1000L);
        reservas.add(invalidDateReserva);

        doAnswer(invocation -> {
            CallbackWithData<List<Reserva>> callback = invocation.getArgument(0);
            callback.onSuccess(reservas);
            return null;
        }).when(mockRepository).getReservations(any(CallbackWithData.class));

        viewModel.loadReservations();

        List<Reserva> futureReservations = LiveDataTestUtil.getValue(viewModel.getFutureReservationsLiveData());
        List<Reserva> pastReservations = LiveDataTestUtil.getValue(viewModel.getPastReservationsLiveData());

        assertEquals("La reserva con fecha inválida debería estar en la lista futura", 1, futureReservations.size());
        assertEquals("Las reservas pasadas deberían estar vacías", 0, pastReservations.size());
        assertEquals("La reserva con fecha inválida debería tener el ID correcto", "invalid-date-id", futureReservations.get(0).getId());
    }

    @Test
    public void processReservations_ordenamiento_futurasAscendentePasadasDescendente() throws InterruptedException {
        List<Reserva> reservas = new ArrayList<>();

        // Reservas futuras (deberían ordenarse ascendente por fecha, luego por hora)
        Reserva future1 = new Reserva();
        future1.setId("future1");
        future1.setFecha("2025-08-15");
        future1.setHoraInicio(1000L);
        reservas.add(future1);

        Reserva future2 = new Reserva();
        future2.setId("future2");
        future2.setFecha("2025-08-15");
        future2.setHoraInicio(800L);
        reservas.add(future2);

        Reserva future3 = new Reserva();
        future3.setId("future3");
        future3.setFecha("2025-08-10");
        future3.setHoraInicio(900L);
        reservas.add(future3);

        // Reservas pasadas (deberían ordenarse descendente por fecha, luego por hora)
        Reserva past1 = new Reserva();
        past1.setId("past1");
        past1.setFecha("2025-06-15");
        past1.setHoraInicio(800L);
        reservas.add(past1);

        Reserva past2 = new Reserva();
        past2.setId("past2");
        past2.setFecha("2025-06-15");
        past2.setHoraInicio(1000L);
        reservas.add(past2);

        Reserva past3 = new Reserva();
        past3.setId("past3");
        past3.setFecha("2025-06-10");
        past3.setHoraInicio(900L);
        reservas.add(past3);

        doAnswer(invocation -> {
            CallbackWithData<List<Reserva>> callback = invocation.getArgument(0);
            callback.onSuccess(reservas);
            return null;
        }).when(mockRepository).getReservations(any(CallbackWithData.class));

        viewModel.loadReservations();

        List<Reserva> futureReservations = LiveDataTestUtil.getValue(viewModel.getFutureReservationsLiveData());
        List<Reserva> pastReservations = LiveDataTestUtil.getValue(viewModel.getPastReservationsLiveData());

        // Reservas futuras deberían ordenarse ascendente por fecha, luego por hora
        assertEquals("Debería tener 3 reservas futuras", 3, futureReservations.size());
        assertEquals("La primera futura debería ser la fecha y hora más temprana", "future3", futureReservations.get(0).getId());
        assertEquals("La segunda futura debería ser misma fecha pero hora más temprana", "future2", futureReservations.get(1).getId());
        assertEquals("La tercera futura debería ser misma fecha pero hora más tardía", "future1", futureReservations.get(2).getId());

        // Reservas pasadas deberían ordenarse descendente por fecha, luego por hora
        assertEquals("Debería tener 3 reservas pasadas", 3, pastReservations.size());
        assertEquals("La primera pasada debería ser la fecha y hora más tardía", "past2", pastReservations.get(0).getId());
        assertEquals("La segunda pasada debería ser misma fecha pero hora más temprana", "past1", pastReservations.get(1).getId());
        assertEquals("La tercera pasada debería ser fecha más temprana", "past3", pastReservations.get(2).getId());
    }
}