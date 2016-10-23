package service.provider.activemq;

import java.net.ConnectException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import service.provider.MessageReceiver;
import service.provider.ServiceProvider;


/**
 * 
 * The service provider with ActiveMQ
 * 
 */
public class ActiveMQProvider implements ServiceProvider, MessageListener {

    private InitialContext initContext;
    private QueueConnectionFactory queueConnectingFactory;
    private QueueConnection queueConnection;
    //private String endPoint;
    private MessageReceiver messageReceiver;

    @Override
    public void sendMessage(String msgText, String destinationEndPoint) {
    	try {
    		Queue destination = (Queue) initContext.lookup("dynamicQueues/" + destinationEndPoint);
    		QueueConnection connection = queueConnectingFactory.createQueueConnection();
    		QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    		MessageProducer sender = session.createProducer(destination);
    		sender.send(session.createTextMessage(msgText));
    		connection.close();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    @Override
    public void startListening(String endPoint, MessageReceiver messageReceiver) {
    	try {
    		//this.endPoint = endPoint;
    		this.messageReceiver = messageReceiver;
	    
    		initContext = new InitialContext();
    		queueConnectingFactory = (QueueConnectionFactory) initContext.lookup("ConnectionFactory");

    		Queue queue = (Queue) initContext.lookup("dynamicQueues/" + endPoint);

    		queueConnection = queueConnectingFactory.createQueueConnection();
    		QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

    		MessageConsumer receiver = session.createConsumer(queue);
    		receiver.setMessageListener(this);

    		queueConnection.start();
    	} catch (Exception e) {
    		if (e.getCause() instanceof ConnectException) {
    			System.err.println("Cannot connect to ActivMQ. Please make sure that ActivMQ is working.");
    		}
    		e.printStackTrace();
    	}
    }
    
    @Override
    public void stopListening() {
    	try {
    		queueConnection.close();
    	} catch (JMSException e) {
    		e.printStackTrace();
    	}
    }

    @Override
    public void onMessage(Message message) {
		String msgText;
		try {
			msgText = ((TextMessage) message).getText();
			messageReceiver.onMessage(msgText);
		} catch (JMSException e) {
			e.printStackTrace();
		}
    }
}
