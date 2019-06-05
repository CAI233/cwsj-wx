/**
 * 单选点击
 */
_checkClick = function (t) {
    if (!$(t).parent().hasClass('active')) {
        $(t).parent().addClass('active')
    } else {
        $(t).parent().removeClass('active')
    }
    var bool = true;
    $('#dataList .checkList').each(function (index, item) {
        if (!$(item).hasClass('active')) {
            bool = false;
        }
    })
    if (bool)
        $('#checkAll').addClass('active')
    else {
        $('#checkAll').removeClass('active')
    }

    _countPrice()
}

/**
 * 全选点击
 */
_checkAllClick = function () {
    if (!$('#checkAll').hasClass('active')) {
        $('#checkAll').addClass('active')
        _checkAll()
    } else {
        $('#checkAll').removeClass('active')
        _checkCancelAll();
    }
    _countPrice()
}

/**
 * 全选
 * @private
 */
_checkAll = function () {
    $('#dataList .checkList').each(function (index, item) {
        $(item).addClass('active')
        dataList[index].checked = true;
    })
    _countPrice()
}
/**
 * 取消全选
 * @private
 */
_checkCancelAll = function () {
    $('#dataList .checkList').each(function (index, item) {
        $(item).removeClass('active')
        dataList[index].checked = false;
    })
    _countPrice()
};
_jian = function (item) {
    var value = parseInt($(item).parent().find('span').html()) || 0;
    if (value > 0)
        $(item).parent().find('span').html(--value)
};
_jia = function (item) {
    var value = parseInt($(item).parent().find('span').html()) || 0;
    $(item).parent().find('span').html(++value)
}

$(function () {


    load();
})
var dataList = {};
load = function () {
    $.ajax({
        type: 'post',
        url: ctxPath + ' /api/wx/shoppingcar/list',
        data: {openid: 'oT3tm1PHz4dRufrxiri3zHVbFo4M'},
        success: function (res) {
            if (res.code == 0) {
                dataList = res.data;
                $('#dataList').empty()
                $.each(dataList, function (index, item) {
                    var col = $('#cloneData').clone().show();
                    $('img', col).attr('src', ctxPath + item.goods_cover_small);
                    $('#goods_name', col).html(item.goods_name)
                    $('#goods_type', col).html(['图书', '音视频', '电子书'][item.goods_type - 1]);
                    $('#real_price', col).html('￥' + item.real_price)
                    $('#price', col).html(item.price)
                    $('#goods_num', col).html(item.goods_num)
                    $('.item-media', col).click(function () {
                        item.checked = $(this).parent().hasClass('active');
                    })
                    //减
                    $('.icon-jian', col).click(function () {
                        if (item.goods_num > 1) {
                            _save(item.id, item.goods_id, --item.goods_num, col);
                        }
                    })
                    //加
                    $('.icon-jia', col).click(function () {
                        _save(item.id, item.goods_id, ++item.goods_num, col);
                    })
                    //删除
                    $('.del', col).click(function () {
                        _save(item.id, item.goods_id, 0, col);
                    })
                    $('#dataList').append(col);
                    var swiper = new Swiper('.swiper-container', {
                        slidesPerView: 'auto',
                        initialSlide: 0,
                        resistanceRatio: 0,
                        slideToClickedSlide: true,
                    });
                })
                if (dataList.length == 0) {
                    $('#dataList').append('' +
                        '<div style="color: #a2a2a2;text-align: center">' +
                        '<p><i style="font-size: 4rem" class="icon iconfont icon-gouwuche"></i></p>' +
                        '<p>购物车是空的</p>' +
                        '<p style="margin-top: 2rem;"><a href="/" style="border: 1px solid #ccc;padding: .5rem 1rem;border-radius: 1rem;">去逛逛</a></p>' +
                        '</div>')
                }


            } else {
                $.alert(res.message)
            }
        }

    })
}

_save = function (id, goods_id, num, col) {
    $.ajax({
        type: 'post',
        url: ctxPath + '/api/wx/shoppingcar/save',
        data: {id: id, goods_id: goods_id, goods_num: num, openid: MEMBERINFO.openid},
        success: function (res) {
            if (res.code == 0) {
                $('#goods_num', col).html(num)
                _countPrice();
                if (num == 0) {
                    load();
                    _checkCancelAll()
                }
            }
        }
    })

}
/**
 * 结算
 * @private
 */
_clearing = function () {
    var ids = [];
    $.each(dataList, function (index, item) {
        if (item.checked) {
            ids.push(item.id)
        }
    })
    if (ids.length == 0) {
        $.alert('请选择结算的商品');
        return false;
    }
    location.href = '/orderPay?ids=' + ids.join(',');
}
/**
 * 计算价格
 */
_countPrice = function () {
    var countNum = 0;
    $('#dataList .checkList').each(function (index, item) {
        if ($(item).hasClass('active')) {
            countNum += parseFloat($('#real_price', item).html().replace('￥', '')) * parseInt($('#goods_num', item).html());
        }
    })

    $('#countNum').html('￥' + countNum.toFixed(2)
    )
}
