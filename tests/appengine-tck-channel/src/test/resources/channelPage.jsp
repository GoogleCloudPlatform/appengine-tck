<%@ page import="com.google.appengine.api.channel.ChannelService" %>
<%@ page import="com.google.appengine.api.channel.ChannelServiceFactory" %>
<%--
  ~ Copyright 2013 Google Inc. All Rights Reserved.
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
<%
    ChannelService channelService = ChannelServiceFactory.getChannelService();
    String channelId = request.getParameter("test-channel-id");
    String token = channelService.createChannel(channelId);
%>
<html>
<head>
<script type="text/javascript" src="/_ah/channel/jsapi"></script>
</head>
<body>

<script>
   var jsChannelId = '<%= channelId %>';

   function logAction(id, data) {
       var entry = document.createElement("div");
       entry.setAttribute("id", id);
       entry.innerHTML = data + "<br/>";
       document.getElementById("actions").appendChild(entry);
   }

   function sendServerMessage() {
       var channelId = document.getElementById("channel-id").innerHTML;
       //var echoMsg = Math.random().toString();
       var msg = "msg:" + channelId;

       var xmlhttp = new XMLHttpRequest();
       xmlhttp.open("GET","channelEcho.jsp?test-channel-id=" + channelId + "&echo=" + msg, true);
       xmlhttp.send();
       document.getElementById("last-sent-message").innerHTML = msg;
       logAction("last-sent-message-" + jsChannelId, msg);
   }

   function onOpened() {
       document.getElementById("status").innerHTML = "opened";
       logAction("opened-" + jsChannelId, "opened");
   }

   function onMessage(msg) {
       document.getElementById("status").innerHTML = msg.data;
       document.getElementById("last-received-message").innerHTML = msg.data;
       logAction("last-received-message-" + jsChannelId, msg.data);
   }

   function onError(err) {
       document.getElementById("status").innerHTML = err;
       logAction("error-" + jsChannelId);
   }

   function onClose() {
       document.getElementById("status").innerHTML = "closed";
       logAction("closed-" + jsChannelId);
   }

    var channel = new goog.appengine.Channel('<%= token %>');

    var socket = channel.open();
    socket.onopen = onOpened;
    socket.onmessage = onMessage;
    socket.onerror = onError;
    socket.onclose = onClose;
</script>

<p><b>channel-id:</b> <span id="channel-id"><%= channelId %></span></p>
<p><b>status:</b> <span id="status">never-set</span></p>
<p><b>last-sent-message:</b> <span id="last-sent-message">never-set</span></p>
<p><b>last-received-message:</b> <span id="last-received-message">never-set</span></p>
<button id="send-message-button" type="button" onclick="sendServerMessage()">send server message</button>
<p><b>Actions:</b><div id="actions"></div></p>
</body>
</html>