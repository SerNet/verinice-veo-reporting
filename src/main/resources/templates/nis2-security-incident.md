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
<#assign incident=nis2incident>
<#assign scope=incident.findFirstLinked('incident_notifyingOrganization')!>

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
<#assign domain=domains?filter(it->it.name == 'NIS2')?filter(it->incident.domains?keys?seq_contains(it.id))?sort_by("createdAt")?last />

<#if scope?has_content>
<@table bundle.org_information,
  scope,
  ['name',
   'scope_address_address1',
   {'scope_address_postcode, scope_address_city' : 'scope_address_postcode scope_address_city'},
   'scope_contactInformation_phone / scope_contactInformation_fax',
   'scope_contactInformation_email',
   'scope_contactInformation_website'
  ]/>



</#if>
<div class="pagebreak"/>

# ${bundle.overview_incident}

<@def bundle.incident_notifyingOrganization, (scope.name!) />

<@def bundle.incident_name incident.name />

<@def bundle.description incident.description />

<@def bundle.status bundle['incident_INC_SecurityIncident_status_'+incident.domains[domain.id].status] />

<#-- ☑ and ☐ are not suppported in Open Sans, we need to find another solution -->
<#macro checkbox selected>
<#if selected>
<#local svg='<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill="#767676" d="M22 2v20h-20v-20h20zm2-2h-24v24h24v-24zm-6 16.538l-4.592-4.548 4.546-4.587-1.416-1.403-4.545 4.589-4.588-4.543-1.405 1.405 4.593 4.552-4.547 4.592 1.405 1.405 4.555-4.596 4.591 4.55 1.403-1.416z"/></svg>'>
<#else>
<#local svg='<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill="#767676" d="M22 2v20h-20v-20h20zm2-2h-24v24h24v-24z"/></svg>'>
</#if>
<img alt="${selected?string('☑', '☐')}" style="height:1em;width:1em;position:relative;top:2px;background-repeat:no-repeat;background-size:100%;background-image: url('data:image/svg+xml;base64,${base64(svg)}');"/>
</#macro>

<#macro checkbox_cmp val1 val2>
  <#compress>
    <@checkbox val1 == val2/> ${bundle[val2]}
  </#compress>
</#macro>

<#macro missing_property>
<div style="color:#cd1719">Diese Information wurde nicht erfasst, sie ist aber erforderlich!</div>
</#macro>

<#assign notificationType=incident.incident_security_notificationType!>
${bundle.incident_security_notificationType}
: <@checkbox_cmp notificationType 'incident_security_notificationType_voluntaryNotification' />
: <@checkbox_cmp notificationType 'incident_security_notificationType_earlyWarning' />
: <@checkbox_cmp notificationType 'incident_security_notificationType_incidentNotification' />
: <@checkbox_cmp notificationType 'incident_security_notificationType_intermediateReport' />
: <@checkbox_cmp notificationType 'incident_security_notificationType_finalReport' />
: <@checkbox_cmp notificationType 'incident_security_notificationType_notReportet' />


<#assign supplierLinks=incident.getLinks('incident_involvedSupplier')!>
<#assign otherParticipantLinks=incident.getLinks('incident_otherParticipants')!>

<#if supplierLinks?has_content || otherParticipantLinks?has_content>
# ${bundle.involved_participants}

<#if supplierLinks?has_content>
## ${bundle.incident_involvedSupplier}
|:------------|:-----|
<#list supplierLinks as supplierLink>
<#assign scope=supplierLink.target />
| ${bundle.incident_supplier_name} | ${scope.name} |
</#list>
</#if>

<#if otherParticipantLinks?has_content>
## ${bundle.incident_otherParticipants}
|:------------|:-----|
<#list otherParticipantLinks as otherParticipantLink>
<#assign otherParticipant=otherParticipantLink.target />
| ${bundle.name} | ${otherParticipant.name} |
</#list>

</#if>


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

<@def bundle.incident_description_affectedProtectiongoals, (incident.incident_description_affectedProtectiongoals?map(item->bundle[item])?join(', '))! />

<@def bundle.incident_description_timeOfIncidentknown, incident.incident_description_timeOfIncidentknown />

<@def bundle.incident_description_dateTimeOfIncident, (incident.incident_description_dateTimeOfIncident?datetime.iso)! />

<@def bundle.incident_description_discoveryDateTimeOfIncident, (incident.incident_description_discoveryDateTimeOfIncident?datetime.iso)! />

<@def bundle.incident_description_stillContinues incident.incident_description_stillContinues />

<@def bundle.incident_description_durationOfIncident, incident.incident_description_durationOfIncident />

</div>
<div class="section">

# ${bundle.detailed_information}

## ${bundle.presumed_motivation}

<#assign typeOfAttack=incident.incident_presumedMotivation_typeOfAttack!>
<#if typeOfAttack?has_content>
<@def bundle.incident_presumedMotivation_typeOfAttack bundle[typeOfAttack] />
</#if>

<@def bundle.incident_presumedMotivation_presumedMotivation, (incident.incident_presumedMotivation_presumedMotivation?map(item->bundle[item])?join(', '))! />

<@def bundle.incident_description_others incident.incident_description_others />

<@def bundle.incident_presumedMotivation_collectedData, (incident.incident_presumedMotivation_collectedData?map(item->bundle[item])?join(', '))! />

<@def bundle.incident_presumedMotivation_others incident.incident_presumedMotivation_others />

<@def bundle.incident_nis2presumedMotivation_unlawfuOrMaliciousActs incident.incident_nis2presumedMotivation_unlawfuOrMaliciousActs />

<#assign lawEnforcement=incident.incident_presumedMotivation_lawEnforcement!>
<#if lawEnforcement?has_content>
<@def bundle.incident_presumedMotivation_lawEnforcement bundle[lawEnforcement] />
</#if>

## ${bundle.presumed_or_actual_causes}

<@def bundle.incident_nis2cause_presumedOrActual incident.incident_nis2cause_presumedOrActual />

<div class="section">

## ${bundle.concerned_targets}

<#assign concernedTargetLinks=incident.getLinks('incident_target')!>
<#if concernedTargetLinks?has_content>
### ${bundle.incident_target}
<#list concernedTargetLinks as concernedTargetLink>
<#assign concernedTarget=concernedTargetLink.target />
#### ${concernedTarget.name}
<@def bundle.incident_target_description concernedTargetLink.incident_target_description />

</#list>
</#if>

<#assign concernedBusinessProcessLinks=incident.getLinks('incident_businessProcess')!>
<#if concernedBusinessProcessLinks?has_content>
### ${bundle.incident_businessProcess}
<#list concernedBusinessProcessLinks as concernedBusinessProcessLink>
<#assign concernedBusinessProcess=concernedBusinessProcessLink.target />
#### ${concernedBusinessProcess.name}

</#list>
</#if>

<div class="section">

## ${bundle.likely_consequences}

<@def bundle.incident_nis2potentialEffect_impactAndConsequences, incident.incident_nis2potentialEffect_impactAndConsequences />

<@def bundle.incident_nis2potentialEffect_significantIncident, incident.incident_nis2potentialEffect_significantIncident />

<@def bundle.incident_nis2potentialEffect_severityRating, (bundle[incident.incident_nis2potentialEffect_severityRating])! />

</div>

<div class="section">

## ${bundle.notification_recipients_and_public}


<@def bundle.incident_nis2notification_recipientsInformed incident.incident_nis2notification_recipientsInformed />

<@def bundle.incident_nis2notification_dateOfRecipientsInformed, (incident.incident_nis2notification_dateOfRecipientsInformed?datetime.iso)! />

<@def bundle.incident_nis2notification_formOfNis2Notification incident.incident_nis2notification_formOfNis2Notification />

<@def bundle.incident_nis2notification_contentNis2Notification incident.incident_nis2notification_contentNis2Notification />

<@def bundle.incident_nis2notification_document incident.incident_nis2notification_document />

<@def bundle.incident_nis2notification_publicInformation incident.incident_nis2notification_publicInformation />

<@def bundle.incident_nis2notification_dateOfPublicInformation, (incident.incident_nis2notification_dateOfPublicInformation?datetime.iso)! />

</div>

<div class="section">

## ${bundle.countermeasures_initiated}

<#assign measuresTakenLinks=incident.getLinks('incident_securityMeasureTaken')!>
<#if measuresTakenLinks?has_content>
### ${bundle.incident_measuresTaken}
<#list measuresTakenLinks as measuresTakenLink>
<#assign measureTaken=measuresTakenLink.target />
 - ${measureTaken.name}
</#list>
</#if>

<#assign measuresPlannedLinks=incident.getLinks('incident_securityMeasurePlaned')!> <#--  sic -->
<#if measuresPlannedLinks?has_content>
### ${bundle.incident_measuresPlaned} <#--  sic -->
<#list measuresPlannedLinks as measuresPlannedLink>
<#assign measuresPlanned=measuresPlannedLink.target />
- ${measuresPlanned.name}
</#list>

</#if>

</div>

<div class="section">

## ${bundle.notification_to_competent_authority}

<#assign notificationMade=incident.incident_competentAuthority_notificationMade!>
<#if notificationMade?has_content>

<@def bundle.incident_competentAuthority_notificationMade notificationMade />

<#if notificationMade>
<@def bundle.incident_competentAuthority_timeDateNotification, (incident.incident_competentAuthority_timeDateNotification?datetime.iso)! />

<#assign withinHours=incident.incident_competentAuthority_withinHoursEarlyWarning!>

<#if withinHours?has_content>
<@def bundle.incident_competentAuthority_withinHoursEarlyWarning bundle[withinHours] />

<#if withinHours != "incident_competentAuthority_within24Hours">
<@def bundle.incident_competentAuthority_delayReasonEarlyWarning incident.incident_competentAuthority_delayReasonEarlyWarning />
</#if>
</#if>
</#if>
<#else>
<@missing_property />
</#if>
<#assign otherAuthorities=incident.incident_competentAuthority_otherGermanAuthorities!>

<#if otherAuthorities?has_content>
<@def bundle.incident_competentAuthority_otherGermanAuthorities otherAuthorities />
<#if otherAuthorities>
<@def bundle.incident_competentAuthority_nameOfOtherAuthorities, incident.incident_competentAuthority_nameOfOtherAuthorities />
</#if>
</#if>

<@def bundle.incident_competentAuthority_comments incident.incident_competentAuthority_comments />

</div>

<#assign crossBorder=incident.incident_nis2CrossBorder_crossBorder!>
<#if crossBorder?has_content>

<div class="section">

## ${bundle.cross_border_incident}

<@def bundle.incident_nis2CrossBorder_crossBorder crossBorder />
<#if crossBorder>

<@def bundle.incident_nis2CrossBorder_moreThan2Concerned, incident.incident_nis2CrossBorder_moreThan2Concerned />

<@def bundle.incident_nis2CrossBorder_concernedStates, incident.incident_nis2CrossBorder_concernedStates />
</#if>

<#assign notificationMadeEnisa=incident.incident_nis2CrossBorder_notificationMadeEnisa!>

<@def bundle.incident_nis2CrossBorder_notificationMadeEnisa, notificationMadeEnisa />

<#if notificationMadeEnisa?has_content && notificationMadeEnisa>

<@def bundle.incident_nis2CrossBorder_dateNotificationEnisa, (incident.incident_nis2CrossBorder_dateNotificationEnisa?date.iso)! />

</#if>

<#assign notificationMadeCrossBorder=incident.incident_nis2CrossBorder_notificationMade!>

<@def bundle.incident_nis2CrossBorder_notificationMade, incident.incident_nis2CrossBorder_notificationMade />

<#if notificationMadeCrossBorder?has_content && notificationMadeCrossBorder>

<@def bundle.incident_nis2CrossBorder_dateNotificationStates, (incident.incident_nis2CrossBorder_dateNotificationStates?date.iso)! />

</#if>

</div>

</#if>