var orderList = [];
var param = {
    pageNum: 1,
    pageSize: 10,
    type: null
}

$(function () {
    if (getType) {
        $('a[href="#tab' + getType + '"]').click();
    }

    load(getType || 0)
})

load = function (type) {
    param.type = type;
    $.ajax({
        type: 'post',
        url: ctxPath + '/api/wx/member/order',
        data: param,
        success: function (res) {
            if (res.code == 0) {
                orderList = res.data.rows;
                $('#orderList_0').empty();
                $('#orderList_1').empty();
                $('#orderList_2').empty();
                $('#orderList_3').empty();
                $.each(orderList, function (index, item) {
                    console.log(item)
                    var col = $('#cloneOrder').clone().show();
                    $('#order_no', col).html('订单编号: ' + item.order_no);
                    $('#order_status', col).html(['取消订单', '待付款', '待发货', '待收货', '交易完成', '支付失败'][item.order_status]).css('color', ['', '#dab865', '#5382d2', '#de9c5d', '#34c136', 'red'][item.order_status]);
                    // $('#nums').html()
                    $('#price', col).html('￥' + item.pay_fee)
                    $('#express_fee', col).html('￥' + item.express_fee)
                    if (item.goods.length > 0) {
                        $('ul', col).empty();
                        $.each(item.goods, function (index2, item2) {
                            $('ul', col).append('<li class="item-content">' +
                                '<div class="item-media">' +
                                '<img src="' + ctxPath + item2.goods_cover_small + '"' +
                                'width="44"></div><div class="item-inner">' +
                                '<div class="item-title-row">' +
                                '<div class="item-title">' + item2.goods_name + '</div>' +
                                '</div><p>' + ["图书", "音视频", "电子书"][item2.goods_type - 1] + '</p><p>' +
                                '<span style="color: red">￥' + item2.goods_price + '</span>' +
                                '<span style="text-decoration: line-through;font-size: .6rem;margin-left: .25rem;vertical-align: bottom;">' + item2.old_price + '</span>' +
                                '</p><div class="nums">x ' + item2.goods_count + '</div> </div></li>')
                        })
                    }

                    if (item.order_status != 1) {
                        $('#goPay', col).css('display', 'none');
                    } else {
                        $('#goPay', col).css('display', 'block');
                        $('#goPay', col).click(function () {
                            location.href = '/wechatPay?order=' + item.order_no + '&money=1';
                        })
                    }
                    if (item.order_status == 3) {

                        var p = $('<p> <a style="width: 30%;margin-top: .25rem;float: right;" class="button  button-fill ">确认收货</a></p>').click(function () {
                            $.ajax({
                                type: 'post',
                                url: ctxPath + '/api/wx/finish/order',
                                data: {order_id: item.order_id},
                                success: function (res) {
                                    if (res.code == 0) {
                                        load();
                                    } else {
                                        $.alert(res.message)
                                    }
                                }
                            })
                        })
                        $('.row-footer', col).append('' +
                            '<p style="text-align: right">' +
                            '<span>快递物流: ' + item.express + '</span></p>' +
                            '<p style="text-align: right;"><span>快递单号: ' + item.express_num + '</pspan></p>').append(p);
                    }
                    $('#orderList_' + (type || 0) + '').append(col)
                })
                if (orderList.length == 0) {
                    $('#orderList_' + (type || 0) + '').append('' +
                        '<div style="color: #a2a2a2;text-align: center">' +
                        '<p><i style="font-size: 4rem" class="icon iconfont icon-zanwushuju"></i></p>' +
                        '<p>暂无订单</p>' +
                        '<p style="margin-top: 2rem;"><a href="/" style="border: 1px solid #ccc;padding: .5rem 1rem;border-radius: 1rem;">去逛逛</a></p>' +
                        '</div>')
                }

            } else {
                $.alert(res.message);
            }
        }
    })
}