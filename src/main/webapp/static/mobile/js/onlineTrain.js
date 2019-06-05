/**
 * Created by Administrator on 2018/4/21 0021.
 */
$(document).ready(function () {
    var swiper = new Swiper('.swiper-container', {
        slidesPerView: 4,
        pagination: {
            el: '.swiper-pagination',
            clickable: true,
        },
    });
})
function goDeTail(id){
    window.open('/video/details?video_id='+id,"_self");
}