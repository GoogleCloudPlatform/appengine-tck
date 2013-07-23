<%@ page import="com.google.appengine.api.channel.ChannelMessage" %>
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
    String echo = request.getParameter("echo");

    channelService.sendMessage(new ChannelMessage(channelId, "echo-from-server:" + echo));
%>
<html>
<head>
</head>
<body>
</body>
</html>