<#import "/libs/commons.md" as com>

<#assign table = com.table,
         def = com.def />

<style>
<#include "styles/default.css">
h1, h2, h3, h4 {
  page-break-after: avoid;
}

.main_page table th:first-child, .main_page table td:first-child {
  width: 8cm;
}

dt {
  font-weight: 600;
}

dl {
  page-break-inside: avoid;
}

.section {
  page-break-inside: avoid;
}
</style>

<#-- FIXME VEO-619/VEO-1175: maybe pass domain into report? -->
<#assign scope=incident.findFirstLinked('incident_notifyingCompany')!>

<div class="footer-left">
  <table>
    <tr>
      <td>${bundle.organization}: </td>
      <td>${(scope.name)!}</td>
      <td>${bundle.time_zone_hint}: </td>
      <td>${timeZone}</td>
    </tr>
    <tr>
      <td>${bundle.creation_date}: </td>
      <td>${.now?date}</td>
    </tr>
  </table>
</div>


<div class="cover">
<h1>${bundle.title}</h1>
<p>powered by verinice</p>
</div>


# ${bundle.main_page} {#main_page}

<div class="main_page">

<#-- FIXME VEO-619/VEO-1175: maybe pass domain into report? -->
<#assign domain=domains?filter(it->it.name == 'DS-GVO')?filter(it->incident.domains?keys?seq_contains(it.id))?sort_by("createdAt")?last />

<#if scope?has_content>
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

<#if management?has_content && headOfDataProcessing?has_content>
| ${bundle.representation}  ||
|:---|:---|
| ${bundle.scope_management} | ${management.name!} |
| ${bundle.scope_headOfDataProcessing}  |  ${headOfDataProcessing.name!} |
</#if>

<#assign dataProtectionOfficer=scope.findFirstLinked('scope_dataProtectionOfficer')! />

<#if dataProtectionOfficer?has_content>
<@table bundle.data_protection_officer,
  dataProtectionOfficer,
  [
   'name',
   {'person_address_postcode, person_address_city' : 'person_address_postcode person_address_city'},
   'person_contactInformation_office / person_contactInformation_fax',
   'person_contactInformation_email'
  ]/>
</#if>
  
<#assign informationSecurityOfficer=scope.findFirstLinked('scope_informationSecurityOfficer')! />

<#if informationSecurityOfficer?has_content>
<@table bundle.information_security_officer,
  informationSecurityOfficer,
  [
   'name',
   {'person_address_postcode, person_address_city' : 'person_address_postcode person_address_city'},
   'person_contactInformation_office / person_contactInformation_fax',
   'person_contactInformation_email'
  ]/>
</#if>
</#if>
<div class="pagebreak"/>

# ${bundle.overview_data_breach}

<@def bundle.incident_name incident.name />

<@def bundle.description incident.description />

<@def bundle.status bundle['incident_INC_DataPrivacyIncident_status_'+incident.domains[domain.id].status] />

<#-- ☑ and ☐ are not suppported in Open Sans, we need to find another solution -->
<#macro checkbox selected>
<#if selected>
<#local svg='<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill="#767676" d="M22 2v20h-20v-20h20zm2-2h-24v24h24v-24zm-6 16.538l-4.592-4.548 4.546-4.587-1.416-1.403-4.545 4.589-4.588-4.543-1.405 1.405 4.593 4.552-4.547 4.592 1.405 1.405 4.555-4.596 4.591 4.55 1.403-1.416z"/></svg>'>
<#else>
<#local svg='<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill="#767676" d="M22 2v20h-20v-20h20zm2-2h-24v24h24v-24z"/></svg>'>
</#if>
<img alt="${selected?string('☑', '☐')}" style="height:1em;width:1em;position:relative;top:2px;background-repeat:no-repeat;background-size:100%;background-image: url('data:image/svg+xml;base64,${base64(svg)}');"/>
</#macro>

<#macro checkbox_seq seq value>
  <#compress>
    <@checkbox seq?seq_contains(value)/> ${bundle[value]}
  </#compress>
</#macro>

<#macro missing_property>
<div style="color:#cd1719">Diese Information wurde nicht erfasst, sie ist aber erforderlich!</div>
</#macro>


<#assign notificationType=incident.incident_dataBreach_notificationType!>
<#if notificationType?has_content>
${bundle.incident_dataBreach_notificationType}
: <@checkbox_seq notificationType 'incident_dataBreach_notificationType_completeNotification' />
: <@checkbox_seq notificationType 'incident_dataBreach_notificationType_temporaryNotification' />
: <@checkbox_seq notificationType 'incident_dataBreach_notificationType_followupNotification' />
</#if>

<@def bundle.incident_contactPerson, (incident.findFirstLinked('incident_contactPerson').name)! />


<#assign processorLinks=incident.getLinks('incident_processor')!>
<#assign jointControllerLinks=incident.getLinks('incident_jointControllership')!>
<#assign additionalParticipants=incident.incident_involvedParticipants_additional!>

<#if processorLinks?has_content || jointControllerLinks?has_content || additionalParticipants?has_content>
# ${bundle.involved_participants}

<#if processorLinks?has_content>
## ${bundle.incident_processor}
|:------------|:-----|
<#list processorLinks as processorLink>
<#assign scope=processorLink.target />
| ${bundle.incident_processor_name} | ${scope.name} |
| ${bundle.incident_processor_comments} | ${processorLink.incident_processor_comments!} |
</#list>
</#if>

<#if jointControllerLinks?has_content>
# ${bundle.incident_jointControllership}
|:------------|:-----|
<#list jointControllerLinks as jointControllerLink>
<#assign jointController=jointControllerLink.target />
| ${bundle.incident_organization_name} | ${jointController.name} |
| ${bundle.incident_jointControllership_comments} | ${jointControllerLink.incident_jointControllership_comments!} |
</#list>

</#if>

<@def bundle.incident_involvedParticipants_additional incident.incident_involvedParticipants_additional />

</#if>

<div class="section">

# ${bundle.general_information_about_incident}

<@def bundle.incident_description_description incident.incident_description_description />

<#assign location=incident.incident_description_locationOfIncident!>
<#if location?has_content>
<@def bundle.incident_description_locationOfIncident bundle[location] />
</#if>

<@def bundle.incident_description_howIncidentNoticed, (incident.incident_description_howIncidentNoticed?map(item->bundle[item])?join(', '))! />

<@def bundle.incident_description_others incident.incident_description_others />

<#-- Read those from the risk definition -->
<#assign protectionGoals = ['confidentiality', 'integrity', 'availability', 'resilience']>

<#assign affectedProtectionGoals = protectionGoals?filter(g->incident["incident_${g}_affected"]!false) />

<@def bundle.affected_protection_goals, (affectedProtectionGoals?map(item->bundle[item])?join(', '))! />

<@def bundle.incident_description_timeOfIncidentKnown, incident.incident_description_timeOfIncidentKnown />

<@def bundle.incident_description_dateTimeOfIncident, (incident.incident_description_dateTimeOfIncident?datetime.iso)! />

<@def bundle.incident_description_discoveryDateTimeOfIncident, (incident.incident_description_discoveryDateTimeOfIncident?datetime.iso)! />

<@def bundle.incident_description_stillContinues incident.incident_description_stillContinues />

<@def bundle.incident_description_durationOfIncident, incident.incident_description_durationOfIncident />

</div>
<div class="section">

# ${bundle.detailed_information}

## ${bundle.reporting_to_controller}

<#assign reportingPersonLinks=incident.getLinks('incident_reportingPerson')!>
<#if reportingPersonLinks?has_content>
|:------------|:-----|
<#list reportingPersonLinks as reportingPersonLink>
<#assign reportingPerson=reportingPersonLink.target />
| ${bundle.incident_reportingPerson} | ${reportingPerson.name} |
<#if reportingPersonLink.incident_reportingPerson_role?has_content>
| ${bundle.incident_reportingPerson_role} | ${reportingPersonLink.incident_reportingPerson_role!} |
</#if>>  
</#list>
</#if>

<@def bundle.incident_description_reportDateTimeOfIncident, (incident.incident_description_reportDateTimeOfIncident?datetime.iso)! />
</div>

## ${bundle.presumed_motivation}

<#assign typeOfAttack=incident.incident_presumedMotivation_typeOfAttack!>
<#if typeOfAttack?has_content>
<@def bundle.incident_presumedMotivation_typeOfAttack bundle[typeOfAttack] />
</#if>

<@def bundle.incident_presumedMotivation_presumedMotivation, (incident.incident_presumedMotivation_presumedMotivation?map(item->bundle[item])?join(', '))! />

<@def bundle.incident_description_others incident.incident_description_others />

<@def bundle.incident_presumedMotivation_collectedData, (incident.incident_presumedMotivation_collectedData?map(item->bundle[item])?join(', '))! />

<@def bundle.incident_presumedMotivation_otherPresumedMotivation incident.incident_presumedMotivation_otherPresumedMotivation />

<#assign lawEnforcement=incident.incident_presumedMotivation_lawEnforcement!>
<#if lawEnforcement?has_content>
<@def bundle.incident_presumedMotivation_lawEnforcement bundle[lawEnforcement] />
</#if>

## ${bundle.presumed_or_actual_causes}

<@def bundle.incident_cause_physicalDamage, (incident.incident_cause_physicalDamage?map(item->bundle[item])?join(', '))! />

<@def bundle.incident_cause_technicalFailure, (incident.incident_cause_technicalFailure?map(item->bundle[item])?join(', '))! />

<@def bundle.incident_cause_organizationalCause, (incident.incident_cause_organizationalCause?map(item->bundle[item])?join(', '))! />

<@def bundle.incident_cause_failureOfInfrastructureUsed, (incident.incident_cause_failureOfInfrastructureUsed?map(item->bundle[item])?join(', '))! />

<@def bundle.incident_cause_technicalAttack, (incident.incident_cause_technicalAttack?map(item->bundle[item])?join(', '))! />

<@def bundle.incident_cause_others incident.incident_cause_others />

<@def bundle.incident_cause_details incident.incident_cause_details />

<div class="section">

## ${bundle.concerned_targets}

<#assign concernedApplicationsLinks=incident.getLinks('incident_concernedApplications')!>
<#if concernedApplicationsLinks?has_content>
### ${bundle.incident_concernedApplications}
<#list concernedApplicationsLinks as concernedApplicationsLink>
<#assign concernedApplication=concernedApplicationsLink.target />
#### ${concernedApplication.name}
<@def bundle.incident_concernedApplications_description concernedApplicationsLink.incident_concernedApplications_description />

</#list>
</#if>

<#assign concernedItsystemsLinks=incident.getLinks('incident_concernedItsystems')!>
<#if concernedItsystemsLinks?has_content>
### ${bundle.incident_concernedItsystems}
<#list concernedItsystemsLinks as concernedItsystemsLink>
<#assign concernedItsystem=concernedItsystemsLink.target />
#### ${concernedItsystem.name}
<@def bundle.incident_concernedItsystems_description concernedItsystemsLink.incident_concernedItsystems_description />

</#list>
</#if>

<#assign concernedDataProcessingLinks=incident.getLinks('incident_dataProcessing')!>
<#if concernedDataProcessingLinks?has_content>
## ${bundle.incident_dataProcessing}
<#list concernedDataProcessingLinks as concernedDataProcessingLink>
<#assign concernedDataProcessing=concernedDataProcessingLink.target />
### ${concernedDataProcessing.name}
</#list>
</#if>
</div>


<div class="section">

<@def bundle.incident_dataType, (incident.findLinked('incident_dataType')?map(item->item.name)?join(', '))! />

<@def bundle.incident_dataConcerned_numberOfPersonalDataRecords incident.incident_dataConcerned_numberOfPersonalDataRecords />

<@def bundle.incident_dataConcerned_categoriesDataSubjects, (incident.incident_dataConcerned_categoriesDataSubjects?map(item->bundle[item])?join(', '))! />

<@def bundle.incident_dataConcerned_numberOfDataSubjectsConcerned incident.incident_dataConcerned_numberOfDataSubjectsConcerned />

<@def bundle.incident_dataConcerned_comments incident.incident_dataConcerned_comments />

</div>

<div class="section">

## ${bundle.likely_consequences}

<@def bundle.incident_potentialEffect_impactOnDataSubjects, (incident.incident_potentialEffect_impactOnDataSubjects?map(item->bundle[item])?join(', '))! />

<#-- FIXME: does not exist 
<@def bundle.incident_potentialEffect_other incident.incident_potentialEffect_other />
 -->
 
<@def bundle.incident_potentialEffect_reason incident.incident_potentialEffect_reason />

<@def bundle.incident_potentialEffect_valuationDataBreach, (bundle[incident.incident_potentialEffect_valuationDataBreach])! />

</div>

<div class="section">

## ${bundle.notification_of_persons_concerned}

<@def bundle.incident_notificationAffectedPersons_highRisk incident.incident_notificationAffectedPersons_highRisk />

<@def bundle.incident_notificationAffectedPersons_reasonRisk incident.incident_notificationAffectedPersons_reasonRisk />

<#assign dataSubjectsInformed=incident.incident_notificationAffectedPersons_dataSubjectsInformed!>

<#if dataSubjectsInformed?has_content>
<@def bundle.incident_notificationAffectedPersons_dataSubjectsInformed, dataSubjectsInformed />

<#if dataSubjectsInformed>

<@def bundle.incident_notificationAffectedPersons_timeOfNotification, (incident.incident_notificationAffectedPersons_timeOfNotification?date.iso)! />

<@def bundle.incident_notificationAffectedPersons_formOfNotification incident.incident_notificationAffectedPersons_formOfNotification />

<@def bundle.incident_notificationAffectedPersons_contentNotification incident.incident_notificationAffectedPersons_contentNotification />

<#else>
<@def bundle.incident_notificationAffectedPersons_reasonNonNotification, incident.incident_notificationAffectedPersons_reasonNonNotification />

</#if>
<#else>
<@missing_property />
</#if>

<@def bundle.incident_notificationAffectedPersons_comments, incident.incident_notificationAffectedPersons_comments />

</div>

<div class="section">

## ${bundle.countermeasures_initiated}

<#assign measuresTakenLinks=incident.getLinks('incident_measuresTaken')!>
<#if measuresTakenLinks?has_content>
### ${bundle.incident_measuresTaken}
<#list measuresTakenLinks as measuresTakenLink>
<#assign measureTaken=measuresTakenLink.target />
 - ${measureTaken.name}
</#list>
</#if>

<#assign measuresPlannedLinks=incident.getLinks('incident_measuresPlaned')!> <#--  sic -->
<#if measuresPlannedLinks?has_content>
### ${bundle.incident_measuresPlaned} <#--  sic -->
<#list measuresPlannedLinks as measuresPlannedLink>
<#assign measuresPlanned=measuresPlannedLink.target />
- ${measuresPlanned.name}
<#if measuresPlannedLink.incident_measuresPlaned_comment?has_content><#--  sic -->
  <br/>${bundle.incident_measuresPlaned_comment}: ${measuresPlannedLink.incident_measuresPlaned_comment} <#--  sic -->
</#if>
</#list>

<#assign privacyRiskLinks=incident.getLinks('incident_privacyRisk')!>
<#if privacyRiskLinks?has_content>
### ${bundle.incident_privacyRisk}
<#list privacyRiskLinks as privacyRiskLink>
<#assign privacyRisk=privacyRiskLink.target />
 - ${privacyRisk.name}
</#list>
</#if>
</#if>

</div>

<div class="section">

## ${bundle.notification_to_supervisory_authority}

<#assign notificationMade=incident.incident_supervisoryAuthority_notificationMade!>
<#if notificationMade?has_content>

<@def bundle.incident_supervisoryAuthority_notificationMade notificationMade />

<#if notificationMade>
<@def bundle.incident_supervisoryAuthority_timeDateNotification, (incident.incident_supervisoryAuthority_timeDateNotification?datetime.iso)! />

<#assign withinHours=incident.incident_supervisoryAuthority_withinHours!>

<#if withinHours?has_content>
<@def bundle.incident_supervisoryAuthority_withinHours bundle[withinHours] />

<#if withinHours != "incident_supervisoryAuthority_withinHours_within72Hours">
<@def bundle.incident_supervisoryAuthority_delayReason incident.incident_supervisoryAuthority_delayReason />
</#if>
</#if>
</#if>
<#else>
<@missing_property />
</#if>
<#assign otherAuthorities=incident.incident_supervisoryAuthority_otherAuthorities!>

<#if otherAuthorities?has_content>
<@def bundle.incident_supervisoryAuthority_otherAuthorities otherAuthorities />
<#if otherAuthorities>
<@def bundle.incident_supervisoryAuthority_nameOfOtherAuthorities, incident.incident_supervisoryAuthority_nameOfOtherAuthorities />
</#if>
</#if>
<@def bundle.incident_supervisoryAuthority_comments incident.incident_supervisoryAuthority_comments />

</div>

<#assign crossBorder=incident.incident_failure_crossBorder!>
<#if crossBorder?has_content>

<div class="section">

## ${bundle.cross_border_incident}

<@def bundle.incident_failure_crossBorder crossBorder />
<#if crossBorder>
<@def bundle.incident_failure_affectedStates, incident.incident_failure_affectedStates />
</#if>

</div>

</#if>