package com.mengpp.zkui.constants;

import java.util.concurrent.CopyOnWriteArraySet;

import com.mengpp.zkui.server.WebSocketServer;
import com.mengpp.zkui.server.ZookeeperServer;

public class ZkConstants {

	public static CopyOnWriteArraySet<ZookeeperServer> zookeeperSet = new CopyOnWriteArraySet<ZookeeperServer>();

	public static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();

	public static final String ZOOKEEPER_GET_NODES = "getNodes";

	public static final String ZOOKEEPER_CLOST = "close";

	public static final String ZOOKEEPER_GET_NODES_DATA = "getNodesData";

	public static final String ZOOKEEPER_UPDATE_NODES_DATA = "updateNodesData";

	public static final String ZOOKEEPER_ADD_NODES = "addNodes";

	public static final String ZOOKEEPER_DEL_NODES = "delNodes";

	public static final String ZOOKEEPER_EXPORT = "export";

	public static final String ZOOKEEPER_IMPORT = "import";
}
