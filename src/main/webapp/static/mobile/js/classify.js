/**
 * Created by Administrator on 2018/4/9 0009.
 */
var data = {
    searchText:null
}
$(document).ready(function () {
    var swiper = new Swiper('.swiper-container', {
        // spaceBetween: 30,
        centeredSlides: true,
        autoplay: {
            delay: 3000,
            disableOnInteraction: false,
        },
        loop : true,
        pagination: {
            el: '.swiper-pagination',
            clickable: true,
        },
    });
    $("#search").on("click",function(){
        window.open("/indexSearch","_self")
    })
})
function goTo(id){
    window.open("/classify/goods?id="+id,"_self")
}
function goWork(id){
    window.open("/classify/works?id="+id,"_self")
}

