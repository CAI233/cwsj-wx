
+function (pub) {
    $.pubAjax = function (a) {
        var load;
        a.data= JSON.stringify(a.data);
        a["contentType"]='application/json; charset=utf-8',
            a['beforeSend'] = function (xhr) {
                if (a.button) {
                    $(a.button).attr("disabled", true);
                }
                if (a.layer) {
                    load = layer.load(1)
                }
                !$.cookie('USERINFO_TOKEN') || xhr.setRequestHeader("token", $.cookie('USERINFO_TOKEN') || '');
            };
        a['complete'] = function (xhr) {
            if (a.button) {
                $(a.button).attr("disabled", false);
            }
            if (a.layer) {
                layer.close(load);
            }
            xhr.responseText && JSON.parse(xhr.responseText).code == 600 && $.cookie('USERINFO_TOKEN', '') && (location.href = ctxPath + '/login');
        };
        $.ajax(a);
    };
    //截取页面的href的参数
    $.GetQueryString = function(name){
        var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if(r!=null)return  unescape(r[2]); return null;
    }


    // $('img').bind('error', function () {
    //     this.src = '/static/mobile/img/img404.png';
    // })
}(window)

