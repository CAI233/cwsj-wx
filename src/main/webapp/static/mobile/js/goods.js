/**
 * Created by Administrator on 2018/4/9 0009.
 */
$(document).ready(function () {
    // var li = document.getElementsByTagName("li");
    // console.log($(".tabs .active li"))
    // for(var i=0;i<$(".tabs .active li").length;i++){
    //     var now_this = i;
    //     $(".tabs .active li").eq(now_this).on("touchstart",function(event){
    //         console.log(event);
    //     })
    //     // li[now_this].on("touchmove",function(event){
    //     //     console.log(event);
    //     // })
    // }

})
function on_touch(id){
    
    // window.open("/classify/goods/detail?id="+type,"_self")
    window.open("/goodsInfo?goods_id="+id,"_self")
    // window
}
function on_touchW(id){
    window.open("/worksInfo?works_id="+id,"_self")
}