package com.mengpp.zkui.bean;

import java.util.List;

/**
 * NodeBean 节点对象
 * 
 * @author mengpp
 * @date 2019年6月24日09:45:55
 */
public class NodeBean {

	/**
	 * id id
	 */
	public String id;

	/**
	 * label 名称
	 */
	public String label;

	/**
	 * children 子节点
	 */
	public List<NodeBean> children;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<NodeBean> getChildren() {
		return children;
	}

	public void setChildren(List<NodeBean> children) {
		this.children = children;
	}

}
