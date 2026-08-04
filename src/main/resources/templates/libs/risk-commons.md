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

<#macro riskCell color>
  <td style="background-image: linear-gradient(to right, ${color} 0mm, ${color} 5mm, white 5mm, white);padding-left: 7mm;"><#nested></td>
</#macro>