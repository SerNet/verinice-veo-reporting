<#import "/libs/commons.md" as com>

<#function riskReductionLabel raw>
  <#return { "RISK_TREATMENT_ACCEPTANCE": "Risikoakzeptanz",
      "RISK_TREATMENT_AVOIDANCE": "Risikovermeidung",
      "RISK_TREATMENT_NONE": "Keins",
      "RISK_TREATMENT_REDUCTION": "Risikoreduktion",
      "RISK_TREATMENT_TRANSFER": "Risikotransfer"}[raw] />
</#function>

<#macro impactdisplay riskDefinition category value><#if value?has_content><span style="color:${riskDefinition.getImpact(category.id, value).color}">${riskDefinition.getImpact(category.id, value).label}</span></#if></#macro>

<#macro probabilitydisplay riskDefinition value><#if value?has_content><span style="color:${riskDefinition.getProbability(value).color}">${riskDefinition.getProbability(value).label}</span></#if></#macro>

<#macro cellStyle color>
  style="background-color:${color};color:${colorContrast(color, '#e3e3e3', '#7c7c7b', '#929292', '#ffffff')}"
</#macro>


<#macro riskdisplay risk domain riskDefinition={}>
<div class="risk">

<#assign scenario = risk.scenario>

### ${(scenario.name)!} (${risk.designator})

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
<td><@probabilitydisplay riskDefinition, (riskValues.effectiveProbability)!/></td>
<#assign maxInherent = riskDefinition.categories?map(c->(riskValues[c.id].inherentRisk)!-1)?max />
<#if (maxInherent > -1)>
<#assign maxInherentData = riskDefinition.getRisk(maxInherent) />
<td <@dpRisk.cellStyle maxInherentData.color />>${maxInherentData.label}</td>
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
<td <@dpRisk.cellStyle maxResidualData.color />>${maxResidualData.label}</td>
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
<td style="color:${implementationStatus.color}">${implementationStatus.label}</td>
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