<%@ page import="com.google.appengine.api.channel.ChannelMessage" %>
<%@ page import="com.google.appengine.api.channel.ChannelService" %>
<%@ page import="com.google.appengine.api.channel.ChannelServiceFactory" %>
<%@ page import="java.util.logging.Logger" %>
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
    final Logger log = Logger.getLogger(getClass().getName());

    ChannelService channelService = ChannelServiceFactory.getChannelService();
    String channelId = request.getParameter("test-channel-id");
    String echo = request.getParameter("echo");

    ChannelMessage received = channelService.parseMessage(request);
    String parsedId = received.getClientId();
    String parsedMsg = received.getMessage();

    String errorMsg = "";
    String expectedClientId = "123abc";
    if (expectedClientId.equals(parsedId) == false) {
        errorMsg += String.format("::Expected parsedId=%s but got %s", expectedClientId, parsedId);
    }
    if (echo.equals(parsedMsg) == false) {
        errorMsg += String.format("::Expected parsedMsg=%s but got %s", echo, parsedMsg);
    }

    String msg;
    if (errorMsg.length() > 0) {
        msg = errorMsg;
        log.warning(String.format("Error parsing channel message: %s", errorMsg));
    } else {
        msg = echo;
    }

    String returnMsg = String.format("echo-from-server:%s", msg);
    ChannelMessage message = new ChannelMessage(channelId, returnMsg);
    channelService.sendMessage(message);

    log.info(String.format("Channel message sent: %s", message));
%>
<html>
<head>
</head>
<body>
</body>
</html>