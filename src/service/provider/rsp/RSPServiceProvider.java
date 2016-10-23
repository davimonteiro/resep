package service.provider.rsp;

import service.provider.MessageReceiver;
import service.provider.ServiceProvider;

/**
 * 
 * A service provider with functions of 
 * sending message and handling incoming messages
 */
public class RSPServiceProvider implements ServiceProvider {

    private RSPMessagingService rspMessagingService;
    private String endPoint;
    private MessageReceiver messageReceiver;
   
    /**
     * Constructor
     */
    public RSPServiceProvider() {
    	rspMessagingService = RSPMessagingService.getInstance();
    }
   
    @Override
    public void startListening(String endPoint, MessageReceiver messageReceiver) {
    	this.endPoint = endPoint;
    	this.messageReceiver = messageReceiver;
    	rspMessagingService.register(endPoint, messageReceiver);
    }

    @Override
    public void stopListening() {
    	rspMessagingService.deregister(endPoint);
    }

    @Override
    public void sendMessage(String msgText, String destinationEndPoint) {
    	rspMessagingService.sendMessage(endPoint, destinationEndPoint, msgText);
    }

}
