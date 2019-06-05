
var nowAnswer = '';
var nowUrl = $.GetQueryString("paper_id");//获取当前页面的peaper_id;
console.log(nowUrl)
var swiper = null;
$(document).ready(function () {

    swiper = new Swiper('.swiper-container', {
        centeredSlides: false,
        pagination: {
            el: '.swiper-pagination',
            type: 'progress',
        },
        observer: true, //修改swiper自己或子元素时，自动初始化swiper，主要是这两行
        observeParents: true ,//修改swiper的父元素时，自动初始化swiper,
        navigation: {
            nextEl: '.swiper-button-next',
            prevEl: '.swiper-button-prev',
            hideOnClick: true,
        },
        on: {
            slideChangeTransitionStart: function(){
                nowAnswer = '';
                console.log(this.activeIndex)
                var now_index = this.activeIndex+1;
                $("#now_index").html(now_index);
                $('.content').scrollTop(0)
            },
            slideChangeTransitionEnd: function(){
                console.log(this.activeIndex)
                $('.content').scrollTop(0)
            },
        }
    });

    $(document).on('click','.open-about', function () {
        $.popup('.popup-about');
        var num = parseInt($("#now_index").html())-1;
        $(".rowNum").removeClass("active").eq(num).addClass("active");

    });

    $(document).on('click','.popup-overlay', function () {
        $.closeModal('.popup-about');
    })
    $(document).on('click','.rowNum', function (event) {
        // var num = parseInt($("#now_index").html());
        var e = event || window.event;
        swiper.slideTo($(e.target).index(), 500, false);
        $("#now_index").html($(e.target).index()+1);
    })
    var time = parseInt($("#nowTime").html())*60
    var setTime = setInterval(function(){
        time--;
        var nowData = (Math.floor(time/60))+':'+(time-60*Math.floor(time/60));
        $("#nowTime").html(nowData);
        if(time<=0){
            endExam();
            clearInterval(setTime);
        }
    },1000)
})
function select_answer(type,answer,e,index){
    var e = e || window.event;
    if($(".answer_foot").eq(index).css("display")=='none'){
        if(type==1 || type==3){
            //单选题
            if($(e.currentTarget).find("i").hasClass("icon-2weixuanzhong")){
                $(e.currentTarget).find("i").removeClass("icon-2weixuanzhong").addClass("icon-selected");
                $(e.currentTarget).siblings().find("i").removeClass("icon-selected").addClass("icon-2weixuanzhong");
                nowAnswer = answer;
            }else{
                $(e.currentTarget).find("i").removeClass("icon-selected").addClass("icon-2weixuanzhong");
                $(e.currentTarget).siblings().find("i").removeClass("icon-selected").addClass("icon-2weixuanzhong");
                nowAnswer = '';
            }
        }else{
            //多选题
            if($(e.currentTarget).find("i").hasClass("icon-selected")){
                $(e.currentTarget).find("i").removeClass("icon-selected").addClass("icon-2weixuanzhong");
                nowAnswer = nowAnswer.replace(answer, '');
            }else{
                $(e.currentTarget).find("i").removeClass("icon-2weixuanzhong").addClass("icon-selected");
                nowAnswer += answer;
            }

        }
    }
    console.log(nowAnswer);
}

//提交
function endExam(){
    window.open("/answerToEnd?paper_id="+nowUrl,"_self")
}

//提交
function save_exam(id,index){
    console.log(index)
    $(".answer_foot").eq(index).show();
    $.ajax({
        type: 'post',
        url: ctxPath + '/answerTheQuestions',
        layui: true,
        data: {
            paper_id:nowUrl,
            openid:MEMBERINFO.openid,
            question_id:id,
            member_answer:nowAnswer
        },
        success: (function (res) {
        })
    })
}

