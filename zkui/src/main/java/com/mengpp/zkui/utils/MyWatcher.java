package com.mengpp.zkui.utils;

import java.util.Iterator;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.mengpp.zkui.constants.ZkConstants;
import com.mengpp.zkui.server.WebSocketServer;

public class MyWatcher implements Watcher {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String zkurl;

	public MyWatcher(String zkurl) {
		this.zkurl = zkurl;
	}

	@Override
	public void process(WatchedEvent event) {
		KeeperState keeperState = event.getState();
		EventType eventType = event.getType();
		String path = event.getPath();
		logger.info("MyWatcher;path={};keeperState={};eventType={}", path, keeperState, eventType);

		if (keeperState.equals(KeeperState.SyncConnected)) {
			JSONObject json;
			switch (eventType) {
			case NodeCreated:
				int lastIndexOf = path.lastIndexOf("/");
				String substring = path.substring(lastIndexOf + 1);
				String substring2 = path.substring(0, lastIndexOf);

				json = new JSONObject();
				json.put("type", EventType.NodeCreated);
				json.put("data", StringUtils.isEmpty(substring2) ? "/" : substring2);
				json.put("id", path);
				json.put("label", substring);
				this.sendMsg(json.toJSONString());
				break;
			case NodeDeleted:
				json = new JSONObject();
				json.put("type", EventType.NodeDeleted);
				json.put("data", path);
				this.sendMsg(json.toJSONString());
				break;
			default:
				break;
			}
		}
	}

	private void sendMsg(String msg) {
		Iterator<WebSocketServer> iterator = ZkConstants.webSocketSet.iterator();
		while (iterator.hasNext()) {
			WebSocketServer webSocketServer = iterator.next();
			if (this.zkurl.equals(webSocketServer.zookeeperServer.zkurl)) {
				webSocketServer.sendMessage(msg);
			}
		}
	}

}
