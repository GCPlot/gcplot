package com.gcplot.log_processor.parser.producers.v8;

import com.gcplot.logs.LogMetadata;
import com.google.common.base.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/6/16
 */
public class MetadataInfoProducer {
    private static final String MEM_STARTER = "Memory:";
    private static final String CMD_STARTER = "CommandLine flags:";
    private static final Pattern PAGE_SIZE_PATTERN = Pattern.compile("(?<pagesize>[0-9]+)(?<pgsign>[kmgGMK]?)[ \\t]page");
    private static final Pattern PHYSICAL_PATTERN = Pattern.compile("physical[ \\t]+(?<total>[0-9]+)(?<totalsign>[kmgGMK]?)\\((?<free>[0-9]+)(?<freesign>[kmgGMK]?)[ \\t]+free\\)");
    private static final Pattern SWAP_PATTERN = Pattern.compile("swap[ \\t]+(?<total>[0-9]+)(?<totalsign>[kmgGMK]?)\\((?<free>[0-9]+)(?<freesign>[kmgGMK]?)[ \\t]+free\\)");
    protected LogMetadata logMetadata;

    public void parse(String s) {
        if (s.startsWith(MEM_STARTER)) {
            checkNonNull();
            Matcher psm = PAGE_SIZE_PATTERN.matcher(s);
            if (psm.find()) {
                long r = ratio(psm.group("pgsign"));
                logMetadata.pageSize(Long.parseLong(psm.group("pagesize")) * r);
            }
            Matcher psp = PHYSICAL_PATTERN.matcher(s);
            if (psp.find()) {
                long tr = ratio(psp.group("totalsign"));
                long fr = ratio(psp.group("freesign"));
                logMetadata.physicalTotal(Long.parseLong(psp.group("total")) * tr);
                logMetadata.physicalFree(Long.parseLong(psp.group("free")) * fr);
            }
            Matcher swp = SWAP_PATTERN.matcher(s);
            if (swp.find()) {
                long tr = ratio(swp.group("totalsign"));
                long fr = ratio(swp.group("freesign"));
                logMetadata.swapTotal(Long.parseLong(swp.group("total")) * tr);
                logMetadata.swapFree(Long.parseLong(swp.group("free")) * fr);
            }
        } else if (s.startsWith(CMD_STARTER)) {
            checkNonNull();
            logMetadata.commandLines(s.replace(CMD_STARTER, "").trim());
        }
    }

    public LogMetadata getLogMetadata() {
        return logMetadata;
    }

    private void checkNonNull() {
        if (logMetadata == null) {
            logMetadata = new LogMetadata();
        }
    }

    private long ratio(String sign) {
        switch (Strings.nullToEmpty(sign).toLowerCase()) {
            case "k": return 1024;
            case "m": return 1024 * 1024;
            case "g": return 1024 * 1024 * 1024;
            default: return 1;
        }
    }

}
