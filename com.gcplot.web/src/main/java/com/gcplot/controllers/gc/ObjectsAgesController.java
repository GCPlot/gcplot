package com.gcplot.controllers.gc;

import com.gcplot.controllers.Controller;
import com.gcplot.messages.ObjectsAgesResponse;
import com.gcplot.model.gc.ObjectsAges;
import com.gcplot.repository.VMEventsRepository;
import com.gcplot.web.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/17/16
 */
public class ObjectsAgesController extends Controller {
    private static final Logger LOG = LoggerFactory.getLogger(EventsController.class);

    @PostConstruct
    public void init() {
        dispatcher.requireAuth().filter(required(), message())
                .get("/jvm/gc/ages/last", this::lastAges);
        dispatcher.requireAuth().filter(required(), message())
                .get("/jvm/gc/ages/erase", this::eraseAges);
    }

    /**
     * GET /jvm/gc/ages/last
     * Require Auth (token)
     * Params:
     *   - analyse_id
     *   - jvm_id
     */
    public void lastAges(RequestContext ctx) {
        String analyseId = ctx.param("analyse_id");
        String jvmId = ctx.param("jvm_id");

        Optional<ObjectsAges> oa = agesStateRepository.lastEvent(analyseId, jvmId, null);
        if (oa.isPresent()) {
            ctx.response(ObjectsAgesResponse.from(oa.get()));
        } else {
            ctx.write("{}");
        }
    }

    /**
     * GET /jvm/gc/ages/erase
     * Require Auth (token)
     * Params:
     *   - analyse_id
     *   - jvm_id
     */
    public void eraseAges(RequestContext ctx) {
        String analyseId = ctx.param("analyse_id");
        String jvmId = ctx.param("jvm_id");

        agesStateRepository.erase(analyseId, jvmId, null);
        ctx.response(SUCCESS);
    }

    private Predicate<RequestContext> required() {
        return c -> c.hasParam("analyse_id") && c.hasParam("jvm_id");
    }

    private String message() {
        return "Params analyse_id and jvm_id are restricted.";
    }

    @Autowired
    protected VMEventsRepository<ObjectsAges> agesStateRepository;

}
