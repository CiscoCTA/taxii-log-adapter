<#--
   Copyright 2015 Cisco Systems

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->
<t:Poll_Request xmlns:t="http://taxii.mitre.org/messages/taxii_xml_binding-1.1" message_id="123" collection_name="${collection}">
  <t:Exclusive_Begin_Timestamp>${begin}</t:Exclusive_Begin_Timestamp>
  <t:Poll_Parameters allow_asynch="false">
    <t:Response_Type>FULL</t:Response_Type>
  </t:Poll_Parameters>
</t:Poll_Request>