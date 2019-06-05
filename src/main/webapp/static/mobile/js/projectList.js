/**
 * Created by Administrator on 2018/4/17 0017.
 */
var id = $.GetQueryString("id");

$(document).ready(function () {
    localStorage.setItem("id",id);
    console.log(id)
})
function goDetails(id){

    window.open("/projectDetails?project_id="+id,"_self")
}
// function rePlace(data){
//     return data.replace(/<p.*?>(.*?)<\/p>/g,function(match,p1){return p1})
// }