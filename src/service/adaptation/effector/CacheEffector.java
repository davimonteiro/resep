package service.adaptation.effector;

import java.util.List;

import service.auxiliary.Operation;
import service.auxiliary.ServiceDescription;
import service.composite.CompositeService;
import service.composite.SDCache;

/**
 * 
 * Responsible for changing cache
 * 
 */
public class CacheEffector {

    private CompositeService compositeService;
  
    /**
     * Constructor
     * @param compositeService which composite service to be affected
     */
    public CacheEffector(CompositeService compositeService) {
    	this.compositeService = compositeService;
    }

    /**
     * Remove service from cache
     * @param service the service description
     */
    public void removeService(ServiceDescription service) {
	    compositeService.getCache().remove(service);
    }
    
    /**
     * Remove service from cache with register id
     * @param service the unique register id
     */
    public void removeService(int registerId) {
	    compositeService.getCache().remove(registerId);
    }

    /**
     * Remove service from cache with service description and operation name
     * @param service the service description
     * @param opName the operation name
     */
    public void removeService(ServiceDescription service, String opName) {
    	compositeService.getCache().remove(service, opName);
    }

    /**
     * Refresh the cache
     * all services will be removed.
     */
    public void refreshCache() {
    	compositeService.getCache().refresh();
    }
    
    /**
     * Return all services with same type and operation
     * @param serviceType the service type
     * @param opName the operation name
     */
    public void getAllServices(String serviceType, String opName){
    	compositeService.getCache().remove(serviceType, opName);
    	compositeService.lookupService(serviceType, opName);
    }

    /**
     * Remove all services with same service description and operation
     * @param service the service description
     * @param opName the operation name
     * @return a list of service descriptions after refreshing
     */
    public List<ServiceDescription> refreshCache(ServiceDescription service, String opName) {
    	removeService(service, opName);
    	return compositeService.lookupService(service.getServiceType(), opName);
    }

    /**
     * Update service description
     * @param oldService the old service description
     * @param newService the new service description
     */
    public void updateServiceDescription(ServiceDescription oldService, ServiceDescription newService) {
    	if (oldService.getRegisterID() == newService.getRegisterID()) {
    		for (Operation operation : oldService.getOperationList())
    			compositeService.getCache().update(oldService, newService, operation.getOpName());
    	}
    }
    
    /**
     * Returns a service description by its registration id
     * @param registerationId
     * @return service description of the service
     */
    public ServiceDescription getService(int registerId){
	return compositeService.getCache().getServiceDescription(registerId);
    }
}
