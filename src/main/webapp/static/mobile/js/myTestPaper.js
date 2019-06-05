$(document).ready(function () {
    // var swiper = new Swiper('.swiper-container', {
    //     // spaceBetween: 30,
    //     centeredSlides: true,
    //     autoplay: {
    //         delay: 1500,
    //         disableOnInteraction: false,
    //     },
    //     loop : true,
    //     pagination: {
    //         el: '.swiper-pagination',
    //         clickable: true,
    //     },
    // });
})
function goTo(id){
    window.open("/myTestList?works_id="+id,"_self")
}