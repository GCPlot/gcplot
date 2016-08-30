package com.gcplot.roles;

import com.gcplot.model.account.Account;
import com.gcplot.model.role.Restriction;
import com.gcplot.model.role.RestrictionType;
import com.gcplot.web.RequestContext;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/22/16
 */
public class Restrictions implements Predicate<RequestContext> {
    protected String action;
    protected ToLongFunction<Account> ls;
    protected BiFunction<Account, String, String> mc;

    @Override
    public boolean test(RequestContext ctx) {
        if (ctx.path().equals(action)) {
            if (ctx.loginInfo().isPresent()) {
                Account account = ctx.loginInfo().get().getAccount();
                if (account.roles().size() == 0) {
                    return true;
                }
                Stream<Restriction> roles = account.roles().stream()
                        .flatMap(r -> r.restrictions().stream())
                        .filter(r -> r.action().equals(action));
                if (ls == null && mc == null) {
                    return roles.count() == 0;
                } else {
                    return roles.allMatch(r -> {
                       if (r.type() == RestrictionType.QUANTITATIVE && ls != null) {
                           return r.amount() <= ls.applyAsLong(account);
                       } else if (r.type() == RestrictionType.PROPERTY_BASED && mc != null) {
                           return r.properties().entrySet().stream().allMatch(e -> {
                               String v = mc.apply(account, e.getKey());
                               return v != null && v.equals(e.getValue());
                           });
                       } else {
                           return !r.restricted();
                       }
                    });
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private Restrictions(String action, ToLongFunction<Account> ls, BiFunction<Account, String, String> mc) {
        this.action = action;
        this.ls = ls;
        this.mc = mc;
    }

    public static Restrictions apply(String action) {
        return new Restrictions(action, null, null);
    }

    public static Restrictions apply(String action, ToLongFunction<Account> ls) {
        return new Restrictions(action, ls, null);
    }

    public static Restrictions apply(String action, BiFunction<Account, String, String> mc) {
        return new Restrictions(action, null, mc);
    }

    public static Restrictions apply(String action, ToLongFunction<Account> ls, BiFunction<Account, String, String> mc) {
        return new Restrictions(action, ls, mc);
    }

}
