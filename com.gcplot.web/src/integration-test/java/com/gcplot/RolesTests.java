package com.gcplot;

import com.gcplot.messages.RegisterRequest;
import org.junit.Test;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/18/16
 */
public class RolesTests extends IntegrationTest {

    @Test
    public void test() throws Exception {
        post("/user/register", new RegisterRequest("admin", "", "", "root", "artem@gcplot.com"), success());
        String token = login("admin", "root").getString("token");


    }

}
