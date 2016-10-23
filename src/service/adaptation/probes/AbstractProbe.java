/**
 * 
 */
package service.adaptation.probes;

import java.util.LinkedList;
import java.util.List;

import service.auxiliary.ServiceDescription;

/**
 * Abstract probe class with lists of defined interfaces
 * @author Yifan Ruan
 * @email ry222ad@student.lnu.se
 *
 */
public abstract class AbstractProbe<E> {

    protected List<E> subscribers = new LinkedList<E>();

    /**
     * Probe when workflow started
     * @param qosRequirement  the QoS requirements for executing the current workflow
     * @param params     initial parameters for the current workflow
     */
    public void workflowStarted(String qosRequirement, Object[] params) {
    }

    /**
     * Probe when workflow ended
     * @param result  the result after executing the current workflow
     * @param qosRequirement the QoS requirements for executing the current workflow
     * @param params initial parameters for the current workflow
     */
    public void workflowEnded(Object result, String qosRequirement, Object[] params) {
    }

    /**
     * Probe when invoking a service
     * @param service the invoked service description
     * @param opName the invoked operation name
     * @param params the parameters of the invoked operation
     */
    public void serviceOperationInvoked(ServiceDescription service, String opName, Object[] params) {
    }

    /**
     * Probe when receiving the response from a service
     * @param service the invoked service description
     * @param result the result after the operation invoked
     * @param opName the invoked operation name
     * @param params the parameters of the invoked operation
     */
    public void serviceOperationReturned(ServiceDescription service, Object result, String opName, Object[] params) {
    }

    /**
     * Probe when a service timeouts
     * @param service the invoked service description
     * @param opName the invoked operation name
     * @param params the parameters of the invoked operation
     */
    public void serviceOperationTimeout(ServiceDescription service, String opName, Object[] params) {
    }

    /**
     * Probe the cost of a service
     * @param service the invoked service description
     * @param opName the invoked operation name
     */
    public void costOperation(ServiceDescription service, String opName) {
    }
    
    /**
     * Probe when the service to be invoked not found
     * @param serviceType the not found service type
     * @param serviceName the not found service name
     */
    public void serviceNotFound(String serviceType, String serviceName){
    }
    
    /**
     * Register a probe 
     * @param e subscriber object
     */
    public void register(E e){
    	subscribers.add(e);
    }
    
    /**
     * Unregister a probe
     * @param e subscriber object
     */
    public void unRegister(E e){
    	subscribers.remove(e);
    }

}
