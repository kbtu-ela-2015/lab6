import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

import java.util.Scanner;
import java.util.UUID;
    
public class Client {
    
  private Connection connection;
  private Channel channel;
  private String requestQueueName = "rpc_queue";
  private String replyQueueName;
  private QueueingConsumer consumer;
  
  private static Client instance;
  
    
  public Client() throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    connection = factory.newConnection();
    channel = connection.createChannel();

    replyQueueName = channel.queueDeclare().getQueue(); 
    consumer = new QueueingConsumer(channel);
    channel.basicConsume(replyQueueName, true, consumer);
  }
  
  public String call(String message) throws Exception {     
    String response = null;
    String corrId = UUID.randomUUID().toString();
    
    BasicProperties props = new BasicProperties
                                .Builder()
                                .correlationId(corrId)
                                .replyTo(replyQueueName)
                                .build();
    
    channel.basicPublish("", requestQueueName, props, message.getBytes());
    
    while (true) {
      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
      if (delivery.getProperties().getCorrelationId().equals(corrId)) {
        response = new String(delivery.getBody(),"UTF-8");
        break;
      }
    }

    return response; 
  }
    
  public void close() throws Exception {
    connection.close();
  }
 
  public static Client getInstance() throws Exception {
	  
      if (instance == null) {
          instance = new Client();  
      }
      return instance;
  }
 
}