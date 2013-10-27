package org.n3r.biz.pagestatic.base;

import org.n3r.biz.pagestatic.bean.RsyncRunInfo;

public interface RsyncCompleteListener {
    void onComplete(RsyncRunInfo rsyncRunInfo);
}
