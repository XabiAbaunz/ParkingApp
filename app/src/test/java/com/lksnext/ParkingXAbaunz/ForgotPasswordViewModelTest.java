package com.lksnext.ParkingXAbaunz;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.utils.LiveDataTestUtil;
import com.lksnext.ParkingXAbaunz.viewmodel.ForgotPasswordViewModel;

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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ForgotPasswordViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private DataRepository mockDataRepository;

    private MockedStatic<DataRepository> mockedStaticDataRepository;
    private ForgotPasswordViewModel viewModel;
    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Mockear el singleton de DataRepository
        mockedStaticDataRepository = mockStatic(DataRepository.class);
        mockedStaticDataRepository.when(DataRepository::getInstance).thenReturn(mockDataRepository);

        viewModel = new ForgotPasswordViewModel();
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

    // Escenario de caso feliz
    @Test
    public void sendResetPasswordEmail_validEmail_sendsEmailSuccessfully() throws InterruptedException {
        // Dado
        String validEmail = "test@example.com";

        // Mockear llamada exitosa al repositorio
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockDataRepository).sendPasswordResetEmail(eq(validEmail), any(Callback.class));

        // Cuando
        viewModel.sendResetPasswordEmail(validEmail);

        // Entonces
        Boolean resetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());
        String error = LiveDataTestUtil.getValue(viewModel.getError());

        assertTrue("El email de reseteo debería enviarse exitosamente", resetEmailSent);
        assertNull("El error debería ser nulo en caso de éxito", error);

        // Verificar que el método del repositorio fue llamado
        verify(mockDataRepository).sendPasswordResetEmail(eq(validEmail), any(Callback.class));
    }

    @Test
    public void sendResetPasswordEmail_validEmail_clearsErrorAndResetState() throws InterruptedException {
        // Dado
        String validEmail = "user@domain.com";
        String invalidEmail = "email-invalido";

        // Establecer estado de error inicial con un email inválido
        viewModel.sendResetPasswordEmail(invalidEmail); // Esto establecerá un error

        // Mockear llamada exitosa al repositorio para email válido
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockDataRepository).sendPasswordResetEmail(eq(validEmail), any(Callback.class));

        // Cuando
        viewModel.sendResetPasswordEmail(validEmail);

        // Entonces
        String error = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean resetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());

        assertNull("El error debería ser limpiado antes de enviar", error);
        assertTrue("El email de reseteo debería ser enviado", resetEmailSent);
    }

    // Escenarios de error - Email vacío
    @Test
    public void sendResetPasswordEmail_emptyEmail_setsErrorMessage() throws InterruptedException {
        // Dado
        String emptyEmail = "";

        // Cuando
        viewModel.sendResetPasswordEmail(emptyEmail);

        // Entonces
        String error = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean resetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());

        assertEquals("Debería mostrar error de email vacío", "Por favor, introduce tu correo electrónico.", error);
        assertNull("El email de reseteo enviado debería ser nulo", resetEmailSent);

        // Verificar que el método del repositorio no fue llamado
        verifyNoInteractions(mockDataRepository);
    }

    @Test
    public void sendResetPasswordEmail_nullEmail_setsErrorMessage() throws InterruptedException {
        // Dado
        String nullEmail = null;

        // Cuando
        viewModel.sendResetPasswordEmail(nullEmail);

        // Entonces
        String error = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean resetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());

        assertEquals("Debería mostrar error de email vacío para nulo", "Por favor, introduce tu correo electrónico.", error);
        assertNull("El email de reseteo enviado debería ser nulo", resetEmailSent);

        // Verificar que el método del repositorio no fue llamado
        verifyNoInteractions(mockDataRepository);
    }

    // Escenarios de error - Formato de email inválido
    @Test
    public void sendResetPasswordEmail_invalidEmailFormat_setsErrorMessage() throws InterruptedException {
        // Dado
        String invalidEmail = "invalid-email";

        // Cuando
        viewModel.sendResetPasswordEmail(invalidEmail);

        // Entonces
        String error = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean resetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());

        assertEquals("Debería mostrar error de formato de email inválido", "El formato del email no es válido.", error);
        assertNull("El email de reseteo enviado debería ser nulo", resetEmailSent);

        // Verificar que el método del repositorio no fue llamado
        verifyNoInteractions(mockDataRepository);
    }

    @Test
    public void sendResetPasswordEmail_invalidEmailWithoutDomain_setsErrorMessage() throws InterruptedException {
        // Dado
        String invalidEmail = "test@";

        // Cuando
        viewModel.sendResetPasswordEmail(invalidEmail);

        // Entonces
        String error = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean resetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());

        assertEquals("Debería mostrar error de formato de email inválido", "El formato del email no es válido.", error);
        assertNull("El email de reseteo enviado debería ser nulo", resetEmailSent);

        // Verificar que el método del repositorio no fue llamado
        verifyNoInteractions(mockDataRepository);
    }

    @Test
    public void sendResetPasswordEmail_invalidEmailWithoutAtSymbol_setsErrorMessage() throws InterruptedException {
        // Dado
        String invalidEmail = "testexample.com";

        // Cuando
        viewModel.sendResetPasswordEmail(invalidEmail);

        // Entonces
        String error = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean resetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());

        assertEquals("Debería mostrar error de formato de email inválido", "El formato del email no es válido.", error);
        assertNull("El email de reseteo enviado debería ser nulo", resetEmailSent);

        // Verificar que el método del repositorio no fue llamado
        verifyNoInteractions(mockDataRepository);
    }

    // Escenarios de fallo del repositorio
    @Test
    public void sendResetPasswordEmail_repositoryFailure_setsErrorMessage() throws InterruptedException {
        // Dado
        String validEmail = "test@example.com";
        String expectedErrorMessage = "Error de red";

        // Mockear fallo del repositorio
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onFailure(expectedErrorMessage);
            return null;
        }).when(mockDataRepository).sendPasswordResetEmail(eq(validEmail), any(Callback.class));

        // Cuando
        viewModel.sendResetPasswordEmail(validEmail);

        // Entonces
        String error = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean resetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());

        assertEquals("Debería mostrar mensaje de error del repositorio", expectedErrorMessage, error);
        assertFalse("El email de reseteo enviado debería ser falso en caso de fallo", resetEmailSent);

        // Verificar que el método del repositorio fue llamado
        verify(mockDataRepository).sendPasswordResetEmail(eq(validEmail), any(Callback.class));
    }

    @Test
    public void sendResetPasswordEmail_repositoryFailureWithNullMessage_setsNullError() throws InterruptedException {
        // Dado
        String validEmail = "test@example.com";

        // Mockear fallo del repositorio con mensaje nulo
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onFailure(null);
            return null;
        }).when(mockDataRepository).sendPasswordResetEmail(eq(validEmail), any(Callback.class));

        // Cuando
        viewModel.sendResetPasswordEmail(validEmail);

        // Entonces
        String error = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean resetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());

        assertNull("El error debería ser nulo cuando el repositorio envía mensaje nulo", error);
        assertFalse("El email de reseteo enviado debería ser falso en caso de fallo", resetEmailSent);
    }

    @Test
    public void sendResetPasswordEmail_whitespaceOnlyEmail_setsErrorMessage() throws InterruptedException {
        // Dado
        String whitespaceEmail = "   ";

        // Cuando
        viewModel.sendResetPasswordEmail(whitespaceEmail);

        // Entonces
        String error = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean resetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());

        // Change the expected message to match your implementation
        assertEquals("Debería mostrar error de formato para espacios en blanco", "El formato del email no es válido.", error);
        assertNull("El email de reseteo enviado debería ser nulo", resetEmailSent);

        // Verificar que el método del repositorio no fue llamado
        verifyNoInteractions(mockDataRepository);
    }

    @Test
    public void sendResetPasswordEmail_emailWithSpaces_setsErrorMessage() throws InterruptedException {
        // Dado
        String emailWithSpaces = "test @example.com";

        // Cuando
        viewModel.sendResetPasswordEmail(emailWithSpaces);

        // Entonces
        String error = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean resetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());

        assertEquals("Debería mostrar error de formato de email inválido para email con espacios", "El formato del email no es válido.", error);
        assertNull("El email de reseteo enviado debería ser nulo", resetEmailSent);

        // Verificar que el método del repositorio no fue llamado
        verifyNoInteractions(mockDataRepository);
    }

    @Test
    public void sendResetPasswordEmail_validEmailWithUppercase_sendsEmailSuccessfully() throws InterruptedException {
        // Dado
        String uppercaseEmail = "TEST@EXAMPLE.COM";

        // Mockear llamada exitosa al repositorio
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockDataRepository).sendPasswordResetEmail(eq(uppercaseEmail), any(Callback.class));

        // Cuando
        viewModel.sendResetPasswordEmail(uppercaseEmail);

        // Entonces
        Boolean resetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());
        String error = LiveDataTestUtil.getValue(viewModel.getError());

        assertTrue("El email de reseteo debería ser enviado para email en mayúsculas", resetEmailSent);
        assertNull("El error debería ser nulo", error);

        // Verificar que el método del repositorio fue llamado con el email original
        verify(mockDataRepository).sendPasswordResetEmail(eq(uppercaseEmail), any(Callback.class));
    }

    @Test
    public void sendResetPasswordEmail_validEmailWithNumbers_sendsEmailSuccessfully() throws InterruptedException {
        // Dado
        String emailWithNumbers = "user123@example.com";

        // Mockear llamada exitosa al repositorio
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockDataRepository).sendPasswordResetEmail(eq(emailWithNumbers), any(Callback.class));

        // Cuando
        viewModel.sendResetPasswordEmail(emailWithNumbers);

        // Entonces
        Boolean resetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());
        String error = LiveDataTestUtil.getValue(viewModel.getError());

        assertTrue("El email de reseteo debería ser enviado para email con números", resetEmailSent);
        assertNull("El error debería ser nulo", error);

        // Verificar que el método del repositorio fue llamado
        verify(mockDataRepository).sendPasswordResetEmail(eq(emailWithNumbers), any(Callback.class));
    }

    @Test
    public void sendResetPasswordEmail_validEmailWithDots_sendsEmailSuccessfully() throws InterruptedException {
        // Dado
        String emailWithDots = "user.name@example.com";

        // Mockear llamada exitosa al repositorio
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockDataRepository).sendPasswordResetEmail(eq(emailWithDots), any(Callback.class));

        // Cuando
        viewModel.sendResetPasswordEmail(emailWithDots);

        // Entonces
        Boolean resetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());
        String error = LiveDataTestUtil.getValue(viewModel.getError());

        assertTrue("El email de reseteo debería ser enviado para email con puntos", resetEmailSent);
        assertNull("El error debería ser nulo", error);

        // Verificar que el método del repositorio fue llamado
        verify(mockDataRepository).sendPasswordResetEmail(eq(emailWithDots), any(Callback.class));
    }

    // Pruebas de gestión de estado
    @Test
    public void sendResetPasswordEmail_callTwiceWithDifferentEmails_managesStateCorrectly() throws InterruptedException {
        // Dado
        String firstEmail = "first@example.com";
        String secondEmail = "second@example.com";

        // Mockear primera llamada para fallar
        doAnswer(invocation -> {
            String email = invocation.getArgument(0);
            Callback callback = invocation.getArgument(1);
            if (email.equals(firstEmail)) {
                callback.onFailure("Primera llamada falló");
            } else {
                callback.onSuccess();
            }
            return null;
        }).when(mockDataRepository).sendPasswordResetEmail(any(String.class), any(Callback.class));

        // Cuando - primera llamada
        viewModel.sendResetPasswordEmail(firstEmail);

        // Entonces - verificar resultados de primera llamada
        String firstError = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean firstResetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());

        assertEquals("Debería mostrar primer error", "Primera llamada falló", firstError);
        assertFalse("El primer reseteo debería ser falso", firstResetEmailSent);

        // Cuando - segunda llamada
        viewModel.sendResetPasswordEmail(secondEmail);

        // Entonces - verificar resultados de segunda llamada
        String secondError = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean secondResetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());

        assertNull("El segundo error debería ser nulo", secondError);
        assertTrue("El segundo reseteo debería ser verdadero", secondResetEmailSent);

        // Verificar que ambas llamadas fueron realizadas
        verify(mockDataRepository).sendPasswordResetEmail(eq(firstEmail), any(Callback.class));
        verify(mockDataRepository).sendPasswordResetEmail(eq(secondEmail), any(Callback.class));
    }

    @Test
    public void sendResetPasswordEmail_callbackInvoked_verifyCallbackParameters() {
        // Dado
        String validEmail = "test@example.com";
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);

        // Cuando
        viewModel.sendResetPasswordEmail(validEmail);

        // Entonces - verificar que el callback fue pasado correctamente
        verify(mockDataRepository).sendPasswordResetEmail(emailCaptor.capture(), callbackCaptor.capture());

        assertEquals("El parámetro de email debería coincidir", validEmail, emailCaptor.getValue());
        assertNotNull("El callback no debería ser nulo", callbackCaptor.getValue());
    }

    @Test
    public void sendResetPasswordEmail_multipleDotsInEmail_setsErrorMessage() throws InterruptedException {
        // Dado
        String invalidEmail = "test..email@example.com";

        // Cuando
        viewModel.sendResetPasswordEmail(invalidEmail);

        // Entonces
        String error = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean resetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());

        assertEquals("Debería mostrar error de formato de email inválido para múltiples puntos", "El formato del email no es válido.", error);
        assertNull("El email de reseteo enviado debería ser nulo", resetEmailSent);

        // Verificar que el método del repositorio no fue llamado
        verifyNoInteractions(mockDataRepository);
    }

    @Test
    public void sendResetPasswordEmail_emailStartingWithDot_setsErrorMessage() throws InterruptedException {
        // Dado
        String invalidEmail = ".test@example.com";

        // Cuando
        viewModel.sendResetPasswordEmail(invalidEmail);

        // Entonces
        String error = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean resetEmailSent = LiveDataTestUtil.getValue(viewModel.getResetEmailSent());

        assertEquals("Debería mostrar error de formato de email inválido para email que empieza con punto", "El formato del email no es válido.", error);
        assertNull("El email de reseteo enviado debería ser nulo", resetEmailSent);

        // Verificar que el método del repositorio no fue llamado
        verifyNoInteractions(mockDataRepository);
    }
}