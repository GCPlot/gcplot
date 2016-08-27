package com.gcplot.controllers.gc;

import com.gcplot.commons.FileUtils;
import com.gcplot.controllers.Controller;
import com.gcplot.repository.GCAnalyseRepository;
import com.gcplot.repository.GCEventRepository;
import com.gcplot.web.RequestContext;
import com.gcplot.web.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/13/16
 */
public class EventsController extends Controller {

    @PostConstruct
    public void init() {
        dispatcher.requireAuth().blocking().filter(c -> c.files().size() == 1,
                "You should provide only a single log file.").post("/gc/upload_log", this::processGCLog);
    }

    public void processGCLog(RequestContext ctx) {
        UploadedFile uf = ctx.files().get(0);

        try {
            System.out.println(uf.file());
        } finally {
            FileUtils.deleteSilent(uf.file());
        }
    }

    @Autowired
    protected GCAnalyseRepository analyseRepository;
    @Autowired
    protected GCEventRepository eventRepository;

}
