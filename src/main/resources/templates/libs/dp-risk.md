<#import "/libs/commons.md" as com>
<#import "/libs/risk-commons.md" as rcom>
<#import "dp-risk-messages.ftl" as messages>

<#-- TODO: #3385: use domain-specific status -->
<#assign statusMap = {
  'YES': {"color":"#12AE0F"},
  'NO': {"color":"#AE0D11"},
  'PARTIAL': {"color":"#EDE92F"},
  'N_A': {"color":"#49A2ED"}
} />

<#function getImplementationStatus ri>
  <#local mitigationStatus=statusMap[ri.status]!/>
  <#if mitigationStatus?has_content>
    <#return {"color":mitigationStatus.color, "label":messages["STATUS_" + ri.status]!ri.status}/>
  </#if>
</#function>

<#function getRI riskAffected control>
  <#return riskAffected.requirementImplementations?filter(ri->ri.control.id == control.id)?first>
</#function>


<#macro riskdisplay headinglevel riskAffected risk domain riskDefinition={}>
<div class="risk">

<#assign scenario = risk.scenario>

<#list 0..<headinglevel as i>#</#list> ${(scenario.name)!} (${risk.designator})

<@com.def messages.risk_owner, (risk.riskOwner.name)! />

<@com.def messages.risk_description, scenario.description />

<#assign riskDataAvailable = riskDefinition?has_content && risk.domains[domain.id].riskDefinitions[riskDefinition.id]?has_content />
<#if riskDataAvailable>
<#assign riskValues = risk.getRiskValues(domain.id, riskDefinition.id)>

<table class="table" style="width:100%;font-size:70%;">
<colgroup>
  <col span="1" style="width: 15%;">
  <col span="1" style="width: 19%;">
  <col span="1" style="width: 17%;">
  <col span="1" style="width: 11%;">
  <col span="1" style="width: 19%;">
  <col span="1" style="width: 19%;">
</colgroup>
<thead>
<tr>
<th colspan="1">${messages.criterion}</th>
<th colspan="3">${messages.assessment_pre}</th>
<th>${messages.treatment_header}</th>
<th colspan="1">${messages.assessment_post}</th>
</tr>
<tr>
<th />
<th>${messages.eff_impact}</th>
<th>${messages.eff_prob}</th>
<th>${messages.gross_risk}</th>
<th>${messages.treatment_options}</th>
<th>${messages.net_risk}</th>
</tr>
</thead>
<tbody>
<#list riskDefinition.categories as category>
<#assign riskValuesForCategory = (riskValues[category.id])! />
<tr>
<td>${category.translations[.lang].name}</td>
<td>
<#if riskValuesForCategory?has_content>
<@rcom.impactdisplay riskDefinition category, riskValuesForCategory.effectiveImpact />
</#if>
<br/>
</td>
<#if category?index == 0>
<td rowspan="${riskDefinition.categories?size}"><@rcom.probabilitydisplay riskDefinition, riskValues.effectiveProbability/></td>
</#if>
<#assign inherent = riskValuesForCategory.inherentRisk! />
<#if inherent?has_content >
<#assign inherentData = riskDefinition.getRisk(inherent) />
<@rcom.riskCell inherentData.color>${inherentData.label}</@rcom.riskCell>
<#else/>
<td />
</#if>
<td>
<#assign riskValuesForCategory = (riskValues[category.id])! />
<#if riskValuesForCategory?has_content>
${(riskValuesForCategory.riskTreatments?map(t->rcom.riskReductionLabel(t))?join(', '))!}
<#if (riskValuesForCategory.riskTreatmentExplanation)?has_content>
<br/>
${riskValuesForCategory.riskTreatmentExplanation}
</#if>
</#if>
<br/>
</td>
<#assign residual = riskValuesForCategory.residualRisk! />
<#if residual?has_content>
<#assign residualData = riskDefinition.getRisk(residual) />
<@rcom.riskCell residualData.color>${residualData.label}<#if riskValuesForCategory.residualRiskExplanation?has_content><br/>${riskValuesForCategory.residualRiskExplanation}</#if></@rcom.riskCell>
<#else/>
<td />
</#if>
</#list>
</tr>
</tbody>
</table>
</#if>

<#if risk.mitigation?has_content && risk.mitigation.parts?has_content>
<table class="table " style="width:100%;font-size:70%;">
<colgroup>
  <col span="1" style="width: 40%;">
  <col span="1" style="width: 10%;">
  <col span="1" style="width: 40%;">
  <col span="1" style="width: 10%;">
</colgroup>
<thead>
<tr>
<th colspan="4">${messages.measures}</th>
</tr>
<tr>
<th>${messages.measure_title}</th>
<th>${messages.impl_status}</th>
<th>${messages.impl_explanation}</th>
<th>${messages.impl_date}</th>
</tr>
</thead>
<tbody>
<#list com.getDescendants(risk.mitigation) as tom>
<#assign ri=getRI(riskAffected, tom)!>
<#assign mitigationStatus=(getImplementationStatus(ri))!/>
<tr>
<td>${tom.designator} ${tom.name}</td>
<#if mitigationStatus?has_content>
<@rcom.riskCell mitigationStatus.color>${mitigationStatus.label}</@rcom.riskCell>
<#else>
<td></td>
</#if>
<td>${ri.implementationStatement!}</td>
<td>${(ri.implementationUntil?date.iso)!}</td>
</tr>
</#list>
</tbody>
</table>
</#if>
</div>
</#macro>