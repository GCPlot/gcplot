package com.gcplot;

import com.gcplot.commons.ErrorMessages;
import com.gcplot.commons.Utils;
import com.gcplot.messages.RegisterRequest;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class LoginTests extends IntegrationTest {

    @Test
    public void testNaiveErrors() throws Exception {
        get("/user/login?login=abc&password=def", ErrorMessages.WRONG_CREDENTIALS);
        get("/user/confirm?token=tk&salt=sl", ErrorMessages.NOT_AUTHORISED);
        post("/user/register", "{\"username\": \"123\", \"password\": \"pass\"}", ErrorMessages.INTERNAL_ERROR);
    }

    @Test
    public void testFullCycle() throws Exception {
        get("/user/register", ErrorMessages.NOT_FOUND);

        RegisterRequest request = new RegisterRequest();
        request.email = "artem@gcplot.com";
        request.username = "admin";
        request.password = "root";
        post("/user/register", request, success());

        JsonObject jo = login(request);
        Assert.assertEquals(jo.getString("username"), request.username);
        Assert.assertEquals(jo.getString("email"), request.email);
        Assert.assertEquals(jo.getBoolean("confirmed"), false);

        get("/user/login?login=" + request.email + "&password=" + request.password, j -> j.containsKey("result"));

        Assert.assertTrue(Utils.waitFor(() -> smtpServer.getReceivedEmails().size() == 1, TimeUnit.SECONDS.toNanos(10)));
        String confirmUrl = smtpServer.getReceivedEmails().get(0).getBody();
        Assert.assertTrue(withRedirect(confirmUrl));

        jo = login(request);
        Assert.assertEquals(jo.getBoolean("confirmed"), true);
    }

    @Test
    public void testRegisterNotUnique() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.email = "artem@gcplot.com";
        request.username = "admin";
        request.password = "root";
        post("/user/register", request, success());

        post("/user/register", request, ErrorMessages.NOT_UNIQUE_FIELDS);
    }

}
