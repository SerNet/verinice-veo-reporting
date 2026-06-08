<#import "/libs/commons.md" as com>
<#import "/libs/dp-risk.md" as dpRisk>

<#assign table = com.table,
         def = com.def />

<style>
<@com.defaultStyles true/>
h1, h2, h3, h4 {
  page-break-after: avoid;
}

.main_page table th:first-child, .main_page table td:first-child {
  width: 8cm;
}

dt {
  font-weight: 600;
}
</style>
<#assign dpia=target />
<#assign scope=dpia.findFirstLinked('process_PIAResponsibleBody')!>

<#assign riskDefinitionId=(scope.domains[domain.id].riskDefinition)! />

<div class="footer-left">
    <table>
        <tr>
            <td>${bundle.organization}: </td>
            <td>${(scope.name)!}</td>
        </tr>
        <tr>
            <td>${bundle.creation_date}: </td>
            <td>${.now?date}</td>
        </tr>
    </table>
</div>


<div class="cover">
<h1>${bundle.title}<br/>${dpia.name}</h1>
<p>powered by verinice</p>
</div>

<#if scope?has_content>

# ${bundle.main_page} {#main_page}

<div class="main_page">

<@table bundle.controller_information,
  scope,
  ['name',
   'scope_address_address1',
   {'scope_address_postcode, scope_address_city' : 'scope_address_postcode scope_address_city'},
   'scope_contactInformation_phone / scope_contactInformation_fax',
   'scope_contactInformation_email',
   'scope_contactInformation_website'
  ]/>

<#assign management=scope.findFirstLinked('scope_management')! />
<#assign headOfDataProcessing=scope.findFirstLinked('scope_headOfDataProcessing')! />
<#assign jointController=dpia.findFirstLinked('process_PIAJointControllership')!>

|  ${bundle.representation}  ||
|:---|:---|
| ${bundle.scope_management} | ${management.name!} |
| ${bundle.scope_headOfDataProcessing}  |  ${headOfDataProcessing.name!} |


<#if jointController?has_content>
<#assign managementJointController=jointController.findFirstLinked('scope_management')! />
<#assign headOfDataProcessingJointController=jointController.findFirstLinked('scope_headOfDataProcessing')! />

Angaben zu gemeinsam Verantwortlichen

<@table 'Angaben zum gemeinsam Verantwortlichen',
  jointController,
  ['name',
   'scope_address_address1',
   {'scope_address_postcode, scope_address_city' : 'scope_address_postcode scope_address_city'},
   'scope_contactInformation_phone / scope_contactInformation_fax',
   'scope_contactInformation_email',
   'scope_contactInformation_website'
  ]/>

| ${bundle.representation}  ||
|:---|:---|
| ${bundle.scope_management} | ${managementJointController.name} |
| ${bundle.scope_headOfDataProcessing} |  ${headOfDataProcessingJointController.name} |
</#if>

</div>

<div class="pagebreak"/>

# ${bundle.team_implementation}

<#assign ownerLinks=dpia.getLinks('process_PIAProcessOwner')!>
<#if ownerLinks?has_content>
<#list ownerLinks as personLink>
<#assign person=personLink.target />
## ${bundle.process_PIAProcessOwner}

|:------------|:-----|
| ${bundle.name}, ${bundle.person_generalInformation_givenName} | ${person.person_generalInformation_familyName!}, ${person.person_generalInformation_givenName!} |
| ${bundle.process_PIAProcessOwner_role} | ${personLink.process_PIAProcessOwner_role!} |
</#list>
</#if>

<#assign dataProtectionOfficer=scope.findFirstLinked('scope_dataProtectionOfficer')! />

## ${bundle.scope_dataProtectionOfficer}

|:------------|:-----|
| ${bundle.name}, ${bundle.person_generalInformation_givenName} | ${dataProtectionOfficer.person_generalInformation_familyName!}, ${dataProtectionOfficer.person_generalInformation_givenName!} |

<#assign representativesLinks=dpia.getLinks('process_PIARepresentatives')!>
<#if representativesLinks?has_content>
## ${bundle.involved_persons}:
|:------------|:-----|
<#list representativesLinks as personLink>
<#assign person=personLink.target />
| ${bundle.name}, ${bundle.person_generalInformation_givenName} | ${person.person_generalInformation_familyName!}, ${person.person_generalInformation_givenName!} |
| ${bundle.process_PIARepresentatives_role} | ${personLink.process_PIARepresentatives_role!} |
</#list>
</#if>

<#assign processorLinks=dpia.getLinks('process_PIAProcessor')!>
<#if processorLinks?has_content>
<#list processorLinks as processorLink>
<#assign scope=processorLink.target />
## ${bundle.involved_processors}

|:------------|:-----|
| ${bundle.processor_name} | ${scope.name} |

</#list>
</#if>

<#if jointController?has_content>
## ${bundle.process_PIAJointControllership}

|:------------|:-----|
| ${bundle.processor_name} | ${jointController.name} |

</#if>

<#assign otherPersonLinks=dpia.getLinks('process_PIAOtherPersonsInvolved')!>
<#assign otherOrganizationLinks=dpia.getLinks('process_PIAOOtherOrganisationsInvolved')!>
<#if otherPersonLinks?has_content || otherOrganizationLinks?has_content>
## ${bundle.other_involved_parties}

|:------------|:-----|
<#list otherPersonLinks as otherPersonLink>
<#assign person=otherPersonLink.target />
| ${bundle.name}, ${bundle.person_generalInformation_givenName} | ${person.person_generalInformation_familyName!}, ${person.person_generalInformation_givenName!} |
</#list>
<#list otherOrganizationLinks as otherOrganizationLink>
<#assign scope=otherOrganizationLink.target />
| ${bundle.organization} | ${scope.name} |
</#list></#if>

# ${bundle.general_information}


<@def bundle.id_and_name dpia.name />

<@def bundle.dpia_desc dpia.description />

<@def bundle.dpia_status bundle['process_PRO_DPIA_status_'+dpia.domains[domain.id].status] />

<@def bundle.process_PIADetails_reason dpia.process_PIADetails_reason?has_content?then(bundle[dpia.process_PIADetails_reason], "") />

<@def bundle.process_PIADetails_date, (dpia.process_PIADetails_date?date.iso)! />

<@def bundle.process_PIAAuditor, (dpia.findFirstLinked('process_PIAAuditor').name)!"" />

<@def bundle.process_PIADataProcessing, dpia.findLinked('process_PIADataProcessing')?map(it->it.name)?join(", ") />

<@def bundle.process_PIADPO_advice, (dpia.process_PIADPO_advice?string(bundle.yes, bundle.no))! />

<@def bundle.comment_or_reason dpia.process_PIADPO_comment />

# ${bundle.affected_persons}

|${bundle.process_PIAInvolvement_affectedPersons}  | ${bundle.process_PIAInvolvement_affectedPersonsPOV} | ${bundle.comment_or_reason}
|:------------|:-----|:-----|
| ${dpia.process_PIAInvolvement_affectedPersons!} | ${(dpia.process_PIAInvolvement_affectedPersonsPOV?string(bundle.yes, bundle.no))!} | ${dpia.process_PIAInvolvement_affectedPersonsComment!} |


<#if dpia.process_PIAInvolvement_stakeholderParticipation!false>

|${bundle.process_PIAInvolvement_stakeholder}  | ${bundle.process_PIAInvolvement_stakeholderPOV} | ${bundle.comment_or_reason}
|:------------|:-----|:-----|
| ${dpia.process_PIAInvolvement_stakeholder!} | ${(dpia.process_PIAInvolvement_stakeholderPOV?string(bundle.yes, bundle.no))!} | ${dpia.process_PIAInvolvement_stakeholderComment!} |

</#if>

<#assign addidionalApplications=dpia.findLinked('process_PIADescriptionAdditionalApplications') />
<#assign addidionalITSystems=dpia.findLinked('process_PIADescriptionAdditionalITSystems') />
<#assign additionalApplicationsAndITSystems = addidionalApplications+addidionalITSystems>

# ${bundle.systematic_description}

|:------------|:-----|
| ${bundle.description_test_object} | ${dpia.process_PIADescription_testObject!} |
<#if dpia.process_PIADescription_intendedPurpose!false>
| ${bundle.process_PIADescription_intendedPurpose} | ${dpia.process_PIADescription_intendedPurposeAdditions!} |
</#if>
<#if dpia.process_PIADescription_legalBasis!false>
| ${bundle.process_PIADescription_legalBasis} | ${dpia.process_PIADescription_legalBasisAdditions!} |
</#if>
<#if dpia.process_PIADescription_dataType!false>
| ${bundle.process_PIADescription_dataType} | ${dpia.process_PIADescription_dataTypeAdditions!} |
</#if>
<#if dpia.process_PIADescription_deletionPeriods!false>
| ${bundle.process_PIADescription_deletionPeriods} | ${dpia.process_PIADescription_deletionPeriodsAdditions!} |
</#if>
<#if dpia.process_PIADescription_affectedPersons!false>
| ${bundle.process_PIADescription_affectedPersons} | ${dpia.process_PIADescription_affectedPersonsAdditions!} |
</#if>
<#if dpia.process_PIADescription_dataTransfer!false>
| ${bundle.process_PIADescription_dataTransfer} | ${dpia.process_PIADescription_dataTransferAdditions!} |
</#if>
<#if dpia.process_PIADescription_accessAuthorisation!false>
| ${bundle.process_PIADescription_accessAuthorisation} | ${dpia.process_PIADescription_accessAuthorisationsAdditions!} |
</#if>
<#if dpia.process_PIADescription_informationObligations!false>
| ${bundle.process_PIADescription_informationObligations} | ${dpia.process_PIADescription_informationObligationsAdditions!} |
</#if>
<#if dpia.process_PIADescription_applicationsSystems!false>
| ${bundle.process_PIADescription_applicationsSystems} | ${additionalApplicationsAndITSystems?map(it->it.name)?join(", ")} | 
</#if>

# ${bundle.assessment}

<#if dpia.process_PIAAssessment_rulesExisting!false>
<@def bundle.process_PIAAssessment_rules, dpia.process_PIAAssessment_rules />
</#if>

<@def bundle.process_PIAAssessment_necessity, dpia.process_PIAAssessment_necessity />

# ${bundle.risk_analysis}

<#assign processRisksInDomain = (dpia.risks?filter(it-> it.domains?keys?seq_contains(domain.id)))![] />

<#if processRisksInDomain?has_content && riskDefinitionId?has_content>

<#assign riskDefinition=domain.riskDefinitions[riskDefinitionId] />

<#list processRisksInDomain as risk>
<@dpRisk.riskdisplay 2 dpia risk domain riskDefinition />
</#list>

</#if>

<#list dpia.findLinked('process_PIADataProcessing') as affectedPA>

<#assign processRisksInDomain = (affectedPA.risks?filter(it-> it.domains?keys?seq_contains(domain.id)))![] />

<#if processRisksInDomain?has_content && riskDefinitionId?has_content>

<#assign riskDefinition=domain.riskDefinitions[riskDefinitionId] />

<#list processRisksInDomain as risk>
<@dpRisk.riskdisplay 2 affectedPA risk domain riskDefinition />
</#list>

</#if>

# ${bundle.dpia_results}

|:------------|:-----|
| ${bundle.high_risk} | ${(dpia.process_PIAResult_risk?string(bundle.yes, bundle.no))!} <#if dpia.process_PIAResult_comment?has_content><br/>${dpia.process_PIAResult_comment}</#if> |
| ${bundle.process_PIAResult_consultationRequired} | ${(dpia.process_PIAResult_consultationRequired?string(bundle.yes, bundle.no))!} <#if dpia.process_PIAResult_consultationJustification?has_content><br/>${dpia.process_PIAResult_consultationJustification}</#if> |
| ${bundle.process_PIAResult_consultationConducted} | ${(dpia.process_PIAResult_consultationConducted?string(bundle.yes, bundle.no))!} <#if dpia.process_PIAResult_consultationDate?has_content><br/>${bundle.date} ${dpia.process_PIAResult_consultationDate?date.iso } <br/>${bundle.consultation_result} ${dpia.process_PIAResult_consultationResult!}</#if>|
| ${bundle.pio_comments} | ${dpia.process_PIAResult_commentsPIO!} |

</#list>
</#if>