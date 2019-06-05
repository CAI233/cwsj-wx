$(function () {


    var swiper = new Swiper('.swiper-container', {
        slidesPerView: 3,
        spaceBetween: 0,

    });
    $('#icon').attr('src', MEMBERINFO.icon)
    $(document).on('click', '.open-about', function () {
        $.popup('.popup-about');
    })

    $(document).on('click', '.open-services', function () {
        $.popup('.popup-services');
    });
})


/**
 * 加入购物车
 */

_addShopCar = function (goods_id) {
    $.showPreloader('正在加入购物车')
    $.ajax({
        type: 'post',
        url: ctxPath + '/api/wx/shoppingcar/save',
        data: {
            openid: MEMBERINFO.openid,
            goods_id: goods_id,
        },
        success: function (res) {
            $.hidePreloader()
            if (res.code == 0) {
                $.alert('操作成功');
            } else {
                $.alert(res.message)
            }
        }
    })
}

//多行文本输入框剩余字数计算
function checkMaxInput(obj, maxLen) {
    if (obj == null || obj == undefined || obj == "") {
        return;
    }
    if (maxLen == null || maxLen == undefined || maxLen == "") {
        maxLen = 100;
    }

    var strResult;
    var $obj = $(obj);
    var newid = $obj.attr("id") + 'msg';

    if (obj.value.length > maxLen) { //如果输入的字数超过了限制
        obj.value = obj.value.substring(0, maxLen); //就去掉多余的字
        strResult = '<span id="' + newid + '" class=\'Max_msg\' ><br/> ' + obj.value.length + '/' + maxLen + '</span>'; //计算并显示剩余字数
    }
    else {
        strResult = '<span id="' + newid + '" class=\'Max_msg\' ><br/> ' + obj.value.length + '/' + maxLen + '</span>'; //计算并显示剩余字数
    }
    $('.Max_msg').remove();
    $obj.after(strResult);
}

//取消收藏
function delColl(type, id, e) {
    var e = e || window.event;
    var nowData = $(e.currentTarget);
    $.ajax({
        type: 'post',
        url: ctxPath + '/saveCollect',
        data: {id: id, type: type},
        success: function (res) {
            $.toast(res.message);
            if (res.data == 1) {
                nowData.addClass("icon-shoucang").removeClass("icon-shoucang1")
            } else {
                nowData.addClass("icon-shoucang1").removeClass("icon-shoucang")
            }
        }
    })

}

/**
 * 资源跳转
 */
_resJump = function (res_id,buy) {
    if(!buy){
        $.alert('请先购买!')
        return false;
    }
    location.href = '/resourceInfo?id=' + res_id;
}
/**
 * 评论
 */
_comment = function (goods_id) {
    var comment_content = $('#comment_content').val();
    if (!comment_content) {
        $.alert('请填写评论内容', '提示!');
        return false;
    }

    $.ajax({
        type: 'post',
        url: ctxPath + '/api/wx/comments/save',
        data: {
            goods_id: goods_id,
            comments_content: comment_content,
        },
        success: function (res) {
            if (res.code == 0) {
                $.alert('评论成功', '提示!');
                // $('.close-popup').click();
                setTimeout(function () {
                    location.reload();
                }, 1500)
            } else {
                $.alert(res.message)
            }
        }
    })
}
/**
 * 点赞
 */
_support = function (id, item) {
    if ($(item).find('.icon').css('color') != 'red') {
        $.ajax({
            type: 'post',
            url: ctxPath + '/api/wx/comments/saveliked',
            data: {
                id: id,
            },
            success: function (res) {
                if (res.code == 0) {
                    $(item).find('.icon').css('color', 'red')
                }
            }
        })
    }
}
/**
 * 立即购买
 * @private
 */
_promptlyPay = function (goods_id) {
    _addShopCar(goods_id);
    location.href = '/shoppingCar'
}

function downList(e) {
    var e = e || window.event;
    if ($("#downList").css("-webkit-line-clamp") != 'unset') {
        $("#downList").css("-webkit-line-clamp", 'unset');
        $(e.currentTarget).addClass("icon-down").removeClass("icon-up");
    } else {
        $("#downList").css("-webkit-line-clamp", '2');
        $(e.currentTarget).addClass("icon-up").removeClass("icon-down");
    }
}




(function () {
    var nelsonATCAControlBar = document.getElementById("nelsonATCAControlBar");
    var nelsonATCAContainer = "";
    var prefixes = ['', '-ms-', '-moz-', '-webkit-', '-khtml-', '-o-'];
    nelsonAddtoCartAnimation = {
        a: "",
        b: "",
        c: "",
        startX: "",
        startY: 0,
        endX: "",
        endY: 0,
        second: 0,
        speed: 10,
        /*初始化‘小球’的位置*/
        init: function (startX, endX, rC, txt) {
            if (!document.getElementById("nelsonATCAContainer")) {
                var _nelsonATCAContainer = document.createElement("div");
                _nelsonATCAContainer.className = "nelsonATCAContainer";
                _nelsonATCAContainer.id = "nelsonATCAContainer";
                // _nelsonATCAContainer.innerText = txt?txt:"";
                _nelsonATCAContainer.innerHTML = txt ? "<img  src='" + txt + "' />" : "";
                _nelsonATCAContainer.style.left = startX + "px";
                nelsonATCAControlBar.appendChild(_nelsonATCAContainer);
                nelsonATCAContainer = _nelsonATCAContainer;
                _nelsonATCAContainer = null;
                this.startX = startX;
                this.endX = endX;
                this.formula(rC);
                this.second = Math.abs(startX - endX) * this.speed / 2000;
                return this;
            }
        },
        /*计算常量*/
        formula: function (rC) {
            var centerX = (this.startX - this.endX) / 2 + this.endX;
            this.a = rC;
            this.b = -2 * this.a * centerX;
            this.c = -1 * this.a * this.startX * this.startX - this.b * this.startX;
        },
        /*开始漂移*/
        move: function (goods_id) {

            var that = this;
            for (var i in prefixes) {
                nelsonATCAContainer.style[prefixes[i] + prefixes[i] ? "A" : "a" + "nimation"] = "moveAnimation " + that.second + "s forwards";
            }
            nelsonATCAContainer.style.display = "block";
            var s = setInterval(function () {
                var startLeft = nelsonATCAContainer.offsetLeft;
                if (startLeft <= that.endX) {
                    clearInterval(s);
                    that.resetPosition();
                    $.ajax({
                        type: 'post',
                        url: ctxPath + '/api/wx/shoppingcar/save',
                        data: {
                            openid: MEMBERINFO.openid,
                            goods_id: goods_id,
                        },
                        success: function (res) {
                            if (res.code == 0) {
                                $(".car_count").html(parseInt($(".car_count").html()) + 1);
                                $(".car_count").addClass('active');
                            } else {
                                $.alert(res.message)
                            }
                        }
                    })
                    that.setValue()
                    return that;
                }
                nelsonATCAContainer.style.left = startLeft - 5 + "px";
                startLeft = nelsonATCAContainer.offsetLeft;
                nelsonATCAContainer.style.top = that.a * startLeft * startLeft + that.b * startLeft + that.c + "px";
            }, that.speed)
        },
        /*重置小球初始状态*/
        resetPosition: function () {
            nelsonATCAContainer.style.display = "none";
            nelsonATCAContainer.style.left = this.startX + "px";
            nelsonATCAContainer.style.top = this.startY + "px";
        },
        /*设置小球中的数字*/
        setValue: function (value) {
            // nelsonATCAContainer.innerText = value;
            setTimeout(function () {
                $(".car_count").removeClass('active');
            }, 300)
        }
    }
})()
