<#assign processesInScope = scope.getMembers()?filter(m -> m.type == 'process')?filter(p ->p.subType?values?seq_contains('PRO_DataProcessing'))>

<style>
<#include "styles/default.css">
@page {
  size : A4 landscape;
}
</style>

<h1>${bundle.title}</h1>

<#assign management=scope.findFirstLinked('scope_management')!>
<#assign dataProtectionOfficer=scope.findFirstLinked('scope_dataProtectionOfficer')!>

|   ||
|:---|:---|
| ${bundle.organization} | ${scope.name} |
| ${bundle.address} | ${scope.scope_address_address1!}, ${scope.scope_address_postcode!} ${scope.scope_address_city!} |
| ${bundle.scope_contactInformation_phone} | ${scope.scope_contactInformation_phone!} |
| ${bundle.scope_contactInformation_fax} | ${scope.scope_contactInformation_fax!} |
| ${bundle.e_mail_contact} | ${scope.scope_contactInformation_email!} |
| ${bundle.management} | ${management.person_generalInformation_givenName!} ${management.person_generalInformation_familyName!} |
| ${bundle.scope_dataProtectionOfficer} | ${dataProtectionOfficer.name!} |

| ${bundle.processing}  | ${bundle.description} | ${bundle.controller_details} | ${bundle.controller_management} | ${bundle.scope_dataProtectionOfficer} |
|:---|:---|:---|:---|:---|
<#list processesInScope?filter(p-> p.process_processing_asProcessor!false) as process>
<#assign controller=process.findFirstLinked('process_controller')!>
<#assign managementController=(controller.findFirstLinked('scope_management'))!>
<#assign dataProtectionOfficerController=(controller.findFirstLinked('scope_dataProtectionOfficer'))!>
| ${process.name} | ${process.description!} | ${controller.name!}<br/>${controller.scope_address_address1!}<br/>${controller.scope_address_postcode!} ${controller.scope_address_city!}| ${managementController.name!}<br/>${managementController.person_contactInformation_office!}<br/>${managementController.person_contactInformation_email!}| ${dataProtectionOfficerController.name!}<br/>${dataProtectionOfficerController.person_contactInformation_office!}<br/>${dataProtectionOfficerController.person_contactInformation_email!} |
</#list>