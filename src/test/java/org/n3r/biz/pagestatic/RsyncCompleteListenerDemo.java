package org.n3r.biz.pagestatic;

import org.n3r.biz.pagestatic.base.RsyncCompleteListener;
import org.n3r.biz.pagestatic.bean.RsyncRunInfo;

public class RsyncCompleteListenerDemo implements RsyncCompleteListener {
    @Override
    public void onComplete(RsyncRunInfo rsyncRunInfo) {
        System.err.println(rsyncRunInfo.getCommandLine() + "'s exitValue:" + rsyncRunInfo.getExitValue());
    }
}
