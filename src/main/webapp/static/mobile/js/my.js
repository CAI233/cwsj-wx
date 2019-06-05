$(function () {
    // $("#first-swiper").swiper({
    //     pagination: '.swiper-pagination'
    // });

    $(".main .row li").click(function () {
        $('.main .row .active').removeClass('active');
        $(this).addClass("active")
    })
    $('#member_name').html(MEMBERINFO.nick_name)
    $('#icon').attr('src',MEMBERINFO.icon)
    // $.ajax({
    //     type: 'post',
    //     url: ctxPath + '/api/busiz/member/login',
    //     data: {nick_name: 'sdgdsgdfg'},
    //     success: function (res) {
    //         if (res.code == 0) {
    //             console.log(res);
    //             // $.('USERINFO', JSON.stringify(res.data));
    //             // $.cookie("USERINFO_TOKEN", res.data.token);
    //             // $.cookie("USERINFO_NAME", user_phone);
    //             // $.cookie("USERINFO_PWD", user_pwd);
    //             // $.cookie("USERINFO_STATICS", user_statics);
    //             // location.href = ctxPath + '/examArrange';
    //
    //         } else {
    //             // layer.alert(res.message);
    //         }
    //     }
    // })
})