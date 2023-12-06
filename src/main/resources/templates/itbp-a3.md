<#import "/libs/commons.md" as com>
<#import "/libs/dp-risk.md" as dpRisk>

<#assign table = com.table
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

<#assign assetTypes= ['AST_Application', 'AST_IT-System', 'AST_Room', 'AST_Network', 'AST_ICS-System', 'AST_Device']>
<#assign processTypes= ['PRO_BusinessProcess', 'PRO_SpecialistMethodologies']>

<#assign assetsInScope = scope.getMembersWithType('asset')/>
<#assign processesInScope = scope.getMembersWithType('process')/>

<#assign assetsBySubType = groupBySubType(assetsInScope, assetTypes)/>
<#assign processesBySubType = groupBySubType(processesInScope, processTypes)/>


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
  <bookmark name="${bundle.used_modules}" href="#used_modules"/>
  <bookmark name="${bundle.information_domain}" href="#information_domain"/>
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
<#if processesInScope?has_content>
  <bookmark name="${bundle.processes}" href="#processes">
    <#list processesBySubType as processType, processesWithType>
      <bookmark name="${bundle[processType]}" href="#${processType}">
        <#list processesWithType as process>
          <bookmark name="${title(process)}" href="#process_${processType}_${process?counter}">
          </bookmark>
        </#list>
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
  <@tocitem 1 "used_modules" "2. ${bundle.used_modules}" />
  <@tocitem 1 "information_domain" "3. ${bundle.information_domain}" />
  <#assign level2counter = 4>
  <#if assetsInScope?has_content>
    <@tocitem 1 "assets" "${level2counter}. ${bundle.assets}" />
    <#list assetsBySubType as assetType, assetsWithType>
      <@tocitem 2 assetType "${assetType?counter}. ${bundle[assetType]}" />
      <#list assetsWithType as asset>
        <@tocitem 3 "asset_${assetType}_${asset?counter}" "${asset?counter}. ${title(asset)}" />
      </#list>
     </#list>
    <#assign level2counter = 5>
  </#if>
  <#if processesInScope?has_content>
    <@tocitem 1 "processes" "${level2counter}. ${bundle.processes}" />
    <#list processesBySubType as processType, processesWithType>
      <@tocitem 2 processType "${processType?counter}. ${bundle[processType]}" />
      <#list processesWithType as process>
        <@tocitem 3 "process_${processType}_${process?counter}" "${process?counter}. ${title(process)}" />
      </#list>
     </#list>
   </#if>   
</tbody>
</table>

# ${bundle.main_page} {#main_page}

<div class="main_page">



<@table bundle.controller_information,
  scope,
  ['name',
   'scope_address_address1',
   {'scope_address_postcode, scope_address_city' : 'scope_address_postcode scope_address_city'},
   'scope_contactInformation_phone',
   'scope_contactInformation_email',
   'scope_contactInformation_website'
  ]/>

<#assign management=scope.findFirstLinked('scope_management')! />
<#assign headOfDataProcessing=scope.findFirstLinked('scope_headOfDataProcessing')! />

| ${bundle.representation}  ||
|:---|:---|
| ${bundle.scope_management} | ${management.name!} |
| ${bundle.scope_headOfDataProcessing}  |  ${headOfDataProcessing.name!} |


<#assign dataProtectionOfficer=scope.findFirstLinked('scope_dataProtectionOfficer')! />

<#--
<@table bundle.data_protection_officer,
  dataProtectionOfficer,
  [
   'name',
   'person_contactInformation_office',
   'person_contactInformation_email'
  ]/>

</div>
-->


<div class="pagebreak"></div>

# ${bundle.used_modules} {#used_modules}

<#assign targetObjects = [scope]+assetsInScope+processesInScope>

<#assign relevantControlImplementations = []>
<#list targetObjects as item>
  <#list item.controlImplementations as ci>
    <#assign relevantControlImplementations = relevantControlImplementations + [ci]>
  </#list>
</#list>

<#assign usedModules = []>
<#assign usedModulesURIs = []>

<#list relevantControlImplementations as ci>
  <#if !(usedModulesURIs?seq_contains(ci.control._self))>
    <#assign usedModules = usedModules + [ci.control]>
    <#assign usedModulesURIs = usedModulesURIs + [ci.control._self]>
  </#if>
</#list>

<#function sortModules modules>
  <#-- apply fancy ITBP sorting -->
  <#assign step1 = modules?sort_by('name_naturalized')?sort_by('abbreviation_naturalized')>
  <#assign sortInfo = step1?map(it->
    {"url":it._self, 
     "key":it.abbreviation_naturalized
       ?replace('^ISMS', ' A', 'r')
       ?replace('^ORP', ' B', 'r')
       ?replace('^CON', ' C', 'r')
       ?replace('^OPS', ' D', 'r')
       ?replace('^DER', ' E', 'r')
       ?replace('^APP', ' F', 'r')
       ?replace('^SYS', ' G', 'r')
       ?replace('^IND', ' H', 'r')
       ?replace('^NET', ' I', 'r')
       ?replace('^INF', ' J', 'r')
    }
  )?sort_by('key')>
  <#assign step2 = sortInfo?map(it->modules?filter(ci->ci._self == it.url)?first)>
  <#return step2?filter(it->it.abbreviation?has_content)+step2?filter(it->!it.abbreviation?has_content)>
</#function>

<#function sortCIs cis>
  <#assign sortedModules = sortModules(cis?map(it->it.control))>
  <#return sortedModules?map(it->cis?filter(ci->ci.control._self == it._self)?first)>
</#function>

<#assign usedModules = sortModules(usedModules)>


<#assign implementationStatuses = {'YES': 'rgb(160, 207, 17)', 'PARTIAL': 'rgb(255, 255, 19)', 'NO': 'rgb(255, 18, 18)', 'N_A': 'rgb(192, 192, 192)', 'UNKNOWN': 'rgb(128, 128, 128)'}>

<#macro chart cis title>
<object type="jfreechart/veo-pie" style="margin-bottom: 2cm;width:10cm;height:8cm;margin:auto;" title="${title}" alt="Diagramm: ${title}">
<#list implementationStatuses?keys as implementationStatus>
  <#assign filteredCIs=cis?filter(it->it.implementationStatus==implementationStatus) />
  <#if filteredCIs?has_content>
    <data name="${bundle[implementationStatus]}" color="${implementationStatuses[implementationStatus]}" value="${filteredCIs?size}"/>
  </#if>
</#list>
</object>
</#macro>

<@chart relevantControlImplementations 'Umsetzungsstatus gesamt'/>

|${bundle.abbreviation}| ${bundle.name}| ${bundle.number_of_occurrences}
|:---|:---|:---|
<#list usedModules as m>
|${m.abbreviation!}|${m.name}|${relevantControlImplementations?filter(it->it.control._self == m._self)?size}|
</#list>
{.table .fullwidth .used_modules}

<div class="pagebreak"></div>



<#macro moduleview targetObject>

<#assign moduleControlImplementations = sortCIs(targetObject.controlImplementations)>

<@chart moduleControlImplementations 'Umsetzungsstatus ${title(targetObject)}'/>


<#if moduleControlImplementations?has_content>

## Bausteine

<#list moduleControlImplementations as moduleControlImplementation>
<div class="nobreak">

### ${title(moduleControlImplementation.control)}

<@def bundle.description moduleControlImplementation.description/>

<@def bundle.control_bpInformation_protectionSequence, (bundle[moduleControlImplementation.control.control_bpInformation_protectionSequence])!/>

<@def bundle.responsible, (moduleControlImplementation.responsible.name)!/>

<@def bundle.implementation_status, bundle[moduleControlImplementation.implementationStatus]!/>

</div>
</#list>

<#-- 
|${bundle.abbreviation}| ${bundle.name}| ${bundle.responsible} | ${bundle.implementation_status} 
|:---|:---|:---|
<#list moduleControlImplementations as moduleControlImplementation>
|${moduleControlImplementation.control.abbreviation!}|${moduleControlImplementation.control.name}|${(moduleControlImplementation.reponsible.name)!}|${bundle[moduleControlImplementation.implementationStatus]}|
</#list>
{.table .fullwidth .used_modules}
 -->
 
</#if>
</#macro> 

# ${title(scope)} {#information_domain}

<@moduleview scope/>

<div class="pagebreak"></div>

<#if assetsInScope?has_content>
# ${bundle.assets} {#assets}
<#list assetsBySubType as assetType, assetsWithType>

## ${bundle[assetType]} {#${assetType}}

<#list assetsWithType as asset>
### ${title(asset)} {#asset_${assetType}_${asset?counter}}

<@moduleview asset/>

</#list>
<div class="pagebreak"></div>
</#list>
</#if>

<#if processesInScope?has_content>
# ${bundle.processes} {#processes}
<#list processesBySubType as processType, processesWithType>

## ${bundle[processType]} {#${processType}}

<#list processesWithType as process>
### ${title(process)} {#process_${processType}_${process?counter}}

<@moduleview process/>

</#list>
<div class="pagebreak"></div>
</#list>
</#if>
