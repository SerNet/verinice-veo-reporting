<#import "/libs/commons.md" as com>

<#function riskReductionLabel raw>
  <#return { "RISK_TREATMENT_ACCEPTANCE": "Risikoakzeptanz",
      "RISK_TREATMENT_AVOIDANCE": "Risikovermeidung",
      "RISK_TREATMENT_NONE": "Keins",
      "RISK_TREATMENT_REDUCTION": "Risikoreduktion",
      "RISK_TREATMENT_TRANSFER": "Risikotransfer"}[raw] />
</#function>

<#macro impactdisplay riskDefinition category value=""><#if value?has_content><span style="color:${riskDefinition.getImpact(category.id, value).color}">${riskDefinition.getImpact(category.id, value).label}</span></#if></#macro>

<#macro probabilitydisplay riskDefinition value=""><#if value?has_content><span style="color:${riskDefinition.getProbability(value).color}">${riskDefinition.getProbability(value).label}</span></#if></#macro>

<#macro riskCell color text>
  <#assign svg='<svg xmlns="http://www.w3.org/2000/svg" height="1" width="1"><polygon points="0,0 0,1 1,1 1,0" style="fill:${color};" /></svg>' />
  <td style="background-repeat:no-repeat;background-size:5mm 100%;background-position:bottom left;background-image: url('data:image/svg+xml;base64,${base64(svg)}');padding-left: 7mm;">${text}</td>
</#macro>

<#macro riskdisplay headinglevel risk domain riskDefinition={}>
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
<@impactdisplay riskDefinition category, riskValuesForCategory.effectiveImpact />
</#if>
<br/>
</#list>
</td>
<td><@probabilitydisplay riskDefinition, riskValues.effectiveProbability/></td>
<#assign maxInherent = riskDefinition.categories?map(c->(riskValues[c.id].inherentRisk)!-1)?max />
<#if (maxInherent > -1)>
<#assign maxInherentData = riskDefinition.getRisk(maxInherent) />
<@riskCell maxInherentData.color maxInherentData.label/>
<#else/>
<td />
</#if>
<td>
<#list riskDefinition.categories as category>
${category.id}:
<#assign riskValuesForCategory = (riskValues[category.id])! />
<#if riskValuesForCategory?has_content>
${(riskValuesForCategory.riskTreatments?map(t->riskReductionLabel(t))?join(', '))!}
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
<@riskCell maxResidualData.color maxResidualData.label/>
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
<#list risk.mitigation.parts as tom>
<#assign mitigationStatus=tom.getImplementationStatus(domain.id, riskDefinition.id)! />
<tr>
<td>${tom.designator} ${tom.name}</td>
<#if mitigationStatus?has_content>
<#assign implementationStatus = riskDefinition.getImplementationStatus(mitigationStatus) />
<@riskCell implementationStatus.color implementationStatus.label />
<#else>
<td></td>
</#if>
<td>${tom.control_implementation_explanation!}</td>
<td>${(tom.control_implementation_date?date.iso)!}</td>
</tr>
</#list>
</tbody>
</table>

</#if>
</div>
</#macro>