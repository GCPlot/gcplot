package com.gcplot;

import com.gcplot.commons.ErrorMessages;
import com.gcplot.commons.serialization.JsonSerializer;
import com.gcplot.messages.*;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.GarbageCollectorType;
import com.gcplot.model.gc.MemoryDetailsImpl;
import com.google.common.collect.Sets;
import io.vertx.core.json.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/23/16
 */
public class GCTests extends IntegrationTest {

    @Test
    public void testAnalyseController() throws Exception {
        String token = login();
        NewAnalyseRequest nar = new NewAnalyseRequest("analyse1", false, "");
        get("/analyse/all", token, j -> r(j).getJsonArray("analyses").size() == 0);
        String analyseId = r(post("/analyse/new", nar, token, j -> r(j).getString("id") != null)).getString("id");
        Assert.assertNotNull(analyseId);

        get("/analyse/all", token, j -> r(j).getJsonArray("analyses").size() == 1);
        JsonObject analyseJson = get("/analyse/get?id=" + analyseId, token, a -> true);
        Assert.assertNotNull(analyseJson);
        AnalyseResponse ar = JsonSerializer.deserialize(r(analyseJson).toString(), AnalyseResponse.class);
        Assert.assertEquals(analyseId, ar.id);
        Assert.assertEquals("analyse1", ar.name);
        Assert.assertEquals(false, ar.continuous);
        Assert.assertTrue(ar.jvmIds.isEmpty());
        Assert.assertTrue(ar.jvmHeaders.isEmpty());
        Assert.assertTrue(ar.jvmVersions.isEmpty());
        Assert.assertTrue(ar.jvmGCTypes.isEmpty());
        Assert.assertTrue(ar.memory.isEmpty());

        get("/analyse/get?id=123", token, ErrorMessages.INTERNAL_ERROR);
        get("/analyse/get?id=" + UUID.randomUUID().toString(), token, ErrorMessages.RESOURCE_NOT_FOUND_RESPONSE);

        post("/analyse/jvm/add", new AddJvmRequest("jvm1", analyseId, VMVersion.HOTSPOT_1_9.type(),
                GarbageCollectorType.ORACLE_SERIAL.type(), "h1,h2", null), token, success());
        post("/analyse/jvm/add", new AddJvmRequest("jvm2", analyseId, VMVersion.HOTSPOT_1_3_1.type(),
                GarbageCollectorType.ORACLE_PAR_OLD_GC.type(), null, new MemoryStatus(1,2,3,4,5)),
                token, success());

        ar = getAnalyse(token, analyseId);
        Assert.assertEquals(analyseId, ar.id);
        Assert.assertEquals(Sets.newHashSet("jvm1", "jvm2"), ar.jvmIds);
        Assert.assertEquals(VMVersion.HOTSPOT_1_9.type(), (int) ar.jvmVersions.get("jvm1"));
        Assert.assertEquals(VMVersion.HOTSPOT_1_3_1.type(), (int) ar.jvmVersions.get("jvm2"));
        Assert.assertEquals(GarbageCollectorType.ORACLE_SERIAL.type(), (int) ar.jvmGCTypes.get("jvm1"));
        Assert.assertEquals(GarbageCollectorType.ORACLE_PAR_OLD_GC.type(), (int) ar.jvmGCTypes.get("jvm2"));
        Assert.assertEquals("h1,h2", ar.jvmHeaders.get("jvm1"));
        Assert.assertNull(ar.jvmHeaders.get("jvm2"));
        Assert.assertEquals(new MemoryStatus(1,2,3,4,5), ar.memory.get("jvm2"));
        Assert.assertNull(ar.memory.get("jvm1"));

        post("/analyse/jvm/update/info", new UpdateJvmInfoRequest(analyseId, "jvm2", null,
                new MemoryStatus(11,12,13,14,15)), token, success());
        ar = getAnalyse(token, analyseId);
        Assert.assertNull(ar.jvmHeaders.get("jvm2"));
        Assert.assertEquals(new MemoryStatus(11,12,13,14,15), ar.memory.get("jvm2"));
        Assert.assertNull(ar.memory.get("jvm1"));

        post("/analyse/jvm/update/version", new UpdateJvmVersionRequest(analyseId, "jvm1", null,
                GarbageCollectorType.ORACLE_G1.type()), token, success());
        ar = getAnalyse(token, analyseId);
        Assert.assertEquals(GarbageCollectorType.ORACLE_G1.type(), (int) ar.jvmGCTypes.get("jvm1"));
        Assert.assertEquals(GarbageCollectorType.ORACLE_PAR_OLD_GC.type(), (int) ar.jvmGCTypes.get("jvm2"));

        post("/analyse/jvm/update/version", new UpdateJvmVersionRequest(analyseId, "jvm1", VMVersion.HOTSPOT_1_2_2.type(),
                null), token, success());
        ar = getAnalyse(token, analyseId);
        Assert.assertEquals(VMVersion.HOTSPOT_1_2_2.type(), (int) ar.jvmVersions.get("jvm1"));
        Assert.assertEquals(VMVersion.HOTSPOT_1_3_1.type(), (int) ar.jvmVersions.get("jvm2"));

        post("/analyse/jvm/update/version", new UpdateJvmVersionRequest(analyseId, "jvm1", null, null), token, ErrorMessages.INTERNAL_ERROR);

        Assert.assertEquals(1, (int) r(delete("/analyse/jvm/delete?analyse_id=" + analyseId + "&jvm_id=jvm1", token)).getInteger("success"));
        ar = getAnalyse(token, analyseId);
        Assert.assertEquals(analyseId, ar.id);
        Assert.assertEquals(Sets.newHashSet("jvm2"), ar.jvmIds);
        Assert.assertEquals(new MemoryStatus(11,12,13,14,15), ar.memory.get("jvm2"));

        delete("/analyse/delete?id=" + UUID.randomUUID().toString(), token);
        get("/analyse/all", token, j -> r(j).getJsonArray("analyses").size() == 1);
        Assert.assertEquals(1, (int) r(delete("/analyse/delete?id=" + analyseId, token)).getInteger("success"));
        get("/analyse/all", token, j -> r(j).getJsonArray("analyses").size() == 0);
    }

    @Test
    public void simpleLogsUploadTest() throws Exception {
        HttpClient hc = HttpClientBuilder.create().build();
        HttpEntity file = MultipartEntityBuilder.create()
                .addBinaryBody("gc.log", GCTests.class.getClassLoader().getResourceAsStream("hs18_log_cms.log"),
                        ContentType.TEXT_PLAIN, "hs18_log_cms.log").build();
        HttpPost post = new HttpPost("http://" + LOCALHOST + ":" + getPort() + "/gc/upload_log?token=" + login());
        post.setEntity(file);
        HttpResponse response = hc.execute(post);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
    }

    private AnalyseResponse getAnalyse(String token, String analyseId) throws Exception {
        JsonObject analyseJson;
        AnalyseResponse ar;
        analyseJson = get("/analyse/get?id=" + analyseId, token, a -> true);
        ar = JsonSerializer.deserialize(r(analyseJson).toString(), AnalyseResponse.class);
        return ar;
    }

    private String login() throws Exception {
        RegisterRequest request = new RegisterRequest("admin", null, null, "root", "a@b.c");
        post("/user/register", request, jo -> jo.containsKey("result"));

        JsonObject jo = login(request);
        return jo.getString("token");
    }

    private Predicate<JsonObject> success() {
        return a -> r(a).getInteger("success").equals(1);
    }

}
