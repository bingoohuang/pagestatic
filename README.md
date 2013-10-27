PageStatic
==========

Retrieve remote web page content and rsync to multiple hosts.

+ when given page url links, PageStatic will reuse http connections by HttpClient.
+ the rsync upload will triggered automatically by the content file max num or timeout.
+ rsync will be checked on timeout and exit-value to ensure its success.
+ all remote hosts will requried password-less to use rsync (rsync -az localdir user@remotehost:remotedir).
+ because of PageStatic's own concurrency, only one PageStatic instance is enough.


## Create PageStatic by program with same upload path on remote hosts

```
PageStatic pageStatic = new PageStaticBuilder()
    	    .addRsyncRemote("10.142.151.1", "mall")	.addRsyncRemote("10.142.151.2", "mall")	.addRsyncRemote("10.142.151.3", "mall")	.addRsyncRemote("10.142.151.4", "mall")	.addRsyncDir("/home/mall/pagestatic/pagehtml/", "/app/pagestatic/")		 // optinal	.httpSocketTimeoutSeconds(60) // default 30 seconds	.triggerUploadWhenMaxFiles(100) // default 100	.triggerUploadWhenMaxSeconds(60) // default 120	.deleteLocalDirAfterRsync(true)  // default true	.maxUrlContentGeneratingThreads(10) // default 1	.rsyncTimeoutSeconds(60) // default 30 seconds	.rsyncRetryTimes(3) // default 3		.build();
```
## Create PageStatic by program with diffrent upload path on remote hosts

```
PageStatic pageStatic = new PageStaticBuilder(
		.addRsyncRemote("10.142.151.1", "mall")	.addRsyncRemote("10.142.151.2", "mall")	.addRsyncRemote("10.142.151.3", "mall")	.addRsyncRemote("10.142.151.4", "mall")	.addRsyncDir("/home/mall/pagestatic/pagehtml/", "10.142.151.1:/app/pagestatic1/")	.addRsyncDir("/home/mall/pagestatic/pagehtml/", "10.142.151.2:/app/pagestatic2/")	.addRsyncDir("/home/mall/pagestatic/pagehtml/", "10.142.151.3:/app/pagestatic3/")	.addRsyncDir("/home/mall/pagestatic/pagehtml/", "10.142.151.4:/app/pagestatic4/")
     // optinal
    .httpSocketTimeoutSeconds(60) // default 30 seconds
    .triggerUploadWhenMaxFiles(100) // default 100
    .triggerUploadWhenMaxSeconds(60) // default 120
    .deleteLocalDirAfterRsync(true)  // default true
    .maxUrlContentGeneratingThreads(10) // default 1
    .rsyncTimeoutSeconds(60) // default 30 seconds
    .rsyncRetryTimes(3) // default 3
	.build();
```

## Create PageStatic by config with same upload path on remote hosts 

The config is based on [diamond-client](https://github.com/bingoohuang/diamond-miner).
Set the group to **PageStatic** and dataid to **Demo**


```
addRsyncRemote(10.142.151.1, mall)addRsyncRemote(10.142.151.2, mall)addRsyncRemote(10.142.151.3, mall)addRsyncRemote(10.142.151.4, mall)addRsyncDir(/home/mall/pagestatic/pagehtml/, /app/pagestatic/)// optinal
httpSocketTimeoutSeconds(60) // default 30 seconds
triggerUploadWhenMaxFiles(100) // default 100
triggerUploadWhenMaxSeconds(60) // default 120
deleteLocalDirAfterRsync(true)  // default true
maxUrlContentGeneratingThreads(10) // default 1
rsyncTimeoutSeconds(60) // default 30 seconds
rsyncRetryTimes(3) // default 3
```

And then build PageStatic from config like:

```
PageStatic pageStatic 	= new PageStaticBuilder().fromSpec("DEMO").build();		
```


## Use PageStatic

```pageStatic.startupBatch();// batch processfor (…) { 	…	// use web url, and local responding file name	pageStatic.urlStaticAndUpload(url, localFile);	// or given the direct content and upload	String content = "<html>I am static html</html>";	pageStatic.directContentUpload(content, localFile);}pageStatic.finishBatch();
```