<%@ include file="/include-internal.jsp" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%--
  ~ Copyright 2000-2012 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>


<jsp:useBean id="constants" class="com.google.appengine.tck.teamcity.ReportsConstants" />

<l:settingsGroup title="Google Cloud Endpoint Client Credentials">
    <tr>
        <th>Client ID:<l:star/></th>
        <td>
            <props:textProperty id="${constants.applicationClientId}" name="${constants.applicationClientId}" className="longField"/>
            <span class="error" id="error_${constants.applicationClientId}"></span>
            <span class="smallNote">Specify the application client id</span>
        </td>
    </tr>
    <tr>
        <th>Client secret:<l:star/></th>
        <td>
            <props:textProperty name="${constants.applicationClientSecret}" className="longField"/>
            <span class="error" id="error_${constants.applicationClientSecret}"></span>
            <span class="smallNote">Specify the application client secret</span>
        </td>
    </tr>
    <tr>
        <th>OAuth code:<l:star/></th>
        <td>
            <props:textProperty name="${constants.applicationOauthCode}" className="longField"/>
            <span class="error" id="error_${constants.applicationOauthCode}"></span>
            <input type="button" id="get-oauth-code" class="btn btn-primary" value="Get OAuth code" />
        </td>
    </tr>
</l:settingsGroup>

<script type="text/javascript">
    (function($) {
        const authorizationUrlPrefix = '${constants.authorizationUrlPrefix}';
        const applicationClientIdSelector = '#${constants.applicationClientId}'.replace( /(:|\.|\[|\])/g, '\\\$1' );

        $('#get-oauth-code').click(function() {
            window.open(
                authorizationUrlPrefix + $(applicationClientIdSelector).val(),
                'Get OAuth code',
                'width=600,height=600'
            );
        });
    })(jQuery);
</script>

