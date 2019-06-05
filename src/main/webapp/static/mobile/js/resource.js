/**
 * Created by Administrator on 2018/4/12 0012.
 */
$(document).ready(function () {
    $(document).ready(function () {
        var swiper = new Swiper('.swiper-container', {
            slidesPerView: 3,
            spaceBetween: 5,
            pagination: {
                el: '.swiper-pagination',
                clickable: true,
            },
        });

    })

})

function info(id){
    window.open('/resourceInfo?id='+id,'_self')
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
// 立即购买
_promptlyPay = function (works_id,works_price) {
    // location.href = '/wechatPay?id='+works_id+'&money=1';
    window.open("/wechatPay?id="+works_id+"&money="+works_price,"_self");
}
