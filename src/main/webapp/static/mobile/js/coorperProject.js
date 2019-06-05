/**
 * Created by Administrator on 2018/4/19 0019.
 */
$(document).ready(function () {
    var swiper = new Swiper('.swiper-container', {
        // spaceBetween: 30,
        centeredSlides: true,
        autoplay: {
            delay: 1500,
            disableOnInteraction: false,
        },
        loop : true,
        pagination: {
            el: '.swiper-pagination',
            clickable: true,
        },
    });
})