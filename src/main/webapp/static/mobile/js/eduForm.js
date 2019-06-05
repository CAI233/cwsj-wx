/**
 * Created by Administrator on 2018/4/13 0013.
 */
$(document).ready(function () {

})

// function slide(bool,e){
//     var e = e || window.event;
//     $(e.currentTarget).removeClass("slide_active").siblings().addClass("slide_active");
//     if(bool){
//         $(".item_cont").css({"-webkit-line-clamp":"unset"})
//     }else{
//         $(".item_cont").css({"-webkit-line-clamp":"3"})
//     }
// }
var bool = false;
function slide(e){
    var e = e || window.event;
    bool = !bool;
    if(bool){
        $(e.currentTarget).find(".item_cont").css({"-webkit-line-clamp":"unset"});
        $(e.currentTarget).find(".item_foot").removeClass("icon-down").addClass("icon-up")
    }else{
        $(e.currentTarget).find(".item_cont").css({"-webkit-line-clamp":"3"});
        $(e.currentTarget).find(".item_foot").removeClass("icon-up").addClass("icon-down")
    }
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