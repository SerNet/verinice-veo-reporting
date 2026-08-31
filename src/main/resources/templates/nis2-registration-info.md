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


<#assign scope = target/>

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

<h1>${bundle.title} - ${scope.name}</h1>

## ${bundle.nis2_org_name}

<@address scope "name", scope.scope_address_address1!, scope.scope_address_postcode!, scope.scope_address_city!, scope.scope_address_country!, scope.scope_address_phone!, scope.scope_address_email! />

## ${bundle.nis2_contact}

<@address scope "scope_nis2Contact_name", scope.scope_nis2Contact_address1!, scope.scope_nis2Contact_postcode!, scope.scope_nis2Contact_city!, scope.scope_nis2Contact_country!, scope.scope_nis2Contact_phone!, scope.scope_nis2Contact_email! />


<#assign contactPersons = scope.findLinked('scope_nis2ContactPerson') />

<#if contactPersons?has_content>
## ${bundle.scope_nis2ContactPerson}

<#list contactPersons as contactPerson>

${bundle.person_generalInformation_familyName}, ${bundle.person_generalInformation_givenName}
: ${contactPerson.person_generalInformation_familyName!}, ${contactPerson.person_generalInformation_givenName!}

<#assign email=contactPerson.person_contactInformation_email! />
<#assign mobile=contactPerson.person_contactInformation_mobile! />
<#assign office=contactPerson.person_contactInformation_office! />

<#if email?has_content || mobile?has_content || office?has_content>
${bundle.contact_info}
<#if email?has_content>: ${email}</#if>
<#if mobile?has_content>: ${mobile}</#if>
<#if office?has_content>: ${office}</#if>
</#if>

</#list>
</#if>

## ${bundle.sector_info}

<@def bundle.scope_specificInformation_sector scope.scope_specificInformation_sector />

<@def bundle.scope_specificInformation_subSector scope.scope_specificInformation_subSector />

<@def bundle.scope_specificInformation_typeOfEntity scope.scope_specificInformation_typeOfEntity />

<@def bundle.scope_specificInformation_furtherSectors scope.scope_specificInformation_furtherSectors />

<@def bundle.scope_specificInformation_identifiedAs, (bundle[scope.scope_specificInformation_identifiedAs])!, true />

<@def bundle.scope_specificInformation_operatorCriticalSystem scope.scope_specificInformation_operatorCriticalSystem />

<#if scope.scope_specificInformation_operatorCriticalSystem!false && domain.name=='NIS2'>
<#-- only show this for the German domain -->

<@def bundle.scope_criticalSystem_institutionId scope.scope_criticalSystem_institutionId />

<@def bundle.scope_criticalSystem_criticalComponents scope.scope_criticalSystem_criticalComponents />

</#if>

<@def bundle.scope_specificInformation_ipRanges scope.scope_specificInformation_ipRanges />

<@def bundle.scope_specificInformation_listOfMemberStates scope.scope_specificInformation_listOfMemberStates />

## ${bundle.competent_authority}

<@address scope "scope_competentAuthority_name", scope.scope_competentAuthority_address1!, scope.scope_competentAuthority_postcode!, scope.scope_competentAuthority_city!, scope.scope_competentAuthority_country!, scope.scope_competentAuthority_phone!, scope.scope_competentAuthority_email! />

## ${bundle.registration_info} 

<@def bundle.scope_registrationDetails_dateInitial, (scope.scope_registrationDetails_dateInitial?date.iso)! />
