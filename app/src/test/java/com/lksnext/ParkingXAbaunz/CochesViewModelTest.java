package com.lksnext.ParkingXAbaunz;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.domain.CallbackWithData;
import com.lksnext.ParkingXAbaunz.domain.Coche;
import com.lksnext.ParkingXAbaunz.utils.LiveDataTestUtil;
import com.lksnext.ParkingXAbaunz.viewmodel.CochesViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CochesViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private DataRepository mockRepository;

    private CochesViewModel viewModel;
    private AutoCloseable mockitoCloseable;

    @Before
    public void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        viewModel = new CochesViewModel(mockRepository);
    }

    @After
    public void tearDown() throws Exception {
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    // ========== Tests de loadCoches() ==========

    @Test
    public void loadCoches_successfulLoad_setsLoadingStateAndEmitsData() throws InterruptedException {
        // Preparación
        List<Coche> expectedCoches = Arrays.asList(
                new Coche("ABC123", "Toyota", "Camry"),
                new Coche("DEF456", "Honda", "Civic")
        );

        // Ejecución
        viewModel.loadCoches();

        // Capturar el callback y simular éxito
        ArgumentCaptor<CallbackWithData<List<Coche>>> callbackCaptor =
                ArgumentCaptor.forClass(CallbackWithData.class);
        verify(mockRepository).getCoches(callbackCaptor.capture());

        // Verificar que el estado de carga se establece en true inicialmente
        Boolean initialLoadingState = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        assertTrue("El estado de carga debería ser true al iniciar", initialLoadingState);

        // Simular callback exitoso
        callbackCaptor.getValue().onSuccess(expectedCoches);

        // Verificación
        Boolean finalLoadingState = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        List<Coche> actualCoches = LiveDataTestUtil.getValue(viewModel.getCochesLiveData());

        assertFalse("El estado de carga debería ser false después de completar", finalLoadingState);
        assertEquals("Debería emitir la lista correcta de coches", expectedCoches, actualCoches);
        assertNotNull("La lista de coches no debería ser null", actualCoches);
        assertEquals("Debería tener el número correcto de coches", 2, actualCoches.size());
    }

    @Test
    public void loadCoches_repositoryFailure_setsLoadingStateAndEmitsError() throws InterruptedException {
        // Preparación
        String expectedError = "Network error occurred";

        // Ejecución
        viewModel.loadCoches();

        // Capturar el callback y simular fallo
        ArgumentCaptor<CallbackWithData<List<Coche>>> callbackCaptor =
                ArgumentCaptor.forClass(CallbackWithData.class);
        verify(mockRepository).getCoches(callbackCaptor.capture());

        // Verificar que el estado de carga se establece en true inicialmente
        Boolean initialLoadingState = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        assertTrue("El estado de carga debería ser true al iniciar", initialLoadingState);

        // Simular callback de fallo
        callbackCaptor.getValue().onFailure(expectedError);

        // Verificación
        Boolean finalLoadingState = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        String actualError = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());

        assertFalse("El estado de carga debería ser false después del fallo", finalLoadingState);
        assertEquals("Debería emitir el mensaje de error correcto", expectedError, actualError);
    }

    @Test
    public void loadCoches_emptyList_setsLoadingStateAndEmitsEmptyList() throws InterruptedException {
        // Preparación
        List<Coche> emptyList = new ArrayList<>();

        // Ejecución
        viewModel.loadCoches();

        // Capturar el callback y simular éxito con lista vacía
        ArgumentCaptor<CallbackWithData<List<Coche>>> callbackCaptor =
                ArgumentCaptor.forClass(CallbackWithData.class);
        verify(mockRepository).getCoches(callbackCaptor.capture());
        callbackCaptor.getValue().onSuccess(emptyList);

        // Verificación
        Boolean loadingState = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        List<Coche> actualCoches = LiveDataTestUtil.getValue(viewModel.getCochesLiveData());

        assertFalse("El estado de carga debería ser false", loadingState);
        assertNotNull("La lista de coches no debería ser null", actualCoches);
        assertTrue("La lista de coches debería estar vacía", actualCoches.isEmpty());
    }

    // ========== Tests de addCoche() ==========

    @Test
    public void addCoche_validInputs_successfullyAddsCarAndReloadsData() throws InterruptedException {
        // Preparación
        String matricula = "ABC123";
        String marca = "Toyota";
        String modelo = "Camry";
        String expectedSuccessMessage = "Coche añadido correctamente";
        List<Coche> mockCoches = Arrays.asList(new Coche(matricula, marca, modelo));

        // Ejecución
        viewModel.addCoche(matricula, marca, modelo);

        // Capturar el callback para addCoche
        ArgumentCaptor<Coche> cocheCaptor = ArgumentCaptor.forClass(Coche.class);
        ArgumentCaptor<Callback> addCallbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(mockRepository).addCoche(cocheCaptor.capture(), addCallbackCaptor.capture());

        // Verificar que el estado de carga se establece en true inicialmente
        Boolean initialLoadingState = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        assertTrue("El estado de carga debería ser true al iniciar", initialLoadingState);

        // Verificar que el objeto coche se pasa correctamente al repositorio
        Coche capturedCoche = cocheCaptor.getValue();
        assertEquals("La matrícula debe coincidir", matricula, capturedCoche.getMatricula());
        assertEquals("La marca debe coincidir", marca, capturedCoche.getMarca());
        assertEquals("El modelo debe coincidir", modelo, capturedCoche.getModelo());

        // Simular callback exitoso de addCoche
        addCallbackCaptor.getValue().onSuccess();

        // Capturar el callback para loadCoches (que se llama automáticamente)
        ArgumentCaptor<CallbackWithData<List<Coche>>> loadCallbackCaptor =
                ArgumentCaptor.forClass(CallbackWithData.class);
        verify(mockRepository).getCoches(loadCallbackCaptor.capture());

        // Simular callback exitoso de loadCoches
        loadCallbackCaptor.getValue().onSuccess(mockCoches);

        // Verificación final
        Boolean finalLoadingState = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        String actualSuccessMessage = LiveDataTestUtil.getValue(viewModel.getSuccessLiveData());
        List<Coche> actualCoches = LiveDataTestUtil.getValue(viewModel.getCochesLiveData());

        assertFalse("El estado de carga debería ser false después de completar", finalLoadingState);
        assertEquals("Debería emitir mensaje de éxito", expectedSuccessMessage, actualSuccessMessage);
        assertEquals("Debería cargar la lista actualizada", mockCoches, actualCoches);
    }

    @Test
    public void addCoche_emptyMatricula_emitsValidationError() throws InterruptedException {
        // Preparación
        String expectedError = "Todos los campos son obligatorios";

        // Ejecución
        viewModel.addCoche("", "Toyota", "Camry");

        // Verificación
        String actualError = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("Debería emitir error de validación", expectedError, actualError);

        // Verificar que el método del repositorio no se llama
        verify(mockRepository, never()).addCoche(any(Coche.class), any(Callback.class));
    }

    @Test
    public void addCoche_emptyMarca_emitsValidationError() throws InterruptedException {
        // Preparación
        String expectedError = "Todos los campos son obligatorios";

        // Ejecución
        viewModel.addCoche("ABC123", "", "Camry");

        // Verificación
        String actualError = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("Debería emitir error de validación", expectedError, actualError);

        // Verificar que el método del repositorio no se llama
        verify(mockRepository, never()).addCoche(any(Coche.class), any(Callback.class));
    }

    @Test
    public void addCoche_emptyModelo_emitsValidationError() throws InterruptedException {
        // Preparación
        String expectedError = "Todos los campos son obligatorios";

        // Ejecución
        viewModel.addCoche("ABC123", "Toyota", "");

        // Verificación
        String actualError = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("Debería emitir error de validación", expectedError, actualError);

        // Verificar que el método del repositorio no se llama
        verify(mockRepository, never()).addCoche(any(Coche.class), any(Callback.class));
    }

    @Test
    public void addCoche_whitespaceOnlyInputs_emitsValidationError() throws InterruptedException {
        // Preparación
        String expectedError = "Todos los campos son obligatorios";

        // Ejecución
        viewModel.addCoche("   ", "  ", "   ");

        // Verificación
        String actualError = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());
        assertEquals("Debería emitir error de validación para espacios en blanco", expectedError, actualError);

        // Verificar que el método del repositorio no se llama
        verify(mockRepository, never()).addCoche(any(Coche.class), any(Callback.class));
    }

    @Test
    public void addCoche_inputsWithWhitespace_trimsAndSucceeds() throws InterruptedException {
        // Preparación
        String matriculaWithSpaces = "  ABC123  ";
        String marcaWithSpaces = "  Toyota  ";
        String modeloWithSpaces = "  Camry  ";

        // Ejecución
        viewModel.addCoche(matriculaWithSpaces, marcaWithSpaces, modeloWithSpaces);

        // Capturar el objeto coche
        ArgumentCaptor<Coche> cocheCaptor = ArgumentCaptor.forClass(Coche.class);
        verify(mockRepository).addCoche(cocheCaptor.capture(), any(Callback.class));

        // Verificación
        Coche capturedCoche = cocheCaptor.getValue();
        assertEquals("La matrícula debe estar recortada", "ABC123", capturedCoche.getMatricula());
        assertEquals("La marca debe estar recortada", "Toyota", capturedCoche.getMarca());
        assertEquals("El modelo debe estar recortado", "Camry", capturedCoche.getModelo());
    }

    @Test
    public void addCoche_repositoryFailure_setsLoadingStateAndEmitsError() throws InterruptedException {
        // Preparación
        String expectedError = "Database error occurred";

        // Ejecución
        viewModel.addCoche("ABC123", "Toyota", "Camry");

        // Capturar el callback y simular fallo
        ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(mockRepository).addCoche(any(Coche.class), callbackCaptor.capture());

        // Simular callback de fallo
        callbackCaptor.getValue().onFailure(expectedError);

        // Verificación
        Boolean loadingState = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        String actualError = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());

        assertFalse("El estado de carga debería ser false después del fallo", loadingState);
        assertEquals("Debería emitir el error del repositorio", expectedError, actualError);
    }

    // ========== Tests de deleteCoche() ==========

    @Test
    public void deleteCoche_validMatricula_successfullyDeletesCarAndReloadsData() throws InterruptedException {
        // Preparación
        String matricula = "ABC123";
        String expectedSuccessMessage = "Coche eliminado correctamente";
        List<Coche> mockCoches = new ArrayList<>(); // Lista vacía después de eliminar

        // Ejecución
        viewModel.deleteCoche(matricula);

        // Capturar el callback para deleteCoche
        ArgumentCaptor<Callback> deleteCallbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(mockRepository).deleteCoche(eq(matricula), deleteCallbackCaptor.capture());

        // Verificar que el estado de carga se establece en true inicialmente
        Boolean initialLoadingState = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        assertTrue("El estado de carga debería ser true al iniciar", initialLoadingState);

        // Simular callback exitoso de deleteCoche
        deleteCallbackCaptor.getValue().onSuccess();

        // Capturar el callback para loadCoches (que se llama automáticamente)
        ArgumentCaptor<CallbackWithData<List<Coche>>> loadCallbackCaptor =
                ArgumentCaptor.forClass(CallbackWithData.class);
        verify(mockRepository).getCoches(loadCallbackCaptor.capture());

        // Simular callback exitoso de loadCoches
        loadCallbackCaptor.getValue().onSuccess(mockCoches);

        // Verificación final
        Boolean finalLoadingState = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        String actualSuccessMessage = LiveDataTestUtil.getValue(viewModel.getSuccessLiveData());
        List<Coche> actualCoches = LiveDataTestUtil.getValue(viewModel.getCochesLiveData());

        assertFalse("El estado de carga debería ser false después de completar", finalLoadingState);
        assertEquals("Debería emitir mensaje de éxito", expectedSuccessMessage, actualSuccessMessage);
        assertEquals("Debería cargar la lista actualizada", mockCoches, actualCoches);
    }

    @Test
    public void deleteCoche_repositoryFailure_setsLoadingStateAndEmitsError() throws InterruptedException {
        // Preparación
        String matricula = "ABC123";
        String expectedError = "Failed to delete car";

        // Ejecución
        viewModel.deleteCoche(matricula);

        // Capturar el callback y simular fallo
        ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(mockRepository).deleteCoche(eq(matricula), callbackCaptor.capture());

        // Verificar que el estado de carga se establece en true inicialmente
        Boolean initialLoadingState = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        assertTrue("El estado de carga debería ser true al iniciar", initialLoadingState);

        // Simular callback de fallo
        callbackCaptor.getValue().onFailure(expectedError);

        // Verificación
        Boolean finalLoadingState = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        String actualError = LiveDataTestUtil.getValue(viewModel.getErrorLiveData());

        assertFalse("El estado de carga debería ser false después del fallo", finalLoadingState);
        assertEquals("Debería emitir el error del repositorio", expectedError, actualError);
    }

    @Test
    public void deleteCoche_nullMatricula_callsRepositoryWithNull() throws InterruptedException {
        // Preparación
        String nullMatricula = null;

        // Ejecución
        viewModel.deleteCoche(nullMatricula);

        // Verificación
        verify(mockRepository).deleteCoche(eq(nullMatricula), any(Callback.class));

        Boolean loadingState = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        assertTrue("El estado de carga debería ser true", loadingState);
    }

    @Test
    public void deleteCoche_emptyMatricula_callsRepositoryWithEmptyString() throws InterruptedException {
        // Preparación
        String emptyMatricula = "";

        // Ejecución
        viewModel.deleteCoche(emptyMatricula);

        // Verificación
        verify(mockRepository).deleteCoche(eq(emptyMatricula), any(Callback.class));

        Boolean loadingState = LiveDataTestUtil.getValue(viewModel.getIsLoadingLiveData());
        assertTrue("El estado de carga debería ser true", loadingState);
    }
}