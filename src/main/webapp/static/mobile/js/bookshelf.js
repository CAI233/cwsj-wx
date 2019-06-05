
var data = {
    searchText:null
}
var ids = [];//书架图书id集合
var nowData = null; //书架图书集合
var swiper = null;
$(document).ready(function () {

    load();


    $("#search").on("click",function(){
        window.open("/indexSearch","_self")
    })
})

function load(){
    $.ajax({
        type: 'post',
        url: ctxPath + '/api/wx/bookshelf/list',
        layui: true,
        data: data,
        success: (function (res) {
            console.log(res);
            nowData = res.data.rows;
            var nowHtml = '';
            $.each(nowData,function(index,item){
                console.log(item)
                nowHtml += '<div class="col-33" onclick="goodsInfo(\''+item.goods_id+'\')">'+
                    '<img src="'+ctxPath+item.goods_cover+'" />'+
                    '<span>'+item.goods_name+'</span>'+
                    '</div>'
            })
            $("#cont").html(nowHtml+'<div class="col-33" onclick="goTo()"><div class="add-book"></div></div>');
        })
    })
}
/**
 * 操作打开
 */
_actionOpen = function () {
    $(".pull-right").css("display","none");
    $('.action-begin,.action-bottom, .action-row').addClass('open');
    $.each(nowData,function(index,item){
        $('.action-row.open > div:not(:last-child)').eq(index).click(function(){
            if($(this).hasClass('active')){
                $(this).removeClass('active')
                for(var i=0;i<ids.length;i++){
                    if(item.shelf_id==ids[i]){
                        ids.splice(i,1);
                    }
                    break;
                }
            }else{
                $(this).addClass('active');
                console.log(item)
                ids.push(item.shelf_id);
            }
        })
        console.log(item)
    })
}
/**
 * 操作关闭
 */
_actionClose = function () {
    $(".pull-right").css("display","block");
    $('.action-begin,.action-bottom, .action-row').removeClass('open');
    _actionCancelCheckAll()
}
/**
 * 点击全选
 */
_actionCheck = function () {
    var bool = true
    $('.action-row > div:not(:last-child)').each(function (index, item) {
        if (!$(item).hasClass('active')) {
            bool = false;
        }
    })
    if (!bool) {
        _actionCheckAll()
    } else {
        _actionCancelCheckAll();
    }
}
/**
 * 全选
 */
_actionCheckAll = function () {
    $.each(nowData,function(index,item){
        ids.push(item.shelf_id );
    })
    $('.action-row > div:not(:last-child)').each(function (index, item) {
        $(item).addClass('active');
    })
    $('#check').html('取消全选');
}
/**
 * 取消全选
 */
_actionCancelCheckAll = function () {
    $('.action-row > div:not(:last-child)').each(function (index, item) {
        $(item).removeClass('active');
        ids = [];
    })
    $('#check').html('全选');
}

/**
 * 删除
 */
_delCheck = function () {
    console.log(ids);
    $.ajax({
        type: 'post',
        url: ctxPath + '/api/wx/bookshelf/del',
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify({ids: ids}),
        success: (function (res) {
            console.log(res.message)
            load();
        })
    })
}
$(function () {
    $('.action-row > div').click(function () {
        $(this).toggleClass('active')
    })
})
function goodsInfo(id){
    if(!$("#cont").hasClass("open")){
        window.open("/goodsInfo?goods_id="+id,"_self");
    }
}
function goTo(){
    window.open("/classify","_self")
}


