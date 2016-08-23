package com.gcplot.controllers.gc;

import com.gcplot.Identifier;
import com.gcplot.commons.ConfigProperty;
import com.gcplot.controllers.Controller;
import com.gcplot.messages.AnalysesResponse;
import com.gcplot.messages.NewAnalyseRequest;
import com.gcplot.messages.NewAnalyseResponse;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.GCAnalyseImpl;
import com.gcplot.model.gc.GarbageCollectorType;
import com.gcplot.repository.GCAnalyseRepository;
import com.gcplot.roles.Restrictions;
import com.gcplot.web.RequestContext;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/13/16
 */
public class AnalyseController extends Controller {
    protected LoadingCache<Identifier, Long> newAnalyses;

    @PostConstruct
    public void init() {
        newAnalyses = Caffeine.newBuilder()
                .maximumSize(config.readLong(ConfigProperty.USER_ANALYSE_COUNT_CACHE_SIZE))
                .expireAfterWrite(config.readLong(ConfigProperty.USER_ANALYSE_COUNT_CACHE_SECONDS), TimeUnit.SECONDS)
                .build(k -> analyseRepository.analysesCount(k).orElse(0));

        dispatcher.requireAuth().get("/analyse/all", this::analyses);
        dispatcher.requireAuth()
                .filter(Restrictions.apply("/analyse/new", a ->
                        newAnalyses.get(a.id(), k -> analyseRepository.analysesCount(k).orElse(0))),
                        "You exceeded the amount of GC analyses per user. Contact to increase this number.")
                .post("/analyse/new", NewAnalyseRequest.class, this::newAnalyse);
    }

    /**
     * GET /analyse/all
     * Require Auth (token)
     * Params: No
     */
    public void analyses(RequestContext ctx) {
        Identifier userId = ctx.loginInfo().get().getAccount().id();
        ctx.response(new AnalysesResponse(
                analyseRepository.analysesFor(userId)));
    }

    /**
     * POST /analyse/new
     * Require Auth (token)
     * Body: NewAnalyseRequest (JSON)
     */
    public void newAnalyse(NewAnalyseRequest req, RequestContext ctx) {
        Identifier userId = ctx.loginInfo().get().getAccount().id();
        GCAnalyseImpl analyse = new GCAnalyseImpl();
        analyse.name(req.name);
        analyse.isContinuous(req.isContinuous);
        analyse.vmVersion(VMVersion.get(req.vmVersion));
        analyse.collectorType(GarbageCollectorType.get(req.gcCollectorType));
        analyse.accountId(userId);
        analyse.start(DateTime.now(DateTimeZone.UTC));
        analyse.jvmIds(req.jvmIds);
        analyse.ext(req.ext);
        analyse.jvmHeaders(new HashMap<>());
        analyse.jvmMemoryDetails(new HashMap<>());

        ctx.response(new NewAnalyseResponse(analyseRepository.newAnalyse(analyse)));
        newAnalyses.invalidate(userId);
    }

    @Autowired
    protected GCAnalyseRepository analyseRepository;
}
