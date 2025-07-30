<#import "/libs/commons.md" as com>
<#import "/libs/itbp-commons.md" as icom>
<#import "/libs/risk-commons.md" as rcom>

<#macro riskdisplay headinglevel targetObject risk domain riskDefinition={}>

<div class="risk">

<#assign scenario = risk.scenario>

<#list 0..<headinglevel as i>#</#list> ${icom.title(scenario)}

<@com.def bundle.risk_owner, (risk.riskOwner.name)! />

<@com.def bundle.risk_description, scenario.description />

<#if riskDefinition?has_content>

<#assign riskCategoriesWithMatrix=(riskDefinition.categories?filter(it->it.valueMatrix?has_content))>

<#assign riskValues = (risk.getRiskValues(domain.id, riskDefinition.id))!>

<#if riskValues?has_content && (riskValues.effectiveProbability?has_content
                              || riskValues.effectiveProbabilityExplanation?has_content
                              || riskCategoriesWithMatrix?map(it->riskValues[it.id])?filter(
                                   it->it.effectiveImpact?has_content
                                   || it.specificImpactExplanation?has_content
                                   || it.inherentRisk?has_content
                                   || it.riskTreatments?has_content
                                   || it.riskTreatmentExplanation?has_content
                                   || it.residualRisk?has_content
                                   || it.residualRiskExplanation?has_content
                                 )?has_content)>

<table class="table" style="width:100%;font-size:70%;">
<colgroup>
<#assign additionalwidth=0>
<#if (riskCategoriesWithMatrix?size > 1)>
  <#assign additionalwidth=3>
  <col span="1" style="width: ${5*additionalwidth}%;">
</#if>
  <col span="1" style="width: ${22-additionalwidth}%;">
  <col span="1" style="width: ${20-additionalwidth}%;">
  <col span="1" style="width: ${14-additionalwidth}%;">
  <col span="1" style="width: ${22-additionalwidth}%;">
  <col span="1" style="width: ${22-additionalwidth}%;">
</colgroup>
<thead>
<tr>
<#if (riskCategoriesWithMatrix?size > 1)>
<th colspan="1">${bundle.criterion}</th>
</#if>
<th colspan="3">${bundle.risk_assessment_before_controls}</th>
<th>${bundle.risk_treatment}</th>
<th colspan="1">${bundle.risk_assessment_after_controls}</th>
</tr>
<tr>
<#if (riskCategoriesWithMatrix?size > 1)>
<th />
</#if>
<th>${bundle.effective_impact}</th>
<th>${bundle.effective_probability}</th>
<th>${bundle.gross_risk}</th>
<th>${bundle.risk_treatment_options}</th>
<th>${bundle.net_risk}</th>
</tr>
</thead>
<tbody>
<#list riskCategoriesWithMatrix as category>
<#assign riskValuesForCategory = (riskValues[category.id])! />
<tr>
<#if (riskCategoriesWithMatrix?size > 1)>
<td>${category.translations[.lang].name}&nbsp;(${category.id})</td>
</#if>
<td>
<#if riskValuesForCategory?has_content>
<@rcom.impactdisplay riskDefinition category, riskValuesForCategory.effectiveImpact /><#if riskValuesForCategory.specificImpactExplanation?has_content><br/>${riskValuesForCategory.specificImpactExplanation}</#if>
</#if>
<br/>
</td>
<#if category?index == 0>
<td rowspan="${riskCategoriesWithMatrix?size}"><@rcom.probabilitydisplay riskDefinition, riskValues.effectiveProbability/><#if riskValues.specificProbabilityExplanation?has_content><br/>${riskValues.specificProbabilityExplanation}</#if></td>
</#if>
<#assign inherent = riskValuesForCategory.inherentRisk! />
<#if inherent?has_content >
<#assign inherentData = riskDefinition.getRisk(inherent) />
<@rcom.riskCell inherentData.color>${inherentData.label}</@rcom.riskCell>
<#else/>
<td />
</#if>
<td>
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

</tr>
</#list>
</tbody>
</table>
<#else>
${bundle.no_assessment_for_scenario}
</#if>

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
<th colspan="4">${bundle.mitigation_controls}</th>
</tr>
<tr>
<th>${bundle.mitigation_control_title}</th>
<th>${bundle.mitigation_control_implementation_status}</th>
<th>${bundle.mitigation_control_implementation_explanation}</th>
<th>${bundle.mitigation_control_implementation_date}</th>
</tr>
</thead>
<tbody>
<#list icom.sortModules(risk.mitigation.parts) as part>
<#assign ri = targetObject.requirementImplementations?filter(it->it.control._self == part._self)?first!>
<tr>
<td>${icom.controlTitle(part)}</td>
<td>${(bundle[ri.status])!}</td>
<td>${ri.implementationStatement!}</td>
<td>${(ri.implementationUntil?date.iso)!}</td>
</tr>
</#list>
</tbody>
</table>

</#if>
</div>
</#macro>