layui.extend({
	eleTree : '/zkui/static/layui/lay/mymodules/eleTree'
});

layui.use([ 'jquery', 'form', 'eleTree', 'layer' ], function() {
	var $ = layui.jquery;
	var form = layui.form;
	var layer = layui.layer;
	var eleTree = layui.eleTree;

	form.on('submit(login)', function(data) {
		var url = window.document.location.href;
		var zkurl = data.field.zkurl;
		var path = data.field.path;

		url = url + "websocket/" + zkurl;

		webSocket(url, path);
		return false;
	});

	form.on('submit(close)', function(data) {
		socket_close();
		return false;
	});

	eleTree.on("nodeClick(zoopeekerNode)", function(d) {
		if (d.data.currentData.children == null) {
			socket_getNodesData(d.data.currentData.id);
		}
	});

	form.on('submit(updateVal)', function(data) {
		var id = data.field.id;
		var val = data.field.val;
		socket_updateNodesData(id, val);
		return false;
	});

});