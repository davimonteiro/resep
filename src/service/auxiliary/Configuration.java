/**
 * 
 */
package service.auxiliary;

/**
 * 
 * List of parameter definitions for configuring
 * 
 */
public class Configuration {
    
    /**
     * Constructor
     * @param multipleThreads use single thread or multiple threads
     * @param maxNoOfThreads the max number of multiple threads to be used
     * @param maxQueueSize the max queue size
     * @param maxResponseTime the max responsible time for services
     * @param sDCacheMode the mode to fetch the available services, from the local cache or from the service registry
     * @param sDCacheShared   allow multiple threads to share the same cache or not
     * @param sDCacheTimeout  the refresh period for the cache
     * @param sDCacheSize the max cache size
     */
    public Configuration(boolean multipleThreads, int maxNoOfThreads, int maxQueueSize, int timeout, boolean ignoreTimeoutError,int maxRetryAttempts,boolean sDCacheMode, boolean sDCacheShared, int sDCacheTimeout,
	    int sDCacheSize) {
		this(multipleThreads, maxNoOfThreads, maxQueueSize);
		this.timeout = timeout;
		this.ignoreTimeoutError = ignoreTimeoutError;
		this.maxRetryAttempts = maxRetryAttempts;
		SDCacheMode = sDCacheMode;
		SDCacheShared = sDCacheShared;
		SDCacheTimeout = sDCacheTimeout;
		SDCacheSize = sDCacheSize;
    }

    /**
     * Constructor
     * @param multipleThreads use single thread or multiple threads
     * @param maxNoOfThreads the max number of multiple threads to be used
     * @param maxQueueSize the max queue size
     */
    public Configuration(boolean multipleThreads, int maxNoOfThreads, int maxQueueSize) {
		this.MultipleThreads = multipleThreads;
		this.maxNoOfThreads = maxNoOfThreads;
		this.maxQueueSize = maxQueueSize;
		if (maxNoOfThreads == -1 && MultipleThreads == true) {
			maxNoOfThreads = Runtime.getRuntime().availableProcessors();
		}
    }
    
    public boolean MultipleThreads;
    public int maxNoOfThreads;
    public int maxQueueSize;
    public int timeout;
    public boolean ignoreTimeoutError;
    public int maxRetryAttempts;
    public boolean SDCacheMode;
    public boolean SDCacheShared;
    public int SDCacheTimeout;
    public int SDCacheSize;

}
