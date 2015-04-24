package kz.kbtu.rooms;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.codebutler.android_websockets.WebSocketClient;

import info.androidhive.webgroupchat.R;

public class MainActivity extends Activity {
	private Button btnSend;
	private EditText inputMsg;

	private WebSocketClient client;
	private MessagesListAdapter adapter;
	private List<Message> listMessages;
	private ListView listViewMessages;

	private Utils utils;

	private String name = null;
    private String room = null;

	private static final String TAG_SELF = "self", TAG_NEW = "new",
			TAG_MESSAGE = "message", TAG_EXIT = "exit";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnSend = (Button) findViewById(R.id.btnSend);
		inputMsg = (EditText) findViewById(R.id.inputMsg);
		listViewMessages = (ListView) findViewById(R.id.list_view_messages);

		utils = new Utils(getApplicationContext());

		Intent i = getIntent();
		name = i.getStringExtra("name");
        room = i.getStringExtra("room");
		btnSend.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				sendMessageToServer(utils.getSendMessageJSON(inputMsg.getText()
						.toString(),"FIT"));
				inputMsg.setText("");
			}
		});

		listMessages = new ArrayList<Message>();

		adapter = new MessagesListAdapter(this, listMessages);
		listViewMessages.setAdapter(adapter);

		client = new WebSocketClient(URI.create(WsConfig.URL_WEBSOCKET
				+ URLEncoder.encode(name)+"&room="+room), new WebSocketClient.Listener() {
			@Override
			public void onConnect() {

			}

			@Override
			public void onMessage(String message) {
				parseMessage(message);

			}

			@Override
			public void onMessage(byte[] data) {
				parseMessage(bytesToHex(data));
			}
			@Override
			public void onDisconnect(int code, String reason) {
				utils.storeSessionId(null);
			}

			@Override
			public void onError(Exception error) {
				showToast("Error : " + error);
			}

		}, null);

		client.connect();
	}


	private void sendMessageToServer(String message) {
		if (client != null && client.isConnected()) {
			client.send(message);
		}
	}
	private void parseMessage(final String msg) {

		try {
			JSONObject jObj = new JSONObject(msg);
			String flag = jObj.getString("flag");
			if (flag.equalsIgnoreCase(TAG_SELF)) {
				String sessionId = jObj.getString("sessionId");
				utils.storeSessionId(sessionId);

			} else if (flag.equalsIgnoreCase(TAG_NEW)) {
				String name = jObj.getString("name");
				String message = jObj.getString("message");
				String onlineCount = jObj.getString("onlineCount");
				showToast(name + message + ". Currently " + onlineCount
						+ " people online!");

			} else if (flag.equalsIgnoreCase(TAG_MESSAGE)) {
				String fromName = name;
				String message = jObj.getString("message");
				String sessionId = jObj.getString("sessionId");
				boolean isSelf = true;
				if (!sessionId.equals(utils.getSessionId())) {
					fromName = jObj.getString("name");
					isSelf = false;
				}
				Message m = new Message(fromName, message, isSelf);
				appendMessage(m);

			} else if (flag.equalsIgnoreCase(TAG_EXIT)) {
				String name = jObj.getString("name");
				String message = jObj.getString("message");
				showToast(name + message);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(client != null & client.isConnected()){
			client.disconnect();
		}
	}

	private void appendMessage(final Message m) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				listMessages.add(m);

				adapter.notifyDataSetChanged();
				playBeep();
			}
		});
	}

	private void showToast(final String message) {

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), message,
						Toast.LENGTH_LONG).show();
			}
		});

	}
	public void playBeep() {

		try {
			Uri notification = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
					notification);
			r.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
