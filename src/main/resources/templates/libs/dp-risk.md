<#import "/libs/commons.md" as com>
<#import "/libs/risk-commons.md" as rcom>

<#-- TODO: #3385: use domain-specific status -->
<#assign statusMap = {
  'YES': {"color":"#12AE0F","labels":{"de":"ja","en":"yes"}},
  'NO': {"color":"#AE0D11","labels":{"de":"nein","en":"no"}},
  'PARTIAL': {"color":"#EDE92F","labels":{"de":"teilweise","en":"partial"}},
  'N_A': {"color":"#49A2ED","labels":{"de":"nicht anwendbar","en":"not applicable"}}
} />

<#function getImplementationStatus ri>
  <#local mitigationStatus=statusMap[ri.status]!/>
  <#if mitigationStatus?has_content>
    <#return {"color":mitigationStatus.color, "label":mitigationStatus.labels[.lang]}/>
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

<@com.def "Risikoverantwortlicher/-Eigentümer", (risk.riskOwner.name)! />

<@com.def "Risikobeschreibung", scenario.description />

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
<th colspan="3">Risikobewertung vor Maßnahmen</th>
<th>Risikobehandlung</th>
<th colspan="1">Risikobewertung nach Maßnahmen</th>
</tr>
<tr>
<th>Effektive Auswirkung</th>
<th>Effektive Eintritts&shy;wahrscheinlichkeit</th>
<th>Bruttorisiko (maximaler Wert)</th>
<th>Risikobehandlungsoptionen</th>
<th>Nettorisiko (maximaler Wert)</th>
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
<th colspan="4">Maßnahmen</th>
</tr>
<tr>
<th>Maßnahmentitel</th>
<th>Umsetzungs&shy;status</th>
<th>Umsetzungserläuterung</th>
<th>Umset&shy;zungs&shy;datum</th>
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