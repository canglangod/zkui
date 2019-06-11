package com.mengpp.zkui.server;

import java.util.List;
import java.util.UUID;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.mengpp.zkui.bean.DataBean;
import com.mengpp.zkui.bean.NodeBean;
import com.mengpp.zkui.constants.ZkConstants;

/**
 * WebSocketServer websocket控制器
 * 
 * @author mengpp
 * @date 2019年6月10日16:57:08
 */
@Component
@ServerEndpoint("/websocket/{zkurl}")
public class WebSocketServer {

	/**
	 * 唯一主键
	 */
	public String key;

	/**
	 * 连接信息
	 */
	public Session session;

	/**
	 * zookepper控制器
	 */
	public ZookeeperServer zookeeperServer;

	/**
	 * socket建立连接
	 * 
	 * @param session 连接信息
	 * @param zkurl   连接地址
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("zkurl") String zkurl) {
		try {
			this.session = session;
			this.zookeeperServer = new ZookeeperServer().inital(zkurl);
			this.key = DigestUtils.md5DigestAsHex((zkurl + UUID.randomUUID().toString()).getBytes());

			if (null != this.zookeeperServer) {
				ZkConstants.webSocketSet.add(this);
				DataBean dataBean = new DataBean();
				dataBean.setType(ZkConstants.ZOOKEEPER_INITAL);

				this.sendMessage(dataBean);
			} else {
				session.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭链接
	 */
	@OnClose
	public void onClose() {
		try {
			ZkConstants.webSocketSet.remove(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 消息
	 * 
	 * @param session 连接信息
	 * @param message 消息内容
	 */
	@OnMessage
	public void onMessage(Session session, String message) {
		try {
			DataBean dataBean = JSON.toJavaObject(JSON.parseObject(message), DataBean.class);
			String path = dataBean.getPath();
			String type = dataBean.getType();
			String data = String.valueOf(dataBean.getData());
			switch (type) {
			// 关闭链接
			case ZkConstants.ZOOKEEPER_CLOST:
				session.close();
				return;
			// 获取全部节点
			case ZkConstants.ZOOKEEPER_GETNODES:
				dataBean.setData(this.zookeeperServer.getChildren(path));
				break;
			// 获取节点值
			case ZkConstants.ZOOKEEPER_GETNODESDATA:
				dataBean.setData(this.zookeeperServer.getNodeData(path));
				break;
			// 修改节点值
			case ZkConstants.ZOOKEEPER_UPDATENODESDATA:
				this.zookeeperServer.updNodeData(path, data);
				dataBean.setData(this.zookeeperServer.getNodeData(path));
				break;
			// 添加节点
			case ZkConstants.ZOOKEEPER_ADDNODES:
				this.zookeeperServer.addNode(path, data);
				return;
			// 删除节点
			case ZkConstants.ZOOKEEPER_DELNODES:
				this.zookeeperServer.delNode(path);
				return;
			// 导出
			case ZkConstants.ZOOKEEPER_EXPORT:
				List<NodeBean> children = this.zookeeperServer.getChildren(path);
				dataBean.setData(this.zookeeperServer.exportNodeToJson(children));
				break;
			// 导入
			case ZkConstants.ZOOKEEPER_IMPORT:
				this.zookeeperServer.importNode(path, JSONArray.parseArray(data));
				return;
			}
			this.sendMessage(dataBean);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送消息
	 * 
	 * @param message 消息内容
	 */
	public void sendMessage(Object message) {
		try {
			this.session.getBasicRemote().sendText(JSON.toJSONString(message));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
