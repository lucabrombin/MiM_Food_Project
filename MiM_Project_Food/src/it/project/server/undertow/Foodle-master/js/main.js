var test_msg = '{"imgs":[{"img":"803481.jpg","classe":"beef_carpaccio"},{"img":"1242241.jpg","classe":"breakfast_burrito"},{"img":"3607592.jpg","classe":"breakfast_burrito"},{"img":"820545.jpg","classe":"bruschetta"},{"img":"206027.jpg","classe":"beef_carpaccio"},{"img":"212368.jpg","classe":"beef_carpaccio"},{"img":"1172580.jpg","classe":"bruschetta"},{"img":"3429450.jpg","classe":"bibimbap"},{"img":"3051510.jpg","classe":"bibimbap"},{"img":"1832620.jpg","classe":"beef_carpaccio"},{"img":"3503609.jpg","classe":"beef_carpaccio"},{"img":"2472060.jpg","classe":"baby_back_ribs"},{"img":"3348442.jpg","classe":"beef_carpaccio"},{"img":"3837961.jpg","classe":"baby_back_ribs"},{"img":"1191063.jpg","classe":"bibimbap"},{"img":"3766342.jpg","classe":"baklava"},{"img":"1585661.jpg","classe":"bread_pudding"},{"img":"3441400.jpg","classe":"breakfast_burrito"},{"img":"3660558.jpg","classe":"beef_carpaccio"},{"img":"318343.jpg","classe":"beet_salad"},{"img":"1162327.jpg","classe":"beef_carpaccio"},{"img":"2221152.jpg","classe":"beet_salad"},{"img":"2024914.jpg","classe":"baklava"},{"img":"3187646.jpg","classe":"bibimbap"},{"img":"374126.jpg","classe":"beet_salad"},{"img":"1600317.jpg","classe":"baklava"},{"img":"239826.jpg","classe":"beet_salad"},{"img":"1894239.jpg","classe":"beef_carpaccio"},{"img":"466260.jpg","classe":"baby_back_ribs"},{"img":"913291.jpg","classe":"beef_tartare"}]}';
var best_result = {};
var related_food = {};
var candidate_class = "";

var socket;

if (window.WebSocket) {
  socket = new WebSocket("ws://localhost:8100/endpoint");

  socket.onmessage = function (event) {
    parse_json_collection(event.data);

    show_candidate_class();
    show_best_result();
    show_collection("collection", create_deck());
    
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

function voting_candiate_class(knn, k = 10) {
  var counter = {};
  knn = knn.slice(0, k);
  knn.forEach(function(neighbour) {
    counter[neighbour.classe] = (counter[neighbour.classe]+1) || 1;
  });
  return Object.keys(counter).reduce(function(a, b){ return counter[a] > counter[b] ? a : b });
}

function show_candidate_class() {
  $("#best_result_class > span").text(candidate_class.replace(/_/g, ' '));
}

function show_best_result() {
  $("#best_result").css("background-image", "url('" + "./img/" + best_result.classe + "/" + best_result.img + "')");
  $("#best_result").on('click', function() {
    open_url("/img/" + best_result.classe + "/" + best_result.img);
  });
}

function create_deck() {
  var deck = [];
  related_food.forEach(function(food) {
    deck.push(create_card(food.classe.replace(/_/g, ' '), food.img));
  });
  return deck;
}

function parse_json_collection(data) {
  var imgs = JSON.parse(data).imgs;

  candidate_class = voting_candiate_class(imgs)
  best_result = imgs.shift();
  related_food = imgs;
}

function submit_picture(){
  if (this.files && this.files[0]) {
    var fileToLoad = this.files[0];
    var fileReader = new FileReader();

    fileReader.onload = function(fileLoadedEvent) {
      var srcData = fileLoadedEvent.target.result; // <--- data: base64
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