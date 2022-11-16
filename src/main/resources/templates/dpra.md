<#import "/libs/commons.md" as com>
<#import "/libs/dp-risk.md" as dpRisk>

<#assign table = com.table
         def = com.def />

<#assign processesInScope = scope.getMembersWithType('process')?filter(p ->p.hasSubType('PRO_DataProcessing'))>

<style>
<#include "styles/default.css">
<#include "styles/default_landscape.css">
h1, h2, h3, h4 {
  page-break-after: avoid;
}

.main_page {
  page-break-after: always;
}

.main_page table th:first-child, .main_page table td:first-child {
  width: 8cm;
}
dt {
  font-weight: 600;
}

.risk_charts_container {
  width:100%;
}

.risk_charts_col {
  text-align: center;
}

.risk_charts_col h2 {
  margin-bottom: 1cm;
}

.risk dl {
  display: table;
}

.risk dt, .risk dd {
  display: table-cell;
}

.risk dt {
  font-weight: normal;
  min-width: 8cm;
}

.risk dd {
  width: 100%
}

.riskmatrix {
  page-break-inside: avoid;
  margin: auto;
  table-layout: fixed;
}

.riskmatrix td, .riskmatrix th {
  border: 0.5mm solid #e3e3e3;
  vertical-align: middle;
  text-align: center;
}

.riskmatrix .caption {
  font-weight: bold;
}

<#assign cellSize="1.8cm"?no_esc />
<#assign labelSize="4cm"?no_esc />
<#assign captionSize="0.9cm"?no_esc />

.riskmatrix .cell,
.riskmatrix tbody .label {
  height: ${cellSize};
}

.riskmatrix col {
  width: ${cellSize};
}

.riskmatrix col:nth-child(1) {
  width: ${captionSize};
}

.riskmatrix col:nth-child(2) {
  width: ${labelSize};
}

.riskmatrix th.caption {
  height: ${captionSize};
}

.riskmatrix th.label {
  font-weight: normal;
  height: ${labelSize};
}

.riskmatrix .spacer {
  border-color: transparent;
}

.riskmatrix .rotate div {
  transform: rotate(-90deg);
}

.riskmatrix td.rotate div {
  width: 0;
  transform-origin: 50% 50%;
  transform: rotate(-90deg) translateY(2mm);
}

</style>

<#-- FIXME VEO-619/VEO-1175: maybe pass domain into report? -->
<#assign domain=domains?filter(it->it.name == 'DS-GVO')?filter(it->scope.domains?keys?seq_contains(it.id))?sort_by("createdAt")?last />
<#assign riskDefinitionId=scope.domains[domain.id].riskDefinition! />


<bookmarks>
  <bookmark name="${bundle.main_page}" href="#main_page"/>
<#if riskDefinitionId?has_content>
  <bookmark name="${bundle.risk_matrix}" href="#risk_matrix"/>
</#if>
<#list processesInScope as process>
  <bookmark name="${process.name}" href="#process_${process?counter}">
  </bookmark>
</#list>
</bookmarks>


<div class="footer-left">
  <table>
    <tr>
      <td>Organisation: </td>
      <td>${scope.name}</td>
    </tr>
    <tr>
      <td>Erstelldatum: </td>
      <td>${.now?date}</td>
    </tr>
  </table>
</div>


<div class="cover">
<h1>${bundle.title}</h1>
<p>powered by verinice</p>
</div>



# ${bundle.main_page} {#main_page}

<div class="main_page">

<@table 'Angaben zum Verantwortlichen',
  scope,
  ['name',
   'scope_address_address1',
   {'scope_address_postcode, scope_address_city' : 'scope_address_postcode scope_address_city'},
   'scope_contactInformation_phone / scope_contactInformation_fax',
   'scope_contactInformation_email',
   'scope_contactInformation_website'
  ]/>


<#assign management=scope.findFirstLinked('scope_management')! />
<#assign headOfDataProcessing=scope.findFirstLinked('scope_headOfDataProcessing')! />


| Vertretung  ||
|:---|:---|
| Leitung der verantwortlichen Stelle<br/>(einschließlich Vertreter) | ${management.person_generalInformation_givenName!} ${management.person_generalInformation_familyName!} |
| Leitung der Datenverarbeitung |  ${headOfDataProcessing.person_generalInformation_givenName!} ${headOfDataProcessing.person_generalInformation_familyName!} |


<#assign dataProtectionOfficer=scope.findFirstLinked('scope_dataProtectionOfficer')! />

<@table 'Datenschutzbeauftragte',
  dataProtectionOfficer,
  [
   {'name' : 'person_generalInformation_givenName person_generalInformation_familyName'},
   {'person_address_postcode, scope_address_city' : 'person_address_postcode scope_address_city'},
   'person_contactInformation_office / person_contactInformation_fax',
   'person_contactInformation_email'
  ]/>

<#if (scope.scope_thirdCountry_status!false)>
<@table bundle.thirdCountry_table_caption, scope, [
  'scope_thirdCountry_name',
  'scope_thirdCountry_address1',
  {'scope_thirdCountry_postcode, scope_thirdCountry_city' : 'scope_thirdCountry_postcode scope_thirdCountry_city'},
  'scope_thirdCountry_country'
]/>
</#if>

</div>
<#if riskDefinitionId?has_content>

# ${bundle.risk_matrix} {#risk_matrix}

<#assign riskDefinition=domain.riskDefinitions[riskDefinitionId] />

<#assign processRisksInDomainWithData = [] />
<#list processesInScope as process>
  <#assign processRisksInDomainWithData = processRisksInDomainWithData + process.risks?filter(it-> it.domains?keys?seq_contains(domain.id) && it.domains[domain.id].riskDefinitions?has_content) />
</#list>

<table class="riskmatrix">
<colgroup>
<col span="1">
<col span="1">
<#list riskDefinition.probability.levels as probability>
<col span="1">
</#list>
</colgroup>
<thead>
<tr>
<th class="spacer"/>
<th class="spacer"/>
<th colspan="${riskDefinition.probability.levels?size}" class="caption">
Eintrittswahrscheinlichkeit
</th>
</tr>
<tr>
<th class="spacer"/>
<th class="spacer"/>
<#list riskDefinition.probability.levels as probability>
<th class="rotate label" <@dpRisk.cellStyle probability.htmlColor />>
<div>${probability.name}</div>
</th>
</#list>
</tr>
</thead>
<#-- we assume that all categories share the same value matrix-->
<#list riskDefinition.categories[0..0] as category>
<tbody>
<#list category.potentialImpacts?reverse as potentialImpact>
<tr class="impactrow${potentialImpact?index}">
<#if potentialImpact?index == 0>
<td class="rotate caption" rowspan="${category.potentialImpacts?size}">
<div>Auswirkung</div>
</td>
</#if>
<td class="label" <@dpRisk.cellStyle potentialImpact.htmlColor />>
${potentialImpact.name}
</td>
<#list riskDefinition.probability.levels as probability>
<#assign risk=category.valueMatrix[probability.ordinalValue][potentialImpact.ordinalValue] />
<td class="cell" <@dpRisk.cellStyle risk.htmlColor />>
${risk.name}
</td>
</#list>
</tr>
</#list>
</tbody>

</#list>
</tbody>
</table>

<div class="pagebreak"></div>

# ${bundle.chart_section_title} {#charts}

<table class="risk_charts_container">
<tbody>
<tr>

<td class="risk_charts_col">
<object type="jfreechart/veo-pie" style="margin-bottom: 2cm;width:10cm;height:8cm;margin:auto;" title="${bundle.risk_distribution} (${bundle.gross})" alt="${bundle.chart}: ${bundle.risk_distribution} (${bundle.gross})">
<#list riskDefinition.riskValues as riskValue>
  <#assign filteredRisks=processRisksInDomainWithData?filter(r->
  r.domains[domain.id].riskDefinitions[riskDefinitionId].riskValues?map(it->it.inherentRisk!-1)?max == riskValue.ordinalValue)>
  <#if filteredRisks?has_content>
    <data name="${riskValue.name}" color="${riskValue.htmlColor}" value="${filteredRisks?size}"/>
  </#if>
</#list>
</object>
</td>

<td class="risk_charts_col">
<object type="jfreechart/veo-pie" style="margin-bottom: 2cm;width:10cm;height:8cm;margin:auto;" title="${bundle.risk_distribution} (${bundle.net})" alt="${bundle.chart}: ${bundle.risk_distribution} (${bundle.net})">
<#list riskDefinition.riskValues as riskValue>
  <#assign filteredRisks=processRisksInDomainWithData?filter(r->
  r.domains[domain.id].riskDefinitions[riskDefinitionId].riskValues?map(it->it.residualRisk!-1)?max == riskValue.ordinalValue)>
  <#if filteredRisks?has_content>
    <data name="${riskValue.name}" color="${riskValue.htmlColor}" value="${filteredRisks?size}"/>
  </#if>
</#list>
</object>
</td>
</tbody>
</table>
</#if>

<div class="pagebreak"></div>

<#list processesInScope as process>

# ${process.name} (${process.designator}) {#process_${process?counter}}

<@def "Beschreibung" process.description true/>

<#assign processRisksInDomain = process.risks?filter(it-> it.domains?keys?seq_contains(domain.id)) />

<#if processRisksInDomain?has_content>
## Risiken

<#list processRisksInDomain as risk>
<@dpRisk.riskdisplay 3 risk domain riskDefinition />
</#list>

</#if>

<#if process?has_next>

<div class="pagebreak"></div>

</#if>
</#list>