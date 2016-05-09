package com.gcplot;

import com.gcplot.commons.ErrorMessages;
import com.gcplot.messages.RegisterRequest;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class LoginTest extends IntegrationTest {

    @Test
    public void testNaiveErrors() throws Exception {
        get("/user/login?username=abc&password=def", ErrorMessages.WRONG_CREDENTIALS);
        get("/user/confirm?token=tk&salt=sl", ErrorMessages.NOT_AUTHORISED);
    }

    @Test
    public void testFullCycle() throws Exception {
        get("/user/register", ErrorMessages.NOT_FOUND);

        RegisterRequest request = new RegisterRequest();
        request.email = "artem@gcplot.com";
        request.username = "admin";
        request.password = "root";
        post("/user/register", request, jo -> jo.containsKey("result"));

        StringBuilder sb = new StringBuilder();
        get("/user/login?username=" + request.username + "&password=" + request.password, jo -> {
            sb.append(jo);
            return jo.containsKey("result");
        });
        JsonObject jo = new JsonObject(sb.toString()).getJsonObject("result");
        Assert.assertEquals(jo.getString("username"), request.username);
        Assert.assertEquals(jo.getString("email"), request.email);
        Assert.assertEquals(jo.getBoolean("confirmed"), false);

        get("/user/login?username=" + request.email + "&password=" + request.password, j -> j.containsKey("result"));
    }

    @Test
    public void testRegisterNotUnique() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.email = "artem@gcplot.com";
        request.username = "admin";
        request.password = "root";
        post("/user/register", request, jo -> jo.containsKey("result"));

        post("/user/register", request, ErrorMessages.NOT_UNIQUE_FIELDS);
    }

}
