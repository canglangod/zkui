package com.mengpp.zkui.utils;

import java.util.Iterator;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.springframework.util.StringUtils;

import com.mengpp.zkui.bean.DataBean;
import com.mengpp.zkui.constants.ZkConstants;
import com.mengpp.zkui.server.WebSocketServer;

/**
 * MyWatcher zookeeper监听器
 * 
 * @author mengpp
 * @date 2019年6月24日09:53:16
 */
public class MyWatcher implements Watcher {

	private String zkurl;

	public MyWatcher(String zkurl) {
		this.zkurl = zkurl;
	}

	@Override
	public void process(WatchedEvent event) {
		KeeperState keeperState = event.getState();
		EventType eventType = event.getType();
		String path = event.getPath();

		if (keeperState.equals(KeeperState.SyncConnected)) {
			DataBean dataBean = new DataBean();
			switch (eventType) {
			case NodeCreated:
				int lastIndexOf = path.lastIndexOf("/");
				String substring = path.substring(lastIndexOf + 1);
				String substring2 = path.substring(0, lastIndexOf);

				dataBean.setId(path);
				dataBean.setType(String.valueOf(EventType.NodeCreated));
				dataBean.setData(StringUtils.isEmpty(substring2) ? "/" : substring2);
				dataBean.setLabel(substring);
				break;
			case NodeDeleted:
				dataBean.setType(String.valueOf(EventType.NodeDeleted));
				dataBean.setData(path);
				break;
			default:
				break;
			}
			this.sendMsg(dataBean);
		}
	}

	private void sendMsg(Object msg) {
		Iterator<WebSocketServer> iterator = ZkConstants.webSocketSet.iterator();
		while (iterator.hasNext()) {
			WebSocketServer webSocketServer = iterator.next();
			if (this.zkurl.equals(webSocketServer.zookeeperServer.zkurl)) {
				webSocketServer.sendMessage(msg);
			}
		}
	}

}
