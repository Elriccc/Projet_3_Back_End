package com.openclassrooms.datashare.validator;

import com.openclassrooms.datashare.entities.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("RegisterTests")
@DisplayName("Créer un utilisateur")
public class UserValidatorTest {
    private static final String LOGIN = "test@gmail.com";
    private static final String PASSWORD = "correctPassword1234*";

    private static final String NOT_AN_EMAIL_LOGIN = "incorrectLogin";
    private static final String TOO_SHORT_PASSWORD = "2short";

    private User user;

    @InjectMocks
    private UserValidator validator;

    @BeforeEach
    public void init(){
        user = new User();
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
    }

    @DisplayName("Avec un login qui n'est pas un email renvoie une erreur")
    @Test
    public void test_user_validator_with_not_an_email_login_throws_IllegalArgumentException(){
        //GIVEN
        Errors errors = Mockito.mock(Errors.class, Answers.CALLS_REAL_METHODS);
        doThrow(new IllegalArgumentException()).when(errors).rejectValue(any(String.class), any(String.class), any(String.class));
        user.setLogin(NOT_AN_EMAIL_LOGIN);

        //THEN
        Assertions.assertThrows(IllegalArgumentException.class, () -> validator.validate(user, errors));
        verify(errors, times(1)).rejectValue(any(String.class), any(String.class), any(String.class));
    }

    @DisplayName("Avec un mot de passe de moins de 8 caractères renvoie une erreur")
    @Test
    public void test_user_validator_with_too_short_password_throws_IllegalArgumentException(){
        //GIVEN
        Errors errors = Mockito.mock(Errors.class, Answers.CALLS_REAL_METHODS);
        doThrow(new IllegalArgumentException()).when(errors).rejectValue(any(String.class), any(String.class), any(String.class));
        user.setPassword(TOO_SHORT_PASSWORD);

        //THEN
        Assertions.assertThrows(IllegalArgumentException.class, () -> validator.validate(user, errors));
        verify(errors, times(1)).rejectValue(any(String.class), any(String.class), any(String.class));
    }

    @DisplayName("Avec des informations correctes passe la validation")
    @Test
    public void test_user_validator_with_correct_infos(){
        //GIVEN
        Errors errors = Mockito.mock(Errors.class, Answers.CALLS_REAL_METHODS);

        //WHEN
        validator.validate(user, errors);

        //THEN
        verify(errors, times(0)).rejectValue(any(String.class), any(String.class), any(String.class));
    }

}
