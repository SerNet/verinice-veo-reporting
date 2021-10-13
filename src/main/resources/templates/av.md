<#assign processesInScope = scope.getMembers()?filter(m -> m.type == 'process')?filter(p ->p.subType?values?seq_contains('PRO_DataProcessing'))?filter(p->p.process_processing_asProcessor == true)>

<#assign management=scope.findFirstLinked('scope_management')!>
<#assign dataProtectionOfficer=scope.findFirstLinked('scope_dataProtectionOfficer')!>

<style>
<#include "styles/default.css">
.scopeinfo {
  padding-top: 2mm;
}
dt {
  font-weight: 600;
}
.scopeinfo dd {
  padding-bottom: 2mm;
}

.processinfo dl {
  padding-left: 4mm;
}
</style>

<bookmarks>
  <bookmark name="${bundle.scope_name}" href="#main_page"/>
  <bookmark name="${bundle.processing_activities}" href="#processing_activities">
<#list processesInScope as process>
    <bookmark name="${process.name}" href="#process_${process?counter}"/>
</#list>
  </bookmark>
</bookmarks>


<div class="cover">
<h1>${bundle.title}</h1>
<p>powered by verinice</p>
</div>

# ${bundle.scope_name}{#main_page}

<div class="scopeinfo">

${bundle.name}
: ${scope.name}

${bundle.address}
: ${scope.scope_address_address1!}  
${scope.scope_address_postcode!} ${scope.scope_address_city!}

${bundle.scope_contactInformation_phone}
: ${scope.scope_contactInformation_phone!}

${bundle.scope_contactInformation_fax}
: ${scope.scope_contactInformation_fax!}

${bundle.scope_contactInformation_email}
: ${scope.scope_contactInformation_email!}

${bundle.scope_management}
: ${management.person_generalInformation_givenName!} ${management.person_generalInformation_familyName!}

${bundle.scope_dataProtectionOfficer}
: ${dataProtectionOfficer.name!}

</div>

<div class="pagebreak"/>

# ${bundle.processing_activities}{#processing_activities}

<#list processesInScope?filter(p-> p.process_processing_asProcessor!false) as process>

<div class="processinfo">

## ${process.name} {#process_${process?counter}}

${process.description!}


### ${bundle.controllers}

<#assign controllers=process.getLinked('process_controller')!>
<#list controllers as controller>
<#assign managementController=(controller.findFirstLinked('scope_management'))!>
<#assign dataProtectionOfficerController=(controller.findFirstLinked('scope_dataProtectionOfficer'))!>

<div class="controllerinfo">

#### ${controller.name!}

${bundle.address}
: ${controller.scope_address_address1!}  
${controller.scope_address_postcode!} ${controller.scope_address_city!}

${bundle.controller_management}
: ${[managementController.name!,managementController.person_contactInformation_office!,managementController.person_contactInformation_email!]?filter(v->v?has_content)?join("  \n")}

${bundle.scope_dataProtectionOfficer}
: ${[dataProtectionOfficerController.name!,dataProtectionOfficerController.person_contactInformation_office!,dataProtectionOfficerController.person_contactInformation_email!]?filter(v->v?has_content)?join("  \n")}

</div>
</#list>
</div>
</#list>