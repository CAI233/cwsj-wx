/**
 * Created by Administrator on 2018/4/19 0019.
 */


var id = null;
$(document).ready(function () {
    id = localStorage.getItem("id");
    // console.log(id);
    // $(".pull-left").attr('href','/projectList?id='+id);
})
function _back(){
    window.open("/projectList?id="+id,"_self");
    localStorage.setItem("id",'');
}