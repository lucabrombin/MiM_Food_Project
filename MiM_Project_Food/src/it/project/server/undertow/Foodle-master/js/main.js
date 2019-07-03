var test_msg = '{"predicted_class":"bruschetta","best_result":"1087448.jpg","related_classes":[{"class":"bruschetta","confidence":83.33332824707031,"imgs":["3375972.jpg","3496454.jpg","770896.jpg","1545121.jpg","3849230.jpg","142836.jpg","3805917.jpg","3585467.jpg","3838937.jpg","3101709.jpg","447701.jpg","827942.jpg","3437632.jpg","2450629.jpg","2411864.jpg","848012.jpg","3276541.jpg","537494.jpg","1607884.jpg","839902.jpg","2626162.jpg","1436597.jpg","3696447.jpg","3653732.jpg"]},{"class":"beef_carpaccio","confidence":16.666667938232422,"imgs":["1393529.jpg","1488494.jpg","3622185.jpg","127274.jpg","2098481.jpg"]}]}';

var best_result = {};
var related_food = {};
var candidate_class = "";

var IMG_HEIGHT = 224, IMG_WIDTH = 224;

var socket;

if (window.WebSocket) {
  socket = new WebSocket("ws://localhost:8100/endpoint");

  socket.onmessage = function (event) {
    parse_json_collection(event.data);

    show_candidate_class();
    show_best_result();
    show_related_classes();
    
    toggle_view();
    toggle_loading();
  };
} 
else {
  alert("Your browser does not support Websockets.");
}

function create_card(title, img_url) {
  var li = $(document.createElement('li'));
  li.addClass("card");

  var className = title.replace(/ /g, "_");
  li.css("background-image", "url('" + "./img/" + className + "/" + img_url + "')");
  
  li.on('click', function(){
    open_url("./img/" + className + "/" + img_url);
  });
  // var p = $(document.createElement('p'));
  // p.addClass("label");
  // p.text(title);
  // li.append(p);
  return li;
}

function show_related_classes() {
  var classes_container = $("#related_classes_container");
  classes_container.empty();

  related_food.forEach(function (rel_class) {
    var div = $(document.createElement('div'));
    var class_title = $(document.createElement('h3'));
    class_title.text(rel_class.class.replace(/_/g, ' '));
    var confidence = $(document.createElement('p'));
    confidence.addClass('confidence');
    confidence.text("Confidence: " + rel_class.confidence.toFixed(2) + "%");
    var collection = build_collection(create_deck(rel_class));
    div.append(class_title);
    div.append(confidence);
    div.append(collection);
    classes_container.append(div);
  });
}

function build_collection(cards){
  collection = $(document.createElement('ul'));
  collection.addClass("collection");
  cards.forEach(function(card) {
    collection.append(card);
  });
  return collection;
}

function show_candidate_class() {
  $("#best_result_class > span").text(candidate_class.replace(/_/g, ' '));
}

function show_best_result() {
  $("#best_result").css("background-image", "url('" + "./img/" + candidate_class + "/" + best_result + "')");
  $("#best_result").on('click', function() {
    open_url("/img/" + candidate_class + "/" + best_result);
  });
}

function create_deck(similar_class) {
  var deck = [];
  similar_class.imgs.forEach(function(url) {
    deck.push(create_card(similar_class.class, url));
  });
  return deck;
}

function parse_json_collection(data) {
  var res = JSON.parse(data);
  candidate_class = res.predicted_class;
  related_food = res.related_classes;
  best_result = res.best_result;
}

function submit_picture(){
  if (this.files && this.files[0]) {
    var fileToLoad = this.files[0];
    var fileReader = new FileReader();

    fileReader.onload = function(fileLoadedEvent) {
      var img = new Image();
      var canvas = document.createElement("canvas");
      var ctx = canvas.getContext("2d");

      img.src = fileLoadedEvent.target.result;
      canvas.width = IMG_WIDTH;
      canvas.height = IMG_HEIGHT;
      ctx.drawImage(img, 0, 0, IMG_WIDTH, IMG_HEIGHT);

      var srcData = canvas.toDataURL("image/png");

      socket.send(srcData)
      toggle_loading();
    }
    fileReader.readAsDataURL(fileToLoad);
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

function toggle_loading() {
  $("#loading").toggle();
}

function open_url(url) {
  console.log("Opening " + url);
  var win = window.open(url, '_blank');
  win.focus();
}