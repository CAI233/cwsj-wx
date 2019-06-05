/**
 * Created by Administrator on 2018/4/23 0023.
 */

$(document).ready(function () {
    $("#keyword").on('keypress',function(e) {
        var e = e || window.event;
        var keycode = e.keyCode;
        if(keycode=='13') {
            search();
        }
    });
    $("#search").on("focus",function(){
        var _this = this;
        setTimeout(function(){
            _this.scrollIntoView(true);
            _this.scrollIntoViewIfNeeded();
        },200)
    })

})


function onSearch(index){
    var str = encodeURI($(".conter").eq(index).html());
    window.open("/search?str=" + str, "_self");
}
function search(){
    var str = encodeURI($("#search").val());
    window.open("/search?str=" + str, "_self");
}
function onTap(index){
    var str = $(".conter").eq(index).html();
    $("#search").val(str);
}

