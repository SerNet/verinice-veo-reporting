<#import "/libs/commons.md" as com>
<#import "/libs/risk-commons.md" as rcom>

<#assign table = com.table
  multiline = com.multiline
  riskCell = rcom.riskCell/>

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

<#assign scope = target/>

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

<#assign cid = (domain.elementTypeDefinitions.scope.controlImplementationDefinition!) />
<#assign useCICAs = (cid.customAspects?keys?seq_contains('scope_isoSoA'))!false />

<#macro row ci>
<#assign ri=scope.requirementImplementations?filter(ri->ri.control._self == control._self)?first />
<#if useCICAs>
  <#local applicable = (ci.scope_isoSoA_applicability)!false>
  <#local selectionCriteria =(ci.scope_isoSoA_selectionCriterion?map(item->cid.translations[.lang][item])?join(', '))! />
  <#local reason =(ci.scope_isoSoA_reason)! />
<#else>
  <#local applicable = ci.implementationStatus != 'N_A'>
  <#local selectionCriteria = ci.description! />
  <#if applicable>
    <#local reason = ci.description! />
  <#else>
    <#local reason =ri.implementationStatement! />
    <#if ci.description?has_content>
      <#local reason = ci.description +"<br>"?no_esc + reason />
    </#if>
  </#if>
</#if>
<tr>
<td>${control.abbreviation!}</td>
<td>${control.name}</td>
<#if applicable>
<td><@sq/></td>
<#else>
<td/>
</#if>
<#if useCICAs && ci.implementationStatus != 'UNKNOWN' || applicable>
<@riskCell color=statusMap[ci.implementationStatus].color>${bundle[ci.implementationStatus]}</@riskCell>
<#else>
<td/>
</#if>
<#if useCICAs>
<td>${selectionCriteria!}</td>
</#if>
<td>${reason!}</td>
</tr>
</#macro>


<#assign numCols = 5/>
<#if useCICAs>
  <#assign numCols = 6/>
</#if>

<#macro section title cis>
<#if cis?has_content>
<tbody>
<tr>
<th colspan="${numCols}">${title}</th>
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
<th>${bundle.applicable_abbr}</th>
<th>${bundle.implementation_status}</th>
<#if useCICAs>
<th>${cid.translations[.lang].scope_isoSoA_selectionCriterion}</th>
<th>${cid.translations[.lang].scope_isoSoA_reason}</th>
<#else>
<th>${bundle.reason}</th>
</#if>
</tr>
</thead>
<@section bundle.a5_controls officialCIs?filter(ci->ci.control.abbreviation?starts_with('A-5')) />
<@section bundle.a6_controls officialCIs?filter(ci->ci.control.abbreviation?starts_with('A-6')) />
<@section bundle.a7_controls officialCIs?filter(ci->ci.control.abbreviation?starts_with('A-7')) />
<@section bundle.a8_controls officialCIs?filter(ci->ci.control.abbreviation?starts_with('A-8')) />
<@section bundle.custom_controls customCIs  />
</table>