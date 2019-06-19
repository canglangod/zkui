/**
 * 首页控制器
 * 
 * @author mengpp
 * @date 2019年5月17日11:57:32
 */
layui.use([ 'jquery', 'form', 'layer' ], function() {
	var $ = layui.jquery;
	var form = layui.form;
	var layer = layui.layer;

	/**
	 * 登录
	 */
	form.on('submit(login)', function(data) {
		var url = window.document.location.href;
		var path = data.field.path;
		var zkurl = data.field.zkurl;

		url = url + "websocket/" + zkurl;
		socket_link(url, path);
		return false;
	});

	/**
	 * 退出
	 */
	form.on('submit(close)', function(data) {
		socket_close();
		return false;
	});

	/**
	 * 修改节点值
	 */
	form.on('submit(updateVal)', function(data) {
		var id = data.field.id;
		var val = data.field.val;
		var jasypt = data.field.jasypt;
		socket_updateNodesData(id, val, jasypt);
		return false;
	});

	/**
	 * 单选框监控
	 */
	form.on('radio(passtype)', function(data) {
		var val = data.value;
		if ('jasypt' == val) {
			$("#passId").removeClass("layui-hide");
		} else {
			form.val('nodeInfo', {
				"jasypt" : null
			})
			$("#passId").addClass("layui-hide");
		}
	});

});