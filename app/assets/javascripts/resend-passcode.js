var ready = (callback) => {
    if (document.readyState != "loading") callback();
    else document.addEventListener("DOMContentLoaded", callback);
}

ready(() => {
    function addOnClickEventHandler(link) {
        link.addEventListener("click", function() {submitResendPasscodeForm()})
    }
    function submitResendPasscodeForm( ) {
        document.getElementById("resendPasscodeForm").submit()
    }
    addOnClickEventHandler(document.getElementById("resendPasscodeLink"))
});