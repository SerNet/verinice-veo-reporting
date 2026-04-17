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
    <#return {"color":mitigationStatus.color, "label":messages.msg["STATUS_" + ri.status]!ri.status}/>
  </#if>
</#function>

<#function getRI riskAffected control>
  <#return riskAffected.requirementImplementations?filter(ri->ri.control.id == control.id)?first>
</#function>

<#assign maxSteps = 1000>
<#function getDescendants composite>
  <#local result = composite.parts>
  <#local currentStep=result>
  <#list 0..maxSteps as iteration>
    <#if currentStep?size==0><#break></#if>
    <#if iteration == maxSteps>
      <#stop "Failed to determine descendants for ${composite.name} in ${maxSteps} steps">
    </#if>
    <#local nextStep=[]>
    <#list currentStep as item>
      <#list item.parts as part>
        <#if !result?seq_contains(part)>
          <#local result = result + [part]>
          <#local nextStep = nextStep + [part]>
        </#if>
      </#list>
    </#list>
    <#local currentStep=nextStep>
  </#list>
  <#return result>
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
  <col span="1" style="width: 10%;">
  <col span="1" style="width: 12%;">
  <col span="1" style="width: 51%;">
  <col span="1" style="width: 12%;">
</colgroup>
<thead>
<tr>
<th colspan="3">${messages.assessment_pre}</th>
<th>${messages.treatment_header}</th>
<th colspan="1">${messages.assessment_post}</th>
</tr>
<tr>
<th>${messages.eff_impact}</th>
<th>${messages.eff_prob}</th>
<th>${messages.gross_risk}</th>
<th>${messages.treatment_options}</th>
<th>${messages.net_risk}</th>
</tr>
</thead>
<tbody>
<tr>
<td>
<#list riskDefinition.categories as category>
<#assign riskValuesForCategory = (riskValues[category.id])! />
${category.id}:
<#if riskValuesForCategory?has_content>
<@rcom.impactdisplay riskDefinition category, riskValuesForCategory.effectiveImpact />
</#if>
<br/>
</#list>
</td>
<td><@rcom.probabilitydisplay riskDefinition, riskValues.effectiveProbability/></td>
<#assign maxInherent = riskDefinition.categories?map(c->(riskValues[c.id].inherentRisk)!-1)?max />
<#if (maxInherent > -1)>
<#assign maxInherentData = riskDefinition.getRisk(maxInherent) />
<@rcom.riskCell maxInherentData.color>${maxInherentData.label}</@rcom.riskCell>
<#else/>
<td />
</#if>
<td>
<#list riskDefinition.categories as category>
${category.id}:
<#assign riskValuesForCategory = (riskValues[category.id])! />
<#if riskValuesForCategory?has_content>
${(riskValuesForCategory.riskTreatments?map(t->rcom.riskReductionLabel(t))?join(', '))!}
<#if (riskValuesForCategory.riskTreatmentExplanation)?has_content>
<br/>
${riskValuesForCategory.riskTreatmentExplanation}
</#if>
</#if>
<br/>
</#list>
</td>
<#assign maxResidual = riskDefinition.categories?map(c->(riskValues[c.id].residualRisk)!-1)?max />
<#if (maxResidual > -1)>
<#assign maxResidualData = riskDefinition.getRisk(maxResidual) />
<@rcom.riskCell maxResidualData.color>${maxResidualData.label}</@rcom.riskCell>
<#else/>
<td />
</#if>
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
<#list getDescendants(risk.mitigation) as tom>
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