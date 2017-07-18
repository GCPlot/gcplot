package com.gcplot.services.mail;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/17/17
 */
public interface MailProvider {

    void send(String to, String subject, String msg, boolean async);

    default void send(String to, String subject, String msg) {
        send(to, subject, msg, true);
    }

}
