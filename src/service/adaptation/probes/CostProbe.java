package service.adaptation.probes;

import service.adaptation.probes.interfaces.CostProbeInterface;
import service.auxiliary.ServiceDescription;

/**
 * 
 * Monitor the cost of service invocations
 * 
 */
public class CostProbe extends AbstractProbe<CostProbeInterface> {

    private double cost;
    
    @Override
    public void costOperation(ServiceDescription service, String opName){
    	cost = service.getOperationCost(opName);
    	notifyCostSubscribers(service.getServiceName(), opName, cost);
    }
    
    /**
     * Notify subscribed probes the cost of a service
     * @param serviceName  the invoked service name
     * @param opName       the invoked operation name
     * @param cost         the operation cost
     */
    private void notifyCostSubscribers(String serviceName, String opName, double cost){
    	for (CostProbeInterface subscriber : subscribers) {
    		subscriber.serviceCost(serviceName, opName, cost);
    	}
    }
    
}
