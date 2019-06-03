/**
 * socket工具类
 * 
 * @author mengpp
 * @date 2019年5月17日11:57:32
 */
layui.extend({
	eleTree : '/zkui/static/layui/lay/mymodules/eleTree'
});

layui.use([ 'jquery', 'form', 'layer', 'eleTree' ],
		function() {
			/**
			 * layui
			 */
			var $ = layui.jquery;
			var form = layui.form;
			var layer = layui.layer;
			var eleTree = layui.eleTree;

			/**
			 * 变量
			 */
			var index, el, socket;

			/**
			 * socket建立链接
			 */
			window.socket_link = function(url, path) {
				if (typeof (WebSocket) == "undefined") {
					layer.msg('您的浏览器不支持WebSocket');
				} else {
					index = layer.load(2);
					$('#login').addClass('layui-btn-disabled').attr('disabled', "true");
					$('#close').removeClass('layui-btn-disabled').removeAttr('disabled',"true");

					if (undefined == socket) {
						socket = new WebSocket((url).replace("http", "ws"));
					}

					/**
					 * scoket打开后执行
					 */
					socket.onopen = function() {
						el = eleTree.render({
							elem : '.zoopeekerNode',
							data : [],
							emptText : ''
						});
					};

					/**
					 * socket关闭后执行
					 */
					socket.onclose = function() {
						$('#login').removeClass('layui-btn-disabled').removeAttr('disabled',"true");
						$('#close').addClass('layui-btn-disabled').attr('disabled', "true");
						
						layer.msg('scoket 已断开');
						socket = undefined;
						el.reload(null);
						layer.close(index);
					};

					/**
					 * socket接收消息处理
					 */
					socket.onmessage = function(msg) {
						var msgData = JSON.parse(msg.data);
						console.log(msgData);
						var type = msgData.type;

						switch (type) {
						// 初始化完成
						case method_name_init:
							socket_getNodes(path);
							break;
						// 获取全部节点
						case method_name_getNodes:
							msg_getNodes(msgData);
							layer.close(index);
							break;
						// 获取节点值
						case method_name_getNodesData:
							msg_getNodesData(msgData);
							break;
						// 添加节点
						case method_name_create:
							msg_create(msgData);
							break;
						// 删除节点
						case method_name_delete:
							msg_delete(msgData);
							break;
						// 导出
						case method_name_export:
							msg_export(msgData);
							break;
						}
					}
				}
			}

			/**
			 * 获取全部节点消息处理
			 */
			window.msg_getNodes = function(msgData) {
				var data = msgData.data;
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
			}

			/**
			 * 获取节点值消息处理
			 */
			window.msg_getNodesData = function(msgData) {
				var path = msgData.path;
				var data = msgData.data;
				form.val('nodeInfo', {
					'id' : path,
					'val' : data,
					'label' : path
				});
			}

			/**
			 * 添加节点
			 */
			window.msg_create = function(msgData) {
				var id = msgData.id;
				var data = msgData.data;
				var label = msgData.label;
				el.append(data, {
					'id' : id,
					'label' : label
				});
			}

			/**
			 * 导出节点
			 */
			window.msg_export = function(msgData) {
				var data = msgData.data;
				var path = msgData.path;
				var layerOpenIndex = getLayerOpenIndex();
				var iframe = window['layui-layer-iframe' + layerOpenIndex];
				iframe.serFromVal(path, data);

				layer.close(index);
			}

			/**
			 * 删除节点
			 */
			window.msg_delete = function(msgData) {
				var data = msgData.data;
				el.remove(data);
			}

			/**
			 * socket发送消息
			 */
			window.socket_send = function(msg) {
				socket.send(msg);
			}

			/**
			 * socket关闭
			 */
			window.socket_close = function() {
				var param = {
					'type' : method_name_close
				}
				socket_send(JSON.stringify(param));
			}

			/**
			 * socket获取全部节点
			 */
			window.socket_getNodes = function(path) {
				var param = {
					'type' : method_name_getNodes,
					'path' : path
				}
				socket_send(JSON.stringify(param));
			};

			/**
			 * socket获取节点值
			 */
			window.socket_getNodesData = function(path) {
				var param = {
					'type' : method_name_getNodesData,
					'path' : path
				}
				socket_send(JSON.stringify(param));
			}

			/**
			 * socket修改节点
			 */
			window.socket_updateNodesData = function(path, data) {
				var param = {
					'type' : method_name_updateNodesData,
					'path' : path,
					'data' : data
				}
				socket_send(JSON.stringify(param));
			}

			/**
			 * socket添加节点
			 */
			window.socket_addNodes = function(path, id, data) {
				var param = {
					'type' : method_name_addNodes,
					'id' : id,
					'path' : path,
					'data' : data
				}
				socket_send(JSON.stringify(param));
			}

			/**
			 * socket删除节点
			 */
			window.socket_delNodes = function(path) {
				var param = {
					'type' : method_name_delNodes,
					'path' : path
				}
				socket_send(JSON.stringify(param));
			}

			/**
			 * socket导出
			 */
			window.socket_export = function(path) {
				var param = {
					'type' : method_name_export,
					'path' : path
				}
				socket_send(JSON.stringify(param));
				index = layer.load(2);
			};

			/**
			 * socket导入
			 */
			window.socket_import = function(path, data) {
				var param = {
					'type' : method_name_import,
					'path' : path,
					'data' : data
				}
				socket_send(JSON.stringify(param));
			};

			/**
			 * 树形节点点击事件
			 */
			eleTree.on("nodeClick(zoopeekerNode)", function(d) {
				socket_getNodesData(d.data.currentData.id);
			});

		});