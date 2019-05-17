package com.mengpp.zkui.server;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mengpp.zkui.bean.NodeBean;
import com.mengpp.zkui.constants.ZkConstants;

@Component
@ServerEndpoint("/websocket/{zkurl}")
public class WebSocketServer {

	public String key;

	public Session session;

	public ZookeeperServer zookeeperServer;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public void sendMessage(String message) {
		try {
			this.session.getBasicRemote().sendText(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnOpen
	public void onOpen(Session session, @PathParam("zkurl") String zkurl) {
		try {
			String key = DigestUtils.md5DigestAsHex((zkurl + UUID.randomUUID().toString()).getBytes());

			this.key = key;
			this.session = session;

			this.zookeeperServer = this.getZookeeperServer(zkurl);
			if (null == this.zookeeperServer) {
				this.zookeeperServer = new ZookeeperServer(zkurl);
			}

			if (null != this.zookeeperServer.zookeeper) {
				ZkConstants.webSocketSet.add(this);
				JSONObject json = new JSONObject();
				json.put("type", "Initialization complete");
				this.sendMessage(json.toJSONString());
				this.logger.info("socket on {}", this.key);
			} else {
				this.msgClose();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void onClose() {
		try {
			ZkConstants.webSocketSet.remove(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnMessage
	public void onMessage(Session session, String message) {
		logger.info("onMessage|onMessage:{}", message);
		try {
			JSONObject json = JSONObject.parseObject(message);
			String type = json.getString("type");

			switch (type) {
			case ZkConstants.ZOOKEEPER_CLOST:
				this.msgClose();
				break;
			case ZkConstants.ZOOKEEPER_GET_NODES:
				this.msgGetNodes(json);
				break;
			case ZkConstants.ZOOKEEPER_GET_NODES_DATA:
				this.msgGetNodesData(json);
				break;
			case ZkConstants.ZOOKEEPER_UPDATE_NODES_DATA:
				this.msgUpdateNodesData(json);
				break;
			case ZkConstants.ZOOKEEPER_ADD_NODES:
				this.msgAddNodes(json);
				break;
			case ZkConstants.ZOOKEEPER_DEL_NODES:
				this.msgDelNodes(json);
				break;
			case ZkConstants.ZOOKEEPER_EXPORT:
				this.msgExport(json);
				break;
			case ZkConstants.ZOOKEEPER_IMPORT:
				this.msgImport(json);
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
			this.msgClose();
		}
	}

	private void msgImport(JSONObject json) {
		try {
			String path = json.getString("path");
			String data = json.getString("data");
			this.zookeeperServer.importNode(path, JSONArray.parseArray(data));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void msgExport(JSONObject json) {
		try {
			String path = json.getString("path");
			List<NodeBean> children = this.zookeeperServer.getChildren(path);
			List<Map<String, Object>> nodes = this.zookeeperServer.exportNodeToJson(children);
			json.put("data", nodes);
			this.sendMessage(json.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void msgDelNodes(JSONObject json) {
		try {
			String path = json.getString("path");
			this.zookeeperServer.delNode(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void msgAddNodes(JSONObject json) {
		try {
			String id = json.getString("id");
			String path = json.getString("path");
			String data = json.getString("data");
			this.zookeeperServer.addNode(path, id, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void msgUpdateNodesData(JSONObject json) {
		try {
			String path = json.getString("path");
			String data = json.getString("data");
			this.zookeeperServer.updNodeData(path, data);
			json.put("data", this.zookeeperServer.getNodeData(path));
			this.sendMessage(json.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void msgGetNodesData(JSONObject json) {
		try {
			String path = json.getString("path");
			json.put("data", this.zookeeperServer.getNodeData(path));
			this.sendMessage(json.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void msgClose() {
		try {
			session.close();
			this.logger.info("socket off {}", this.key);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void msgGetNodes(JSONObject json) {
		try {
			String path = json.getString("path");
			json.put("data", this.zookeeperServer.getChildren(path));
			this.sendMessage(json.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ZookeeperServer getZookeeperServer(String zkurl) {
		ZookeeperServer zookeeperServer = null;
		Iterator<ZookeeperServer> iterator = ZkConstants.zookeeperSet.iterator();
		while (iterator.hasNext()) {
			zookeeperServer = iterator.next();
			if (zkurl.equals(zookeeperServer.zkurl)) {
				break;
			}
		}
		return zookeeperServer;
	}

}
