$(function () {
    _getAdv();
    _getWindows();

    $("#search").on("click",function(){
        window.open("/indexSearch","_self")
    })

    // $.closeModal(popup)
})

/**
 * 广告
 */
_getAdv = function () {
    $.ajax({
        type: 'post',
        url: ctxPath + '/api/wx/windows/adv',
        success: function (res) {
            if (res.code == 0) {
                $('#adv').empty();
                $.each(res.data, function (index, item) {
                    $('#adv').append('<div class="swiper-slide"><a href=""><img src="' + ctxPath + item.adv_img + '" alt=""></a></div>')
                })
                var swiper = new Swiper('.swiper-container', {
                    centeredSlides: true,
                    autoplay: {
                        delay: 1500,
                        disableOnInteraction: false,
                    },
                    loop: true,
                    pagination: {
                        el: '.swiper-pagination',
                        clickable: true,
                    },
                });
            }
        }
    })
}


/**
 * 橱窗
 */
_getWindows = function () {
    $.ajax({
        type: 'post',
        url: ctxPath + '/api/wx/windows/list',
        success: function (res) {
            if (res.code == 0) {
                var icon = ['icon-huo', 'icon-fabuzhuanjiawenzhang', 'icon-jiaoyu', 'icon-book'];
                $('#windows').empty();
                $.each(res.data, function (index, item) {
                    $('<a href="#tab' + index + '" data-id="item.window_id" class="tab-link button">\n' +
                        '<span data-index="' + index + '" class="icon iconfont ' + icon[index] + ' col-100"></span>\n' +
                        '<br><span>' + item.window_name + '</span>\n' +
                        '</a>').click(function () {
                        $('.buttons-tab a').removeClass('active');
                        $(this).addClass('active');
                        _getWindowList(item.window_id);
                    }).appendTo('#windows');
                })
                $('a[href="#tab0"]').click();
            }
        }
    })
}

/**
 * 橱窗列表
 */
_getWindowList = function (id) {
    $.ajax({
        type: 'post',
        url: ctxPath + '/api/wx/windows/goods',
        data: {id: id},
        success: function (res) {
            if (res.code == 0) {
                var icon = ['icon-huo', 'icon-fabuzhuanjiawenzhang', 'icon-jiaoyu', 'icon-book'];
                $('#w_list').empty();
                $.each(res.data, function (index, item) {
                    // $('<a class="col-50" href="/goodsInfo?goods_id='+item.goods_id+'">\n' +
                    //     '<img src="' + ctxPath + item.goods_cover_small + '" >\n' +
                    //     '<p class="w_title">' + item.goods_name + '</p>\n' +
                    //     '<p class="w_price">' + (item.real_price==0?'免费':'￥'+item.real_price.toFixed(2)) + '<span>' + (item.price || '') + '</span></p>\n' +
                    //     '</a>').appendTo('#w_list');
                    $('<a style="width:31.33333%;display:inline-block;margin-right:2%;margin-bottom: .5rem;background: #fff;" href="/goodsInfo?goods_id=' + item.goods_id + '">\n' +
                        '<span style="background:url(' + ctxPath + item.goods_cover_small + ') no-repeat center;vertical-align: top;background-size:contain;display:inline-block;width:100%;padding-top:133%;"></span>\n' +
                        '<p class="w_title" >' + item.goods_name + '</p>\n' +
                        '<p class="w_price">' + (item.real_price == 0 ? '免费' : '￥' + item.real_price.toFixed(2)) + '<span>' + (item.price || '') + '</span></p>\n' +
                        '</a>').appendTo('#w_list');
                })
            }
        }
    })
}
