/**
 * Created by Administrator on 2018/4/16 0016.
 */
var nowNums = 0;
var nowImg = [];
var nowHtml = '';
$(document).ready(function () {
    // span的点击事件
    $("._cont_upload .upload").click(function (event) {
        var e = event || window.event;
        nowNums = $(e.target).index();
        console.log(nowNums)

        var u = navigator.userAgent;
        // var isAndroid = u.indexOf('Android') > -1 || u.indexOf('Adr') > -1; //android终端
        var isiOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/); //ios终端

        if(isiOS){
            $("#up").click().click();
        }else{
            $("#up").click()
        }
    });

    // var _JQ =$.noConflict();

    $('#up').prettyFile({
        text: "上传头像",
        change: function (res, obj) {
            var nowUrl = res.data[0].url;
            nowImg[nowNums] = nowUrl;
            // $(".upload").eq(nowNums).css({'background':'url('+ctxPath + nowUrl+') no-repeat center '});
            $(".upload").eq(nowNums).empty().append('<img src=' + ctxPath + nowUrl + ' style="width:100%;height:100%;"/>')
            $(".upload").eq(nowNums).find("i").css("display", "none");
            $.toast(1)
        }
    });

})

function _QS_submit() {
    var id = parseInt($("#worksId").html());
    var cont = nowHtml;
    console.log(cont)
    if (cont.length == 0) {
        // alert("请填写相关问题");
        $("body").append($('<div class="modal toast modal-in" style="display:block !important;margin-left:-3.75rem;">请填写相关问题</div>'));
        setTimeout(function(){
            $(".toast").hide();
            $("body").find(".toast").remove();
        },1000)
        return false;
    }
    $.ajax({
        type: 'post',
        url: ctxPath + '/api/wx/problem/submit',
        layui: true,
        data: {
            works_id: id,
            problem_content: cont,
            img1: nowImg[0],
            img2: nowImg[1]
        },
        success: (function (res) {
            console.log(res);
            window.open(res.data, "_self");
        })
    })
}

// /多行文本输入框剩余字数计算
function checkMaxInput(obj, maxLen) {
    if (obj == null || obj == undefined || obj == "") {
        return;
    }
    if (maxLen == null || maxLen == undefined || maxLen == "") {
        maxLen = 100;
    }
    var $obj = $(obj);
    if (obj.value.length >= maxLen) { //如果输入的字数超过了限制
        obj.value = obj.value.substring(0, maxLen);
    }
    nowHtml = obj.value;
    $("#nums").html(obj.value.length);
}
function collect(type,id){
    var e = e || window.event;
    var nowData = $(e.currentTarget);
    $.ajax({
        type: 'post',
        url: ctxPath + '/saveCollect',
        data: {id: id, type: type},
        success: function (res) {

            $("body").append($('<div class="modal toast modal-in" style="display:block !important;margin-left:-3.75rem;">'+res.message+'</div>'));
            setTimeout(function(){
                $(".toast").hide();
                $("body").find(".toast").remove();
            },1000)
            if(res.data ==1){
                nowData.addClass("icon-shoucang").removeClass("icon-shoucang1")
            }else{
                nowData.addClass("icon-shoucang1").removeClass("icon-shoucang")
            }
        }
    })
}
