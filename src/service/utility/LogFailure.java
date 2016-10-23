package service.utility;
import service.adaptation.probes.AbstractProbe;
import service.auxiliary.ServiceDescription;

/**
 * 
 * @author Yifan Ruan
 * @email  ry222ad@student.lnu.se
 */
public class LogFailure extends AbstractProbe{

    @Override
    public void workflowStarted(String qosRequirement, Object[] params) {
	
    }

    @Override
    public void workflowEnded(Object result, String qosRequirement, Object[] params) {
	
    }

    @Override
    public void serviceOperationInvoked(ServiceDescription service, String opName, Object[] params) {
    }

    @Override
    public void serviceOperationReturned(ServiceDescription service, Object result, String opName, Object[] params) {
    }

    @Override
    public void serviceOperationTimeout(ServiceDescription service, String opName, Object[] params) {
    	System.err.println("Service Failed:" + service.getServiceType());
    }

}
