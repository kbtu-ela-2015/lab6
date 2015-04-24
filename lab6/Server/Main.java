package com.company;

import com.rabbitmq.client.*;

import java.math.BigInteger;

public class Main {
    private static final String RPC_QUEUE_NAME = "rpc_queue";
    public static void main(String[] args) throws Exception {


        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);

        channel.basicQos(1);

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(RPC_QUEUE_NAME, false, consumer);

        System.out.println(" [x] Awaiting RPC requests");

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();

            AMQP.BasicProperties props = delivery.getProperties();
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(props.getCorrelationId())
                    .build();

            String message = new String(delivery.getBody());
            int n = Integer.parseInt(message);

            System.out.println(" [.] fib(" + message + ")");
            String response = "" + fib(n);

            channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes());

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }
    private static BigInteger fib(int n) throws Exception {
        if (n <= 2) return BigInteger.ONE;
        //F(n-2)
        BigInteger x = BigInteger.ONE;
        //F(n-1)
        BigInteger y = BigInteger.ONE;
        //F(n)
        BigInteger ans = BigInteger.ZERO;
        for (int i = 2; i < n; i++)
        {
            ans = x.add(y);
            x = y;
            y = ans;
        }
        return ans;
    }

}
