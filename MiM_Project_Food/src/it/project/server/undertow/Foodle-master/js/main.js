var test_msg = '{"imgs":[{"img":"803481.jpg","classe":"beef_carpaccio"},{"img":"1242241.jpg","classe":"breakfast_burrito"},{"img":"3607592.jpg","classe":"breakfast_burrito"},{"img":"820545.jpg","classe":"bruschetta"},{"img":"206027.jpg","classe":"beef_carpaccio"},{"img":"212368.jpg","classe":"beef_carpaccio"},{"img":"1172580.jpg","classe":"bruschetta"},{"img":"3429450.jpg","classe":"bibimbap"},{"img":"3051510.jpg","classe":"bibimbap"},{"img":"1832620.jpg","classe":"beef_carpaccio"},{"img":"3503609.jpg","classe":"beef_carpaccio"},{"img":"2472060.jpg","classe":"baby_back_ribs"},{"img":"3348442.jpg","classe":"beef_carpaccio"},{"img":"3837961.jpg","classe":"baby_back_ribs"},{"img":"1191063.jpg","classe":"bibimbap"},{"img":"3766342.jpg","classe":"baklava"},{"img":"1585661.jpg","classe":"bread_pudding"},{"img":"3441400.jpg","classe":"breakfast_burrito"},{"img":"3660558.jpg","classe":"beef_carpaccio"},{"img":"318343.jpg","classe":"beet_salad"},{"img":"1162327.jpg","classe":"beef_carpaccio"},{"img":"2221152.jpg","classe":"beet_salad"},{"img":"2024914.jpg","classe":"baklava"},{"img":"3187646.jpg","classe":"bibimbap"},{"img":"374126.jpg","classe":"beet_salad"},{"img":"1600317.jpg","classe":"baklava"},{"img":"239826.jpg","classe":"beet_salad"},{"img":"1894239.jpg","classe":"beef_carpaccio"},{"img":"466260.jpg","classe":"baby_back_ribs"},{"img":"913291.jpg","classe":"beef_tartare"}]}';
var best_result = {};
var related_food = {};

var socket;
if (window.WebSocket) {
    socket = new WebSocket("ws://localhost:8100/endpoint");
    socket.onmessage = function (event) {
        var chat = document.getElementById('chat');
        var obj = JSON.parse(event.data);
        var i = 0;
        var dim = Object.keys(obj.imgs).length;

        var classe = obj.imgs[0].classe;
        var nameImg = "url('./img/"+classe+"/" + obj.imgs[0].img + "')";
        $("#best_result").css("background-image", nameImg);
        toggle_view();

        parse_json_collection(obj);
        var deck = create_deck();
        show_best_result();
        show_collection("collection", deck);
        
        /*for(i = 0; i < dim; i++){
          var div = document.createElement('div');
          var classe = obj.imgs[i].classe;
          var nameImg = "url('./img/"+classe+"/" + obj.imgs[i].img + "')";
          div.style.backgroundImage = nameImg;
          div.style.width = "500";
          div.style.height = "500";
          div.style.display = "inline-block";
          div.style.float="left";

          chat.appendChild(div);
        }*/
        //chat.innerHTML = event.data + "<br />";
    };
} else {
    alert("Your browser does not support Websockets. (Use Chrome)");
}

function encodeImageFileAsURL() {

    var filesSelected = document.getElementById("filechooser").files;
    if (filesSelected.length > 0) {
        var fileToLoad = filesSelected[0];

        var fileReader = new FileReader();

        fileReader.onload = function(fileLoadedEvent) {
            var srcData = fileLoadedEvent.target.result; // <--- data: base64
            //var chat = document.getElementById('chat');
            //chat.innerHTML = srcData;
            socket.send(srcData)
            var newImage = document.createElement('img');
            newImage.src = srcData;

            document.getElementById("imgTest").innerHTML = newImage.outerHTML;
            //alert("Converted Base64 version is " + document.getElementById("imgTest").innerHTML);
            //console.log("Converted Base64 version is " + document.getElementById("imgTest").innerHTML);
        }
        fileReader.readAsDataURL(fileToLoad);
    }
}

function create_card(title, img_url) {
  var li = $(document.createElement('li'));
  li.addClass("card");
  var nameImg = "url('./img/"+title+"/" + img_url + "')";
  li.css("background-image", nameImg);
  var p = $(document.createElement('p'));
  p.addClass("label");
  p.text(title);
  li.append(p);
  return li;
}

function show_collection(collection_id, cards){
  var collection = $('#'+collection_id);
  $(collection).empty();
  cards.forEach(function(card) {
    collection.append(card);
  });
}

function show_best_result() {
  $("#best_result_class > span").text(best_result.classe.replace('_', ' '))
}

function create_deck() {
  var deck = [];
  related_food.forEach(function(food) {
    //deck.push(create_card(food.classe.replace('_', ' '), food.img));
    deck.push(create_card(food.classe, food.img));
  });
  return deck;
}

function parse_json_collection(data) {
  var imgs = JSON.parse(data).imgs;
  best_result = imgs.shift();
  related_food = imgs;
}

function submit_picture(){
  if (this.files && this.files[0]) {
    //var reader = new FileReader();

    var fileToLoad = this.files[0];
    var fileReader = new FileReader();

    fileReader.onload = function(fileLoadedEvent) {
        var srcData = fileLoadedEvent.target.result; // <--- data: base64
        //var chat = document.getElementById('chat');
        //chat.innerHTML = srcData;
        socket.send(srcData)
        //var newImage = document.createElement('img');
        //newImage.src = srcData;

        //document.getElementById("imgTest").innerHTML = newImage.outerHTML;
        //alert("Converted Base64 version is " + document.getElementById("imgTest").innerHTML);
        //console.log("Converted Base64 version is " + document.getElementById("imgTest").innerHTML);
    }
    fileReader.readAsDataURL(fileToLoad);

    /*
    reader.onload = function(e) {      

      ///////
      // actually all this stuff should be done in the callback after the upload 
      // (THIS IS JUST FOR DEBUGGING!)
      $("#best_result").css("background-image", "url('" + e.target.result + "')");
      toggle_view();

      // Testing...
      parse_json_collection(test_msg);
      var deck = create_deck();
      show_best_result();
      show_collection("collection", deck);
      ///////
    }
    reader.readAsDataURL(this.files[0]);
  }*/
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