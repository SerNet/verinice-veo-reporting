<#import "/libs/commons.md" as com>
<#import "/libs/risk-commons.md" as rcom>

<#assign table = com.table
def = com.def
multiline = com.multiline
riskCell = rcom.riskCell />


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

table.asset_list {
  -fs-table-paginate: paginate;
  font-size: 80%;
}

table.asset_list th:first-child, table.asset_list td:first-child {
  white-space: nowrap;
}

.fullwidth {
  width: 100%;
}

.asset_list a {
  color: #767676;
  text-decoration: none;
}

.asset_list a span::after {
  content: leader('.') " " target-counter(attr(href), page);
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

# ${bundle.overview}

<#assign riskDefinitionId=scope.domains[domain.id].riskDefinition! />

<#if riskDefinitionId?has_content>
  <#assign riskDefinition=domain.riskDefinitions[riskDefinitionId] />
</#if>

<#macro row label definition="" alwaysShow=false>
  <#if definition?has_content || alwaysShow>
    <tr>
    <td>${label}</td>
    <td> ${definition}</td>
  </tr>
  </#if>
</#macro>

<#assign assets = scope.getMembersWithType('asset')?filter(a->a.domains?keys?seq_contains(domain.id))?sort_by('abbreviation_naturalized') />

<table class="table fullwidth asset_list">
<thead>
<tr>
<th>${bundle.abbreviation}</th>
<#if riskDefinitionId?has_content>
<#list riskDefinition.categories as cat>
<th>${cat.translations[.lang].abbreviation}</th>
</#list>
</#if>
<th>${bundle.name}</th>
</tr>
</thead>
<tbody>
<#list assets as asset>
<tr>
<td>${asset.abbreviation!}</td>
<#if riskDefinitionId?has_content>
<#list riskDefinition.categories as cat>
<#assign impactForCat = (cat.potentialImpacts[asset.domains[domain.id].riskValues[riskDefinitionId].potentialImpactsEffective[cat.id]])! />
<#if impactForCat?has_content>
<@riskCell color=impactForCat.htmlColor>${impactForCat.translations[.lang].abbreviation}</@riskCell>
<#else>
<td/>
</#if>
</#list>
</#if>
<td><a href="#asset_${asset?counter}" title="${bundle('jumpto', asset.name)}">${asset.name}<span href="#asset_${asset?counter}"/></a></td>
</tr>
</#list>
</tbody>
</table>

# ${bundle.asset_AST_Asset_plural}

<#list assets as asset>
<div style="page-break-inside: avoid;">


## ${asset.abbreviation!} ${asset.name} {#asset_${asset?counter}}

<table>
<tbody>
<@row bundle.description asset.description true/>
<@row bundle.asset_assetType_assetType asset.asset_assetType_assetType true />
<#assign responsibleLinks = asset.getLinks('asset_responsiblePerson')! />
<#if responsibleLinks?has_content>
<@row bundle.asset_responsiblePerson responsibleLinks?map(l->l.target.name)?join(", ") true />
</#if>
<@row bundle.asset_details_number asset.asset_details_number true />
<@row bundle.asset_details_operatingStage asset.asset_details_operatingStage true />
<#if riskDefinitionId?has_content>
<#list riskDefinition.categories as cat>
<#assign impactForCat = (cat.potentialImpacts[asset.domains[domain.id].riskValues[riskDefinitionId].potentialImpactsEffective[cat.id]])! />
<@row "${cat.translations[.lang].name} (${cat.id})" "${(impactForCat.translations[.lang].name)!}" true />
</#list>
</#if>
<#assign requiredAssetLinks = asset.getLinks('asset_requiredAsset')! />
<#if requiredAssetLinks?has_content>
<@row bundle.asset_requiredAsset requiredAssetLinks?map(l->l.target.name)?join(", ") true />
</#if>
<@row bundle.updatedAt asset.updatedAt?datetime.iso true />
</tbody>
</table>


</div>
</#list>