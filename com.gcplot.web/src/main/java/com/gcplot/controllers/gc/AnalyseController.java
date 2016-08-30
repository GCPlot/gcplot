package com.gcplot.controllers.gc;

import com.gcplot.Identifier;
import com.gcplot.commons.ConfigProperty;
import com.gcplot.commons.ErrorMessages;
import com.gcplot.controllers.Controller;
import com.gcplot.messages.*;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.*;
import com.gcplot.repository.GCAnalyseRepository;
import com.gcplot.roles.Restrictions;
import com.gcplot.web.RequestContext;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Optional;
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

        dispatcher.requireAuth().filter(c -> c.hasParam("id"), "Param 'id' of analyse is missing.")
                .get("/analyse/get", this::analyse);
        dispatcher.requireAuth().filter(c -> c.hasParam("id"), "Param 'id' of analyse is missing.")
                .delete("/analyse/delete", this::deleteAnalyse);
        dispatcher.requireAuth().get("/analyse/all", this::analyses);
        dispatcher.requireAuth().post("/analyse/jvm/add", AddJvmRequest.class, this::addJvm);
        dispatcher.requireAuth().post("/analyse/jvm/update/version", UpdateJvmVersionRequest.class, this::updateJvmVersion);
        dispatcher.requireAuth().post("/analyse/jvm/update/info", UpdateJvmInfoRequest.class, this::updateJvmInfo);
        dispatcher.requireAuth()
                .filter(c -> c.hasParam("analyse_id"), "Param 'analyse_id' is missing.")
                .filter(c -> c.hasParam("jvm_id"), "Param 'jvm_id' is missing.")
                .delete("/analyse/jvm/delete", this::deleteJvm);
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
     * Responds: AnalysesResponse (JSON)
     */
    public void analyses(RequestContext ctx) {
        Identifier userId = account(ctx).id();
        ctx.response(new AnalysesResponse(
                analyseRepository.analysesFor(userId)));
    }

    /**
     * GET /analyse/get
     * Require Auth (token)
     * Params:
     *   - id (Identity of the analyse)
     * Responds: AnalyseResponse (JSON)
     */
    public void analyse(RequestContext ctx) {
        String id = ctx.param("id");
        Optional<GCAnalyse> oa = analyseRepository.analyse(id);
        if (oa.isPresent()) {
            GCAnalyse analyse = oa.get();
            if (analyse.accountId().equals(account(ctx).id())) {
                ctx.response(new AnalyseResponse(analyse));
            } else {
                ctx.write(ErrorMessages.buildJson(ErrorMessages.RESOURCE_NOT_FOUND_RESPONSE));
            }
        } else {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.RESOURCE_NOT_FOUND_RESPONSE));
        }
    }

    /**
     * POST /analyse/jvm/add
     * Require Auth (token)
     * Body: AddJvmRequest (JSON)
     * Responds: SUCCESS or ERROR
     */
    public void addJvm(AddJvmRequest req, RequestContext ctx) {
        Identifier userId = account(ctx).id();
        MemoryDetails md = null;
        if (req.memoryStatus != null) {
            md = new MemoryDetailsImpl(req.memoryStatus.pageSize,
                    req.memoryStatus.physicalTotal, req.memoryStatus.physicalFree,
                    req.memoryStatus.swapTotal, req.memoryStatus.swapFree);
        }
        analyseRepository.addJvm(userId, req.analyseId, req.jvmId, VMVersion.get(req.vmVersion),
                GarbageCollectorType.get(req.gcType), req.headers, md);
        ctx.response(SUCCESS);
    }

    /**
     * DELETE /analyse/jvm/delete
     * Require Auth (token)
     * Params:
     *   - analyse_id (an ID of the GCAnalyse)
     *   - jvm_id (an ID of particular JVM inside GCAnalyse)
     * Responds: SUCCESS or ERROR
     */
    public void deleteJvm(RequestContext ctx) {
        String analyseId = ctx.param("analyse_id");
        String jvmId = ctx.param("jvm_id");

        analyseRepository.removeJvm(account(ctx).id(), analyseId, jvmId);
        ctx.response(SUCCESS);
    }

    /**
     * POST /analyse/jvm/update/version
     * Require Auth (token)
     * Body: UpdateJvmVersionRequest (JSON)
     * Responds: SUCCESS or ERROR
     */
    public void updateJvmVersion(UpdateJvmVersionRequest req, RequestContext ctx) {
        VMVersion vmVersion = null;
        GarbageCollectorType gcType = null;
        if (req.vmVersion != null) {
            vmVersion = VMVersion.get(req.vmVersion);
        }
        if (req.gcType != null) {
            gcType = GarbageCollectorType.get(req.gcType);
        }
        analyseRepository.updateJvmVersion(account(ctx).id(), req.analyseId, req.jvmId,
                vmVersion, gcType);
        ctx.response(SUCCESS);
    }

    /**
     * POST /analyse/jvm/update/info
     * Require Auth (token)
     * Body: UpdateJvmInfoRequest (JSON)
     * Responds: SUCCESS or ERROR
     */
    public void updateJvmInfo(UpdateJvmInfoRequest req, RequestContext ctx) {
        analyseRepository.updateJvmInfo(account(ctx).id(), req.analyseId, req.jvmId, req.headers,
                req.memoryStatus != null ? req.memoryStatus.toDetails() : null);
        ctx.response(SUCCESS);
    }

    /**
     * DELETE /analyse/delete
     * Require Auth (token)
     * Params:
     *   - id (Identity of the analyse)
     * Responds: SUCCESS or ERROR
     */
    public void deleteAnalyse(RequestContext ctx) {
        String id = ctx.param("id");
        analyseRepository.removeAnalyse(account(ctx).id(), id);
        ctx.response(SUCCESS);
    }

    /**
     * POST /analyse/new
     * Require Auth (token)
     * Body: NewAnalyseRequest (JSON)
     * Responds: NewAnalyseResponse (JSON)
     */
    public void newAnalyse(NewAnalyseRequest req, RequestContext ctx) {
        Identifier userId = account(ctx).id();
        GCAnalyseImpl analyse = new GCAnalyseImpl();
        analyse.name(req.name);
        analyse.isContinuous(req.isContinuous);
        analyse.accountId(userId);
        analyse.jvmGCTypes(Collections.emptyMap());
        analyse.jvmVersions(Collections.emptyMap());
        analyse.start(DateTime.now(DateTimeZone.UTC));
        analyse.ext(req.ext);
        analyse.jvmHeaders(Collections.emptyMap());
        analyse.jvmMemoryDetails(Collections.emptyMap());

        ctx.response(new NewAnalyseResponse(analyseRepository.newAnalyse(analyse)));
        newAnalyses.invalidate(userId);
    }

    @Autowired
    protected GCAnalyseRepository analyseRepository;
}
