package com.gcplot;

import com.gcplot.commons.ErrorMessages;
import com.gcplot.commons.serialization.JsonSerializer;
import com.gcplot.messages.*;
import com.gcplot.model.role.RestrictionType;
import com.gcplot.repository.AccountRepository;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.net.URLEncoder;
import java.util.Collections;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/18/16
 */
public class RolesTests extends IntegrationTest {

    @Test
    public void testAttachRoleAndAccessAdmin() throws Exception {
        post("/user/register", new RegisterRequest("admin", "", "", "root", "artem@gcplot.com"), success());
        JsonObject account = login("admin", "root");
        String token = account.getString("token");

        String id = rs(get("/admin/account/id", token));
        get("/admin/account/info?account_id=" + URLEncoder.encode(id, "UTF-8"), token, ErrorMessages.REQUEST_FILTERED);
        getApplicationContext().getBean(AccountRepository.class).roleManagement(Identifier.fromStr(id), true);
        get("/admin/account/info?account_id=" + URLEncoder.encode(id, "UTF-8"), token, ErrorMessages.REQUEST_FILTERED);

        NewOrUpdateRoleRequest rr = new NewOrUpdateRoleRequest(null, "role1", false,
                Collections.singletonList(new RestrictionMessage(RestrictionType.TOGGLE, "/admin/account/info",
                        false, 0, null)));
        String roleId = rs(post("/roles/store", rr, token, a -> true));
        Assert.assertNotNull(roleId);

        post("/roles/account/attach", new AttachRoleMessage(id, roleId), token, success());

        JsonObject accountInfo = r(get("/admin/account/info?account_id=" + URLEncoder.encode(id, "UTF-8"), token));
        Assert.assertFalse(accountInfo.isEmpty());
        AccountResponse accountResponse = JsonSerializer.deserialize(accountInfo.toString(), AccountResponse.class);
        Assert.assertEquals(id, accountResponse.id);
        Assert.assertEquals(token, accountResponse.token);
        Assert.assertTrue(accountResponse.roleManagement);
        Assert.assertEquals(1, accountResponse.roles.size());
        Assert.assertEquals(roleId, accountResponse.roles.get(0).id);
        Assert.assertEquals(1, accountResponse.roles.get(0).restrictions.size());

        post("/roles/account/detach", new AttachRoleMessage(id, roleId), token, success());
        get("/admin/account/info?account_id=" + URLEncoder.encode(id, "UTF-8"), token, ErrorMessages.REQUEST_FILTERED);
    }
}
