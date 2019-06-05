$(function () {

})

/**
 * 提交订单
 * @private
 */
_submitOrder = function () {

    if (!member_address_id) {
        $.alert('请填写收货地址');
        return false;
    }
    var ids = $.GetQueryString('ids').split(',');
    $.showPreloader('正在提交订单');
    $.ajax({
        type: 'post',
        url: ctxPath + '/api/wx/order/submit',
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify({
            ids: ids,
            id: member_address_id
        }),
        success: function (res) {
            $.hidePreloader();
            if (res.code == 0) {
                location.href = '/myOrder'
            } else {
                $.alert(res.message);
            }
        }

    })
}