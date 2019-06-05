/**
 * Created by Administrator on 2018/4/13 0013.
 */
var works_id = $.GetQueryString("works_id");
console.log(works_id);
$(document).ready(function () {
    // var swiper = new Swiper('.swiper-container', {
    //     // spaceBetween: 30,
    //     centeredSlides: true,
    //     autoplay: {
    //         delay: 1500,
    //         disableOnInteraction: false,
    //     },
    //     loop : true,
    //     pagination: {
    //         el: '.swiper-pagination',
    //         clickable: true,
    //     },
    // });
})
function go_exam(id){
    window.open("/myExam?paper_id="+id,"_self")
}
// 立即购买
_promptlyPay = function (works_id,works_price) {
    // location.href = '/wechatPay?id='+works_id+'&money=1';
    window.open("/wechatPay?id="+works_id+"&money=1","_self");
}
function collect(type,id){
    var e = e || window.event;
    var nowData = $(e.currentTarget);
    $.ajax({
        type: 'post',
        url: ctxPath + '/saveCollect',
        data: {id: id, type: type},
        success: function (res) {
            $.toast(res.message);
            if(res.data ==1){
                nowData.addClass("icon-shoucang").removeClass("icon-shoucang1")
            }else{
                nowData.addClass("icon-shoucang1").removeClass("icon-shoucang")
            }
        }
    })
}