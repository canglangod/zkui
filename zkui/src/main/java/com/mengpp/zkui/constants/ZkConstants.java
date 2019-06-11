package com.mengpp.zkui.constants;

import java.util.concurrent.CopyOnWriteArraySet;

import com.mengpp.zkui.server.WebSocketServer;
import com.mengpp.zkui.server.ZookeeperServer;

public class ZkConstants {

	public static CopyOnWriteArraySet<ZookeeperServer> zookeeperSet = new CopyOnWriteArraySet<ZookeeperServer>();

	public static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();

	public static final String ZOOKEEPER_GETNODES = "getNodes";

	public static final String ZOOKEEPER_CLOST = "close";

	public static final String ZOOKEEPER_GETNODESDATA = "getNodesData";

	public static final String ZOOKEEPER_UPDATENODESDATA = "updateNodesData";

	public static final String ZOOKEEPER_ADDNODES = "addNodes";

	public static final String ZOOKEEPER_DELNODES = "delNodes";

	public static final String ZOOKEEPER_EXPORT = "export";

	public static final String ZOOKEEPER_IMPORT = "import";

	public static final String ZOOKEEPER_INITAL = "Initialization complete";
}
