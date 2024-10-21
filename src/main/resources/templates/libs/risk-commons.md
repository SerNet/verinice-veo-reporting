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
  <#assign svg='<svg xmlns="http://www.w3.org/2000/svg" height="1" width="1"><polygon points="0,0 0,1 1,1 1,0" style="fill:${color};" /></svg>' />
  <td style="background-repeat:no-repeat;background-size:5mm 100%;background-position:bottom left;background-image: url('data:image/svg+xml;base64,${base64(svg)}');padding-left: 7mm;"><#nested></td>
</#macro>