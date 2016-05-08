package com.gcplot;

import com.gcplot.commons.ErrorMessages;
import org.junit.Test;

public class LoginTest extends IntegrationTest {

    @Test
    public void testNaiveErrors() throws Exception {
        get("/user/login?username=abc&password=def", ErrorMessages.WRONG_CREDENTIALS);
        get("/user/confirm?token=tk&salt=sl", ErrorMessages.NOT_AUTHORISED);
    }

}
