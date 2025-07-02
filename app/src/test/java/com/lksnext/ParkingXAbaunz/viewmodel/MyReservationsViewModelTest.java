package com.lksnext.ParkingXAbaunz.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.domain.Hora;
import com.lksnext.ParkingXAbaunz.domain.Plaza;
import com.lksnext.ParkingXAbaunz.domain.Reserva;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MyReservationsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private DataRepository dataRepository;

    @Mock
    private Observer<Boolean> loadingObserver;

    @Mock
    private Observer<String> successMessageObserver;

    @Mock
    private Observer<String> errorMessageObserver;

    @Mock
    private Observer<List<Reserva>> reservationsObserver;

    private MyReservationsViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new MyReservationsViewModel();
        
        // Observe LiveData
        viewModel.isLoading().observeForever(loadingObserver);
        viewModel.getSuccessMessage().observeForever(successMessageObserver);
        viewModel.getErrorMessage().observeForever(errorMessageObserver);
        viewModel.getReservations().observeForever(reservationsObserver);
        
        // Reset mock interactions for better test isolation
        reset(dataRepository, loadingObserver, successMessageObserver, errorMessageObserver, reservationsObserver);
    }

    @After
    public void tearDown() {
        // Improved test isolation: Clear observers and reset state
        viewModel.isLoading().removeObserver(loadingObserver);
        viewModel.getSuccessMessage().removeObserver(successMessageObserver);
        viewModel.getErrorMessage().removeObserver(errorMessageObserver);
        viewModel.getReservations().removeObserver(reservationsObserver);
        
        // Clear any messages to reset ViewModel state
        viewModel.clearMessages();
    }

    @Test
    public void deleteReservation_successfulDeletion_setsSuccessMessageAndReloadsReservations() {
        String reservationId = "123";
        List<Reserva> mockReservations = createMockReservations();

        try (MockedStatic<DataRepository> mockedStatic = mockStatic(DataRepository.class)) {
            mockedStatic.when(DataRepository::getInstance).thenReturn(dataRepository);
            
            // Fixed: Properly simulate deleteReservation callback
            doAnswer(invocation -> {
                Callback callback = invocation.getArgument(1);
                callback.onSuccess();
                return null;
            }).when(dataRepository).deleteReservation(anyString(), any(Callback.class));

            // Fixed: Simulate getReservations callback for loadReservations() calls
            doAnswer(invocation -> {
                DataRepository.ReservationsCallback callback = invocation.getArgument(0);
                callback.onSuccess(mockReservations);
                return null;
            }).when(dataRepository).getReservations(any(DataRepository.ReservationsCallback.class));
            
            viewModel.deleteReservation(reservationId);

            // Fixed: Correct verification counts - delete triggers loadReservations
            verify(loadingObserver, times(2)).onChanged(true); // Initial delete + loadReservations
            verify(successMessageObserver).onChanged("Reserva eliminada exitosamente");
            verify(loadingObserver, times(2)).onChanged(false); // Both operations complete
            verify(dataRepository).deleteReservation(reservationId, any(Callback.class));
            verify(dataRepository).getReservations(any(DataRepository.ReservationsCallback.class));
        }
    }

    @Test
    public void deleteReservation_failureDeletion_setsErrorMessage() {
        String reservationId = "123";

        try (MockedStatic<DataRepository> mockedStatic = mockStatic(DataRepository.class)) {
            mockedStatic.when(DataRepository::getInstance).thenReturn(dataRepository);
            
            doAnswer(invocation -> {
                Callback callback = invocation.getArgument(1);
                callback.onFailure();
                return null;
            }).when(dataRepository).deleteReservation(anyString(), any(Callback.class));

            viewModel.deleteReservation(reservationId);

            verify(loadingObserver).onChanged(true);
            verify(errorMessageObserver).onChanged("Error al eliminar la reserva");
            verify(loadingObserver).onChanged(false);
            verify(dataRepository, never()).getReservations(any(DataRepository.ReservationsCallback.class)); // Should not reload on failure
        }
    }

    @Test
    public void updateReservation_successfulUpdate_setsSuccessMessageAndReloadsReservations() {
        Reserva reservation = createMockReservation();
        List<Reserva> mockReservations = createMockReservations();

        try (MockedStatic<DataRepository> mockedStatic = mockStatic(DataRepository.class)) {
            mockedStatic.when(DataRepository::getInstance).thenReturn(dataRepository);
            
            // Fixed: Properly simulate updateReservation callback
            doAnswer(invocation -> {
                Callback callback = invocation.getArgument(1);
                callback.onSuccess();
                return null;
            }).when(dataRepository).updateReservation(any(Reserva.class), any(Callback.class));

            // Fixed: Simulate getReservations callback for loadReservations() calls
            doAnswer(invocation -> {
                DataRepository.ReservationsCallback callback = invocation.getArgument(0);
                callback.onSuccess(mockReservations);
                return null;
            }).when(dataRepository).getReservations(any(DataRepository.ReservationsCallback.class));
            
            viewModel.updateReservation(reservation);

            // Fixed: Correct verification counts - update triggers loadReservations
            verify(loadingObserver, times(2)).onChanged(true); // Initial update + loadReservations
            verify(successMessageObserver).onChanged("Reserva actualizada exitosamente");
            verify(loadingObserver, times(2)).onChanged(false); // Both operations complete
            verify(dataRepository).updateReservation(reservation, any(Callback.class));
            verify(dataRepository).getReservations(any(DataRepository.ReservationsCallback.class));
        }
    }

    @Test
    public void updateReservation_failureUpdate_setsErrorMessage() {
        Reserva reservation = createMockReservation();

        try (MockedStatic<DataRepository> mockedStatic = mockStatic(DataRepository.class)) {
            mockedStatic.when(DataRepository::getInstance).thenReturn(dataRepository);
            
            doAnswer(invocation -> {
                Callback callback = invocation.getArgument(1);
                callback.onFailure();
                return null;
            }).when(dataRepository).updateReservation(any(Reserva.class), any(Callback.class));

            viewModel.updateReservation(reservation);

            verify(loadingObserver).onChanged(true);
            verify(errorMessageObserver).onChanged("Error al actualizar la reserva");
            verify(loadingObserver).onChanged(false);
            verify(dataRepository, never()).getReservations(any(DataRepository.ReservationsCallback.class)); // Should not reload on failure
        }
    }

    @Test
    public void deleteAndUpdate_multipleOperations_handleCorrectly() {
        String reservationId = "123";
        Reserva reservation = createMockReservation();
        List<Reserva> mockReservations = createMockReservations();

        try (MockedStatic<DataRepository> mockedStatic = mockStatic(DataRepository.class)) {
            mockedStatic.when(DataRepository::getInstance).thenReturn(dataRepository);
            
            // Fixed: Properly simulate both operations with callbacks
            doAnswer(invocation -> {
                Callback callback = invocation.getArgument(1);
                callback.onSuccess();
                return null;
            }).when(dataRepository).deleteReservation(anyString(), any(Callback.class));

            doAnswer(invocation -> {
                Callback callback = invocation.getArgument(1);
                callback.onSuccess();
                return null;
            }).when(dataRepository).updateReservation(any(Reserva.class), any(Callback.class));

            // Fixed: Simulate getReservations callback for both loadReservations() calls
            doAnswer(invocation -> {
                DataRepository.ReservationsCallback callback = invocation.getArgument(0);
                callback.onSuccess(mockReservations);
                return null;
            }).when(dataRepository).getReservations(any(DataRepository.ReservationsCallback.class));
            
            viewModel.deleteReservation(reservationId);
            viewModel.updateReservation(reservation);

            // Fixed: Correct verification counts for multiple operations
            verify(dataRepository, times(2)).getReservations(any(DataRepository.ReservationsCallback.class)); // Two loadReservations calls
            verify(loadingObserver, times(4)).onChanged(true); // Two operations, each triggering loadReservations
            verify(loadingObserver, times(4)).onChanged(false); // All operations complete
        }
    }

    @Test
    public void loadReservations_successfulLoad_setsReservationsAndStopsLoading() {
        List<Reserva> mockReservations = createMockReservations();

        try (MockedStatic<DataRepository> mockedStatic = mockStatic(DataRepository.class)) {
            mockedStatic.when(DataRepository::getInstance).thenReturn(dataRepository);
            
            doAnswer(invocation -> {
                DataRepository.ReservationsCallback callback = invocation.getArgument(0);
                callback.onSuccess(mockReservations);
                return null;
            }).when(dataRepository).getReservations(any(DataRepository.ReservationsCallback.class));

            viewModel.loadReservations();

            verify(loadingObserver).onChanged(true);
            verify(loadingObserver).onChanged(false);
            verify(reservationsObserver).onChanged(mockReservations);
            verify(dataRepository).getReservations(any(DataRepository.ReservationsCallback.class));
        }
    }

    @Test
    public void loadReservations_failureLoad_setsErrorMessage() {
        try (MockedStatic<DataRepository> mockedStatic = mockStatic(DataRepository.class)) {
            mockedStatic.when(DataRepository::getInstance).thenReturn(dataRepository);
            
            doAnswer(invocation -> {
                DataRepository.ReservationsCallback callback = invocation.getArgument(0);
                callback.onFailure();
                return null;
            }).when(dataRepository).getReservations(any(DataRepository.ReservationsCallback.class));

            viewModel.loadReservations();

            verify(loadingObserver).onChanged(true);
            verify(loadingObserver).onChanged(false);
            verify(errorMessageObserver).onChanged("Error al cargar las reservas");
            verify(dataRepository).getReservations(any(DataRepository.ReservationsCallback.class));
        }
    }

    private Reserva createMockReservation() {
        Plaza plaza = new Plaza(1, "Normal");
        Hora hora = new Hora(9 * 3600, 11 * 3600);
        return new Reserva("2025-06-01", "test@example.com", "123", plaza, hora);
    }

    private List<Reserva> createMockReservations() {
        List<Reserva> reservations = new ArrayList<>();
        reservations.add(createMockReservation());
        return reservations;
    }
}