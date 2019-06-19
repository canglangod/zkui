package com.mengpp.zkui.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mengpp.zkui.bean.NodeBean;
import com.mengpp.zkui.constants.ZkConstants;
import com.mengpp.zkui.utils.MyWatcher;
import com.mengpp.zkui.utils.SecretkeyGenerationUtil;

/**
 * ZookeeperServer zookeeper控制器
 * 
 * @author mengpp
 * @date 2019年6月10日16:47:02
 */
public class ZookeeperServer {

	public String zkurl;

	public ZooKeeper zookeeper;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * ZookeeperServer 建立连接
	 * 
	 * @param zkurl 连接地址
	 */
	public ZookeeperServer inital(String zkurl) {
		logger.info("zookepper [open] {}", zkurl);
		ZookeeperServer zookeeperServer = null;
		try {
			zookeeperServer = this.getZookeeperServer(zkurl);
			if (null == zookeeperServer) {
				ZooKeeper zookeeper = new ZooKeeper(zkurl, 3000, new MyWatcher(this.zkurl));

				// 连接超时检测
				Integer failureStatistics = 0;
				while (true) {
					long sessionId = zookeeper.getSessionId();
					if (sessionId > 0) {
						this.zkurl = zkurl;
						this.zookeeper = zookeeper;

						ZkConstants.zookeeperSet.add(this);
						zookeeperServer = this;
						break;
					} else {
						++failureStatistics;
						if (failureStatistics > 3) {
							this.zookeeper = null;
							zookeeper.close();
							break;
						}
					}
					Thread.sleep(1000);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return zookeeperServer;
	}

	/**
	 * 关闭连接
	 */
	public void close() {
		logger.info("zookepper【关闭】连接：{}", zkurl);
		try {
			this.zookeeper.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取全部节点
	 * 
	 * @param path 节点
	 * @return
	 */
	public List<NodeBean> getChildren(String path) {
		List<NodeBean> nodes = null;
		try {
			path = this.checkPath(path);

			nodes = new ArrayList<>(1);
			NodeBean node = new NodeBean();
			node.setId(path);
			node.setLabel(path);
			nodes.add(node);

			nodes = this.getAllNodes(nodes, this.zookeeper);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nodes;
	}

	/**
	 * 获取节点值
	 * 
	 * @param path 节点
	 * @return
	 */
	public String getNodeData(String path) {
		try {
			byte[] data = this.zookeeper.getData(path, false, null);
			if (null != data) {
				return new String(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 修改节点值
	 * 
	 * @param path   节点
	 * @param data   值
	 * @param jasypt 密码
	 * @return
	 */
	public Boolean updNodeData(String path, String data, String jasypt) {
		try {
			if (!StringUtils.isEmpty(jasypt)) {
				data = SecretkeyGenerationUtil.encryption(data, jasypt);
			}
			this.zookeeper.setData(path, data.getBytes(), -1);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 添加节点
	 * 
	 * @param path 节点
	 * @param data 值
	 */
	public void addNode(String path, String data) {
		try {
			path = path.replace("//", "/");
			this.zookeeper.exists(path, new MyWatcher(this.zkurl));
			this.zookeeper.create(path, data.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除节点
	 * 
	 * @param path 节点
	 */
	public void delNode(String path) {
		try {
			List<NodeBean> nodeBeanList = getChildren(path);
			if (nodeBeanList.size() == 0) {
				NodeBean nodeBean = new NodeBean();
				nodeBean.setId(path);
				nodeBeanList.add(nodeBean);
			}
			this.zookeeper.exists(path, new MyWatcher(this.zkurl));
			this.delAllNode(this.zookeeper, nodeBeanList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 导出全部节点
	 * 
	 * @param nodeBeanList 节点
	 * @return
	 */
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

	/**
	 * 导入节点
	 * 
	 * @param path 节点
	 * @param data 值
	 * @return
	 */
	public Boolean importNode(final String path, JSONArray data) {
		try {
			for (Object obj : data) {
				JSONObject jsonObj = JSONObject.parseObject(obj.toString());
				String id = jsonObj.getString("id");
				String val = jsonObj.getString("data");

				this.addNode(path + "/" + id, val);

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

	/**
	 * 连接检测，检测当前地址是否存在连接
	 * 
	 * @param zkurl 连接地址
	 * @return
	 */
	private ZookeeperServer getZookeeperServer(String zkurl) {
		ZookeeperServer zookeeperServer = null;
		Iterator<ZookeeperServer> iterator = ZkConstants.zookeeperSet.iterator();
		while (iterator.hasNext()) {
			zookeeperServer = iterator.next();
			if (zkurl.equals(zookeeperServer.zkurl)) {
				break;
			} else {
				zookeeperServer = null;
			}
		}
		return zookeeperServer;
	}

	/**
	 * 校验地址
	 * 
	 * @param path 路径
	 * @return
	 */
	private String checkPath(String path) {
		if (StringUtils.isEmpty(path)) {
			return "/";
		} else {
			return path;
		}
	}

	/**
	 * 获取全部节点
	 * 
	 * @param nodes     节点
	 * @param zookeeper zookepper
	 * @return
	 */
	private List<NodeBean> getAllNodes(List<NodeBean> nodes, ZooKeeper zookeeper) {
		List<NodeBean> returnNodes = new ArrayList<>(nodes.size());
		try {
			for (NodeBean node : nodes) {
				String id = node.getId();

				List<String> childrens = zookeeper.getChildren(id, false);

				// 组装节点数据
				List<NodeBean> nodes2 = new ArrayList<>(nodes.size());
				for (String children : childrens) {
					NodeBean node2 = new NodeBean();
					node2.setId(id.equals("/") ? id + children : id + "/" + children);
					node2.setLabel(children);
					nodes2.add(node2);
				}

				// 递归
				if (nodes2.size() > 0) {
					node.setChildren(nodes2);
					returnNodes.add(node);
					getAllNodes(nodes2, zookeeper);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnNodes;
	}

	/**
	 * 递归删除节点
	 * 
	 * @param zookeeper    zookepper
	 * @param nodeBeanList 节点集合
	 */
	private void delAllNode(ZooKeeper zookeeper, List<NodeBean> nodeBeanList) {
		try {
			for (NodeBean nodeBean : nodeBeanList) {
				String id = nodeBean.getId();
				List<NodeBean> children = nodeBean.getChildren();
				if (null != children) {
					delAllNode(zookeeper, children);
				}
				zookeeper.delete(id, -1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
