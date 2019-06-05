/**
 * Created by Administrator on 2018/4/21 0021.
 */
$(document).ready(function () {

})

function info(id){
    window.open('/resourceInfo?id='+id,'_self');
}
function downList(e){
    var e = e || window.event;
    if($("#downList").css("-webkit-line-clamp")!='unset'){
        $("#downList").css("-webkit-line-clamp",'unset');
        $(e.currentTarget).addClass("icon-down").removeClass("icon-up");
    }else{
        $("#downList").css("-webkit-line-clamp",'2');
        $(e.currentTarget).addClass("icon-up").removeClass("icon-down");
    }
}