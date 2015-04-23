import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;
import java.util.StringTokenizer;
  
public class RPCServer {
  
  private static final String RPC_QUEUE_NAME = "rpc_queue";
  
  private static int fib(int n) {
    if (n ==0) return 0;
    if (n == 1) return 1;
    return fib(n-1) + fib(n-2);
  }
    
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
  
      System.out.println(" [x] Awaiting RPC requests");
  
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
         
  /////////////////////////operations
          System.out.println(" [.] reseived>>" + message);
          
          response = getResult(message)+"";
          if(response.equalsIgnoreCase("Infinity")) response="error: divide by zero";
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
  
  
  
  public static double getResult(String s){
      double res = 0;
      
      if(s.charAt(0)=='+'){
          StringTokenizer stringTokenizer = new StringTokenizer(s.substring(1), "|");
          double fnum = Double.parseDouble(stringTokenizer.nextToken());
          double lnum = Double.parseDouble(stringTokenizer.nextToken());
          
          res=fnum+lnum;
      }else
          if(s.charAt(0)=='-'){
            StringTokenizer stringTokenizer = new StringTokenizer(s.substring(1), "|");
            double fnum = Double.parseDouble(stringTokenizer.nextToken());
            double lnum = Double.parseDouble(stringTokenizer.nextToken());
            
            res=fnum-lnum;
          }else
              if(s.charAt(0)=='*'){
                StringTokenizer stringTokenizer = new StringTokenizer(s.substring(1), "|");
                double fnum = Double.parseDouble(stringTokenizer.nextToken());
                double lnum = Double.parseDouble(stringTokenizer.nextToken());
                
                res=fnum*lnum;
              }else
                  if(s.charAt(0)=='/'){
                StringTokenizer stringTokenizer = new StringTokenizer(s.substring(1), "|");
                double fnum = Double.parseDouble(stringTokenizer.nextToken());
                double lnum = Double.parseDouble(stringTokenizer.nextToken());
                
                res=fnum/lnum;
                      System.out.println("send>>>"+res);
              }else
                      if(s.charAt(0)=='e'){
                          StringTokenizer stringTokenizer = new StringTokenizer(s.substring(1), "|");
                            double fnum = Double.parseDouble(stringTokenizer.nextToken());
                            double lnum = Double.parseDouble(stringTokenizer.nextToken());
                            
                            res=Math.pow(fnum, lnum);
                
                      }else
                          if(s.charAt(0)=='r'){
                              res=Math.sqrt(Double.parseDouble(s.substring(1)));
                          }else
                              if(s.charAt(0)=='v'){
                                  res=1/Double.parseDouble(s.substring(1));
                              }else
                                  if(s.startsWith("sin")){
                                      res=Math.sin(Double.parseDouble(s.substring(3)));
                                  }else
                                      if(s.startsWith("cos")){
                                          res=Math.cos(Double.parseDouble(s.substring(3)));
                                      }else
                                          if(s.startsWith("tan")){
                                              res=Math.tan(Double.parseDouble(s.substring(3)));
                                              
                                          }else
                                              if(s.startsWith("ctg")){
                                                  res=Math.cos(Double.parseDouble(s.substring(3)))/Math.sin(Double.parseDouble(s.substring(3)));
                                              }
      
      return res;
  }
  
}
