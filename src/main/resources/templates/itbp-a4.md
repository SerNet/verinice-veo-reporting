<#import "/libs/commons.md" as com>
<#import "/libs/itbp-commons.md" as icom>

<#assign table = com.table
         def = com.def
         multiline = com.multiline
         groupBySubType = com.groupBySubType
         sortModules = icom.sortModules
         heading = com.heading
         title = icom.title
         controlTitle = icom.controlTitle
         riStatusColors = icom.riStatusColors/>

<style>
<#include "styles/default.css">
h1, h2, h3, h4, h5, h6 {
  page-break-after: avoid;
}

td {
  vertical-align: top;
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

.nobreak {
  page-break-inside: avoid;
}
</style>

<#assign scope = informationDomain/>
<#assign domain=domains?filter(it->it.name == 'IT-Grundschutz')?filter(it->scope.domains?keys?seq_contains(it.id))?sort_by("createdAt")?last />
<#assign institutions=scope.scopes?filter(it->it.hasSubType('SCP_Institution')) />

<#assign elementSubTypeGroups = groupBySubType(scope.members, 'process', domain)
+ groupBySubType(scope.members, 'asset', domain) />

<bookmarks>
  <bookmark name="${bundle.toc}" href="#toc"/>
  <bookmark name="${bundle.main_page}" href="#main_page"/>
  <bookmark name="${bundle.scope_SCP_InformationDomain_singular}" href="#information_domain"/>
  <#list elementSubTypeGroups as group>
    <bookmark name="${group.subTypePlural}" href="#${group.elementType}_${group.subType}">
    <#list group.elements as element>
      <bookmark name="${title(element)}" href="#${group.elementType}_${group.subType}_${element?counter}"/>
    </#list>
    </bookmark>
  </#list>
</bookmarks>


<div class="footer-left">
  <table>
    <tr>
      <td>${bundle.scope_SCP_InformationDomain_singular}: </td>
      <td>${scope.name}</td>
    </tr>
    <tr>
      <td>${bundle.creation_date}: </td>
      <td>${.now?date}</td>
    </tr>
  </table>
</div>

<div class="cover">
<h1><@multiline bundle.title/></h1>
<p>powered by verinice</p>
</div>


# ${bundle.toc} {#toc}
<#macro tocitem level target text>
  <tr class="level${level}">
    <td>
      <a title="${bundle('jumpto', text)}" href="#${target}">${text}</a>
    </td>
    <td>
      <span href="#${target}"/>
    </td>
  </tr>
</#macro>

<table class="toc">
<tbody>
  <@tocitem 1 "main_page" "1. ${bundle.main_page}" />
  <@tocitem 1 "information_domain" "2. ${bundle.scope_SCP_InformationDomain_singular}" />
  <#list elementSubTypeGroups as group>
      <@tocitem 1 "${group.elementType}_${group.subType}" "${group?counter+2}. ${group.subTypePlural}"/>
      <#list group.elements as element>
          <@tocitem 2 "${group.elementType}_${group.subType}_${element?counter}" "${element?counter}. ${title(element)}"/>
      </#list>
  </#list>
</tbody>
</table>

# ${bundle.main_page} {#main_page}

<div class="main_page">

<#if institutions?has_content>

<#list institutions as institution>
    <@table bundle.scope_SCP_Institution_singular,
    institution,
    ['name',
    'scope_address_address1',
    {'scope_address_postcode, scope_address_city' : 'scope_address_postcode scope_address_city'},
    'scope_contactInformation_phone',
    'scope_contactInformation_email',
    'scope_contactInformation_website'
    ]/>
</#list>
<#else>
${bundle.no_institutions}
</#if>

<@table bundle.scope_SCP_InformationDomain_singular,
scope,
['name',
'description',
'scope_protection_approach',
'status'
],
domain/>

</div>

<#function sortCIs cis>
  <#assign sortedModules = sortModules(cis?map(it->it.control))>
  <#return sortedModules?map(it->cis?filter(ci->ci.control._self == it._self)?first)>
</#function>

<#macro moduleview targetObject nestingLevel>

<#assign moduleControlImplementations = sortCIs(targetObject.controlImplementations)>

<#if moduleControlImplementations?has_content>

<@heading bundle.control_CTL_Module_plural nestingLevel+1 />

<#list moduleControlImplementations as moduleControlImplementation>
<div class="nobreak">

<@heading title(moduleControlImplementation.control) nestingLevel+2 />

<@def bundle.description moduleControlImplementation.description true/>

<@def bundle.responsible, (moduleControlImplementation.responsible.name)!, true/>

<#assign moduleRequirementImplementations =
  sortCIs(
    targetObject.requirementImplementations
      ?filter(it->moduleControlImplementation.control.parts
        ?map(it->it._self)
        ?seq_contains(it.control._self)
      )
  )>

<#list moduleRequirementImplementations as ri>

<div class="nobreak" style="border-left: 1mm solid ${riStatusColors[ri.status].color}; padding-left: 0.6cm;">

<@heading controlTitle(ri.control) nestingLevel+3/>

<@def bundle.implementation_status bundle[ri.status] true />

<@def bundle.implementationStatement ri.implementationStatement true/>

<@def bundle.responsible, (ri.responsible.name)!, true/>

</div>

</#list>

</div>
</#list>

</#if>
</#macro>

# ${title(scope)} {#information_domain}

<@moduleview scope 1/>

<#list elementSubTypeGroups as group>

# ${group.subTypePlural} {#${group.elementType}_${group.subType}}

<#list group.elements as element>

## ${title(element)} {#${group.elementType}_${group.subType}_${element?counter}}

<@moduleview element 2/>

</#list>
<div class="pagebreak"></div>
</#list>