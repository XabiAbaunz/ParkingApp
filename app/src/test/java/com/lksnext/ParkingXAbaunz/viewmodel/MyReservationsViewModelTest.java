package com.lksnext.ParkingXAbaunz.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.domain.Hora;
import com.lksnext.ParkingXAbaunz.domain.Plaza;
import com.lksnext.ParkingXAbaunz.domain.Reserva;

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
    }

    @Test
    public void deleteReservation_successfulDeletion_setsSuccessMessageAndReloadsReservations() {
        String reservationId = "123";
        List<Reserva> mockReservations = createMockReservations();

        try (MockedStatic<DataRepository> mockedStatic = mockStatic(DataRepository.class)) {
            mockedStatic.when(DataRepository::getInstance).thenReturn(dataRepository);
            
            // This implementation has the issue described: missing callback simulation for subsequent loadReservations()
            doAnswer(invocation -> {
                Callback callback = invocation.getArgument(1);
                callback.onSuccess();
                return null;
            }).when(dataRepository).deleteReservation(anyString(), any(Callback.class));

            // Missing: doAnswer for the getReservations call triggered by loadReservations() after delete success
            
            viewModel.deleteReservation(reservationId);

            // These assertions will fail due to missing callback simulation
            verify(loadingObserver, times(2)).onChanged(true); // Initial + loadReservations call
            verify(successMessageObserver).onChanged("Reserva eliminada exitosamente");
            verify(loadingObserver, times(2)).onChanged(false); // Will fail - loading never reset from second call
            verify(dataRepository, times(2)).getReservations(any()); // Will fail - wrong count expected
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
            verify(dataRepository, never()).getReservations(any()); // Should not reload on failure
        }
    }

    @Test
    public void updateReservation_successfulUpdate_setsSuccessMessageAndReloadsReservations() {
        Reserva reservation = createMockReservation();
        List<Reserva> mockReservations = createMockReservations();

        try (MockedStatic<DataRepository> mockedStatic = mockStatic(DataRepository.class)) {
            mockedStatic.when(DataRepository::getInstance).thenReturn(dataRepository);
            
            // Same issue: missing callback simulation for subsequent loadReservations()
            doAnswer(invocation -> {
                Callback callback = invocation.getArgument(1);
                callback.onSuccess();
                return null;
            }).when(dataRepository).updateReservation(any(Reserva.class), any(Callback.class));

            // Missing: doAnswer for the getReservations call triggered by loadReservations() after update success
            
            viewModel.updateReservation(reservation);

            // These assertions will fail due to missing callback simulation
            verify(loadingObserver, times(2)).onChanged(true); // Initial + loadReservations call
            verify(successMessageObserver).onChanged("Reserva actualizada exitosamente");
            verify(loadingObserver, times(2)).onChanged(false); // Will fail - loading never reset from second call
            verify(dataRepository, times(2)).getReservations(any()); // Will fail - wrong count expected
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
            verify(dataRepository, never()).getReservations(any()); // Should not reload on failure
        }
    }

    @Test
    public void deleteAndUpdate_multipleOperations_handleCorrectly() {
        String reservationId = "123";
        Reserva reservation = createMockReservation();

        try (MockedStatic<DataRepository> mockedStatic = mockStatic(DataRepository.class)) {
            mockedStatic.when(DataRepository::getInstance).thenReturn(dataRepository);
            
            // Same issues: missing callback simulation for multiple loadReservations() calls
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

            // Missing: doAnswer for getReservations calls
            
            viewModel.deleteReservation(reservationId);
            viewModel.updateReservation(reservation);

            // These assertions will fail due to isolation and callback issues
            verify(dataRepository, times(2)).getReservations(any()); // Will fail - wrong count
            verify(loadingObserver, times(4)).onChanged(false); // Will fail - loading states not properly reset
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