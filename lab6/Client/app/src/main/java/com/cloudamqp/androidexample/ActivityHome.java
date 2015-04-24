package com.cloudamqp.androidexample;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import com.cloudamqp.R;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;

import android.app.Activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ActivityHome extends Activity {
    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue";
    private String replyQueueName;
    ConnectionFactory factory;
    private QueueingConsumer consumer;
    private EditText et;
    private Button btn;
    private TextView tv;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        et = (EditText)findViewById(R.id.editText);
        btn = (Button)findViewById(R.id.button);
        tv = (TextView)findViewById(R.id.textView);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    new Call().execute(et.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        factory = new ConnectionFactory();
        factory.setHost("172.20.10.3");
        factory.setUsername("username");
        factory.setPassword("mypass");

        new Task().execute();

    }


	@Override
	protected void onDestroy() {
		super.onDestroy();
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class Call extends AsyncTask<String,String,String>{
        ProgressDialog progress;
        @Override
        public void onPreExecute(){
            progress = new ProgressDialog(ActivityHome.this);
            progress.show();
        }
        @Override
        protected String doInBackground(String... strings) {
            String response = "ERROR";
            String corrId = java.util.UUID.randomUUID().toString();

            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueueName)
                    .build();

            try {
                channel.basicPublish("", requestQueueName, props, strings[0].getBytes());
                while (true) {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                    if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                        response = new String(delivery.getBody());
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            return response;
        }
        @Override
        public void onPostExecute(String result){
            progress.dismiss();
            tv.setText(result);
        }
    }
    private class Task extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                connection = factory.newConnection();
                channel = connection.createChannel();

                replyQueueName = channel.queueDeclare().getQueue();
                consumer = new QueueingConsumer(channel);
                channel.basicConsume(replyQueueName, true, consumer);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("CONNECTION","FAILED");
            }
            return Boolean.FALSE;
        }
    }
}
