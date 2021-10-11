<#assign processings = scope.getMembers()?filter(m -> m.type == 'process')?filter(p ->p.subType?values?seq_contains('PRO_DataProcessing'))?filter(p-> p.process_processing_asProcessor!false)>

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
.processinfo h3 {
  page-break-after: avoid;
}
.processinfo dl {
  padding-left: 4mm;
}

.transmissioninfo {
  page-break-inside: avoid;
}

.transmissioninfo dl {
  padding-bottom: 2mm;
}
</style>

<bookmarks>
  <bookmark name="${bundle.scope_name}" href="#main_page"/>
  <bookmark name="${bundle.processing_activities}" href="#processing_activities">
<#list processings as processing>
    <bookmark name="${processing.name}" href="#processing_${processing?counter}"/>
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

<#list processings as processing>

<div class="processinfo">

## ${processing.name} {#processing_${processing?counter}}

${processing.description!}


### ${bundle.controllers}

<#assign controllers=processing.getLinked('process_controller')!>
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

<#macro def term definition="" >
${term}
<#if definition?has_content>
: ${definition}
<#else>
: &nbsp;
</#if>
</#macro>

<#macro transmissions_section title transmissions recipientLinkType thirdCountryFilterAttribute thirdCountryNameAttribute thirdCountryGuaranteesAttribute>

<#if transmissions?has_content>

### ${title}

<#list transmissions as transmission>

<div class="transmissioninfo">

#### ${transmission.name}

<#local recipientLinks=(transmission.getLinks(recipientLinkType))![] />
<#local recipientLinksThirdCountry=recipientLinks?filter(l->l[thirdCountryFilterAttribute]!false) />

<#if recipientLinksThirdCountry?has_content>

<#list recipientLinksThirdCountry as recipientLinkThirdCountry>
<div class="recipientinfo">

<@def bundle.name recipientLinkThirdCountry.getTarget().name />

<@def bundle[thirdCountryNameAttribute] recipientLinkThirdCountry[thirdCountryNameAttribute] />

<@def bundle[thirdCountryGuaranteesAttribute] recipientLinkThirdCountry[thirdCountryGuaranteesAttribute] />

</div>
</#list>
</#if>
</div>
</#list>
</#if>
</#macro>

<#assign transmissions=processing.getLinked('process_dataTransmission')! />

<#assign relevantTransmissionsExternal=transmissions?filter(t-> 
  (t.getLinks('process_externalRecipient')![])?filter(l->l.process_externalRecipient_thirdCountryProcessing!false)?has_content) />

<#assign relevantTransmissionsProcessor=transmissions?filter(t-> 
  (t.getLinks('process_processor')![])?filter(l->l.process_processor_thirdCountryProcessing!false)?has_content) />

<@transmissions_section 
  "Datenübertragungen an externe Empfänger in Drittstaaten"
  relevantTransmissionsExternal
  'process_externalRecipient'
  'process_externalRecipient_thirdCountryProcessing'
  'process_externalRecipient_thirdCountryName'
  'process_externalRecipient_thirdCountryGuarantees' />

<@transmissions_section 
  "Datenübertragungen an Auftragsverarbeiter in Drittstaaten"
  relevantTransmissionsProcessor
  'process_processor'
  'process_processor_thirdCountryProcessing'
  'process_processor_thirdCountryName'
  'process_processor_thirdCountryGuarantees' />

</#list>
</div>
</#list>