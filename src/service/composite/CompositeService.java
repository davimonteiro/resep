package service.composite;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import service.adaptation.effector.ConfigurationEffector;
import service.adaptation.probes.CostProbe;
import service.adaptation.probes.WorkflowProbe;
import service.auxiliary.AbstractService;
import service.auxiliary.CompositeServiceConfiguration;
import service.auxiliary.Configuration;
import service.auxiliary.LocalOperation;
import service.auxiliary.Param;
import service.auxiliary.ServiceDescription;
import service.auxiliary.ServiceOperation;
import service.auxiliary.TimeOutError;
import service.composite.qos.MinCostQoS;
import service.composite.qos.ReliabilityQoS;
import service.registry.ServiceRegistry;
import service.workflow.AbstractQoSRequirement;
import service.workflow.WorkflowEngine;

/**
 * 
 * Providing an abstraction to create composite services
 *
 */
public class CompositeService extends AbstractService {

	private String workflow;
	// Initializing probes
	private CostProbe costProbe = new CostProbe();
	private WorkflowProbe workflowProbe = new WorkflowProbe();
	// Initializing effectors
	private ConfigurationEffector configurationEffector = new ConfigurationEffector(
			this);

	// This variable will effect only one thread/invocation of the workflow
	private AtomicBoolean stopRetrying = new AtomicBoolean(false);

	/**
	 * Set the workflow
	 * 
	 * @param workflow
	 *            the new workflow
	 */
	public void setWorkflow(String workflow) {
		this.workflow = workflow;
	}

	private Map<String, AbstractQoSRequirement> qosRequirements = new HashMap<String, AbstractQoSRequirement>();

	private SDCache cache;

	/**
	 * Return the cache
	 * 
	 * @return the current cache
	 */
	public SDCache getCache() {
		return cache;
	}

	@Override
	protected void readConfiguration() {
		try {
			Annotation annotation = this.getClass().getAnnotation(
					CompositeServiceConfiguration.class);
			if (annotation != null
					&& annotation instanceof CompositeServiceConfiguration) {
				CompositeServiceConfiguration CSConfiguration = (CompositeServiceConfiguration) annotation;
				this.configuration = new Configuration(
						CSConfiguration.MultipeThreads(),
						CSConfiguration.MaxNoOfThreads(),
						CSConfiguration.MaxQueueSize(),
						CSConfiguration.Timeout(),
						CSConfiguration.IgnoreTimeOutError(),
						CSConfiguration.MaxRetryAttempts(),
						CSConfiguration.SDCacheMode(),
						CSConfiguration.SDCacheShared(),
						CSConfiguration.SDCacheTimeout(),
						CSConfiguration.SDCacheSize());
			} else {
				// the default configuration
				this.configuration = new Configuration(false, 1, 0, 10, false,
						1, true, true, -1, -1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor
	 * 
	 * @param serviceName
	 *            the service name
	 * @param serviceEndpoint
	 *            the service endpoint
	 * @param workflow
	 *            the workflow
	 */
	public CompositeService(String serviceName, String serviceEndpoint, String workflow) {
		super(serviceName, serviceEndpoint);
		
		this.workflow = workflow;

		if (configuration.SDCacheMode == false) {
			System.err.println("Warning! Cache mode cannot be turned off.");
		} else if (configuration.SDCacheShared == false) {
			System.err.println("Warning! Cache mode sharing cannot be turned off.");
		} else if (configuration.SDCacheSize == 0) {
			System.err.println("Warning! Cache size cannot be equal to zero.");
		}

		// if (this.configuration.SDCacheMode) {
		cache = new SDCache();
		// }
	}

	/**
	 * Add QoS requirement
	 * 
	 * @param requirementName
	 *            the QoS requirement name
	 * @param qosRequirement
	 *            the QoS requirement Object
	 */
	public void addQosRequirement(String requirementName,
			AbstractQoSRequirement qosRequirement) {
		qosRequirements.put(requirementName, qosRequirement);
	}

	/**
	 * Return QoS requirements
	 * 
	 * @return the current QoS requirements
	 */
	public Map<String, AbstractQoSRequirement> getQosRequirements() {
		return qosRequirements;
	}

	/**
	 * Returns list of QoS names added in to the composite service
	 * 
	 * @return list of QoS requirement names
	 */
	@ServiceOperation
	public List<String> getQosRequirementNames() {
		List<String> list = new LinkedList<String>();
		list.addAll(qosRequirements.keySet());
		return list;
	}

	/**
	 * Invoke this composite service to start a workflow with specific QoS
	 * requirements and initial parameters for the workflow
	 * 
	 * @param qosRequirementName
	 *            the QoS requirement name for executing the workflow
	 * @param params
	 *            the initial parameters for the workflow
	 * @return the result after executing the workflow
	 */
	@ServiceOperation
	public Object invokeCompositeService(String qosRequirementName,
			Object params[]) {
		// AbstractQoSRequirement qosRequirement =
		// qosRequirements.get(qosRequirementName);

		// If SDCache shared is not on then a new cache object for the workflow
		// should be created
		// ToDo: Cache is shared among all the workflow invocations. separate
		// local cache is not supported yet.
		// SDCache sdCache = configuration.SDCacheShared == true ? cache : new
		// SDCache() ;
		// WorkflowEngine engine = new WorkflowEngine(this, sdCache);
		WorkflowEngine engine = new WorkflowEngine(this);
		workflowProbe.workflowStarted(qosRequirementName, params);
		Object result = engine.executeWorkflow(workflow, qosRequirementName,
				params);
		workflowProbe.workflowEnded(result, qosRequirementName, params);
		return result;
	}

	@Override
	public Object invokeOperation(String opName, Param[] params) {
		for (Method operation : this.getClass().getMethods()) {
			if (operation.getAnnotation(ServiceOperation.class) != null) {
				try {
					if (operation.getName().equals(opName)) {
						Class<?>[] paramTypes = operation.getParameterTypes();
						int size = paramTypes.length;
						if (size == params.length) {
							Object[] args = new Object[size];
							for (int i = 0; i < size; i++) {
								args[i] = params[i].getValue();
							}
							return operation.invoke(this, args);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("The operation name or params are not valid. Please check and send again!");
				}
			}
		}
		return null;
	}

	/**
	 * Search through service registry to get the list of service descriptions
	 * 
	 * @param serviceType
	 *            the service type
	 * @param opName
	 *            the operation name
	 * @return list of service descriptions with the same service type and
	 *         operation name
	 */
	@SuppressWarnings("unchecked")
	public List<ServiceDescription> lookupService(String serviceType,
			String opName) {
		List<ServiceDescription> serviceDescriptions = cache.get(serviceType,
				opName);
		if (serviceDescriptions == null) {
			serviceDescriptions = (List<ServiceDescription>) this.sendRequest(
					ServiceRegistry.NAME, ServiceRegistry.ADDRESS, true,
					"lookup", serviceType, opName);
			if (serviceDescriptions == null || serviceDescriptions.size() == 0) {
				this.getWorkflowProbe().serviceNotFound(serviceType, opName);
				// serviceDescriptions = this.lookupService(serviceType,
				// opName);
			} else {
				cache.add(serviceType, opName, serviceDescriptions);
			}
		}
		return serviceDescriptions;
	}

	/**
	 * Returns the cost probe
	 * 
	 * @return the cost probe for this composite service
	 */
	public CostProbe getCostProbe() {
		return costProbe;
	}

	/**
	 * Return the workflow probe
	 * 
	 * @return the workflow probe for this composite service
	 */
	public WorkflowProbe getWorkflowProbe() {
		return workflowProbe;
	}

	/**
	 * Return the configuration effector
	 * 
	 * @return the configuration effector for this composite service
	 */
	public ConfigurationEffector getConfigurationEffector() {
		return configurationEffector;
	}

	/**
	 * Returns true if composite service cache contains instances of the
	 * specific service type with operation name
	 * 
	 * @param serviceType
	 *            the service type
	 * @param opName
	 *            the operation name
	 * @return true if cache has service with the same type and operation,
	 *         otherwise false
	 */
	public boolean containServices(String serviceType, String opName) {
		return cache.containsCache(serviceType, opName);
	}

	/**
	 * Get service description using register ID of the service from cache
	 * 
	 * @param registerId
	 *            the register id
	 * @return the service description
	 */
	public ServiceDescription getServiceDescription(int registerId) {
		return cache.getServiceDescription(registerId);
	}

	protected ServiceDescription applyQoSRequirement(String qosRequirementName,
			List<ServiceDescription> serviceDescriptions, String opName,
			Object... params) {
		AbstractQoSRequirement qosRequirement = qosRequirements.get(qosRequirementName);
		if (qosRequirement == null) {
			System.err.println("QoS requirement is null. To select among multiple services, a QoS requirement must have been provided.");
			System.err.println("Selecting a service randomly...");
			return serviceDescriptions.get(new Random().nextInt(serviceDescriptions.size()));
		}
		return qosRequirement.applyQoSRequirement(serviceDescriptions, opName, params);
	}
	
	private MinCostQoS minCostQoS = new MinCostQoS();
	
	private ReliabilityQoS reliabilityQoS = new ReliabilityQoS();
	
	
	private AbstractQoSRequirement requirement = minCostQoS;
	
	public void useMinCostQoSStrategy() {
		requirement = minCostQoS;
	}
	
	public void useReliabilityQoSStrategy() {
		requirement = reliabilityQoS;
	}
	
	// TODO Implementando a estrategia
	private ServiceDescription applyLotusStrategy(List<ServiceDescription> serviceDescriptions, String opName,
			Object... params) {
		return requirement.applyQoSRequirement(serviceDescriptions, opName, params);
	}

	public Object invokeServiceOperation(String qosRequirement,
			String serviceName, String operationName, Object[] params) {

		int timeout = this.getConfiguration().timeout;
		Object resultVal;
		int retryAttempts = 0;
		stopRetrying.set(false);
		do {
			List<ServiceDescription> services = lookupService(serviceName, operationName);
			if (services == null || services.size() == 0) {
				System.out.println("ServiceName: " + serviceName + "." + operationName + "not found!");
				return new TimeOutError();
			}

			// TODO Apply my own strategy
			ServiceDescription service = applyLotusStrategy(services, operationName, params);
			
			// Apply strategy
			//ServiceDescription service = applyQoSRequirement(qosRequirement, services, operationName, params);

			System.out.println("Operation " + service.getServiceType() + "."
					+ operationName
					+ " has been selected with following custom properties:"
					+ service.getCustomProperties());

			this.getWorkflowProbe().serviceOperationInvoked(service, operationName, params);

			int maxResponseTime = timeout != 0 ? timeout : service.getResponseTime() * 3;
			resultVal = this.sendRequest(service.getServiceType(), service.getServiceEndpoint(), true, maxResponseTime, operationName, params);

			if (resultVal instanceof TimeOutError) {
				this.getWorkflowProbe().serviceOperationTimeout(service, operationName, params);
			} else {
				this.getWorkflowProbe().serviceOperationReturned(service, resultVal, operationName, params);
				this.getCostProbe().costOperation(service, operationName);
			}

			if (stopRetrying.get() == true) {
				stopRetrying.set(false);
				break;
			}

			retryAttempts++;
		} while (resultVal instanceof TimeOutError && retryAttempts < this.getConfiguration().maxRetryAttempts);

		return resultVal;
	}

	public Object invokeLocalOperation(String operationName, Object[] params) {
		for (Method operation : this.getClass().getMethods()) {
			if (operation.getAnnotation(LocalOperation.class) != null) {
				if (operation.getName().equals(operationName)) {
					try {
						return operation.invoke(this, params);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		throw new RuntimeException("Local operation " + operationName
				+ " is not found.");
	}

	/**
	 * If a service failed and composite service is retrying, this method will
	 * effect to stop retrying for that service. This method once called will
	 * effect only one service invocation/thread.
	 */
	public void stopRetrying() {
		stopRetrying.set(true);
	}
}
