$(function () {

    // Disallow multiple press of button
    $(".oneClickBttn").one("click", function () {
        window.stop();
        document.execCommand("Stop");
        $(this).replaceWith("<button class='btn btn-evolutions'><img src='/assets/images/buttonLoading.gif'></button>");
        window.location = $(this).data("href");
    });

});