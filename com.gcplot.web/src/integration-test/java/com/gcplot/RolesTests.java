package com.gcplot;

import com.gcplot.commons.ErrorMessages;
import com.gcplot.messages.RegisterRequest;
import com.gcplot.repository.AccountRepository;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/18/16
 */
public class RolesTests extends IntegrationTest {

    @Test
    public void test() throws Exception {
        post("/user/register", new RegisterRequest("admin", "", "", "root", "artem@gcplot.com"), success());
        JsonObject account = login("admin", "root");
        String token = account.getString("token");

        String id = rs(get("/admin/account/id", token));
        get("/admin/account/info?account_id=" + id, token, ErrorMessages.REQUEST_FILTERED);
        getApplicationContext().getBean(AccountRepository.class).roleManagement(Identifier.fromStr(id), true);
        JsonObject accountInfo = get("/admin/account/info?account_id=" + id, token);
        Assert.assertFalse(accountInfo.isEmpty());
    }
}
