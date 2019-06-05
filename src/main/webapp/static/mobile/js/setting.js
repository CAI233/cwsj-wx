$(function(){
    $('#icon').attr('src',MEMBERINFO.icon)
    $('#nick_name').html(MEMBERINFO.nick_name)
    $('#sex').html(MEMBERINFO.sex==1?'男':'女')
})