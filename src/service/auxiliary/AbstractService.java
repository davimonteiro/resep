package service.auxiliary;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import service.provider.MessageReceiver;
import service.provider.ServiceProvider;
import service.provider.ServiceProviderFactory;
import service.utility.Time;

/**
 * 
 *  Basic unit to create a service,
 *  providing functionality to create atomic and composite services
 */
public abstract class AbstractService implements MessageReceiver {

    //private String serviceName;
    private String serviceEndpoint;
    private ServiceProvider serviceProvider;

    private AtomicInteger messageCount = new AtomicInteger(0);
    private Map<Integer, Object> results = new ConcurrentHashMap<Integer, Object>();
    private ServiceDescription serviceDescription;
    private Object NullObject = new Object();
    private ExecutorService executors;

    public static final boolean DEBUG = false;

    /**
     * Constructor
     * @param serviceName  the service name
     * @param serviceEndpoint the service endpoint
     */
    public AbstractService(String serviceName, String serviceEndpoint) {
		serviceProvider = ServiceProviderFactory.createServiceProvider();
		//this.serviceName = serviceName;
		this.serviceEndpoint = serviceEndpoint;
		serviceDescription = new ServiceDescription(serviceName,serviceEndpoint);
		createServiceDescription();
		readConfiguration();
		applyConfiguration();
    }

    /**
     * Constructor
     * @param serviceName the service name
     * @param serviceEndpoint the service endpoint
     * @param responseTime the response time
     */
    public AbstractService(String serviceName, String serviceEndpoint, int responseTime) {
    	this(serviceName, serviceEndpoint);
    	serviceDescription = new ServiceDescription(serviceName, serviceEndpoint, responseTime);
    }

    /**
     * Send request to invoke a service
     * @param service     the service name
     * @param destination the target endpoint
     * @param reply       requires reply or not
     * @param opName      the invoked operation name
     * @param params      parameters for the operation
     * @return the service result
     */
    public Object sendRequest(String service, String destination, boolean reply, String opName, Object... params) {
    	return sendRequest(service, destination, reply, -1, opName, params);
    }

    /**
     * Send request to invoke a service with specific waiting time
     * @param service the service name
     * @param destination the target endpoint
     * @param reply requires reply or not
     * @param responseTime   the max time for waiting a reply
     * @param opName  the invoked operation name
     * @param params parameters for the operation
     * @return the service result
     */
    public Object sendRequest(String service, String destination, boolean reply, long responseTime, String opName, Object... params) {
		try {
			int messageID = messageCount.incrementAndGet();
			Request request = new Request(messageID, this.serviceEndpoint,service, opName, params);
			XMLBuilder build = new XMLBuilder();
			String requestMessage = build.toXML(request);

			serviceProvider.sendMessage(requestMessage, destination);

			if (DEBUG)
				System.out.println("The request message is: \n"+ requestMessage);

			if (reply) {
				synchronized (this) {
					if (responseTime == -1) {
						while (!results.containsKey(messageID)) {
							this.wait();
						}
					} else {
						long startTime = System.currentTimeMillis();
						while (!results.containsKey(messageID)) {
							this.wait(responseTime * Time.scale);
							long endTime = System.currentTimeMillis();
							if ((endTime - startTime) / Time.scale >= responseTime) {
								results.put(messageID, new TimeOutError());
							}
						}
					}
				}

				Object result = results.get(messageID);
				results.remove(messageID);
				return result != NullObject ? result : null;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }

    /**
     * Send response with a result for a specific request message
     * @param requestID the id of request to be responsed
     * @param result the result of invoked operation
     * @param destination the target endpoint
     */
    private void sendResponse(int requestID, Object result, String destination) {
    	Response response = new Response(messageCount.incrementAndGet(), requestID, this.serviceEndpoint, result);
    	XMLBuilder build = new XMLBuilder();
    	String responseMessage = build.toXML(response);
    	serviceProvider.sendMessage(responseMessage, destination);
    }

    /**
     * Start the service
     * Listen for incoming messages
     */
    public void startService() {
    	serviceProvider.startListening(serviceEndpoint, this);
    }

    /**
     * Stop the service
     */
    public void stopService() {
    	serviceProvider.stopListening();
    }

    @Override
    public void onMessage(final String message) {
		try {
			AbstractMessage msg = (AbstractMessage) (new XMLBuilder()
					.fromXML(message));
			final int requestID = msg.getId();
			String messageType = msg.getType();
			final String destination = msg.getEndpoint();
			switch (messageType) {
			case "request": {
				if (DEBUG)
					System.out.println("Receiving the request: \n" + message);
				final Request request = (Request) msg;
				executors.submit(new Callable<Object>() {

					@Override
					public Object call() throws Exception {
						try {

							Object result = invokeOperation(request.getOpName(), request.getParams());

							if (result instanceof OperationAborted)
								return null;
							sendResponse(requestID, result, destination);

						} catch (Exception e) {
							e.printStackTrace();
						}

						return null;
					}
				});
				break;
			}
			case "response": {
				if (DEBUG)
					System.out.println("Receiving the response: \n" + message);
				Response response = (Response) msg;
				if (response.getReturnType() != null) {
					Class<?> type = (Class<?>) response.getReturnType();
					results.put(response.getRequestID(),type.cast(response.getReturnValue()));
				} else {
					results.put(response.getRequestID(), NullObject);
				}
				synchronized (this) {
					this.notifyAll();
				}
				break;
			}
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    abstract public Object invokeOperation(String opName, Param[] args);

    /**
     * Register to the service registry
     */
    public void register() {
    	int registerId = (int) this.sendRequest(ServiceRegistryInterface.NAME, ServiceRegistryInterface.ADDRESS, true, "register", serviceDescription);
    	this.serviceDescription.setRegisterID(registerId);
    	System.out.println("The service " + serviceDescription.getServiceType() + " has been registered. The registerID is " + this.serviceDescription.getRegisterID());
    }

    /**
     * Un register from the service registry
     */
    public void unRegister() {
    	this.sendRequest(ServiceRegistryInterface.NAME, ServiceRegistryInterface.ADDRESS, true, "unRegister", this.serviceDescription.getRegisterID());
    }

    /**
     * Helps to dynamically update the service description
     */
    public void updateServiceDescription() {
    	if (serviceDescription.getRegisterID() > 0)
    		this.sendRequest(ServiceRegistryInterface.NAME, ServiceRegistryInterface.ADDRESS, true, "update", this.serviceDescription);
    	else
    		System.err.println("Service is not registered in the registy yet. It can't be updated.");
    }

    /**
     * Return the service description
     * @return the service description
     */
    public ServiceDescription getServiceDescription() {
    	return serviceDescription;
    }

    /**
     * Set the service description
     * @param serviceDescription the new service description
     */
    public void setServiceDescription(ServiceDescription serviceDescription) {
    	this.serviceDescription = serviceDescription;
    }

    // ////////////////////////////////////////// Service Configuration //////////////////////////////////////////////////////
    protected Configuration configuration;

    /**
     * Return the configuration
     * @return the configuration
     */
    public Configuration getConfiguration() {
    	return this.configuration;
    }

    protected void createServiceDescription() {
		List<Operation> opList = new ArrayList<Operation>();
		for (Method operation : this.getClass().getMethods()) {
			if (operation.getAnnotation(ServiceOperation.class) != null) {
				ServiceOperation serviceOperation = operation.getAnnotation(ServiceOperation.class);
				Operation op = new Operation(operation.getName(),operation.getParameterTypes(), operation.getReturnType().getName());
				op.setOpCost(serviceOperation.OperationCost());
				opList.add(op);
			}
		}
		serviceDescription.setOperationList(opList);
		serviceDescription.setServiceType(this.getClass().getSimpleName());
    }

    abstract protected void readConfiguration();

    protected void applyConfiguration() {
		if (configuration.MultipleThreads == false) {
			executors = Executors.newSingleThreadExecutor();
		} else {
			executors = Executors.newFixedThreadPool(configuration.maxNoOfThreads);
		}
    }
}