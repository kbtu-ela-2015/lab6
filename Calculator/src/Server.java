import java.math.BigInteger;
import java.util.StringTokenizer;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;
  
public class Server {
  
  private static final String RPC_QUEUE_NAME = "rpc_queue";
  

  
    
  public static void main(String[] argv) {
    Connection connection = null;
    Channel channel = null;
    try {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost("localhost");
  
      connection = factory.newConnection();
      channel = connection.createChannel();
      
      channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
  
      channel.basicQos(1);
  
      QueueingConsumer consumer = new QueueingConsumer(channel);
      channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
  
      System.out.println(" [x] Awaiting requests from GUI");

      while (true) {
        String response = null;
        
        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
        
        BasicProperties props = delivery.getProperties();
        BasicProperties replyProps = new BasicProperties
                                         .Builder()
                                         .correlationId(props.getCorrelationId())
                                         .build();
        
        try {
        	
          String message = new String(delivery.getBody(),"UTF-8");          
          if(message.charAt(0)=='F'){  	  
              double s = Double.parseDouble(message.substring(1));
              response = Double.toString(s*s);
          }else 
        	  if(message.charAt(0)=='S'){
                  double s = Double.parseDouble(message.substring(1));
                  response = Double.toString(Math.sqrt(s));
        	  }else
        		  if(message.charAt(0)=='X'){
                      double s = Double.parseDouble(message.substring(1));
                      response = Double.toString(1/s);
        		  }else
            		  if(message.charAt(0)=='P'){
                          double s = Double.parseDouble(message.substring(1));
                          response = Double.toString(s/100);
            		  }else
            			  if(message.charAt(0)=='/'){
            				  StringTokenizer w = new StringTokenizer(message.substring(1),"|");
                              double s1 = Double.parseDouble(w.nextToken());
                              double s2 = Double.parseDouble(w.nextToken());
                              response = Double.toString(s1/s2);
                		  }else
                			  if(message.charAt(0)=='*'){
                				  StringTokenizer w = new StringTokenizer(message.substring(1),"|");
                                  double s1 = Double.parseDouble(w.nextToken());
                                  double s2 = Double.parseDouble(w.nextToken());
                                  response = Double.toString(s1*s2);
                    		  }else
                    			  if(message.charAt(0)=='-'){
                    				  StringTokenizer w = new StringTokenizer(message.substring(1),"|");
                                      double s1 = Double.parseDouble(w.nextToken());
                                      double s2 = Double.parseDouble(w.nextToken());
                                      response = Double.toString(s1-s2);
                        		  }else
                        			  if(message.charAt(0)=='+'){
                        				  StringTokenizer w = new StringTokenizer(message.substring(1),"|");
                                          double s1 = Double.parseDouble(w.nextToken());
                                          double s2 = Double.parseDouble(w.nextToken());
                                          response = Double.toString(s1+s2);
                            		  }
        			  
        	        
        }
        catch (Exception e){
          System.out.println(" [.] " + e.toString());
          response = "";
        }
        finally {  
          channel.basicPublish( "", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
  
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
      }
    }
    catch  (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (connection != null) {
        try {
          connection.close();
        }
        catch (Exception ignore) {}
      }
    }      		      
  }
}