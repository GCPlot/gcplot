package com.gcplot.services.triggers;

import com.gcplot.model.account.Account;
import com.gcplot.model.account.config.ConfigProperty;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.repository.AccountRepository;
import com.gcplot.repository.GCAnalyseRepository;
import com.gcplot.repository.TriggerRepository;
import com.gcplot.services.triggers.strategy.TriggerStrategy;
import com.gcplot.services.triggers.strategy.TriggerStrategyType;
import com.gcplot.triggers.Trigger;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 9/5/17
 */
public class TriggerService {
    private static final Logger LOG = LoggerFactory.getLogger(TriggerService.class);
    private TriggerRepository triggerRepository;
    private EnumMap<TriggerStrategyType, List<TriggerStrategy>> strategies;
    private GCAnalyseRepository analyseRepository;
    private AccountRepository accountRepository;

    public void processRealtimeAnalyzes() {
        LOG.info("Begin processing real-time analyzes.");
        try {
            List<TriggerStrategy> s = strategies.get(TriggerStrategyType.REALTIME_ANALYSIS);
            LOG.info("Found {} strategies.", s.size());
            Map<Account, Pair<List<Trigger>, List<GCAnalyse>>> analyses = new HashMap<>();
            analyseRepository.analyses(true).forEach(a -> {
                try {
                    Account account = accountRepository.account(a.accountId()).orElse(null);
                    if (account != null && account.config().asBoolean(ConfigProperty.NOTIFICATIONS_ENABLED)) {
                        analyses.computeIfAbsent(account, k ->
                                Pair.of(triggerRepository.triggersFor(a.accountId()), new ArrayList<>())).getRight().add(a);
                    }
                } catch (Throwable t) {
                    LOG.error("processRealtimeAnalyzes {}", a, t);
                }
            });
            analyses.forEach((account, p) -> {
                LOG.info("Checking for {} account.", account.username());
                try {
                    s.stream().filter(i -> i.isActive(account)).forEach(
                            i -> i.process(account, p.getRight(), p.getLeft()));
                } catch (Throwable t) {
                    LOG.error("processRealtimeAnalyzes {}", account, t);
                }
            });
        } catch (Throwable t) {
            LOG.error("processRealtimeAnalyzes", t);
        } finally {
            LOG.info("Finish processing real-time analyzes.");
        }
    }

    public void setTriggerRepository(TriggerRepository triggerRepository) {
        this.triggerRepository = triggerRepository;
    }

    public void setStrategies(List<TriggerStrategy> strategies) {
        EnumMap<TriggerStrategyType, List<TriggerStrategy>> ss = new EnumMap<>(TriggerStrategyType.class);
        strategies.forEach(st -> ss.computeIfAbsent(st.type(), k -> new ArrayList<>()).add(st));
        this.strategies = ss;
    }

    public void setAnalyseRepository(GCAnalyseRepository analyseRepository) {
        this.analyseRepository = analyseRepository;
    }

    public void setAccountRepository(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
}
