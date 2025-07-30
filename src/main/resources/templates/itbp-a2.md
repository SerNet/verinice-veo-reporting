<#import "/libs/commons.md" as com>
<#import "/libs/itbp-commons.md" as icom>

<#assign table = com.table
        row = com.row
         def = com.def
         multiline = com.multiline
         groupBySubType = com.groupBySubType
         title = icom.title />


<style>
<@com.defaultStyles />
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

.table {
  width:  100%;
  table-layout: fixed;
  margin: 0;
}
.table td {
  vertical-align: top;
}
</style>

<#assign scope = informationDomain/>
<#assign domain=domains?filter(it->it.name == 'IT-Grundschutz')?filter(it->scope.domains?keys?seq_contains(it.id))?sort_by("createdAt")?last />
<#assign riskDefinitionId=(scope.domains[domain.id].riskDefinition)! />
<#assign impacts=riskDefinitionId?has_content?then(domain.riskDefinitions[riskDefinitionId].categories[0].potentialImpacts,[])/>
<#assign institutions=scope.scopes?filter(it->it.hasSubType('SCP_Institution')) />

<#assign elementSubTypeGroups =
 groupBySubType(scope.members, 'process', domain) + groupBySubType(scope.members, 'asset', domain)/>

<bookmarks>
    <bookmark name="${bundle.toc}" href="#toc"/>
    <bookmark name="${bundle.main_page}" href="#main_page"/>
    <bookmark name="${bundle.impactDefinitions}" href="#impact_definitions"/>
    <#list impacts as impact>
    <bookmark name="${bundle.level}: ${impact.translations.de.name}" href="#impact_definitions_${impact?counter}"/>
    </#list>
    <bookmark name="${bundle.targets}" href="#targets"/>
    <#list elementSubTypeGroups as group>
        <bookmark name="${group.subTypePlural}" href="#targets_${group.elementType}_${group.subType}"/>
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
  <@tocitem 1 "impact_definitions" "2. ${bundle.impactDefinitions}"/>
  <#list impacts as impact>
      <@tocitem 2 "impact_definitions_${impact?counter}" "${impact?counter}. ${bundle.level}: ${impact.translations.de.name}" />
  </#list>
  <@tocitem 1 "targets" "3. ${bundle.targets}" />
  <#assign level2Counter=1/>
  <#list elementSubTypeGroups as group>
      <@tocitem 2 "targets_${group.elementType}_${group.subType}" "${level2Counter}. ${group.subTypePlural}"/>
      <#assign level2Counter++/>
  </#list>
</tbody>
</table>

# ${bundle.main_page} {#main_page}

<div class="main_page">

<#if institutions?has_content>

<#list institutions as institution>
<@table bundle.institution,
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

<@table bundle.information_domain_information,
  scope,
  ['name',
  'description',
  'scope_protection_approach',
  'status'
  ],
  domain/>

</div>

<div class="pagebreak"></div>

## ${bundle.impactDefinitions} {#impact_definitions}

<#if !riskDefinitionId?has_content>
${bundle.scopeHasNoRiskDefinition}
</#if>

<#list impacts as impact>
  ### ${bundle.level}: ${impact.translations.de.name} {#impact_definitions_${impact?counter}}
  <!-- Add line break before each sentence that contains a colon. -->
  ${impact.translations.de.description?replace("\\. ([A-Z][^\\.]*: )", ".\n\n $1", "r")}
</#list>

<#assign categories=riskDefinitionId?has_content?then(domain.riskDefinitions[riskDefinitionId].categories,[])/>

<div class="pagebreak"></div>

<#function getImpactLevel element category>
   <#if element.domains[domain.id]??
   && element.domains[domain.id].riskValues[riskDefinitionId]??
   && element.domains[domain.id].riskValues[riskDefinitionId].potentialImpactsEffective[category.id]??>
   <#assign impactValue=element.domains[domain.id].riskValues[riskDefinitionId].potentialImpactsEffective[category.id]/>
   <#return category.potentialImpacts[impactValue].translations.de.name/>
   </#if>
   <#return '-'/>
</#function>

<#function getImpactReason element category>
    <#if element.domains[domain.id]??
    && element.domains[domain.id].riskValues[riskDefinitionId]??
    && element.domains[domain.id].riskValues[riskDefinitionId].potentialImpactEffectiveReasons??
    && element.domains[domain.id].riskValues[riskDefinitionId].potentialImpactEffectiveReasons[category.id]??>
        <#return '('+bundle[element.domains[domain.id].riskValues[riskDefinitionId].potentialImpactEffectiveReasons[category.id]+'_abbreviation']+') '/>
    </#if>
    <#return ''/>
</#function>

<#function getImpactExplanation element category>
    <#if element.domains[domain.id]??
    && element.domains[domain.id].riskValues[riskDefinitionId]??
    && element.domains[domain.id].riskValues[riskDefinitionId].potentialImpactExplanations??
    && element.domains[domain.id].riskValues[riskDefinitionId].potentialImpactExplanations[category.id]??>
        <#return element.domains[domain.id].riskValues[riskDefinitionId].potentialImpactExplanations[category.id]/>
    </#if>
    <#return ''/>
</#function>

## ${bundle.targets} {#targets}
### ${bundle.legend} {#targets_legend}
${bundle.impactReasons}:

<#assign abbreviations=bundle?keys?filter(it->it?starts_with("impact_") && it?ends_with("_abbreviation"))?sort />
|    |    |
|----|----|
<#list abbreviations as abbreviation>
| (${bundle[abbreviation]}) | ${bundle[abbreviation?remove_ending("_abbreviation")]} |
</#list>

<#list elementSubTypeGroups as group>

### ${group.subTypePlural} {#targets_${group.elementType}_${group.subType}}
<!-- Use separate table for head and one table per target, so there can be a page break before each target. -->
<table class="table">
<thead class="dark-gray">
<tr>
<#list categories as category>
<td>
${category.translations.de.name}
</td>
</#list>
</tr>
</thead>
</table>

<#list group.elements as targetElement>
<table class="table">
<tr class="dark-gray">
<td colspan="${categories?size}"> ${title(targetElement)} </td>
</tr>
<tr>
<td colspan="3">${targetElement.description!}</td>
</tr>
<tr>
<#list categories as category>
<td>
<b>${getImpactLevel(targetElement,category)}</b> ${getImpactReason(targetElement, category)}

${getImpactExplanation(targetElement,category)}
</td>
</#list>

</tr>
</#list>
</table>
</#list>