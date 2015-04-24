package kz.kbtu.rooms;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;

@ServerEndpoint("/chat")
public class SocketServer {

	private static final Set<Session> sessionsFIT = Collections
	    .synchronizedSet(new HashSet<Session>());
	private static final Set<Session> sessionsFOGI = Collections
	    .synchronizedSet(new HashSet<Session>());
	private static final Set<Session> sessionsBS = Collections
	    .synchronizedSet(new HashSet<Session>());
	private static final Set<Session> sessionsISE = Collections
	    .synchronizedSet(new HashSet<Session>());
	private static final HashMap<String, String> nameSessionPair = new HashMap<String, String>();
	private static final String TAG_FIT = "FIT";
	private static final String TAG_FOGI = "FOGI";
	private static final String TAG_BS = "BS";
	private static final String TAG_ISE = "ISE";
	private JSONUtils jsonUtils = new JSONUtils();

	public static Map<String, String> getQueryMap(String query) {
		Map<String, String> map = Maps.newHashMap();
		if (query != null) {
		    String[] params = query.split("&");
		    for (String param : params) {
			String[] nameval = param.split("=");
			map.put(nameval[0], nameval[1]);
		    }
		}
		return map;
	}

	@OnOpen
	public void onOpen(Session session) {

		System.out.println(session.getId() + " has opened a connection");

		Map<String, String> queryParams = getQueryMap(session.getQueryString());

		String name = "";
		String room = "";
		Set<Session> sessions = null;
		if (queryParams.containsKey("name")) {
		    name = queryParams.get("name");
		    room = queryParams.get("room");
		    nameSessionPair.put(session.getId(), name);
		    if (room==null)
		    	return;
		    if (room.equals(TAG_FIT)){
			sessions = sessionsFIT;
		    }else if (room.equals(TAG_FOGI)){
			sessions = sessionsFOGI;
		    }else if (room.equals(TAG_BS)){
			sessions = sessionsBS;
		    }else if (room.equals(TAG_ISE)){
			sessions = sessionsISE;
		    }

		}
		sessions.add(session);

		try {
		    session.getBasicRemote().sendText(
			    jsonUtils.getClientDetailsJson(session.getId(),
				    "Your session details"));
		} catch (IOException e) {
		    e.printStackTrace();
		}

		sendMessageToAll(sessions, session.getId(), name, " joined conversation!", true,
			false);
		}
		@OnMessage
		public void onMessage(String message, Session session) {

		System.out.println("Message from " + session.getId() + ": " + message);

		String msg = null;
		Set<Session> sessions = null;
		try {
		    JSONObject jObj = new JSONObject(message);
		    msg = jObj.getString("message");
		} catch (JSONException e) {
		    e.printStackTrace();
		}
		if (sessionsFIT.contains(session))
		    sessions = sessionsFIT;
		else if (sessionsFOGI.contains(session))
		    sessions = sessionsFOGI;
		else if (sessionsBS.contains(session))
		    sessions = sessionsBS;
		else if (sessionsISE.contains(session))
		    sessions = sessionsISE;
		else
			return;
		sendMessageToAll(sessions,session.getId(), nameSessionPair.get(session.getId()),
			msg, false, false);
	}

	@OnClose
	public void onClose(Session session) {

		System.out.println("Session " + session.getId() + " has ended");

		String name = nameSessionPair.get(session.getId());

		Set<Session> sessions = null;
		if (sessionsFIT.contains(session))
		    sessions = sessionsFIT;
		else if (sessionsFOGI.contains(session))
		    sessions = sessionsFOGI;
		else if (sessionsBS.contains(session))
		    sessions = sessionsBS;
		else if (sessionsISE.contains(session))
		    sessions = sessionsISE;
		else
			return;
		sessions.remove(session);

		sendMessageToAll(sessions,session.getId(), name, " left conversation!", false,
			true);

	}

	private void sendMessageToAll(Set<Session> sessions, String sessionId, String name,
		                  String message, boolean isNewClient, boolean isExit) {
		for (Session s : sessions) {
		    String json = null;
		    if (isNewClient) {
			json = jsonUtils.getNewClientJson(sessionId, name, message,
				sessions.size());

		    } else if (isExit) {
			json = jsonUtils.getClientExitJson(sessionId, name, message,
				sessions.size());
		    } else {
			json = jsonUtils
				.getSendAllMessageJson(sessionId, name, message);
		    }

		    try {
			System.out.println("Sending Message To: " + sessionId + ", "
				+ json);

			s.getBasicRemote().sendText(json);
		    } catch (IOException e) {
			System.out.println("error in sending. " + s.getId() + ", "
				+ e.getMessage());
			e.printStackTrace();
		    }
		}
	}
}
