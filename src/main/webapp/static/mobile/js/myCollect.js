var nowIndex = $.GetQueryString("type") || 0;
$(document).ready(function () {
    $(".tab-link").eq(nowIndex).click();
    var swiper = new Swiper('.swiper-container', {
        slidesPerView: 'auto',
        initialSlide: 0,
        resistanceRatio: 0,
        slideToClickedSlide: true,
        observer: true, //修改swiper自己或子元素时，自动初始化swiper，主要是这两行
        observeParents: true ,//修改swiper的父元素时，自动初始化swiper,
        on: {
            touchStart: function(event){
                var e = event || window.event;
                e.stopPropagation();
            },
            touchMove: function(event){
                var e = event || window.event;
                e.stopPropagation();
            },
            touchEnd: function(event){
                var e = event || window.event;
                e.stopPropagation();
            },
        },
    });
})

function delColl(type,id){
    var type = $(".tab-link.active").index();
    $.ajax({
        type: 'post',
        url: ctxPath + '/saveCollect',
        data: {id: id,type: type},
        success: function (res) {
            window.open("/myCollect?type="+type,"_self");
        }
    })
}
function _goTo(bool,id){
    if(bool){
        window.open("/goodsInfo?goods_id="+id,"_self")
    }else{
        window.open("/worksInfo?works_id="+id,"_self")
    }
}
