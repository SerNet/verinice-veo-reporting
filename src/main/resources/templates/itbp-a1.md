<#import "/libs/commons.md" as com>

<#assign table = com.table
        row = com.row
         def = com.def
         groupBySubType = com.groupBySubType />


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

<#assign assetTypes= ['AST_Application', 'AST_IT-System', 'AST_Room', 'AST_Network', 'AST_ICS-System', 'AST_Device']>
<#assign assetsInScope = scope.getMembersWithType('asset')/>
<#assign assetsBySubType = groupBySubType(assetsInScope, assetTypes)/>

<#function findBySubType elements subType domain>
<#return elements?filter(it -> it.domains[domain.id].subType == subType)?sort_by('name_naturalized')>
</#function>

<#assign businessProcesses = findBySubType(scope.getMembersWithType('process'), 'PRO_BusinessProcess', domain)/>
<#assign externalContractors = findBySubType(scope.getMembersWithType('scope'), 'SCP_OutsourcingProvider', domain)/>


<#function title element>
<#if element.abbreviation?has_content>
  <#return "${element.abbreviation} ${element.name}">
  <#else>
  <#return element.name>
</#if>
</#function>

<bookmarks>
  <bookmark name="${bundle.toc}" href="#toc"/>
  <bookmark name="${bundle.main_page}" href="#main_page"/>
<#if businessProcesses?has_content>
  <bookmark name="${bundle.PRO_BusinessProcess}" href="#businessProcesses">
    <#list businessProcesses as process>
      <bookmark name="${title(process)}" href="#process_${process?counter}">
      </bookmark>
    </#list>
  </bookmark>    
</#if>    
<#if assetsInScope?has_content>
  <bookmark name="${bundle.assets}" href="#assets">
    <#list assetsBySubType as assetType, assetsWithType>
      <bookmark name="${bundle[assetType]}" href="#${assetType}">
        <#list assetsWithType as asset>
          <bookmark name="${title(asset)}" href="#asset_${assetType}_${asset?counter}">
          </bookmark>
        </#list>
      </bookmark>
    </#list>
  </bookmark>    
</#if>
<#if externalContractors?has_content>
  <bookmark name="${bundle.external_contractors}" href="#externalContractors">
    <#list externalContractors as externalContractor>
      <bookmark name="${title(externalContractor)}" href="#externalContractor_${externalContractor?counter}">
      </bookmark>
    </#list>
  </bookmark>    
</#if>
</bookmarks>


<div class="footer-left">
  <table>
    <tr>
      <td>${bundle.organization}: </td>
      <td>${scope.name}</td>
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
  <#assign level2counter = 2>
  <#if businessProcesses?has_content>
    <@tocitem 1 "businessProcesses" "${level2counter}. ${bundle.PRO_BusinessProcess}" />
    <#list businessProcesses as process>
      <@tocitem 2 "process_${process?counter}" "${process?counter}. ${title(process)}" />
     </#list>
    <#assign level2counter = 3>
  </#if>   
  <#if assetsInScope?has_content>
    <@tocitem 1 "assets" "${level2counter}. ${bundle.assets}" />
    <#list assetsBySubType as assetType, assetsWithType>
      <@tocitem 2 assetType "${assetType?counter}. ${bundle[assetType]}" />
      <#list assetsWithType as asset>
        <@tocitem 3 "asset_${assetType}_${asset?counter}" "${asset?counter}. ${title(asset)}" />
      </#list>
    </#list>
    <#assign level2counter = 4>
  </#if>
  <#if externalContractors?has_content>
    <@tocitem 1 "externalContractors" "${level2counter}. ${bundle.external_contractors}" />
    <#list externalContractors as externalContractor>
      <@tocitem 2 "externalContractor_${externalContractor?counter}" "${externalContractor?counter}. ${title(externalContractor)}" />
     </#list>
    <#assign level2counter = 3>
  </#if>   
</tbody>
</table>

# ${bundle.main_page} {#main_page}

<div class="main_page">

<@table bundle.information_domain_information,
  scope,
  ['name',
   'scope_address_address1',
   {'scope_address_postcode, scope_address_city' : 'scope_address_postcode scope_address_city'},
   'scope_contactInformation_phone',
   'scope_contactInformation_email',
   'scope_contactInformation_website'
  ]/>

<div class="pagebreak"></div>

<#function status element>
<#return bundle[element.type+'_'+element.domains[domain.id].subType+'_status_'+element.domains[domain.id].status] />
</#function>


<#if businessProcesses?has_content>
# ${bundle.PRO_BusinessProcess} {#businessProcesses}

<#list businessProcesses as process>
## ${title(process)} {#process_${process?counter}}

|||
|:------------|:-----|
<@row process, 'abbreviation'/>
<@row process, 'name'/>
<@row process, 'description'/>
| ${bundle.process_details_processType} | ${(bundle[process.process_details_processType])!}
| ${bundle.status} | ${status(process)} |


</#list>
<div class="pagebreak"></div>
</#if>

<#if assetsInScope?has_content>
# ${bundle.assets} {#assets}
<#list assetsBySubType as assetType, assetsWithType>

## ${bundle[assetType]} {#${assetType}}

<#list assetsWithType as asset>
### ${title(asset)} {#asset_${assetType}_${asset?counter}}

|||
|:------------|:-----|
<@row asset, 'abbreviation'/>
<@row asset, 'name'/>
<@row asset, 'description'/>
<@row asset, 'asset_details_platform'/>
<@row asset, 'asset_details_number'/>
| ${bundle.status} | ${status(asset)} |

</#list>
<div class="pagebreak"></div>
</#list>
</#if>

<#if externalContractors?has_content>
# ${bundle.external_contractors} {#externalContractors}

<#list externalContractors as externalContractor>
## ${title(externalContractor)} {#externalContractor_${externalContractor?counter}}

<@table '',
externalContractor,
['name',
'description',
'scope_address_address1',
{'scope_address_postcode, scope_address_city' : 'scope_address_postcode scope_address_city'},
'scope_contactInformation_phone',
'scope_contactInformation_email',
'scope_contactInformation_website'
]/>


</#list>
<div class="pagebreak"></div>
</#if>