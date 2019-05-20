package com.mengpp.zkui.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.ZooKeeper;
import org.springframework.util.StringUtils;

import com.mengpp.zkui.bean.NodeBean;

public class ZkUtil {

	/**
	 * checkPath 校验地址
	 * 
	 * @param path 路径
	 * @return
	 */
	public static String checkPath(String path) {
		if (StringUtils.isEmpty(path)) {
			return "/";
		} else {
			return path;
		}
	}

	/**
	 * getAllNodes 通过路径获取全部节点
	 * 
	 * @param nodes     节点
	 * @param zookeeper
	 * @return
	 */
	public static List<NodeBean> getAllNodes(List<NodeBean> nodes, ZooKeeper zookeeper) {
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
	 * delAllNode 删除节点
	 * 
	 * @param zookeeper
	 * @param nodeBeanList 节点
	 */
	public static void delAllNode(ZooKeeper zookeeper, List<NodeBean> nodeBeanList) {
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
