package com.mengpp.zkui.bean;

/**
 * 返回参数对象
 * 
 * @author mengpp
 * @date 2019年6月11日09:11:49
 */
public class DataBean {

	/**
	 * 类型
	 */
	private String type;

	/**
	 * 地址
	 */
	private String path;

	/**
	 * 数据
	 */
	private Object data;

	/**
	 * id
	 */
	private String id;

	/**
	 * 标签
	 */
	private String label;

	/**
	 * 加密密码
	 */
	private String jasypt;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

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

	public String getJasypt() {
		return jasypt;
	}

	public void setJasypt(String jasypt) {
		this.jasypt = jasypt;
	}

}
