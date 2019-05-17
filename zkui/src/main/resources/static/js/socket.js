layui.use([ 'jquery', 'form', 'eleTree', 'layer' ], function() {
	var $ = layui.jquery;
	var form = layui.form, layer = layui.layer, eleTree = layui.eleTree;

	var index, el;

	var method_name_getNodes = 'getNodes';
	var method_name_close = 'close';
	var method_name_getNodesData = 'getNodesData';
	var method_name_updateNodesData = 'updateNodesData';
	var method_name_addNodes = 'addNodes';
	var method_name_delNodes = 'delNodes';
	var method_name_export = 'export';
	var method_name_import = 'import';
	var method_name_create = "NodeCreated";
	var method_name_delete = "NodeDeleted";

	/**
	 * socket
	 */
	var socket;

	/**
	 * socket link
	 */
	window.webSocket = function(url, path) {
		index = layer.load(2);

		if (typeof (WebSocket) == "undefined") {
			console.log('您的浏览器不支持WebSocket');
		} else {
			if (undefined == socket) {
				socket = new WebSocket((url).replace("http", "ws"));
			} else {
				socket_getNodes(path);
			}

			/**
			 * socket on
			 */
			socket.onopen = function() {
				el = eleTree.render({
					elem : '.zoopeekerNode',
					data : [],
					emptText : ''
				});

				socket_getNodes(path);
			};

			/**
			 * socket off
			 */
			socket.onclose = function() {
				layer.close(index);
			};

			/**
			 * socket error
			 */
			socket.onerror = function() {
				layer.close(index);
				layer.msg('失败，请检查zookeeper是否正常启动');
			}

			/**
			 * socket msg
			 */
			socket.onmessage = function(msg) {
				console.log(msg.data);
				var msgData = JSON.parse(msg.data);
				var data = msgData.data;
				var type = msgData.type;

				switch (type) {
				case method_name_getNodes:
					el.reload(null);

					el = eleTree.render({
						elem : '.zoopeekerNode',
						data : data,
						expandOnClickNode : true,
						highlightCurrent : true,
						showCheckbox : true,
						emptText : '',
						contextmenuList : [ 'addNode', 'delNode', 'impNode',
								'expNode' ]
					});
					break;
				case method_name_getNodesData:
					var id = msgData.id;
					form.val('nodeInfo', {
						'id' : id,
						'val' : data,
						'label' : id
					});
					break;
				case method_name_export:
					var nodes = msgData.nodes;
					var layerOpenIndex = getLayerOpenIndex();
					var iframe = window['layui-layer-iframe' + layerOpenIndex];
					iframe.serFromVal(data, nodes);
					break;
				case method_name_create:
					el.append(data, {
						'id' : msgData.id,
						'label' : msgData.label,
					});
					break;
				case method_name_delete:
					el.remove(data);
					break;
				}

				layer.close(index);
			}
		}
	};

	window.socket_send = function(msg) {
		socket.send(msg);
	}

	window.socket_close = function() {
		var param = {
			'type' : method_name_close
		}
		socket_send(JSON.stringify(param));
	}

	window.socket_getNodesData = function(path) {
		var param = {
			'type' : method_name_getNodesData,
			'data' : path
		}
		socket_send(JSON.stringify(param));
	}

	window.socket_updateNodesData = function(path, val) {
		var param = {
			'type' : method_name_updateNodesData,
			'data' : path,
			'val' : val
		}
		socket_send(JSON.stringify(param));
	}

	window.socket_addNodes = function(path, id, val) {
		var param = {
			'type' : method_name_addNodes,
			'data' : path,
			'id' : id,
			'val' : val
		}
		socket_send(JSON.stringify(param));
	}

	window.socket_delNodes = function(path) {
		var param = {
			'type' : method_name_delNodes,
			'data' : path
		}
		socket_send(JSON.stringify(param));
	}

	window.socket_getNodes = function(path) {
		var param = {
			'type' : method_name_getNodes,
			'data' : path
		}
		socket_send(JSON.stringify(param));
	};

	window.socket_export = function(path) {
		var param = {
			'type' : method_name_export,
			'data' : path
		}
		socket_send(JSON.stringify(param));
	};

	window.socket_import = function(path, data) {
		var param = {
			'type' : method_name_import,
			'data' : path,
			'node' : data
		}
		socket_send(JSON.stringify(param));
	};

});