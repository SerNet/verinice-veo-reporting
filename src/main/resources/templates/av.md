<#import "/libs/commons.ftlh" as com>

<#function resolve uri>
  <#local parts = uri?split("/")>
  <#local type = parts[parts?size-2]>
  <#local id = parts[parts?size-1]>

  <#local lookup = {
    "persons" : persons,
    "processes" : processes,
    "scopes" : scopes
  }/>

  <#local coll = lookup[type]! />
  <#local filteredColl = coll?filter(s -> s.id == id)>
  <#if (filteredColl?size == 0)>
    <#stop "Cannot resolve ${uri}, ${type} with id ${id} not found">
  </#if>
  <#return filteredColl?first>
</#function>

<#function rel object name>
  <#local links = object.getLinks(name)!>
  <#if (links!?size > 0)>
    <#return resolve(links?first.target.targetUri)>
  </#if>
</#function>

<#assign processesInScope = scope.members?filter(m -> m.targetUri?contains('/processes/'))?map(m -> resolve(m.targetUri))?filter(p ->p.subType?values?seq_contains('PRO_DataProcessing'))>

<style>
<#include "styles/default.css">
@page {
  size : A4 landscape;
}
</style>

<h1>${bundle.title}</h1>

<#assign management=rel(scope, 'scope_management')!>
<#assign dataProtectionOfficer=rel(scope, 'scope_dataProtectionOfficer')!>

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
<#assign controller=rel(process, 'process_controller')!>
<#assign managementController=(rel(controller, 'scope_management'))!>
<#assign dataProtectionOfficerController=(rel(controller, 'scope_dataProtectionOfficer'))!>
| ${process.name} | ${process.description!} | ${controller.name!}<br/>${controller.scope_address_address1!}<br/>${controller.scope_address_postcode!} ${controller.scope_address_city!}| ${managementController.name!}<br/>${managementController.person_contactInformation_office!}<br/>${managementController.person_contactInformation_email!}| ${dataProtectionOfficerController.name!}<br/>${dataProtectionOfficerController.person_contactInformation_office!}<br/>${dataProtectionOfficerController.person_contactInformation_email!} |
</#list>