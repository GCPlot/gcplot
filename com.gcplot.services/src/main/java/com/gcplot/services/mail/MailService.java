package com.gcplot.services.mail;

import com.gcplot.configuration.ConfigProperty;
import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.model.account.Account;
import com.gcplot.services.UrlBuilder;
import com.gcplot.services.mail.data.JvmAgentStatus;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.joda.time.Seconds;
import org.joda.time.format.PeriodFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/18/17
 */
public class MailService {
    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);
    private static final String CONFIRM_PROPERTY_NAME = "${email.confirm.template}";
    private static final String NEW_PASS_URL_PROPERTY_NAME = "${new.pass.url}";
    private MailProvider mailProvider;
    private ConfigurationManager config;
    private UrlBuilder urlBuilder;
    private boolean isAsync = false;

    public void init() {
        Velocity.init();
    }

    public void sendConfirmationFor(Account account) {
        String template = config.readString(ConfigProperty.EMAIL_CONFIRM_TEMPLATE);
        String url = urlBuilder.apiUrl("/user/confirm", "token", account.token(), "salt", account.confirmationSalt());
        String msg = MoreObjects.firstNonNull(Strings.emptyToNull(template), CONFIRM_PROPERTY_NAME).replace(CONFIRM_PROPERTY_NAME, url);
        send(account, "Confirmation notice", msg);
    }

    public void sendNewPassUrl(Account account) {
        String template = config.readString(ConfigProperty.EMAIL_NEW_PASS_TEMPLATE);
        String url = urlBuilder.uiNewPasswordPageUrl(account.token(), account.confirmationSalt());
        String msg = MoreObjects.firstNonNull(Strings.emptyToNull(template), NEW_PASS_URL_PROPERTY_NAME).replace(NEW_PASS_URL_PROPERTY_NAME, url);
        send(account, "Password Change Request", msg);
    }

    public void sendRealtimeAgentHealth(Account account, List<JvmAgentStatus> statusList) {
        VelocityContext ctx = new VelocityContext();
        ctx.put("username", account.firstName() + " " + account.lastName());
        List<Cluster> clusters = new ArrayList<>();
        statusList.forEach(jas -> {
            Cluster c = new Cluster();
            c.name = jas.getAnalysis().name();
            List<Jvm> jvms = new ArrayList<>();
            jas.getJvms().forEach(p -> {
                Jvm jvm = new Jvm();
                jvm.name = p.getKey();
                jvm.period = PeriodFormat.getDefault().print(Seconds.seconds(p.getValue().intValue()));
                jvms.add(jvm);
            });
            c.jvms = jvms;
            clusters.add(c);
        });
        ctx.put("clusters", clusters);
        String template = config.readString(ConfigProperty.EMAIL_AGENT_HEALTH_TEMPLATE);

        StringWriter msg = new StringWriter((int) (template.length() * 1.5));
        if (Velocity.evaluate(ctx, msg, account.username(), template)) {
            send(account, "Real-time Agent Health Status", msg.toString());
        } else {
            LOG.info("Unable to generate velocity template for message.");
        }
    }

    private void send(Account account, String subject, String msg) {
        mailProvider.send(account.email(), subject, msg, isAsync);
    }

    public void setMailProvider(MailProvider mailProvider) {
        this.mailProvider = mailProvider;
    }

    public void setUrlBuilder(UrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

    public void setConfig(ConfigurationManager config) {
        this.config = config;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }

    public static class Cluster {
        public String name;
        public List<Jvm> jvms;
    }

    public static class Jvm {
        public String name;
        public String period;
    }
}
