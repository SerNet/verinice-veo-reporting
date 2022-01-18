<#import "/libs/commons.md" as com>

<#assign to_user_presentable = com.to_user_presentable
 row = com.row
 table = com.table
 >
 
<#macro def term definition="" alwaysShow=false>
<#if definition?has_content || alwaysShow>
${term}
<#if definition?has_content>
: ${definition}
<#else>
: &nbsp;
</#if>
</#if>
</#macro>

<#--  OLD VERSION, recursive membership
<#function is_member_recursive scope entity>
  <#local filteredMembers = scope.getMembers()?filter(m -> m.type == 'scope' || m.type == 'process')>
  <#return filteredMembers?map(member->member.id)?seq_contains(entity.id) || (filteredMembers?filter(member->member.type=='scope' && is_member_recursive(member, entity))?size > 0)>
</#function>

<#assign processesInScope = processes?filter(p ->p.subType?values?seq_contains('PRO_DataProcessing'))?filter(p -> is_member_recursive(scope, p))>
-->

<#assign processesInScope = scope.getMembers()?filter(m -> m.type == 'process')?filter(p ->p.hasSubType('PRO_DataProcessing'))>

<style>
<#include "styles/default.css">
.section {
  page-break-inside: avoid;
}
.section h3 {
  margin-bottom: 1mm;
}
.transmission {
  margin-left: 5mm;
}
.transmission .section {
  margin-left: 1cm;
  margin-top: 5mm;
  margin-bottom: 5mm;
}
.main_page table th:first-child, .main_page table td:first-child {
  width: 8cm;
}
dt {
  font-weight: 600;
}
.tomsectiontitle {
  page-break-after: avoid;
}
dl.tom {
  margin: 1mm 0mm;
}

dl.tom dd {
  margin-left: 2mm;
}
</style>

<bookmarks>
  <bookmark name="${bundle.toc}" href="#toc"/>
  <bookmark name="${bundle.main_page}" href="#main_page"/>
  <bookmark name="${bundle.activities_overview}" href="#overview"/>
<#list processesInScope as process>
  <bookmark name="${process.name}" href="#process_${process?counter}">
    <bookmark name="Prüfergebnis zur materiellen Rechtmäßigkeit" href="#process_FIXME_${process?counter}" />
    <bookmark name="Detailergebnisse" href="#process_details_${process?counter}">
      <#if process.getLinks('process_dataTransmission')?has_content>
        <bookmark name="Art übermittelter Daten und deren Empfänger" href="#process_transmissions_${process?counter}"/>
      </#if>
      <#if process.getLinks('process_tom')?has_content>
        <bookmark name="Technische und organisatorische Maßnahmen" href="#process_toms_${process?counter}"/>
      </#if>
    </bookmark>
  </bookmark>
</#list>
</bookmarks>

<div class="cover">
<h1>${bundle.title}</h1>
<p>powered by verinice</p>
</div>


# ${bundle.toc} {#toc}

<ol class="toc">
  <li><a href="#toc">${bundle.toc}</a> <span href="#toc"></span></li>
  <li><a href="#main_page">${bundle.main_page}</a> <span href="#main_page"></span></li>
  <li><a href="#overview">${bundle.activities_overview}</a> <span href="#overview"></span></li>
<#list processesInScope as process>
  <li><a href="#process_${process?counter}">${process.name}</a> <span href="#process_${process?counter}"></span>
    <ol>
      <li><a href="#process_FIXME_${process?counter}">Prüfergebnis zur materiellen Rechtmäßigkeit</a> <span href="#process_FIXME_${process?counter}"></span></li>
      <li><a href="#process_details_${process?counter}">Detailergebnisse</a> <span href="#process_details_${process?counter}"></span>
        <ol>
         <#if process.getLinks('process_dataTransmission')?has_content>
            <li><a href="#process_transmissions_${process?counter}">Art übermittelter Daten und deren Empfänger</a> <span href="#process_transmissions_${process?counter}"></span></li>
         </#if>
         <#if process.getLinks('process_tom')?has_content>
            <li><a href="#process_toms_${process?counter}">Technische und organisatorische Maßnahmen</a> <span href="#process_toms_${process?counter}"></span></li>
         </#if>
        </ol>
      </li>
    </ol>
  </li>
</#list>
</ol>

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


| Vertretung  ||
|:---|:---|
| Leitung der verantwortlichen Stelle<br/>(einschließlich Vertreter) | ${management.person_generalInformation_givenName!} ${management.person_generalInformation_familyName!} |
| Leitung der Datenverarbeitung |  ${headOfDataProcessing.person_generalInformation_givenName!} ${headOfDataProcessing.person_generalInformation_familyName!} |


<#assign dataProtectionOfficer=scope.findFirstLinked('scope_dataProtectionOfficer')! />

<@table 'Datenschutzbeauftragte',
  dataProtectionOfficer,
  [
   {'name' : 'person_generalInformation_givenName person_generalInformation_familyName'},
   {'person_address_postcode, scope_address_city' : 'person_address_postcode scope_address_city'},
   'person_contactInformation_office / person_contactInformation_fax',
   'person_contactInformation_email'
  ]/>

<#if (scope.scope_thirdCountry_status??)>
<@table bundle.thirdCountry_table_caption, scope, [
  'scope_thirdCountry_name',
  'scope_thirdCountry_address1',
  {'scope_thirdCountry_postcode, scope_thirdCountry_city' : 'scope_thirdCountry_postcode scope_thirdCountry_city'},
  'scope_thirdCountry_country'
]/>
</#if>

<@table bundle.responsibleRegulatoryAuthority_table_caption, scope, [
  'scope_regulatoryAuthority_name',
  'scope_regulatoryAuthority_address1',
  {'scope_regulatoryAuthority_postcode, scope_regulatoryAuthority_city' : 'scope_regulatoryAuthority_postcode scope_regulatoryAuthority_city'},
  'scope_regulatoryAuthority_phone / scope_regulatoryAuthority_fax',
  'scope_regulatoryAuthority_email'
]/>

</div>
<div class="pagebreak"></div>

# ${bundle.activities_overview}{#overview}

<#list processesInScope as process>
${process.name}  
</#list>

<div class="pagebreak"></div>

<#list processesInScope as process>

<#assign controller=process.findFirstLinked('process_controller')! />
<#assign jointControllership=process.findFirstLinked('process_jointControllership')! />
<#assign transmissions=process.getLinked('process_dataTransmission')! />


# <span style="display:inline-block; width: 6cm;">Verarbeitung: </span>${process.name} {#process_${process?counter}}
<#if process.process_processing_asProcessor!false>
Auftragsverarbeitung i.S.d. Art. 30 II DS-GVO {.text-center }
</#if>
## Prüfergebnis zur materiellen Rechtmäßigkeit {.text-center .underline #process_FIXME_${process?counter}}

### Rechtmäßigkeit der Verarbeitung{.underline}

#### Feststellungen{.underline}
${process.process_opinionDPO_findings!} 
#### Empfehlungen{.underline}
${process.process_opinionDPO_recommendations!} 
<div class="pagebreak"></div>

## Detailergebnisse {#process_details_${process?counter} .text-center .underline}

<#macro section title id="">
<div class="section">

### ${title} { .sectionheader }
<#if id?has_content>
{ #${id?no_esc} }
</#if>
<#nested>
</div>
</#macro>

<@section 'Allgemeine Informationen'>

Name des Unternehmens
: ${scope.name}

<#assign responsiblePerson=process.findFirstLinked('process_responsiblePerson')! />

Abteilung/Fachbereich
: ${process.process_processingDetails_responsibleDepartment!}

Leiter Fachabteilung
: ${responsiblePerson.name!}

${bundle.process_processingDetails_surveyConductedOn}
: ${(process.process_processingDetails_surveyConductedOn?date.iso)!}
</@section>

<@section 'Allgemeine Verarbeitungsangaben'>

Bezeichnung der Verarbeitung
: ${process.name}

Beschreibung der Verarbeitung
: ${process.description!}

Art der Verarbeitung
: ${(bundle[process.process_processingDetails_typeOfSurvey])!}

Auftragsverarbeitung i.S.d. Art. 30 II DS-GVO
: ${(process.process_processing_asProcessor?string(bundle.yes, bundle.no))!}
</@section>

<@section 'Angaben zum gemeinsam Verantwortlichen'>
<@def "Gemeinsam für die Verarbeitung Verantwortliche Art. 26 DS-GVO" jointControllership.name true />
</@section>

<@section 'Zweckbestimmung der Datenverarbeitung'>
${process.process_intendedPurpose_intendedPurpose!}
</@section>

<@section 'Rechtsgrundlage für die Datenverarbeitung'>
${(process.process_dataProcessing_legalBasis?map(item->bundle[item])?join(', '))!}
</@section>

<@def "Sonstige Rechtsgrundlagen" process.process_dataProcessing_otherLegalBasis true />

<@def "Erläuterungen" process.process_dataProcessing_explanation true />

<#assign processDataTypeLinks=process.getLinks('process_dataType')! />

<#if processDataTypeLinks?has_content>
<@section 'Datenkategorien'>
<#list processDataTypeLinks as dataTypeLink>
<#assign dataType=dataTypeLink.getTarget() />
<#assign dataOrigin=dataTypeLink.process_dataType_dataOrigin! />

#### ${dataType.name}

<@def bundle.process_dataType_dataOrigin bundle[dataOrigin]! true />

<@def bundle.process_dataType_comment dataTypeLink.process_dataType_comment />

<#if dataTypeLink.process_dataType_deletionPeriod?has_content>
${bundle.process_dataType_deletionPeriod}
: ${(bundle.getObject(dataTypeLink.process_dataType_deletionPeriod))}
</#if>

<@def bundle.process_dataType_deletionPeriod dataTypeLink.process_dataType_otherDeletionPeriod />

<#if dataTypeLink.process_dataType_deletionPeriodStart?has_content>
${bundle.process_dataType_deletionPeriodStart}
: ${(bundle.getObject(dataTypeLink.process_dataType_deletionPeriodStart))}
</#if>

<@def bundle.process_dataType_deletionPeriodStart dataTypeLink.process_dataType_otherDeletionPeriodStart />

<@def bundle.process_dataType_descriptionDeletionProcedure dataTypeLink.process_dataType_descriptionDeletionProcedure />

</#list>
</@section>
</#if>


<#assign dataSubjects=process.process_dataSubjects_dataSubjects! />

<#if dataSubjects?has_content>
<#assign hasOtherDataSubjects=dataSubjects?seq_contains('process_dataSubjects_dataSubjects_other') />
<#assign dataSubjects=dataSubjects?filter(v -> v != 'process_dataSubjects_dataSubjects_other') />
<#assign dataSubjects=dataSubjects?map(v->bundle.getObject(v)) />
<#assign effectiveDataSubjects=dataSubjects?join(', ') />

<#if hasOtherDataSubjects>
<#assign otherDataSubjects=process.process_dataSubjects_otherDataSubjects! />
<#if effectiveDataSubjects?has_content>
<#assign effectiveDataSubjects='${effectiveDataSubjects},${otherDataSubjects}' />
<#else>
<#assign effectiveDataSubjects=otherDataSubjects />
</#if>
</#if>


<@section "Kategorien betroffener Personen">
${effectiveDataSubjects}
</@section>
</#if>

<@section 'Informationspflichten Art. 13, 14 DS-GVO'>
${(process.process_informationsObligations_status?string(bundle.yes, bundle.no))!}

<@def "Erläuterungen" process.process_informationsObligations_explanation true />

</@section>

<#if transmissions?has_content>

<@section 'Art übermittelter Daten und deren Empfänger' 'process_transmissions_${process?counter}'>

<#list transmissions as transmission>

<#assign recipientType=transmission.process_recipient_type! />
<#assign transmissionDataTypes=transmission.getLinked('process_dataType')! />

<#assign dataTransferLegalBasis=transmission.process_dataTransfer_legalBasis! />
<#assign hasOtherLegalBasis=transmission.process_dataTransfer_otherLegalBasis?has_content />
<#assign dataTransferLegalBasis=dataTransferLegalBasis?map(v->bundle.getObject(v)) />
<#assign effectiveDataTransferLegalBasis=dataTransferLegalBasis?join(', ') />

<#if hasOtherLegalBasis>
<#assign otherLegalBasis=transmission.process_dataTransfer_otherLegalBasis />
<#if effectiveDataTransferLegalBasis?has_content>
<#assign effectiveDataTransferLegalBasis='${effectiveDataTransferLegalBasis},${otherLegalBasis}' />
<#else>
<#assign effectiveDataTransferLegalBasis=otherLegalBasis />
</#if>
</#if>

<div class="transmission">

#### ${transmission.name}

Art der Daten
: ${transmissionDataTypes?map(t->t.name)?join(", ")}

Rechtsgrundlage für Datenübertragung
: ${effectiveDataTransferLegalBasis!}

<#macro recipient_section link_to_recipient recipient_label>
<div class="section">
<#assign recipient=link_to_recipient.getTarget() />

<@def recipient_label recipient.name true />

<@def "Datenübermittlung in Drittland", (link_to_recipient.process_internalRecipient_thirdCountryProcessing?string(bundle.yes, bundle.no))!"" true />

<@def "Name des Landes" link_to_recipient.process_internalRecipient_thirdCountryName true />

<@def "Angabe geeigneter Garantien" link_to_recipient.process_internalRecipient_thirdCountryGuarantees true />
	
<@def "Erläuterungen" link_to_recipient.process_internalRecipient_thirdCountryExplanation true />

</div>
</#macro>

<#switch recipientType>

<#case "process_recipient_type_internal">
<#assign internalRecipientLinks=transmission.getLinks('process_internalRecipient')! />
#### Interne Empfänger
<#list internalRecipientLinks as internalRecipientLink>
<@recipient_section internalRecipientLink "Interne Stelle"/>
</#list>
<#break>

<#case "process_recipient_type_external">
<#assign externalRecipientLinks=transmission.getLinks('process_externalRecipient')! />
#### Externe Empfänger
<#list externalRecipientLinks as externalRecipientLink>
<@recipient_section externalRecipientLink "Externe Stelle"/>
</#list>
<#break>

<#case "process_recipient_type_processor">
<#assign processorsLinks=transmission.getLinks('process_processor')! />
#### Auftragnehmer / Dienstleister
<#list processorsLinks as processorLink>
<@recipient_section processorLink "Auftragnehmer"/>
</#list>
<#break>

<#default>
!!! UNBEKANNTER EMPFÄNGERTYP
</#switch>

<@def "Erläuterungen" transmission.process_dataTransfer_explanation true />
</div>

</#list>
</@section>
</#if>

<@section 'Zugriffsberechtigte Personengruppen (Berechtigungsgruppen)'>
<@def "Ein Berechtigungskonzept ist vorhanden", (process.process_accessAuthorization_concept?string(bundle.yes, bundle.no))!"" true />

<@def "Beschreibung des Berechtigungsverfahrens" process.process_accessAuthorization_description true />
</@section>

<#assign relatedAssets=(process.getLinked('process_requiredApplications')![]) + (process.getLinked('process_requiredITSystems')![]) />
<#if relatedAssets?has_content>

<@section 'Systeminformationen über Hard- und Software'>
|:---|
| **Name** {.text-center .underline}| **Beschreibung** {.text-center .underline}|
<#list relatedAssets as asset>
| ${(asset.name)!} | ${asset.description!} |
</#list>
</@section>
</#if>

<@section 'Betriebsstadium'>
${(bundle[process.process_processingDetails_operatingStage])!}
</@section>

<@section 'Datenschutz-Folgenabschätzung'>
${bundle.process_opinionDPO_privacyImpactAssessment}
: ${(process.process_opinionDPO_privacyImpactAssessment?string(bundle.yes, bundle.no))!""}

<#if process.process_opinionDPO_privacyImpactAssessment!false && process.process_opinionDPO_comment?has_content>
${bundle.process_opinionDPO_comment}
: ${process.process_opinionDPO_comment}
</#if>
</@section>


<#assign toms=process.getLinked('process_tom')! />
<#if toms?has_content>
<@section 'Technische und organisatorische Maßnahmen' 'process_toms_${process?counter}'  >

<#macro tomsection objective title>
<#assign tomsinsection = toms?filter(t->t.control_dataProtection_objectives!?seq_contains(objective))!>
<#if tomsinsection?has_content>
<tr class="gray tomsectiontitle">
  <td colspan="2">${title}</td>
</tr>
<#list tomsinsection as t>
<#assign tom_status=t.control_implementation_status />
<#assign className=t.control_implementation_status?switch(
  'control_implementation_status_yes', 'green',
  'control_implementation_status_no', 'red',
  'control_implementation_status_partially', 'yellow',
  'control_implementation_status_notApplicable', 'light-gray',
  '') />
<tr>
  <td class="${className}">${bundle[tom_status]}</td>
  <td><dl class="tom"><dt>${t.name}</dt><dd>${t.control_implementation_explanation!}</dd></dl></td>
</tr>
</#list>
</#if>
</#macro>

<table class="table toms">
  <thead>
    <tr>
      <td>Ums.</td>
      <td>Maßnahme</td>
    </tr>
  </thead>
  <tbody>
    <@tomsection 'control_dataProtection_objectives_pseudonymization', 'Pseudonymisierung'/>
    <@tomsection 'control_dataProtection_objectives_confidentiality', 'Gewährleistung der Vertraulichkeit'/>
    <@tomsection 'control_dataProtection_objectives_integrity', 'Gewährleistung der Integrität'/>
    <@tomsection 'control_dataProtection_objectives_availability', 'Gewährleistung der Verfügbarkeit'/>
    <@tomsection 'control_dataProtection_objectives_resilience', 'Gewährleistung der Belastbarkeit'/>
    <@tomsection 'control_dataProtection_objectives_recoverability', 'Wiederherstellbarkeit'/>
    <@tomsection 'control_dataProtection_objectives_effectiveness', 'Wirksamkeit der TOMs'/>
    <@tomsection 'control_dataProtection_objectives_encryption', 'Verschlüsselung'/>
  </tbody>
</table> 
</@section>
</#if>

<#--  DEBUG   -->
<#assign DEBUG=false />


<#if DEBUG>
<#-- Print all booleans as true/false. This should be removed once we have proper custom aspect handling. -->
<#setting boolean_format="c">

Abkürzung
: ${process.abbreviation!"&nbsp;"}

Beschreibung
: ${process.description!"&nbsp;"}


<#if (process.customAspects!?size > 0)>

### Custom aspects

<ul>

<#list process.customAspects as id, customAspect>

<li>${id}

<#list customAspect.attributes as k, v>

${k}
: ${to_user_presentable(v)}

</#list>


</li>

</#list>


</ul>

</#if>

<#if (process.links!?size > 0)>

### Links

<ul>

<#list process.links as type, links>

<li>${type}
<ul>
<#list links as link>

<li>${link.target.displayName}</li>

</#list>
</ul>

</li>

</#list>


</ul>

</#if>
</#if>
<#--  DEBUG   -->

<#if process?has_next>

<div class="pagebreak"></div>

</#if>
</#list>

</body>
</html>
