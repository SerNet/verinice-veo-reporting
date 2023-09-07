<#import "/libs/commons.md" as com>

<#assign table = com.table,
         def = com.def />
         
<style>
<#include "styles/default.css">
h1, h2, h3, h4 {
  page-break-after: avoid;
}

.main_page {
  page-break-after: always;
}

.main_page table th:first-child, .main_page table td:first-child {
  width: 8cm;
}
</style>

<#-- Maybe add a link to the document OS? -->
<#assign scope=scopes?filter(it->it.hasSubType('SCP_ResponsibleBody'))
                     ?filter(it->it.getMembersWithType('document')?filter(it->it.hasSubType('DOC_RequestDataSubject'))?map(it->it._self)?seq_contains(request._self))
                     ?first! />

<div class="footer-left">
  <table>
    <tr>
      <td>${bundle.organization}: </td>
      <td>${(scope.name)!}</td>
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

<#-- FIXME VEO-619/VEO-1175: maybe pass domain into report? -->
<#assign domain=domains?filter(it->it.name == 'DS-GVO')?filter(it->request.domains?keys?seq_contains(it.id))?sort_by("createdAt")?last />


<#if scope?has_content>

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
</#if>

</div>

<div class="pagebreak"></div>
</#if>

# ${bundle.general_information}

<@def bundle.name_of_request request.name />

<@def bundle.description request.description />

<@def bundle.status bundle['document_DOC_RequestDataSubject_status_'+request.domains[domain.id].status] />

<@def bundle.document_dataSubjectRight_rightType, (request.document_dataSubjectRight_rightType?map(item->bundle[item])?join(', '))! />

<@def bundle.document_dataSubjectRight_otherRightType request.document_dataSubjectRight_otherRightType />

<@def bundle.document_dataSubjectRight_dateOfReceipt request.document_dataSubjectRight_dateOfReceipt />

<@def bundle.document_dataSubjectRight_deadline request.document_dataSubjectRight_deadline />

<@def bundle.document_dataSubjectRight_receiptConfirmation request.document_dataSubjectRight_receiptConfirmation />

<@def bundle.document_dataSubjectRight_extensionOfDeadline request.document_dataSubjectRight_extensionOfDeadline />

<#if request.document_dataSubjectRight_extensionOfDeadline!false>

<@def bundle.document_dataSubjectRight_extendedUntil request.document_dataSubjectRight_extendedUntil />

<@def bundle.document_dataSubjectRight_informationAboutExtensionOfDeadline request.document_dataSubjectRight_informationAboutExtensionOfDeadline />

<@def bundle.document_dataSubjectRight_dateOfInformationAboutExtension request.document_dataSubjectRight_dateOfInformationAboutExtension />

<@def bundle.document_dataSubjectRight_reasonForExtensionOfDeadline request.document_dataSubjectRight_reasonForExtensionOfDeadline />

</#if>>

<@def bundle.document_dataSubjectRight_contentRequest request.document_dataSubjectRight_contentRequest />

<@def bundle.document_dataSubjectRight_formOfRequest request.document_dataSubjectRight_formOfRequest />

<#assign editors=request.findLinked('document_editedBy')! />

<@def bundle.document_editedBy, (editors?map(item->item.name)?join(", "))! />

# ${bundle.document_contactDetailsOfRequester}

<@def "${bundle.requester_surname}, ${bundle.document_contactDetailsOfRequester_givenName}" "${request.document_contactDetailsOfRequester_familyName!} ${request.document_contactDetailsOfRequester_givenName!}" />

<@def bundle.document_contactDetailsOfRequester_address request.document_contactDetailsOfRequester_address />

<@def "${bundle.document_contactDetailsOfRequester_postcode}, ${bundle.document_contactDetailsOfRequester_city}" "${request.document_contactDetailsOfRequester_postcode!} ${request.document_contactDetailsOfRequester_city!}" />

<@def bundle.document_contactDetailsOfRequester_country request.document_contactDetailsOfRequester_country />

<@def bundle.document_contactDetailsOfRequester_phone request.document_contactDetailsOfRequester_phone />

<@def bundle.document_contactDetailsOfRequester_mobile request.document_contactDetailsOfRequester_mobile />

<@def bundle.document_contactDetailsOfRequester_email request.document_contactDetailsOfRequester_email />

# ${bundle.assignment_to_category_of_data_subject}

<@def bundle.document_dataSubjectRight_categoriesDataSubjects, (request.document_dataSubjectRight_categoriesDataSubjects?map(item->bundle[item])?join(', '))! />

<@def bundle.document_dataSubjectRight_otherDataSubjects request.document_dataSubjectRight_otherDataSubjects />

<#assign concernedDataProcessingLinks=request.getLinks('document_dataProcessing')!>
<#if concernedDataProcessingLinks?has_content>
# ${bundle.document_dataProcessing}
<#list concernedDataProcessingLinks as concernedDataProcessingLink>
<#assign concernedDataProcessing=concernedDataProcessingLink.target />
## ${concernedDataProcessing.name}
</#list>
</#if>

# ${bundle.document_identify}

<@def bundle.document_identify_dataSubject request.document_identify_dataSubject />

<@def bundle.document_identify_identityChecked request.document_identify_identityChecked />

<@def bundle.document_identify_methodOfIdentification, (request.document_identify_methodOfIdentification?map(item->bundle[item])?join(', '))! />

<@def bundle.document_identify_other request.document_identify_other />

<@def bundle.document_identityVerifiedBy, (request.document_identityVerifiedBy?map(item->item.name)?join(', '))! />

# ${bundle.review_and_decision}

<@def bundle.document_requestVerifiedBy, (request.document_requestVerifiedBy?map(item->item.name)?join(', '))! />

<@def bundle.document_decisionRequest_dateOfRequestVerification, (request.document_decisionRequest_dateOfRequestVerification?date.iso)! />

<@def bundle.document_decisionRequest_requestRejection request.document_decisionRequest_requestRejection />

<@def bundle.document_decisionRequest_rejectionReason request.document_decisionRequest_rejectionReason />

<@def bundle.document_decisionRequest_negativeMessage request.document_decisionRequest_negativeMessage />

<@def bundle.document_decisionRequest_negativeMessageReason request.document_decisionRequest_negativeMessageReason />

<@def bundle.document_decisionRequest_grantingOfRequest request.document_decisionRequest_grantingOfRequest />

<@def bundle.document_decisionRequest_grantingReason request.document_decisionRequest_grantingReason />

<@def bundle.document_decisionRequest_grantingOfRequest request.document_decisionRequest_grantingOfRequest />

<#assign approvalEffected=request.document_decisionRequest_approvaleffected!false>

<@def bundle.document_decisionRequest_approvaleffected approvalEffected />

<#if approvalEffected>
<@def bundle.document_decisionApprovalBy, (request.document_decisionApprovalBy?map(item->item.name)?join(', '))! />
</#if>

<@def bundle.document_decisionRequest_requestAnswered, (request.document_decisionRequest_requestAnswered?date.iso)! />

<@def bundle.document_decisionRequest_FormOfResponse, (request.document_decisionRequest_FormOfResponse?map(item->bundle[item])?join(', '))! /> <#-- sic! -->

<@def bundle.document_decisionRequest_otherForm request.document_decisionRequest_otherForm />