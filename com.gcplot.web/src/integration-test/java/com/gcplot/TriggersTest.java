package com.gcplot;

import com.gcplot.configuration.ConfigProperty;
import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.messages.*;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.GarbageCollectorType;
import com.gcplot.model.gc.SourceType;
import com.gcplot.repository.GCAnalyseRepository;
import com.gcplot.repository.operations.analyse.UpdateCornerEventsOperation;
import com.gcplot.services.triggers.TriggerService;
import com.gcplot.utils.Utils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 10/17/17
 */
public class TriggersTest extends IntegrationTest {
    private static final String AGENT_TEMPLATE = "<b>#foreach ($cluster in $clusters)<div>$cluster.name</div>#foreach ($jvm in $cluster.jvms)<d>$jvm.name</d><c>$jvm.period</c>#end#end</b>";

    @Test
    public void testRealtimeAnalysisTrigger() throws Exception {
        ConfigurationManager config = getApplicationContext().getBean(ConfigurationManager.class);
        config.putProperty(ConfigProperty.EMAIL_AGENT_HEALTH_TEMPLATE, AGENT_TEMPLATE);
        config.putProperty(ConfigProperty.TRIGGERS_EMAIL_ENABLED, true);

        RegisterRequest request = register();
        post("/user/register", request, success());
        UpdateConfigRequest ucr = new UpdateConfigRequest(com.gcplot.model.account.ConfigProperty.REALTIME_AGENT_INACTIVE_SECONDS.getId(),
                "100");
        String token = login(request).getString("token");
        Identifier accountId = Identifier.fromStr(get("/user/account/id", token).getString("result"));
        post("/user/config/update?token=" + token, ucr, success());
        Assert.assertNotNull(token);

        get("/analyse/all", token, j -> r(j).getJsonArray("analyses").size() == 0);
        String analyseId = createAnalyse(token);
        Assert.assertNotNull(analyseId);

        String jvm1 = "3bb13695-7cbb-44b4-bc8a-5d1f2f687cc0";
        String jvm2 = "7acada7b-e109-4d11-ac01-b3521d9d58c3";
        post("/analyse/jvm/add", new AddJvmRequest(UUID.fromString(jvm1), analyseId, "JVM 1", VMVersion.HOTSPOT_1_8.type(),
                GarbageCollectorType.ORACLE_CMS.type(), "h1,h2", null), token, success());
        post("/analyse/jvm/add", new AddJvmRequest(UUID.fromString(jvm2), analyseId, "JVM 2", VMVersion.HOTSPOT_1_8.type(),
                        GarbageCollectorType.ORACLE_CMS.type(), null, new MemoryStatus(1,2,3,4,5)),
                token, success());

        processGCLogFile(token, analyseId, jvm1, "hs18_log_cms.log");
        processGCLogFile(token, analyseId, jvm2, "hs18_log_cms.log");

        GCAnalyseRepository analyseRepository = getApplicationContext().getBean(GCAnalyseRepository.class);
        DateTime now = DateTime.now(DateTimeZone.UTC);
        UpdateCornerEventsOperation op1 = new UpdateCornerEventsOperation(accountId, analyseId, jvm1, now, now.minusSeconds(90));
        UpdateCornerEventsOperation op2 = new UpdateCornerEventsOperation(accountId, analyseId, jvm2, now, now.minusSeconds(200));
        analyseRepository.perform(Arrays.asList(op1, op2));

        TriggerService ts = getApplicationContext().getBean(TriggerService.class);
        DateTimeUtils.setCurrentMillisFixed(now.getMillis());
        try {
            ts.processRealtimeAnalyzes();

            Assert.assertTrue(Utils.waitFor(() -> smtpServer.getReceivedEmails().size() == 2, TimeUnit.SECONDS.toNanos(30)));
            Assert.assertEquals("<b><div>analyse1</div><d>JVM 2</d><c>3 mins, 20 secs</c></b>", smtpServer.getReceivedEmails().get(1).getBody());

            ts.processRealtimeAnalyzes();
            Assert.assertFalse(Utils.waitFor(() -> smtpServer.getReceivedEmails().size() == 3, TimeUnit.SECONDS.toNanos(30)));
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    private String createAnalyse(String token) throws Exception {
        NewAnalyseRequest nar = new NewAnalyseRequest("analyse1", true, "Africa/Harare", Collections.emptyList(), SourceType.NONE, "", "");
        return r(post("/analyse/new", nar, token, j -> r(j).getString("id") != null)).getString("id");
    }

}
