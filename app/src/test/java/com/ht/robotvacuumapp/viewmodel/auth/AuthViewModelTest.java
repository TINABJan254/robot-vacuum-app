package com.ht.robotvacuumapp.viewmodel.auth;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import com.google.gson.Gson;
import com.ht.robotvacuumapp.api.dto.LoginResponse;
import com.ht.robotvacuumapp.api.dto.RegisterResponse;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AuthViewModelTest {
    @Rule public InstantTaskExecutorRule executor = new InstantTaskExecutorRule();
    private AuthRepository repository;

    @Before public void setUp() { repository = mock(AuthRepository.class); }

    @Test public void loginValidatesBeforeSending() {
        LoginViewModel vm = new LoginViewModel(repository);
        vm.login("  ", "", false);
        assertNotNull(vm.getUiState().getValue().usernameError);
        assertNotNull(vm.getUiState().getValue().passwordError);
        verifyNoInteractions(repository);
    }

    @Test public void loginTrimsInputBlocksDuplicatesAndAcknowledgesSuccess() {
        LoginViewModel vm = new LoginViewModel(repository);
        vm.login(" user ", " secret ", true);
        vm.login("user", "secret", true);
        assertTrue(vm.getUiState().getValue().loading);
        ArgumentCaptor<AuthRepository.AuthCallback<LoginResponse>> callback = loginCallback();
        verify(repository, times(1)).login(eq("user"), eq("secret"), callback.capture());
        LoginResponse response = new Gson().fromJson("{\"accessToken\":\"token\",\"user\":{\"name\":\"Test\"}}", LoginResponse.class);
        callback.getValue().onSuccess(response);
        assertFalse(vm.getUiState().getValue().loading);
        assertTrue(vm.getUiState().getValue().loginSucceeded);
        assertEquals("Test", vm.getUiState().getValue().displayName);
        verify(repository).saveLoginPreference("user", true);
        vm.acknowledgeFeedback();
        assertFalse(vm.getUiState().getValue().loginSucceeded);
    }

    @Test public void loginErrorAllowsRetryAndCannotChangeServerDuringRequest() {
        LoginViewModel vm = new LoginViewModel(repository);
        vm.login("user", "password", false);
        vm.setServer("http://other/", false);
        verify(repository, never()).setBaseUrl(anyString());
        ArgumentCaptor<AuthRepository.AuthCallback<LoginResponse>> callback = loginCallback();
        verify(repository).login(eq("user"), eq("password"), callback.capture());
        callback.getValue().onError("Offline");
        assertEquals("Offline", vm.getUiState().getValue().message);
        assertFalse(vm.getUiState().getValue().loading);
        vm.acknowledgeFeedback();
        assertNull(vm.getUiState().getValue().message);
        vm.login("user", "password", false);
        verify(repository, times(2)).login(eq("user"), eq("password"), any());
    }

    @Test public void registerValidatesRequiredFieldsLengthAndConfirmation() {
        RegisterViewModel vm = new RegisterViewModel(repository);
        vm.register("", "", "", "");
        assertNotNull(vm.getUiState().getValue().nameError);
        assertNotNull(vm.getUiState().getValue().usernameError);
        vm.register("Name", "user", "short", "short");
        assertNotNull(vm.getUiState().getValue().passwordError);
        vm.register("Name", "user", "password1", "different");
        assertNotNull(vm.getUiState().getValue().confirmError);
        verifyNoInteractions(repository);
    }

    @Test public void passwordStrengthSupportsAllExistingLevels() {
        RegisterViewModel vm = new RegisterViewModel(repository);
        String[] passwords = {"", "a", "Abcdefgh", "Abcdefg1", "Abcdef1!"};
        for (int score = 0; score < passwords.length; score++) {
            vm.updatePasswordStrength(passwords[score]);
            assertEquals(score, vm.getUiState().getValue().passwordStrength);
        }
        verifyNoInteractions(repository);
    }

    @Test public void registerBlocksDuplicateRequestsAndReturnsUsernameWithoutLogin() {
        RegisterViewModel vm = new RegisterViewModel(repository);
        vm.register(" Name ", " user ", " password ", " password ");
        vm.register("Name", "user", "password", "password");
        ArgumentCaptor<AuthRepository.AuthCallback<RegisterResponse>> callback = registerCallback();
        verify(repository, times(1)).register(eq("Name"), eq("user"), eq("password"), callback.capture());
        assertTrue(vm.getUiState().getValue().loading);
        callback.getValue().onSuccess(new RegisterResponse());
        assertFalse(vm.getUiState().getValue().loading);
        assertEquals("user", vm.getUiState().getValue().registeredUsername);
        verify(repository, never()).login(anyString(), anyString(), any());
        verify(repository, never()).saveLoginPreference(anyString(), anyBoolean());
        vm.acknowledgeFeedback();
        assertNull(vm.getUiState().getValue().registeredUsername);
    }

    @Test public void registerErrorClearsLoadingAndIsAcknowledged() {
        RegisterViewModel vm = new RegisterViewModel(repository);
        vm.register("Name", "user", "password", "password");
        ArgumentCaptor<AuthRepository.AuthCallback<RegisterResponse>> callback = registerCallback();
        verify(repository).register(eq("Name"), eq("user"), eq("password"), callback.capture());
        callback.getValue().onError("Username exists");
        assertFalse(vm.getUiState().getValue().loading);
        assertEquals("Username exists", vm.getUiState().getValue().message);
        vm.acknowledgeFeedback();
        assertNull(vm.getUiState().getValue().message);
    }

    @Test public void storeRetainsRequestUntilClearedAndLateCallbackIsIgnored() {
        ViewModelStore store = new ViewModelStore();
        ViewModelProvider.Factory factory = new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
                return type.cast(new LoginViewModel(repository));
            }
        };
        LoginViewModel first = new ViewModelProvider(store, factory).get(LoginViewModel.class);
        first.login("user", "password", false);
        LoginViewModel recreated = new ViewModelProvider(store, factory).get(LoginViewModel.class);
        assertSame(first, recreated);
        assertTrue(recreated.getUiState().getValue().loading);
        verify(repository, never()).cancelPendingRequests();
        ArgumentCaptor<AuthRepository.AuthCallback<LoginResponse>> callback = loginCallback();
        verify(repository).login(eq("user"), eq("password"), callback.capture());
        store.clear();
        verify(repository).cancelPendingRequests();
        callback.getValue().onError("Late response");
        assertNull(first.getUiState().getValue().message);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ArgumentCaptor<AuthRepository.AuthCallback<LoginResponse>> loginCallback() {
        return ArgumentCaptor.forClass((Class) AuthRepository.AuthCallback.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ArgumentCaptor<AuthRepository.AuthCallback<RegisterResponse>> registerCallback() {
        return ArgumentCaptor.forClass((Class) AuthRepository.AuthCallback.class);
    }
}
