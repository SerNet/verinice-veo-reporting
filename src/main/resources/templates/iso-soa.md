<#import "/libs/commons.md" as com>
<#import "/libs/risk-commons.md" as rcom>

<#assign table = com.table
  multiline = com.multiline
  riskCell = rcom.riskCell/>

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

table.control_list {
  -fs-table-paginate: paginate;
  font-size: 80%;
}

table.control_list th:first-child, table.control_list td:first-child {
  width: 1cm;
}

.fullwidth {
  width: 100%;
}
</style>

<#assign scope = isoOrg/>
<#assign domain=domains?filter(it->it.name == 'ISO 27001 (DE)')?filter(it->scope.domains?keys?seq_contains(it.id))
?sort_by("createdAt")?last />

<div class="footer-left">
  <table>
    <tr>
      <td>${bundle.scope_SCP_isoScope_singular}: </td>
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

# ${bundle.main_page} {#main_page}

<div class="main_page">

<@table bundle.scope_SCP_isoScope_singular,
scope,
['name',
'description',
'status'
],
domain/>

</div>

# ${bundle.control_CTL_ISOControl_plural}

<#function sortCIs cis>
<#assign sortedControls = cis?map(it->it.control)?sort_by('abbreviation_naturalized')>
<#return sortedControls?map(it->cis?filter(ci->ci.control._self == it._self)?first)>
</#function>

<#-- TODO: #3385: use domain-specific status -->
<#assign statusMap = {
'YES': {"color":"#12AE0F"},
'NO': {"color":"#AE0D11"},
'PARTIAL': {"color":"#EDE92F"},
'N_A': {"color":"#49A2ED"},
'UNKNOWN': {"color": "#ffffff"}
} />


<#macro sq color="#767676">
<div style="background-color:${color};width:1em;height:1em;"></div>
</#macro>

<#macro row ci>
<#assign ri=scope.requirementImplementations?filter(ri->ri.control._self == control._self)?first />
<tr>
<td>${control.abbreviation!}</td>
<td>${control.name}</td>
<#if ci.implementationStatus != 'N_A'>
<td><@sq/></td>
<@riskCell color=statusMap[ci.implementationStatus].color>${bundle[ci.implementationStatus]}</@riskCell>
<td>${ci.description!}</td>
<#else>
<td/>
<td colspan="2">
<#if ci.description?has_content>${ci.description}<br /></#if>${ri.implementationStatement!}
</td>
</#if>
<#-- old:
<td><#if ci.implementationStatus != 'N_A'><@sq/></#if></td>
<#if ci.implementationStatus != 'N_A'>
<@riskCell color=statusMap[ci.implementationStatus].color>${bundle[ci.implementationStatus]}</@riskCell>
<#else>
<td />
</#if>
<td><#if ci.description?has_content>${ci.description}<br /></#if>${ri.implementationStatement!}</td>
-->
</tr>
</#macro>

<#macro section title cis>
<#if cis?has_content>
<tbody>
<tr>
<th colspan="5">${title}</th>
</tr>

<#list cis as ci>
<#assign control=ci.control />
<@row ci />
</#list>
</tbody>
</#if>
</#macro>

<#assign cis=sortCIs(scope.controlImplementations?filter(ci->ci.control.domains[domain.id].subType == 'CTL_ISOControl')) />
<#assign officialCIs=cis?filter(ci->ci.control.domains[domain.id].appliedCatalogItem?has_content) />
<#assign customCIs=cis?filter(ci->!ci.control.domains[domain.id].appliedCatalogItem?has_content) />

<#-- TODO group controls -->
<table class="table fullwidth control_list">
<thead>
<tr>
<th>${bundle.abbr}</th>
<th>${bundle.name}</th>
<th>Anw.</th>
<th>${bundle.implementation_status}</th>
<th>${bundle.reason}</th>
</tr>
</thead>
<@section "A-5 Organisatorische Maßnahmen" officialCIs?filter(ci->ci.control.abbreviation?starts_with('A-5')) />
<@section "A-6 Personenbezogene Maßnahmen" officialCIs?filter(ci->ci.control.abbreviation?starts_with('A-6')) />
<@section "A-7 Physische Maßnahmen" officialCIs?filter(ci->ci.control.abbreviation?starts_with('A-7')) />
<@section "A-8 Technologische Maßnahmen" officialCIs?filter(ci->ci.control.abbreviation?starts_with('A-8')) />
<@section "Benutzerdefinierte Maßnahmen" customCIs  />
</table>