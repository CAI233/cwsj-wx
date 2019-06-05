var formData = {
    openid: MEMBERINFO.openid,
    province: null,              //省份
    province_code: null,
    city: null,                  //	城市
    city_code: null,
    area: null,                  //	区域
    area_code: null,
    street: null,                //街道
    street_code: null,
    address_detail: null,        //详细地址
    zip_code: null,              //邮政编码
    consignee: null,             //收货人姓名
    consignee_phone: null,       //收货人联系方式
    is_default: null,            //是否默认地址
    remark: null,
}
$(function () {
    $("#city-picker").cityPicker({
        toolbarTemplate: '<header class="bar bar-nav">\
    <button class="button button-link pull-right close-picker">确定</button>\
    <h1 class="title">选择收货地址</h1>\
    </header>'
    });

    $(document).on('click', '.open-about', function () {
        $.popup('.popup-about');
    });

    load();
})
/**
 *  加载
 */
load = function () {
    $.ajax({
        type: 'post',
        url: ctxPath + '/api/wx/member/address/list',
        data: {openid: MEMBERINFO.openid},
        success: function (res) {
            if (res.code == 0) {
                $('#list').empty()
                $.each(res.data, function (index, item) {
                    var col = $('#cloneCard').clone().show();
                    $('.item-title', col).html(item.consignee + '&emsp;' + item.consignee_phone);
                    $('.item-right', col).html(item.is_default == 1 ? '默认地址' : '');
                    $('.item-subtitle', col).html(item.province + ',' + item.city + ',' + item.area + item.address_detail)
                    $('.default-address a', col).html(item.is_default == 2 ? '设为默认地址' : '').click(function () {
                        _isDefault(item.member_address_id)
                    })
                    $('.icon-shanchu', col).click(function () {
                        _delRow(item.member_address_id)
                    })
                    $('#list').append(col);
                })
            } else {
                $.alert(res.message)
            }
        }

    })
}

/**
 *  设为默认
 */
_isDefault = function (id) {
    $.showPreloader('正在设为默认地址');
    $.ajax({
        type: 'post',
        url: ctxPath + '/api/wx/member/address/setdefault',
        data: {member_address_id: id, openid: MEMBERINFO.openid},
        success: function (res) {
            $.hidePreloader();
            if (res.code == 0) {
                load();
            } else {
                $.alert(res.message);
            }
        }
    })
}

/**
 * 删除
 * @private
 */
_delRow = function (id) {
    $.showPreloader('正在删除');
    $.ajax({
        type: 'post',
        url: ctxPath + '/api/wx/member/address/del',
        data: {member_address_id: id},
        success: function (res) {
            $.hidePreloader();
            if (res.code == 0) {
                load();
            } else {
                $.alert(res.message);
            }
        }
    })
}
/**
 * 保存
 * @private
 */
_formSave = function () {
    var pickerValue = $('#city-picker').val();
    formData.consignee = $('#consignee').val()
    formData.consignee_phone = $('#consignee_phone').val()
    formData.province = pickerValue.substring(0, pickerValue.indexOf(' '));
    formData.city = pickerValue.substring(pickerValue.indexOf(' ') + 1, pickerValue.lastIndexOf(' '));
    formData.area = pickerValue.substring(pickerValue.lastIndexOf(' ') + 1);
    formData.address_detail = $('#address_detail').val()
    formData.zip_code = $('#zip_code').val()
    formData.is_default = $('#default').is(':checked') == true ? 1 : 2;

    if (!formData.consignee) {
        $.alert('请填写收货人姓名', '提示!');
        return false
    }
    if (!formData.consignee_phone) {
        $.alert('请填写联系电话', '提示!');
        return false
    }
    if (!(/^1[3|4|5|6|7|8][0-9]\d{4,8}$/.test(formData.consignee_phone))) {
        $.alert('请填写正确的联系电话', '提示!');
        return false
    }
    if (!pickerValue) {
        $.alert('请选择收货地址', '提示!');
        return false
    }
    if (!formData.address_detail) {
        $.alert('请输入详细地址', '提示!');
        return false
    }
    if (!(/^[1-9][0-9]{5}$/.test(formData.zip_code))) {
        $.alert('请填写正确的邮政编码', '提示!');
        return false
    }
    console.log(formData)

    $.showPreloader('正在保存')
    $.ajax({
        type: 'post',
        url: ctxPath + '/api/wx/member/address/save',
        data: formData,
        success: function (res) {
            $.hidePreloader();
            if (res.code == 0) {
                location.href = '/address'
            } else {
                $.alert(res.message)
            }
        }
    })
}