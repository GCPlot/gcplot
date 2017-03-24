package com.gcplot.controllers.gc;

import com.gcplot.analytics.AnalyticsService;
import com.gcplot.analytics.EventsResult;
import com.gcplot.analytics.GCEventFeature;
import com.gcplot.commons.*;
import com.gcplot.commons.exceptions.Exceptions;
import com.gcplot.commons.serialization.JsonSerializer;
import com.gcplot.controllers.Controller;
import com.gcplot.logs.*;
import com.gcplot.messages.GCEventResponse;
import com.gcplot.messages.GCRateResponse;
import com.gcplot.model.gc.*;
import com.gcplot.repository.GCAnalyseRepository;
import com.gcplot.repository.GCEventRepository;
import com.gcplot.services.logs.FileLogSource;
import com.gcplot.services.stats.StatisticAggregateInterceptor;
import com.gcplot.web.RequestContext;
import com.gcplot.web.UploadedFile;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.joda.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/13/16
 */
public class EventsController extends Controller {
    public static final String DEFAULT_CHUNK_DELIMETER = "$d";
    public static final String ANONYMOUS_ANALYSE_ID = "7acada7b-e109-4d11-ac01-b3521d9d58c3";
    protected static final Logger LOG = LoggerFactory.getLogger(EventsController.class);
    private static final int ERASE_ALL_PERIOD_YEARS = 10;
    private static final EnumSet<Generation> OTHER_GENERATION = EnumSet.of(Generation.OTHER);
    private Cache<Triple, StatisticAggregateInterceptor> analyseStatsCache;
    @Autowired
    private GCAnalyseRepository analyseRepository;
    @Autowired
    private GCEventRepository eventRepository;
    @Autowired
    private AnalyticsService analyticsService;
    @Autowired
    private LogsProcessorService logsProcessorService;

    @PostConstruct
    public void init() {
        analyseStatsCache = Caffeine.newBuilder()
                .maximumSize(config.readLong(ConfigProperty.ANALYSIS_STATISTIC_CACHE_SIZE))
                .expireAfterWrite(config.readLong(ConfigProperty.ANALYSIS_STATISTIC_CACHE_SECONDS), TimeUnit.SECONDS)
                .build();
        dispatcher.requireAuth().blocking().filter(c ->
                c.files().size() == 1,
                "You should provide only a single log file.")
                .postUpload("/gc/jvm/log/process", this::processJvmLog);
        dispatcher.blocking().requireAuth().filter(requiredWithPeriod(), periodMessage())
                .get("/gc/jvm/events", this::jvmEvents);
        dispatcher.blocking().requireAuth().filter(requiredWithPeriod(), periodMessage())
                .get("/gc/jvm/events/stream", this::jvmEventsStream);
        dispatcher.blocking().requireAuth().filter(requiredWithPeriod(), periodMessage())
                .get("/gc/jvm/events/full/stream", this::fullJvmEventsStream);
        dispatcher.blocking().requireAuth().filter(requiredWithPeriod(), periodMessage())
                .get("/gc/jvm/events/full/sample/stream", this::fullJvmSampleEventsStream);
        dispatcher.requireAuth().filter(requiredWithPeriod(), periodMessage())
                .get("/gc/jvm/events/erase", this::jvmEventsErase);
        dispatcher.requireAuth().filter(requiredWithoutPeriod(), message())
                .get("/gc/jvm/events/erase/all", this::jvmEventsEraseAll);
        dispatcher.requireAuth().filter(requiredWithPeriod(), message())
                .get("/gc/jvm/events/stats", this::jvmStats);
    }

    /**
     * POST /gc/jvm/log/process
     * Require Auth (token)
     * Body: Single file upload
     */
    public void processJvmLog(RequestContext ctx) {
        UploadedFile uf = ctx.files().get(0);
        final boolean isSync = Boolean.parseBoolean(ctx.param("sync", "false"));
        final String analyzeId = ctx.param("analyse_id", LogsProcessorService.ANONYMOUS_ANALYSE_ID);
        final String jvmId = ctx.param("jvm_id", UUID.randomUUID().toString());

        LogProcessResult r = logsProcessorService.process(new FileLogSource(uf.file(), new LogHandle(uf.originalName(),
                        account(ctx).id().toString(), analyzeId, jvmId)), account(ctx),
                analyzeId, jvmId, isSync);

        if (r.isSuccess()) {
            ctx.response(SUCCESS);
        } else {
            ctx.response(r.getErrorMessage());
        }
    }

    /**
     * GET /gc/jvm/events
     * Require Auth (token)
     * Params:
     *  - analyse_id, string (required)
     *  - jvm_id, string (required)
     *  - from, timestamp (required)
     *  - to, timestamp (required)
     *  - tz, string (optional)
     */
    public void jvmEvents(RequestContext ctx) {
        PeriodParams pp = new PeriodParams(ctx);

        checkPeriodAndExecute(pp, ctx, () -> {
            ctx.setChunked(false);
            List<GCEvent> events = eventRepository.pauseEvents(pp.getAnalyseId(), pp.getJvmId(), Range.of(pp.getInterval()));
            ctx.response(GCEventResponse.from(events));
        });
    }

    /**
     * GET /gc/jvm/events
     * Require Auth (token)
     * Params:
     *  - analyse_id, string (required)
     *  - jvm_id, string (required)
     *  - from, timestamp (required)
     *  - to, timestamp (required)
     *  - tz, string (optional)
     */
    public void jvmEventsStream(RequestContext ctx) {
        PeriodParams pp = new PeriodParams(ctx);

        checkPeriodAndExecute(pp, ctx, () -> {
            ctx.setChunked(true);
            Iterator<GCEvent> eventIterator = eventRepository.lazyPauseEvents(pp.getAnalyseId(), pp.getJvmId(),
                    Range.of(pp.getInterval()));
            streamEvents(ctx, pp, eventIterator);
            ctx.finish();
        });
    }

    /**
     * GET /gc/jvm/events/full/stream
     */
    public void fullJvmEventsStream(RequestContext ctx) {
        PeriodParams pp = new PeriodParams(ctx);

        checkPeriodAndExecute(pp, ctx, () -> {
            ctx.setChunked(true);
            Iterator<GCEvent> eventIterator = eventRepository.lazyEvents(pp.getAnalyseId(), pp.getJvmId(),
                    Range.of(pp.getInterval()));
            streamEvents(ctx, pp, eventIterator);
            ctx.finish();
        });
    }

    /**
     * GET /gc/jvm/events/full/sample/stream
     */
    public void fullJvmSampleEventsStream(RequestContext ctx) {
        PeriodParams pp = new PeriodParams(ctx);

        checkPeriodAndExecute(pp, ctx, () -> {
            ctx.setChunked(true);
            EnumSet<GCEventFeature> features = pp.isStats() ? GCEventFeature.getAll() : GCEventFeature.getSamplers();
            StatisticAggregateInterceptor cached = analyseStatsCache.get(statsKey(pp), k -> null);
            if (cached != null) {
                features = EnumSet.copyOf(features);
                features.remove(GCEventFeature.CALC_STATISTIC);
            }

            EventsResult r = analyticsService.events(account(ctx).id(), pp.getAnalyseId(), pp.getJvmId(), pp.getInterval(),
                    features, e -> {
                        if (e.isGCEvent()) {
                            ctx.write(GCEventResponse.toJson((GCEvent) e));
                        } else if (e.isRate()) {
                            ctx.write(GCRateResponse.toJson((GCRate) e));
                        } else if (e.isStatistic()) {
                            analyseStatsCache.put(statsKey(pp), (StatisticAggregateInterceptor) e);
                            ctx.write(JsonSerializer.serialize(e));
                        }
                        delimit(ctx, pp);
                    });
            if (!r.isSuccess()) {
                ctx.write(r.getErrorMessage());
                delimit(ctx, pp);
            } else if (cached != null) {
                ctx.write(JsonSerializer.serialize(cached));
                delimit(ctx, pp);
            }
        });
    }

    private Triple statsKey(PeriodParams pp) {
        return Triple.of(Pair.of(pp.getInterval().getStart().getMillis(), pp.getInterval().getEnd().getMillis()),
                pp.getAnalyseId(), pp.getJvmId());
    }

    /**
     * GET /gc/jvm/events/erase
     * Require Auth (token)
     * Params:
     *  - analyse_id, string (required)
     *  - jvm_id, string (required)
     *  - from, timestamp (required)
     *  - to, timestamp (required)
     *  - tz, string (optional)
     */
    public void jvmEventsErase(RequestContext ctx) {
        PeriodParams pp = new PeriodParams(ctx);

        Optional<GCAnalyse> analyse = analyseRepository.analyse(account(ctx).id(), pp.getAnalyseId());
        analyse.ifPresent(gcAnalyse ->
                eventRepository.erase(pp.getAnalyseId(), pp.getJvmId(), Range.of(pp.getInterval())));
        ctx.response(SUCCESS);
    }

    /**
     * GET /gc/jvm/events/erase/all
     * Require Auth (token)
     * Params:
     *  - analyse_id, string (required)
     *  - jvm_id, string (required)
     */
    public void jvmEventsEraseAll(RequestContext ctx) {
        final String analyseId = ctx.param("analyse_id");
        final String jvmId = ctx.param("jvm_id");

        Optional<GCAnalyse> analyse = analyseRepository.analyse(account(ctx).id(), analyseId);
        if (analyse.isPresent()) {
            DateTime lastEvent = analyse.get().lastEvent().get(jvmId);

            if (lastEvent != null) {
                eventRepository.erase(analyseId, jvmId, Range.of(lastEvent.minusYears(ERASE_ALL_PERIOD_YEARS), lastEvent.plusDays(1)));
            }
        }
        ctx.response(SUCCESS);
    }

    /**
     * GET /gc/jvm/events/stats
     */
    public void jvmStats(RequestContext ctx) {
        PeriodParams pp = new PeriodParams(ctx);

        checkPeriodAndExecute(pp, ctx, () -> {
            EventsResult r = analyticsService.events(account(ctx).id(), pp.getAnalyseId(), pp.getJvmId(), pp.getInterval(),
                    EnumSet.of(GCEventFeature.CALC_STATISTIC), e -> {
                        if (e.isStatistic()) {
                            ctx.write(JsonSerializer.serialize(e));
                        }
                    });
            if (!r.isSuccess()) {
                ctx.write(r.getErrorMessage());
            }
        });
    }

    private void streamEvents(RequestContext ctx, PeriodParams pp, Iterator<GCEvent> eventIterator) {
        while (eventIterator.hasNext()) {
            GCEvent event = eventIterator.next();
            if (event != null) {
                write(ctx, pp, event);
            }
        }
    }

    private void write(RequestContext ctx, PeriodParams pp, GCEvent event) {
        if (!event.generations().equals(OTHER_GENERATION)) {
            ctx.write(GCEventResponse.toJson(event));
            delimit(ctx, pp);
        }
    }

    private void delimit(RequestContext ctx, PeriodParams pp) {
        if (pp.isDelimit()) {
            ctx.write(DEFAULT_CHUNK_DELIMETER);
        }
    }

    private void checkPeriodAndExecute(PeriodParams pp, RequestContext ctx, Runnable ex) {
        if (Days.daysBetween(pp.getInterval().getStart(), pp.getInterval().getEnd()).getDays() >
                config.readInt(ConfigProperty.GC_EVENTS_MAX_INTERVAL_DAYS)) {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Days interval exceeds max "
                    + config.readInt(ConfigProperty.GC_EVENTS_MAX_INTERVAL_DAYS) + " days."));
        } else if (Minutes.minutesBetween(pp.getInterval().getStart(), pp.getInterval().getEnd()).getMinutes() < 1) {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM,
                    "The interval should higher or equal to 1 minute."));
        } else {
            ex.run();
        }
    }

    private Predicate<RequestContext> requiredWithoutPeriod() {
        return c -> c.hasParam("analyse_id") && c.hasParam("jvm_id");
    }

    private Predicate<RequestContext> requiredWithPeriod() {
        return c -> c.hasParam("analyse_id") && c.hasParam("jvm_id")
                && c.hasParam("from") && c.hasParam("to");
    }

    private String message() {
        return "Params analyse_id and jvm_id are restricted.";
    }

    private String periodMessage() {
        return "Params required: analyse_id, jvm_id, from, to";
    }

    protected static class PeriodParams {
        private final String analyseId;
        private final String jvmId;
        private final Interval interval;
        private final boolean delimit;
        private final boolean stats;

        public String getAnalyseId() {
            return analyseId;
        }
        public String getJvmId() {
            return jvmId;
        }
        public Interval getInterval() {
            return interval;
        }
        public boolean isDelimit() {
            return delimit;
        }
        public boolean isStats() {
            return stats;
        }

        public PeriodParams(RequestContext ctx) {
            DateTimeZone tz;
            try {
                tz = DateTimeZone.forID(ctx.param("tz", "UTC"));
            } catch (Throwable t) {
                ctx.finish(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Param tz is invalid."));
                throw Exceptions.runtime(t);
            }
            this.analyseId = ctx.param("analyse_id");
            this.jvmId = ctx.param("jvm_id");
            this.interval = new Interval(new DateTime(Long.parseLong(ctx.param("from")), tz),
                    new DateTime(Long.parseLong(ctx.param("to")), tz));
            this.delimit = Boolean.parseBoolean(ctx.param("delimit", "false"));
            this.stats = Boolean.parseBoolean(ctx.param("stats", "false"));
        }
    }
}
