<#import "/libs/commons.md" as com>
<#import "/libs/dp-risk.md" as dpRisk>

<#assign multiline = com.multiline
         def = com.def/>

<style>
<@com.defaultStyles />
h1, h2, h3, h4 {
  page-break-after: avoid;
}

body  {
  font-size: 90%;
}
</style>


<#assign scope = organization/>

<#macro address scope nameProperty address1 postcode city country phone email>
<#local name=scope[nameProperty]!>
<#if name?has_content>
${name}  
<#else>
<span style="color:#cd1719">${bundle('missing_property', bundle[nameProperty])}</span>  
</#if>
<#if address1?has_content>
${address1}  
</#if>
<#if postcode?has_content>
${postcode} 
</#if>
<#if city?has_content>
${city}  
</#if>
<#if country?has_content>
${country}  
</#if>
<#if phone?has_content>
  
${bundle.phone} ${phone}    
</#if>
<#if email?has_content>
  
${bundle.email} ${email}  
</#if>
</#macro>

<h1><@multiline bundle.title /></h1>

## ${bundle.address}

<@address scope "scope_nis2Contact_name", scope.scope_nis2Contact_address1!, scope.scope_nis2Contact_postcode!, scope.scope_nis2Contact_city!, scope.scope_nis2Contact_country!, scope.scope_nis2Contact_phone!, scope.scope_nis2Contact_email! />

## ${bundle.sector_info}

<@def bundle.scope_specificInformation_sector scope.scope_specificInformation_sector />

<@def bundle.scope_specificInformation_subSector scope.scope_specificInformation_subSector />

<@def bundle.scope_specificInformation_typeOfEntity scope.scope_specificInformation_typeOfEntity />

<@def bundle.scope_specificInformation_furtherSectors scope.scope_specificInformation_furtherSectors />

<@def bundle.scope_specificInformation_identifiedAs bundle[scope.scope_specificInformation_identifiedAs]! />

<@def bundle.scope_specificInformation_operatorCriticalSystem scope.scope_specificInformation_operatorCriticalSystem />

<@def bundle.scope_specificInformation_ipRanges scope.scope_specificInformation_ipRanges />

<@def bundle.scope_specificInformation_listOfMemberStates scope.scope_specificInformation_listOfMemberStates />

## ${bundle.competent_authority}

<@address scope "scope_competentAuthority_name", scope.scope_competentAuthority_address1!, scope.scope_competentAuthority_postcode!, scope.scope_competentAuthority_city!, scope.scope_competentAuthority_country!, scope.scope_competentAuthority_phone!, scope.scope_competentAuthority_email! />

## ${bundle.registration_info} 

<@def bundle.scope_registrationDetails_dateInitial, (scope.scope_registrationDetails_dateInitial?date.iso)! />
