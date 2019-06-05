/**
 * Created by Administrator on 2018/4/12 0012.
 */
$(document).ready(function () {
    var total = parseInt($("#total").html());
    var url = $("#dataUrl").html();
    init(url,total);
    var swiper = new Swiper('.swiper-container', {
        // spaceBetween: 30,
        centeredSlides: true,
        pagination: {
            el: '.swiper-pagination',
            type: 'fraction',
        }

    });

        //播放完毕
        $('#mp3Btn').on('ended', function() {
            console.log("音频已播放完成");
            $('.btn-audio').addClass("icon-kaishi").removeClass("icon-zanting");
        })
        //播放器控制
        var audio = document.getElementById('mp3Btn');
        // audio.volume = .3;
        console.log(parseInt(audio.duration));
        $('.btn-audio').click(function() {
            event.stopPropagation();//防止冒泡
            if(audio.paused){ //如果当前是暂停状态
                console.log('播放')
                $("#shade_on i").removeClass("icon-kaishi").addClass("icon-zanting")
                audio.play(); //播放
                return;
            }else{//当前是播放状态
                console.log('暂停')
                $("#shade_on i").addClass("icon-kaishi").removeClass("icon-zanting")
                audio.pause(); //暂停
            }
        });
        setInterval(function(){
           console.log(audio.currentTime)
        },100)

})

function init(url,num){
    for(var i=0;i<num;i++){
        var number = i+1;
        $('<div class="swiper-slide"><a href=""><img src="'+ctxPath+url+'/img'+number+'.png" alt="" style="width:100%;height:100%;"></a></div>').appendTo(".swiper-wrapper")
    }
}
