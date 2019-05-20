package com.mengpp.zkui.bean;

import java.util.List;

public class NodeBean {

	/**
	 * ID
	 */
	public String id;

	/**
	 * 名称
	 */
	public String label;

	/**
	 * 子节点
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
