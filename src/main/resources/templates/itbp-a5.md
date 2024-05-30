<#import "/libs/commons.md" as com>
<#import "/libs/itbp-commons.md" as icom>
<#import "/libs/dp-risk.md" as dpRisk>

<#assign table = com.table
         def = com.def
         groupBySubType = com.groupBySubType
         title = icom.title />

<#assign scope = informationDomain/>

<#assign processesInScope = scope.getMembersWithType('process')?filter(p ->p.hasSubType('PRO_BusinessProcess'))>

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
<#assign domain=domains?filter(it->it.name == 'IT-Grundschutz')?filter(it->scope.domains?keys?seq_contains(it.id))?sort_by("createdAt")?last />
<#assign institutions=scope.scopes?filter(it->it.hasSubType('SCP_Institution')) />

<#assign elementSubTypeGroups = groupBySubType(scope.members, 'process', domain)
+ groupBySubType(scope.members, 'asset', domain) />

<#assign riskDefinitionId=scope.domains[domain.id].riskDefinition! />

<#function title element>
<#if element.abbreviation?has_content>
  <#return "${element.abbreviation} ${element.name}">
  <#else>
  <#return element.name>
</#if>
</#function>

<bookmarks>
  <bookmark name="${bundle.main_page}" href="#main_page"/>
<#if riskDefinitionId?has_content>
  <bookmark name="${bundle.risk_matrix}" href="#risk_matrix"/>
</#if>
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
<#if riskDefinitionId?has_content>

# ${bundle.risk_matrix} {#risk_matrix}

<#assign riskDefinition=domain.riskDefinitions[riskDefinitionId] />

<#assign risksInDomainWithData = [] />
<#list elementSubTypeGroups as group>
  <#list group.elements as element>
    <#assign risksInDomainWithData = risksInDomainWithData + element.risks?filter(it-> it.domains?keys?seq_contains(domain.id) && it.domains[domain.id].riskDefinitions?has_content) />
  </#list>
</#list>

<#macro cellStyle color>
<#assign svg='<svg xmlns="http://www.w3.org/2000/svg" height="100" width="100"><polygon points="0,0 0,100 100,100" style="fill:${color};" /></svg>' />
  style="background-size:7mm;background-repeat:no-repeat;background-position:bottom left;background-image: url('data:image/svg+xml;base64,${base64(svg)}');"
</</#macro>

<#macro matrixCell color text>
  <td <@cellStyle color />>${text}</td>
</#macro>


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
<th class="rotate label" <@cellStyle probability.htmlColor />>
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
<td class="label" <@cellStyle potentialImpact.htmlColor />>
${potentialImpact.name}
</td>
<#list riskDefinition.probability.levels as probability>
<#assign risk=category.valueMatrix[probability.ordinalValue][potentialImpact.ordinalValue] />
<@matrixCell risk.htmlColor risk.name />
</#list>
</tr>
</#list>
</tbody>

</#list>
</tbody>
</table>

<div class="pagebreak"></div>


## Auswirkungen

<#list riskDefinition.categories[0].potentialImpacts as impact>

<@def impact.translations.de.name impact.translations.de.description/>

</#list>

## Eintrittswahrscheinlichkeit

<#list riskDefinition.probability.levels as probability>

<@def probability.translations.de.name probability.translations.de.description/>

</#list>

## Risikokategorien

<#list riskDefinition.riskValues as risk>

<@def risk.translations.de.name risk.translations.de.description/>

</#list>


# ${bundle.chart_section_title} {#charts}

<table class="risk_charts_container">
<tbody>
<tr>

<td class="risk_charts_col">
<object type="jfreechart/veo-pie" style="margin-bottom: 2cm;width:10cm;height:8cm;margin:auto;" title="${bundle.risk_distribution} (${bundle.gross})" alt="${bundle.chart}: ${bundle.risk_distribution} (${bundle.gross})">
<#list riskDefinition.riskValues as riskValue>
  <#assign filteredRisks=risksInDomainWithData?filter(r->
  (r.domains[domain.id].riskDefinitions[riskDefinitionId].riskValues?map(it->it.inherentRisk!-1)?max!-1) == riskValue.ordinalValue)>
  <#if filteredRisks?has_content>
    <data name="${riskValue.name}" color="${riskValue.htmlColor}" value="${filteredRisks?size}"/>
  </#if>
</#list>
</object>
</td>

<td class="risk_charts_col">
<object type="jfreechart/veo-pie" style="margin-bottom: 2cm;width:10cm;height:8cm;margin:auto;" title="${bundle.risk_distribution} (${bundle.net})" alt="${bundle.chart}: ${bundle.risk_distribution} (${bundle.net})">
<#list riskDefinition.riskValues as riskValue>
  <#assign filteredRisks=risksInDomainWithData?filter(r->
  (r.domains[domain.id].riskDefinitions[riskDefinitionId].riskValues?map(it->it.residualRisk!-1)?max!-1) == riskValue.ordinalValue)>
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


<#macro moduleview targetObject>
<@def "Beschreibung" targetObject.description true/>

<#assign targetObjectRisksInDomain = (targetObject.risks?filter(it-> it.domains?keys?seq_contains(domain.id)))! />

<#if targetObjectRisksInDomain?has_content>
## Risiken

<#list targetObjectRisksInDomain as risk>
<@dpRisk.riskdisplay 3 risk domain riskDefinition />
</#list>

</#if>
</#macro>

# ${title(scope)} {#information_domain}

<@moduleview scope/>

<#list elementSubTypeGroups as group>

# ${group.subTypePlural} {#${group.elementType}_${group.subType}}

<#list group.elements as element>

## ${title(element)} {#${group.elementType}_${group.subType}_${element?counter}}

<@moduleview element/>

</#list>

<div class="pagebreak"></div>

</#list>
