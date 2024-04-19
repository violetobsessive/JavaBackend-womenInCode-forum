$(function(){

    // 填完信息点发送button才会触发这个方法
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
    // 发送时把弹出框先关闭，然后显示提示框
	$("#sendModal").modal("hide");

    // 向server发送数据，server返回结果之后再显示提示
    // recipient-name 是dm.html 的发送人id名
	var toName = $("recipient-name").val;
	// 内容
	var content = $("#message-text").val();

    // 发送异步请求
    $.post(
        CONTEXT_PATH + "/dm/send",
         {"toName":toName,"content":content},

         //处理server返回的结果 - 用callback
        	    function(data) {
        	        data = $.parseJSON(data);
        	        if(data.code == 0) {
        	            $("#hintBody").text("Send successfully!");
        	        } else {
        	            $("#hintBody").text(data.msg);
        	        }
                    // 刷新页面
        	        $("#hintModal").modal("show");
                    setTimeout(function(){
                        $("#hintModal").modal("hide");
                        location.reload();
                    }, 2000);
        	    }
    );

	$("#hintModal").modal("show");
	setTimeout(function(){
		$("#hintModal").modal("hide");
	}, 2000);
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}