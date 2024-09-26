<#import "/libs/commons.md" as com>
<#import "/libs/dp-risk.md" as dpRisk>

<#assign table = com.table
         def = com.def/>

<style>
<#include "styles/default.css">
h1, h2, h3, h4 {
  page-break-after: avoid;
}

</style>


<#assign scope = organization/>

<#macro address name address1 postcode city>
${name}  
<#if address1?has_content>
${address1}  
</#if>
<#if postcode?has_content>
${postcode} 
</#if>
<#if city?has_content>
${city}
</#if>
</#macro>


<h1>${bundle.title}<br/>${scope.name}</h1>

## ${bundle.address}

<@address scope.name, scope.scope_address_address1!, scope.scope_address_postcode!, scope.scope_address_city! />

## ${bundle.contact_info}

<@def bundle.scope_contactInformation_phone scope.scope_contactInformation_phone />

<@def bundle.scope_contactInformation_email scope.scope_contactInformation_email />
 
<@def bundle.scope_contactInformation_website scope.scope_contactInformation_website />

## ${bundle.sector_info}

<@def bundle.scope_specificInformation_sector scope.scope_specificInformation_sector />
<@def bundle.scope_specificInformation_subSector scope.scope_specificInformation_subSector />

## ${bundle.competent_authority}

<@address scope.scope_competentAuthority_name, scope.scope_competentAuthority_address1!, scope.scope_competentAuthority_postcode!, scope.scope_competentAuthority_city! />

## ${bundle.registration_info} 

<@def bundle.scope_registrationDetails_dateInitial, (scope.scope_registrationDetails_dateInitial?date.iso)! />
