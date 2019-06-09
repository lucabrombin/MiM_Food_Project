function submit_picture(){
  if (this.files && this.files[0]) {
    var reader = new FileReader();
    reader.onload = function(e) {

      ///////
      // actually all this stuff should be done in the callback after the upload 
      // (THIS IS JUST FOR DEBUGGING!)
      $("#best_result").css("background-image", "url('" + e.target.result + "')");
      toggle_view();
      ///////
    }
    reader.readAsDataURL(this.files[0]);
  }
}

function toggle_view() {
  $("#content").toggle();
  $("#landing").toggle();
  $("#img_uploader").val("");
}

$(function() {
  
  $("header h1").on('click', toggle_view);
  
  $("#img_uploader").on('change', submit_picture);

  $("#upload_button").on('click', function(){ 
    document.getElementById("img_uploader").click();
  });
});