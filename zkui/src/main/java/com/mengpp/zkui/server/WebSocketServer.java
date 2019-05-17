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
				this.logger.info("socket on {}", this.key);
			} else {
				session.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void onClose() {
		try {
			this.logger.info("socket off {}", this.key);
			ZkConstants.webSocketSet.remove(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnMessage
	public void onMessage(Session session, String message) {
		try {
			logger.info("onMessage|onMessage:{}", message);
			JSONObject json = JSONObject.parseObject(message);

			String type = json.getString("type");
			String path = json.getString("data");

			List<NodeBean> children;
			String nodeData;
			String val;
			String id;

			switch (type) {
			case ZkConstants.ZOOKEEPER_GET_NODES:
				children = this.zookeeperServer.getChildren(path);

				json.put("data", children);
				this.sendMessage(json.toJSONString());
				break;
			case ZkConstants.ZOOKEEPER_CLOST:
				session.close();
				break;
			case ZkConstants.ZOOKEEPER_GET_NODES_DATA:
				nodeData = this.zookeeperServer.getNodeData(path);
				json.put("id", path);
				json.put("data", nodeData);
				this.sendMessage(json.toJSONString());
				break;
			case ZkConstants.ZOOKEEPER_UPDATE_NODES_DATA:
				val = json.getString("val");
				this.zookeeperServer.updNodeData(path, val);

				nodeData = this.zookeeperServer.getNodeData(path);
				json.put("id", path);
				json.put("data", nodeData);
				json.put("type", ZkConstants.ZOOKEEPER_GET_NODES_DATA);
				this.sendMessage(json.toJSONString());
				break;
			case ZkConstants.ZOOKEEPER_ADD_NODES:
				id = json.getString("id");
				val = json.getString("val");
				this.zookeeperServer.addNode(path, id, val);
				break;
			case ZkConstants.ZOOKEEPER_DEL_NODES:
				this.zookeeperServer.delNode(path);
				break;
			case ZkConstants.ZOOKEEPER_EXPORT:
				children = this.zookeeperServer.getChildren(path);
				List<Map<String, Object>> nodes = this.zookeeperServer.exportNodeToJson(children);
				json.put("nodes", nodes);
				this.sendMessage(json.toJSONString());
				break;
			case ZkConstants.ZOOKEEPER_IMPORT:
				String node = json.getString("node");
				this.zookeeperServer.importNode(path, JSONArray.parseArray(node));
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(String message) {
		try {
			this.session.getBasicRemote().sendText(message);
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
