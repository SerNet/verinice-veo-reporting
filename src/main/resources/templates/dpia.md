<#import "/libs/commons.md" as com>
<#import "/libs/dp-risk.md" as dpRisk>

<#assign table = com.table,
         def = com.def />

<style>
<#include "styles/default.css">
<#include "styles/default_landscape.css">
h1, h2, h3, h4 {
  page-break-after: avoid;
}

.main_page table th:first-child, .main_page table td:first-child {
  width: 8cm;
}

dt {
  font-weight: 600;
}

.risk dl dd:after{
  display: block;
  content: '';
}

.risk dt {
  font-weight: normal;
  display: inline-block;
  min-width: 7cm;
}

.risk dd {
  display: inline;
}
</style>

<#-- FIXME VEO-619/VEO-1175: maybe pass domain into report? -->
<#assign domain=domains?filter(it->it.name == 'DS-GVO')?filter(it->scope.domains?keys?seq_contains(it.id))?sort_by("createdAt")?last />
<#assign scope=dpia.findFirstLinked('process_PIAResponsibleBody')!>
<#assign riskDefinitionId=(scope.domains[domain.id].riskDefinition)! />

<div class="footer-left">
  <table>
    <tr>
      <td>Organisation: </td>
      <td>${(scope.name)!}</td>
    </tr>
    <tr>
      <td>Erstelldatum: </td>
      <td>${.now?date}</td>
    </tr>
  </table>
</div>


<div class="cover">
<h1>${bundle.title}<br/>${dpia.name}</h1>
<p>powered by verinice</p>
</div>

<#if scope?has_content>

# ${bundle.main_page}{#main_page}

<div class="main_page">

<@table 'Angaben zum Verantwortlichen',
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

| Vertretung  ||
|:---|:---|
| Leitung der verantwortlichen Stelle<br/>(einschließlich Vertreter) | ${management.person_generalInformation_givenName!} ${management.person_generalInformation_familyName!} |
| Leitung der Datenverarbeitung |  ${headOfDataProcessing.person_generalInformation_givenName!} ${headOfDataProcessing.person_generalInformation_familyName!} |


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

| Vertretung  ||
|:---|:---|
| Leitung der verantwortlichen Stelle<br/>(einschließlich Vertreter) | ${managementJointController.person_generalInformation_givenName!} ${managementJointController.person_generalInformation_familyName!} |
| Leitung der Datenverarbeitung |  ${headOfDataProcessingJointController.person_generalInformation_givenName!} ${headOfDataProcessingJointController.person_generalInformation_familyName!} |
</#if>

</div>

<div class="pagebreak"/>

# Team zur Durchführung DSFA 

<#assign ownerLinks=dpia.getLinks('process_PIAProcessOwner')!>
<#if ownerLinks?has_content>
<#list ownerLinks as personLink>
<#assign person=personLink.target />
## Projektverantwortliche

|:------------|:-----|
| Name, Vorname | ${person.person_generalInformation_familyName}, ${person.person_generalInformation_givenName} |  
| Rolle bei der Durchführung der DSFA | ${personLink.process_PIAProcessOwner_role} |
</#list>
</#if>

<#assign dataProtectionOfficer=scope.findFirstLinked('scope_dataProtectionOfficer')! />

## Datenschutzbeauftragte

|:------------|:-----|
| Name, Vorname | ${dataProtectionOfficer.person_generalInformation_familyName}, ${dataProtectionOfficer.person_generalInformation_givenName} |

<#assign representativesLinks=dpia.getLinks('process_PIARepresentatives')!>
<#if representativesLinks?has_content>
## Beteiligte Personen:
|:------------|:-----|
<#list representativesLinks as personLink>
<#assign person=personLink.target />
| Name, Vorname | ${person.person_generalInformation_familyName!}, ${person.person_generalInformation_givenName!} |  
| Rolle bei der Durchführung der DSFA | ${personLink.process_PIARepresentatives_role} |
</#list>
</#if>

<#assign processorLinks=dpia.getLinks('process_PIAProcessor')!>
<#if processorLinks?has_content>
<#list processorLinks as processorLink>
<#assign scope=processorLink.target />
## Beteiligte Auftragsverarbeiter

|:------------|:-----|
| Name des Auftzragsverarbeiters | ${scope.name} |  

</#list>
</#if>

<#if jointController?has_content>
## Gemeinsame Verantwortliche

|:------------|:-----|
| Name des Auftzragsverarbeiters | ${jointController.name} |  

</#if>

<#assign otherPersonLinks=dpia.getLinks('process_PIAOtherPersonsInvolved')!>
<#assign otherOrganizationLinks=dpia.getLinks('process_PIAOOtherOrganisationsInvolved')!>
<#if otherPersonLinks?has_content || otherOrganizationLinks?has_content>
## Weitere beteiligte Personen und oder Organisationen

|:------------|:-----|
<#list otherPersonLinks as otherPersonLink>
<#assign person=otherPersonLink.target />
| Name, Vorname | ${person.person_generalInformation_familyName!}, ${person.person_generalInformation_givenName!} |  
</#list>
<#list otherOrganizationLinks as otherOrganizationLink>
<#assign scope=otherOrganizationLink.target />
| Organisation | ${scope.name} |  
</#list></#if>

# Allgemeine Angaben zur DSFA


<@def "ID und Name der  DSFA" dpia.name />

<@def "Beschreibung der DSFA" dpia.description />

<@def "Status der DSFA" bundle['process_PRO_DPIA_status_'+dpia.domains[domain.id].status] />

<@def "Anlass der DSFA" dpia.process_PIADetails_reason?has_content?then(bundle[dpia.process_PIADetails_reason], "") />

<@def bundle.process_PIADetails_date dpia.process_PIADetails_date?date.iso />

<@def "Prüfer", (dpia.findFirstLinked('process_PIAAuditor').name)!"" />

<@def "Betroffenen Verarbeitungstätigkeit(en)", dpia.getLinked('process_PIADataProcessing')?map(it->it.name)?join(", ") />

<@def "Rat des Datenschutzbeauftragten wurde eingeholt", (dpia.process_PIADPO_advice?string(bundle.yes, bundle.no))! />

<@def "Anmerkung oder Begründung", dpia.process_PIADPO_comment />

# Akteuren und betroffenen Personen

|Betroffene Personen/Vertreter  | Standpunkt wurde eingeholt | Anmerkung oder Begründung 
|:------------|:-----|:-----|
| ${dpia.process_PIAInvolvement_affectedPersons!} | ${(dpia.process_PIAInvolvement_affectedPersonsPOV?string(bundle.yes, bundle.no))!} | ${dpia.process_PIAInvolvement_affectedPersonsComment!} |


<#if dpia.process_PIAInvolvement_stakeholderParticipation!false>

|Akteure  | Standpunkt wurde eingeholt | Anmerkung oder Begründung 
|:------------|:-----|:-----|
| ${dpia.process_PIAInvolvement_stakeholder!} | ${(dpia.process_PIAInvolvement_stakeholderPOV?string(bundle.yes, bundle.no))!} | ${dpia.process_PIAInvolvement_stakeholderComment!} |

</#if>

<#assign addidionalApplications=dpia.getLinked('process_PIADescriptionAdditionalApplications')![] />
<#assign addidionalITSystems=dpia.getLinked('process_PIADescriptionAdditionalITSystems')![] />
<#assign additionalApplicationsAndITSystems = addidionalApplications+addidionalITSystems>

# Systematische Beschreibung der Verarbeitungsvorgänge und Zwecke

|:------------|:-----|
| Beschreibung des Prüfgegenstandes | ${dpia.process_PIADescription_testObject!} |  
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

# Bewertung der Notwendigkeit und Verhältnismäßigkeit

<#if dpia.process_PIAAssessment_rulesExisting!false>
<@def "Genehmigte Verhaltensregeln", dpia.process_PIAAssessment_rules />
</#if>

<@def "Notwendigkeit und Verhältnismäßigkeitsprüfung", dpia.process_PIAAssessment_necessity />

# Risikoanalyse

<#assign processRisksInDomain = (dpia.risks?filter(it-> it.domains?keys?seq_contains(domain.id)))![] />

<#if processRisksInDomain?has_content && riskDefinitionId?has_content>

<#assign riskDefinition=domain.riskDefinitions[riskDefinitionId] />

<#list processRisksInDomain as risk>
<@dpRisk.riskdisplay risk domain riskDefinition />
</#list>

</#if>

<#list dpia.getLinked('process_PIADataProcessing') as affectedPA>

<#assign processRisksInDomain = (affectedPA.risks?filter(it-> it.domains?keys?seq_contains(domain.id)))![] />

<#if processRisksInDomain?has_content && riskDefinitionId?has_content>

<#assign riskDefinition=domain.riskDefinitions[riskDefinitionId] />

<#list processRisksInDomain as risk>
<@dpRisk.riskdisplay risk domain riskDefinition />
</#list>

</#if>

# Ergebnis der DSFA nach Durchführung

|:------------|:-----|
| Hohes Risiko | ${(dpia.process_PIAResult_risk?string(bundle.yes, bundle.no))!} ${dpia.process_PIAResult_comment} |  
| Konsultation der Aufsichtsbehörde erforderlich | ${(dpia.process_PIAResult_consultationRequired?string(bundle.yes, bundle.no))!} ${dpia.process_PIAResult_consultationJustification} |
| Konsultation durchgeführt | ${(dpia.process_PIAResult_consultationConducted?string(bundle.yes, bundle.no))!} <br/>Datum ${dpia.process_PIAResult_consultationDate?date.iso } <br/>Ergebnis der Konsultation ${dpia.process_PIAResult_consultationResult!}|
| Bemerkungen DSB | ${dpia.process_PIAResult_commentsPIO} |

</#list>
</#if>