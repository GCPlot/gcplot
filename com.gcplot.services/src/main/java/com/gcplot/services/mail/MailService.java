package com.gcplot.services.mail;

import com.gcplot.configuration.ConfigProperty;
import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.model.account.Account;
import com.gcplot.services.UrlBuilder;
import com.gcplot.services.mail.data.JvmAgentStatus;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import net.time4j.ClockUnit;
import net.time4j.Duration;
import net.time4j.PrettyTime;
import net.time4j.format.TextWidth;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
                Duration dur = Duration.of(p.getValue(), ClockUnit.SECONDS).with(Duration.STD_CLOCK_PERIOD);
                jvm.period = PrettyTime.of(Locale.ENGLISH).print(dur, TextWidth.ABBREVIATED);
                jvms.add(jvm);
            });
            c.jvms = jvms;
            clusters.add(c);
        });
        ctx.put("clusters", clusters);
        String template = config.readString(ConfigProperty.EMAIL_AGENT_HEALTH_TEMPLATE);

        StringWriter msg = new StringWriter((int) (template.length() * 1.5));
        if (Velocity.evaluate(ctx, msg, account.username(), template)) {
            sendNotification(account, "Real-time Agent Health Status", msg.toString());
        } else {
            LOG.info("Unable to generate velocity template for message.");
        }
    }

    private void send(Account account, String subject, String msg) {
        mailProvider.send(account.email(), subject, msg, isAsync);
    }

    private void sendNotification(Account account, String subject, String msg) {
        mailProvider.send(Strings.isNullOrEmpty(account.notificationEmail()) ? account.email() : account.notificationEmail(),
                subject, msg, isAsync);
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

        public String getName() {
            return name;
        }

        public List<Jvm> getJvms() {
            return jvms;
        }
    }

    public static class Jvm {
        public String name;
        public String period;

        public String getName() {
            return name;
        }

        public String getPeriod() {
            return period;
        }
    }
}
