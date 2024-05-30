<#import "/libs/commons.md" as com>
<#import "/libs/itbp-commons.md" as icom>

<#assign table = com.table
        row = com.row
         def = com.def
         status = com.status
         multiline = com.multiline
         groupBySubType = com.groupBySubType
         title = icom.title />


<style>
<#include "styles/default.css">
h1, h2, h3, h4 {
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

table.used_modules th:first-child, table.used_modules td:first-child {
  width: 2cm;
}

table.used_modules th:last-child, table.used_modules td:last-child {
  width: 5cm;
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
 + groupBySubType(scope.members, 'asset', domain)
 + groupBySubType(scope.members, 'scope', domain)?filter(g -> g.subType == "SCP_ExternalServiceProvider") />

<bookmarks>
  <bookmark name="${bundle.toc}" href="#toc"/>
  <bookmark name="${bundle.main_page}" href="#main_page"/>
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
  <#list elementSubTypeGroups as group>
    <@tocitem 1 "${group.elementType}_${group.subType}" "${group?counter+1}. ${group.subTypePlural}"/>
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

<#list elementSubTypeGroups as group>

# ${group.subTypePlural} {#${group.elementType}_${group.subType}}

<#list group.elements as element>

## ${title(element)} {#${group.elementType}_${group.subType}_${element?counter}}

|||
|:------------|:-----|
<@row element, 'abbreviation'/>
<@row element, 'name'/>
<@row element, 'description'/>
<#if group.elementType == "process">
    <@row element, 'process_details_processType'/>
<#elseif group.elementType == "asset">
    <@row element, 'asset_bpDetails_platform'/>
    <@row element, 'asset_details_number'/>
<#elseif group.elementType == "scope">
    <@row element, 'scope_address_address1'/>
    <@row element, {'scope_address_postcode, scope_address_city' : 'scope_address_postcode scope_address_city'}/>
    <@row element, 'scope_contactInformation_phone'/>
    <@row element, 'scope_contactInformation_email'/>
    <@row element, 'scope_contactInformation_website'/>
</#if>
| ${bundle.status} | ${status(element, domain)} |

</#list>
<div class="pagebreak"></div>
</#list>
