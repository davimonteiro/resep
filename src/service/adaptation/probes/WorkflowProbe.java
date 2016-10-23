package service.adaptation.probes;

import service.adaptation.probes.interfaces.WorkflowProbeInterface;
import service.auxiliary.ServiceDescription;

/**
 * 
 * Monitor the start and end of a workflow execution
 * 
 */
public class WorkflowProbe extends AbstractProbe<WorkflowProbeInterface>{
    
    @Override
    public void workflowStarted(String qosRequirement, Object[] params){
    	notifyWorkflowStarted(qosRequirement, params);
    }

    @Override
    public void workflowEnded(Object result, String qosRequirement, Object[] params){
    	notifyWorkflowEnded(result, qosRequirement, params);
    }
    
    @Override
    public void serviceOperationInvoked(ServiceDescription service, String opName, Object[] params){
    	notifyServiceOperationInvoked(service, opName, params);
    }

    @Override
    public  void serviceOperationReturned(ServiceDescription service, Object result, String opName, Object[] params){
    	notifyServiceOperationReturned(service, result, opName, params);
    }

    @Override
    public void serviceOperationTimeout(ServiceDescription service, String opName, Object[] params){
    	notifyServiceOperationTimeout(service, opName, params);
    }
    
    @Override
    public void serviceNotFound(String serviceType, String opName){
    	notifyServiceNotFound(serviceType, opName);
    }
    
    private void notifyWorkflowStarted(String qosRequirement, Object[] params){
    	for(WorkflowProbeInterface workflowInterface: subscribers){
    		workflowInterface.workflowStarted(qosRequirement, params);
    	}
    }

    private void notifyWorkflowEnded(Object result, String qosRequirement, Object[] params){
    	for(WorkflowProbeInterface workflowInterface: subscribers){
    		workflowInterface.workflowEnded(result, qosRequirement, params);
    	}
    }
    
    private void notifyServiceOperationInvoked(ServiceDescription service, String opName, Object[] params){
    	for (WorkflowProbeInterface workflowInterface : subscribers) {
    		workflowInterface.serviceOperationInvoked(service, opName, params);
    	}
    }
    
    private void notifyServiceOperationReturned(ServiceDescription service, Object result, String opName, Object[] params){
    	for (WorkflowProbeInterface workflowInterface : subscribers) {
    		workflowInterface.serviceOperationReturned(service, result, opName, params);
    	}
    }
    
    private void notifyServiceOperationTimeout(ServiceDescription service, String opName, Object[] params){
    	for (WorkflowProbeInterface workflowInterface : subscribers) {
    		workflowInterface.serviceOperationTimeout(service, opName, params);
    	}
    }
    
    private void notifyServiceNotFound(String serviceType, String opName){
    	for (WorkflowProbeInterface workflowInterface : subscribers) {
    		workflowInterface.serviceNotFound(serviceType, opName);
    	}
    }
}
