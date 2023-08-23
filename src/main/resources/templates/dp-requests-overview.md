<#import "/libs/commons.md" as com>
<#import "/libs/dp-risk.md" as dpRisk>

<#assign table = com.table/>

<#assign requests = scope.getMembersWithType('document')?filter(p ->p.hasSubType('DOC_RequestDataSubject'))>

<style>
<#include "styles/default.css">
<#include "styles/default_landscape.css">
h1, h2, h3, h4 {
  page-break-after: avoid;
}

.main_page {
  page-break-after: always;
}

.main_page table th:first-child, .main_page table td:first-child {
  width: 8cm;
}

.fullwidth {
  width: 100%;
}
</style>

<div class="footer-left">
  <table>
    <tr>
      <td>${bundle.organization}: </td>
      <td>${scope.name}</td>
    </tr>
    <tr>
      <td>${bundle.creation_date}: </td>
      <td>${.now?date}</td>
    </tr>
  </table>
</div>

<div class="cover">
<h1>${bundle.title}</h1>
<p>powered by verinice</p>
</div>

# ${bundle.main_page} {#main_page}

<div class="main_page">

<@table bundle.controller_information,
  scope,
  ['name',
   'scope_address_address1',
   {'scope_address_postcode, scope_address_city' : 'scope_address_postcode scope_address_city'},
   'scope_contactInformation_phone',
   'scope_contactInformation_email',
   'scope_contactInformation_website'
  ]/>

<#assign management=scope.findFirstLinked('scope_management')! />
<#assign headOfDataProcessing=scope.findFirstLinked('scope_headOfDataProcessing')! />

| ${bundle.representation}  ||
|:---|:---|
| ${bundle.scope_management} | ${management.name!} |
| ${bundle.scope_headOfDataProcessing}  |  ${headOfDataProcessing.name!} |


<#assign dataProtectionOfficer=scope.findFirstLinked('scope_dataProtectionOfficer')! />

<#if dataProtectionOfficer?has_content>
<@table bundle.data_protection_officer,
  dataProtectionOfficer,
  [
   'name',
   'person_contactInformation_office',
   'person_contactInformation_email'
  ]/>
</#if>>
</div>

<div class="pagebreak"></div>

| ${bundle.request} | ${bundle.document_dataSubjectRight_rightType} | ${bundle.document_dataSubjectRight_dateOfReceipt} | ${bundle.date_of_response} | ${bundle.result_of_request} |
|:---|:---|:---|:---|:---|
<#list requests as request>
| ${request.name} | ${(request.document_dataSubjectRight_rightType?map(item->bundle[item])?join(', '))!} | ${(request.document_dataSubjectRight_dateOfReceipt?date.iso)!} | ${(request.document_decisionRequest_requestAnswered?date.iso)!} | ${bundle.document_decisionRequest_requestRejection}<br/>${(request.document_decisionRequest_requestRejection?string(bundle.yes, bundle.no))!}<br/><br/>${bundle.document_decisionRequest_negativeMessage}<br/>${(request.document_decisionRequest_negativeMessage?string(bundle.yes, bundle.no))!}<br/><br/>${bundle.document_decisionRequest_grantingOfRequest}<br/>${(request.document_decisionRequest_grantingOfRequest?string(bundle.yes, bundle.no))!} |
</#list>
{.table .simple .fullwidth}