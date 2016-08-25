package com.gcplot;

import com.gcplot.commons.ErrorMessages;
import com.gcplot.messages.NewAnalyseRequest;
import com.gcplot.messages.RegisterRequest;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.GarbageCollectorType;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/23/16
 */
public class GCTests extends IntegrationTest {

    @Test
    public void test() throws Exception {
        RegisterRequest request = new RegisterRequest("admin", null, null, "root", "a@b.c");
        post("/user/register", request, jo -> jo.containsKey("result"));

        JsonObject jo = login(request);
        String token = jo.getString("token");
        NewAnalyseRequest nar = new NewAnalyseRequest("analyse1", false, VMVersion.HOTSPOT_1_8.type(),
                GarbageCollectorType.ORACLE_G1.type(), Collections.emptySet(), "");
        final String[] analyseId = { "" };
        get("/analyse/all", token, j -> r(j).getJsonArray("analyses").size() == 0);
        post("/analyse/new", nar, token, j -> {
            analyseId[0] = r(j).getString("id");
            return r(j).getString("id") != null;
        });
        Assert.assertNotNull(analyseId[0]);

        get("/analyse/all", token, j -> r(j).getJsonArray("analyses").size() == 1);
        JsonObject analyseJson = get("/analyse/get?id=" + analyseId[0], token, a -> true);
        Assert.assertNotNull(analyseJson);
        Assert.assertEquals(analyseId[0], r(analyseJson).getString("id"));
        Assert.assertEquals("analyse1", r(analyseJson).getString("name"));
        Assert.assertEquals(false, r(analyseJson).getBoolean("cnts"));

        get("/analyse/get?id=123", token, ErrorMessages.INTERNAL_ERROR);
        get("/analyse/get?id=" + UUID.randomUUID().toString(), token, ErrorMessages.RESOURCE_NOT_FOUND_RESPONSE);
    }

}
