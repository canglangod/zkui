package com.mengpp.zkui.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mengpp.zkui.bean.NodeBean;
import com.mengpp.zkui.constants.ZkConstants;
import com.mengpp.zkui.utils.MyWatcher;
import com.mengpp.zkui.utils.ZkUtil;

public class ZookeeperServer {

	public String zkurl;

	public ZooKeeper zookeeper;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public ZookeeperServer(String zkurl) {
		this.zkurl = zkurl;
		try {
			ZooKeeper zookeeper = new ZooKeeper(zkurl, 3000, new MyWatcher(this.zkurl));

			for (Integer i = 1; i <= 3; i++) {
				Thread.sleep(1000);

				long sessionId = zookeeper.getSessionId();
				if (sessionId > 0) {
					this.zookeeper = zookeeper;
					this.logger.info("zookeeper on {}", this.zkurl);

					ZkConstants.zookeeperSet.add(this);
					break;
				}
				if (i == 3) {
					this.zookeeper = null;
					zookeeper.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			this.zookeeper.close();
			this.logger.info("zookeeper on {}", this.zkurl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<NodeBean> getChildren(String path) {
		List<NodeBean> nodes = null;
		try {
			path = ZkUtil.checkPath(path);

			nodes = new ArrayList<>(1);
			NodeBean node = new NodeBean();
			node.setId(path);
			node.setLabel(path);
			nodes.add(node);

			nodes = ZkUtil.getAllNodes(nodes, this.zookeeper);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nodes;
	}

	public void addNode(String path, String id, String data) {
		try {
			if (!path.equals("/")) {
				id = "/" + id;
			}
			path = path + id;
			this.zookeeper.exists(path, new MyWatcher(this.zkurl));
			this.zookeeper.create(path, data.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void delNode(String path) {
		try {
			List<NodeBean> nodeBeanList = getChildren(path);
			if (nodeBeanList.size() == 0) {
				NodeBean nodeBean = new NodeBean();
				nodeBean.setId(path);
				nodeBeanList.add(nodeBean);
			}
			this.zookeeper.exists(path, new MyWatcher(this.zkurl));
			ZkUtil.delAllNode(this.zookeeper, nodeBeanList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getNodeData(String path) {
		try {
			this.zookeeper.exists(path, new MyWatcher(this.zkurl));
			byte[] data = this.zookeeper.getData(path, false, null);
			if (null != data) {
				return new String(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Boolean updNodeData(String path, String data) {
		try {
			this.zookeeper.exists(path, new MyWatcher(this.zkurl));
			this.zookeeper.setData(path, data.getBytes(), -1);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public List<Map<String, Object>> exportNodeToJson(List<NodeBean> nodeBeanList) {
		List<Map<String, Object>> listMap = new ArrayList<>();
		for (NodeBean bean : nodeBeanList) {
			String id = bean.getId();
			String nodeData = this.getNodeData(id);

			String[] idArray = id.split("/");
			Integer idArrayLength = idArray.length;
			if (idArrayLength == 0) {
				id = "/";
			} else {
				id = idArray[idArrayLength - 1];
			}

			List<Map<String, Object>> listMapChildren = null;
			List<NodeBean> children = bean.getChildren();
			if (null != children) {
				listMapChildren = this.exportNodeToJson(children);
			}

			Map<String, Object> map = new HashMap<>();
			map.put("id", id);
			map.put("data", nodeData);
			map.put("children", listMapChildren);
			listMap.add(map);
		}
		return listMap;
	}

	public Boolean importNode(final String path, JSONArray data) {
		try {
			for (Object obj : data) {
				JSONObject jsonObj = JSONObject.parseObject(obj.toString());
				String id = jsonObj.getString("id");
				String val = jsonObj.getString("data");

				this.addNode(path, id, val);

				JSONArray jsonArray = jsonObj.getJSONArray("children");
				if (null != jsonArray) {
					String pathNew = path.equals("/") ? path : path + "/";
					pathNew = pathNew + id;

					this.importNode(pathNew, jsonArray);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
