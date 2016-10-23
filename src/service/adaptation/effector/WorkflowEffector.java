package service.adaptation.effector;

import service.auxiliary.ServiceDescription;
import service.composite.CompositeService;

/**
 * 
 * Responsible for changing workflow
 * 
 */
public class WorkflowEffector extends AbstractEffector {

    private CacheEffector cacheEffector;
    
    /**
     * Constructor
     * @param compositeService which composite service to be affected
     */
    public WorkflowEffector(CompositeService compositeService) {
    	super(compositeService);
    	cacheEffector = new CacheEffector(compositeService);
    }

    /**
     * Update the workflow
     * @param workflow new workflow content
     */
    public void updateWorkflow(String workflow) {
    	compositeService.setWorkflow(workflow);
    }
    
    /**
     * Remove the service from workflow cache
     * @param sd  the service description
     */
    public void removeService(ServiceDescription sd){
    	cacheEffector.removeService(sd);
    }
    
    /**
     * Remove the service from workflow cache with service id
     * @param serviceId the service id
     */
    public void removeService(int serviceId){
    	cacheEffector.removeService(serviceId);
    }
    
    /**
     * Clear the workflow cache
     */
    public void refreshAllServices(){
    	cacheEffector.refreshCache();
    }

    /**
     * Clear the workflow cache
     * @param serviceType  the service type
     * @param opName the operation name
     */
    public void refreshAllServices(String serviceType, String opName){
    	cacheEffector.getAllServices(serviceType, opName);
    }


    /**
     * Update the service description in the workflow cache
     * @param oldService the old service description
     * @param newService the new service description
     */
    public void updateServiceDescription(ServiceDescription oldService, ServiceDescription newService) {
    	cacheEffector.updateServiceDescription(oldService, newService);
    }
    
    /**
     * Update a custom property of a service in service description. This method only effects the values in cache.
     * @param serviceId the service id
     * @param propertyName the property name
     * @param value the value
     */
    public void updateServiceCustomProperty(int serviceId, String propertyName, Object value){
	cacheEffector.getService(serviceId).getCustomProperties().put(propertyName, value);
    }
    
    /**
     * If a service failed and composite service is retrying, this method will effect to stop retrying for that service. 
     * This method once called will effect only one service invocation/thread.
     */
    public void stopRetrying(){
	compositeService.stopRetrying();
    }
}
