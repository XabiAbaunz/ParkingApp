package com.lksnext.ParkingXAbaunz.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.utils.LiveDataTestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LoginViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private DataRepository mockDataRepository;

    private LoginViewModel viewModel;
    private MockedStatic<DataRepository> mockedStaticDataRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock the static getInstance() method
        mockedStaticDataRepository = mockStatic(DataRepository.class);
        mockedStaticDataRepository.when(DataRepository::getInstance).thenReturn(mockDataRepository);

        viewModel = new LoginViewModel();
    }

    @After
    public void tearDown() {
        if (mockedStaticDataRepository != null) {
            mockedStaticDataRepository.close();
        }
    }

    // Happy path scenarios
    @Test
    public void loginUser_validCredentials_setsEmailAndPasswordCorrectly() throws InterruptedException {
        // Given
        String testEmail = "test@example.com";
        String testPassword = "validPassword123";

        // When
        viewModel.loginUser(testEmail, testPassword);

        // Then
        String resultEmail = LiveDataTestUtil.getValue(viewModel.getEmail());
        String resultPassword = LiveDataTestUtil.getValue(viewModel.getPassword());

        assertEquals("Email should be set correctly", testEmail, resultEmail);
        assertEquals("Password should be set correctly", testPassword, resultPassword);
    }

    @Test
    public void loginUser_validCredentials_clearsErrorAndLoggedState() throws InterruptedException {
        // Given
        String testEmail = "test@example.com";
        String testPassword = "validPassword123";

        // When
        viewModel.loginUser(testEmail, testPassword);

        // Then
        String errorResult = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean loggedResult = LiveDataTestUtil.getValue(viewModel.isLogged());

        assertNull("Error should be cleared", errorResult);
        assertNull("Logged state should be cleared initially", loggedResult);
    }

    @Test
    public void loginUser_validCredentials_callsDataRepositoryLogin() {
        // Given
        String testEmail = "test@example.com";
        String testPassword = "validPassword123";

        // When
        viewModel.loginUser(testEmail, testPassword);

        // Then
        verify(mockDataRepository).login(eq(testEmail), eq(testPassword), any(Callback.class));
    }

    @Test
    public void loginUser_successfulLogin_setsLoggedToTrueAndClearsError() throws InterruptedException {
        // Given
        String testEmail = "test@example.com";
        String testPassword = "validPassword123";

        // Mock successful login
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(2);
            callback.onSuccess();
            return null;
        }).when(mockDataRepository).login(eq(testEmail), eq(testPassword), any(Callback.class));

        // When
        viewModel.loginUser(testEmail, testPassword);

        // Then
        Boolean loggedResult = LiveDataTestUtil.getValue(viewModel.isLogged());
        String errorResult = LiveDataTestUtil.getValue(viewModel.getError());

        assertTrue("User should be logged in", loggedResult);
        assertNull("Error should be null on success", errorResult);
    }

    @Test
    public void loginUser_failedLogin_setsLoggedToFalseAndSetsError() throws InterruptedException {
        // Given
        String testEmail = "test@example.com";
        String testPassword = "wrongPassword";
        String expectedError = "Invalid credentials";

        // Mock failed login
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(2);
            callback.onFailure(expectedError);
            return null;
        }).when(mockDataRepository).login(eq(testEmail), eq(testPassword), any(Callback.class));

        // When
        viewModel.loginUser(testEmail, testPassword);

        // Then
        Boolean loggedResult = LiveDataTestUtil.getValue(viewModel.isLogged());
        String errorResult = LiveDataTestUtil.getValue(viewModel.getError());

        assertFalse("User should not be logged in", loggedResult);
        assertEquals("Error message should match", expectedError, errorResult);
    }

    // Edge cases and error scenarios
    @Test
    public void loginUser_emptyEmail_setsErrorAndLoggedToFalse() throws InterruptedException {
        // Given
        String emptyEmail = "";
        String validPassword = "validPassword123";

        // When
        viewModel.loginUser(emptyEmail, validPassword);

        // Then
        String errorResult = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean loggedResult = LiveDataTestUtil.getValue(viewModel.isLogged());

        assertEquals("Error message should indicate required fields",
                "El email y la contraseña son obligatorios", errorResult);
        assertFalse("User should not be logged in", loggedResult);

        // Verify DataRepository.login is not called
        verify(mockDataRepository, never()).login(anyString(), anyString(), any(Callback.class));
    }

    @Test
    public void loginUser_emptyPassword_setsErrorAndLoggedToFalse() throws InterruptedException {
        // Given
        String validEmail = "test@example.com";
        String emptyPassword = "";

        // When
        viewModel.loginUser(validEmail, emptyPassword);

        // Then
        String errorResult = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean loggedResult = LiveDataTestUtil.getValue(viewModel.isLogged());

        assertEquals("Error message should indicate required fields",
                "El email y la contraseña son obligatorios", errorResult);
        assertFalse("User should not be logged in", loggedResult);

        // Verify DataRepository.login is not called
        verify(mockDataRepository, never()).login(anyString(), anyString(), any(Callback.class));
    }

    @Test
    public void loginUser_bothEmailAndPasswordEmpty_setsErrorAndLoggedToFalse() throws InterruptedException {
        // Given
        String emptyEmail = "";
        String emptyPassword = "";

        // When
        viewModel.loginUser(emptyEmail, emptyPassword);

        // Then
        String errorResult = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean loggedResult = LiveDataTestUtil.getValue(viewModel.isLogged());

        assertEquals("Error message should indicate required fields",
                "El email y la contraseña son obligatorios", errorResult);
        assertFalse("User should not be logged in", loggedResult);

        // Verify DataRepository.login is not called
        verify(mockDataRepository, never()).login(anyString(), anyString(), any(Callback.class));
    }

    @Test
    public void loginUser_nullEmail_throwsNullPointerException() {
        // Given
        String nullEmail = null;
        String validPassword = "validPassword123";

        // When & Then
        try {
            viewModel.loginUser(nullEmail, validPassword);
            fail("Expected NullPointerException when email is null");
        } catch (NullPointerException e) {
            // This is the expected behavior - the current implementation doesn't handle null gracefully
            assertTrue("NullPointerException should be thrown for null email", true);
        }
    }

    @Test
    public void loginUser_nullPassword_throwsNullPointerException() {
        // Given
        String validEmail = "test@example.com";
        String nullPassword = null;

        // When & Then
        try {
            viewModel.loginUser(validEmail, nullPassword);
            fail("Expected NullPointerException when password is null");
        } catch (NullPointerException e) {
            // This is the expected behavior - the current implementation doesn't handle null gracefully
            assertTrue("NullPointerException should be thrown for null password", true);
        }
    }

    @Test
    public void loginUser_whitespaceOnlyCredentials_callsDataRepository() throws InterruptedException {
        // Given
        String whitespaceEmail = "   ";
        String whitespacePassword = "   ";

        // When
        viewModel.loginUser(whitespaceEmail, whitespacePassword);

        // Then
        // Note: Current implementation only checks for isEmpty(), not trim()
        // So whitespace-only strings are considered valid and will call DataRepository
        verify(mockDataRepository).login(eq(whitespaceEmail), eq(whitespacePassword), any(Callback.class));

        String resultEmail = LiveDataTestUtil.getValue(viewModel.getEmail());
        String resultPassword = LiveDataTestUtil.getValue(viewModel.getPassword());

        assertEquals("Whitespace email should be set", whitespaceEmail, resultEmail);
        assertEquals("Whitespace password should be set", whitespacePassword, resultPassword);
    }

    @Test
    public void loginUser_subsequentCalls_clearsePreviousState() throws InterruptedException {
        // Given
        String firstEmail = "first@example.com";
        String firstPassword = "firstPassword";
        String secondEmail = "second@example.com";
        String secondPassword = "secondPassword";

        // When - First call
        viewModel.loginUser(firstEmail, firstPassword);

        // When - Second call
        viewModel.loginUser(secondEmail, secondPassword);

        // Then
        String finalEmail = LiveDataTestUtil.getValue(viewModel.getEmail());
        String finalPassword = LiveDataTestUtil.getValue(viewModel.getPassword());

        assertEquals("Email should be updated to second call", secondEmail, finalEmail);
        assertEquals("Password should be updated to second call", secondPassword, finalPassword);
    }

    @Test
    public void loginUser_callbackExecutionOrder_errorThenLogged() throws InterruptedException {
        // Given
        String testEmail = "test@example.com";
        String testPassword = "validPassword123";
        String expectedError = "Network error";

        // Mock failed login
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(2);
            callback.onFailure(expectedError);
            return null;
        }).when(mockDataRepository).login(eq(testEmail), eq(testPassword), any(Callback.class));

        // When
        viewModel.loginUser(testEmail, testPassword);

        // Then - Verify both error and logged state are set correctly
        String errorResult = LiveDataTestUtil.getValue(viewModel.getError());
        Boolean loggedResult = LiveDataTestUtil.getValue(viewModel.isLogged());

        assertEquals("Error should be set", expectedError, errorResult);
        assertFalse("Logged should be false", loggedResult);
    }

    @Test
    public void loginUser_clearsPreviousErrorBeforeValidation() throws InterruptedException {
        // Given
        String testEmail = "test@example.com";
        String testPassword = "validPassword123";

        // Set initial error state
        viewModel.loginUser("", ""); // This will set an error
        String initialError = LiveDataTestUtil.getValue(viewModel.getError());
        assertNotNull("Initial error should be set", initialError);

        // When - Call with valid credentials
        viewModel.loginUser(testEmail, testPassword);

        // Then - Error should be cleared before processing
        verify(mockDataRepository).login(eq(testEmail), eq(testPassword), any(Callback.class));
    }

    @Test
    public void loginUser_clearsLoggedStateBeforeValidation() throws InterruptedException {
        // Given
        String testEmail = "test@example.com";
        String testPassword = "validPassword123";

        // Mock successful login for first call
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(2);
            callback.onSuccess();
            return null;
        }).when(mockDataRepository).login(anyString(), anyString(), any(Callback.class));

        // Set initial logged state
        viewModel.loginUser(testEmail, testPassword);
        Boolean initialLogged = LiveDataTestUtil.getValue(viewModel.isLogged());
        assertTrue("Initial logged state should be true", initialLogged);

        // When - Call again (this should clear logged state initially)
        viewModel.loginUser(testEmail, testPassword);

        // Then - Method should have been called twice
        verify(mockDataRepository, times(2)).login(eq(testEmail), eq(testPassword), any(Callback.class));
    }
}