<#import "/libs/commons.md" as com>
<#import "/libs/dp-risk.md" as dpRisk>

<#assign to_user_presentable = com.to_user_presentable
 row = com.row
 table = com.table
 def = com.def
 >

<#--  OLD VERSION, recursive membership
<#function is_member_recursive scope entity>
  <#local filteredMembers = scope.members?filter(m -> m.type == 'scope' || m.type == 'process')>
  <#return filteredMembers?map(member->member.id)?seq_contains(entity.id) || (filteredMembers?filter(member->member.type=='scope' && is_member_recursive(member, entity))?size > 0)>
</#function>

<#assign processesInScope = processes?filter(p ->p.subType?values?seq_contains('PRO_DataProcessing'))?filter(p -> is_member_recursive(scope, p))>
-->

<#assign processesInScope = scope.getMembersWithType('process')?filter(p ->p.hasSubType('PRO_DataProcessing'))>

<style>
<@com.defaultStyles />
.section {
  page-break-inside: avoid;
}

h3, h4 {
  page-break-after: avoid;
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
dl.tom {
  margin: 1mm 0mm;
}

dl.tom dd {
  margin-left: 2mm;
}

.pullup {
  margin-top: -4mm;
}
</style>

<bookmarks>
  <bookmark name="${bundle.toc}" href="#toc"/>
  <bookmark name="${bundle.main_page}" href="#main_page"/>
<#list processesInScope as process>
  <bookmark name="${process.name}" href="#process_${process?counter}">
    <bookmark name="Prüfergebnis zur materiellen Rechtmäßigkeit" href="#process_opinionDPO_${process?counter}" />
    <bookmark name="Detailergebnisse" href="#process_details_${process?counter}">
      <#if process.getLinks('process_dataTransmission')?has_content>
        <bookmark name="Art übermittelter Daten und deren Empfänger" href="#process_transmissions_${process?counter}"/>
      </#if>
      <#if process.risks?map(r->r.mitigation!)?filter(it -> it??)?filter(it -> it.parts?has_content)?has_content>
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
<#macro tocitem level target text>
  <tr class="level${level}">
    <td>
      <a title="${bundle('jumpto', text)}" href="#${target}">${text}</a>
    </td>
    <td>
      <span href="#${target}"/>
    </td>
  </tr>
</#macro>

<table class="toc">
<tbody>
  <@tocitem 1 "main_page" "1. ${bundle.main_page}" />
  <#list processesInScope as process>
    <@tocitem 1 "process_${process?counter}" "${1+process?counter}. ${process.name}" />
    <@tocitem 2 "process_opinionDPO_${process?counter}" "1. Prüfergebnis zur materiellen Rechtmäßigkeit" />
    <@tocitem 2 "process_details_${process?counter}" "2. Detailergebnisse" />
    <#assign level3counter = 1>
    <#if process.getLinks('process_dataTransmission')?has_content>
      <@tocitem 3 "process_transmissions_${process?counter}" "${level3counter}. Art übermittelter Daten und deren Empfänger" />
      <#assign level3counter = 2>
    </#if>
    <#if process.risks?map(r->r.mitigation!)?filter(it -> it??)?filter(it -> it.parts?has_content)?has_content>
      <@tocitem 3 "process_toms_${process?counter}" "${level3counter}. Technische und organisatorische Maßnahmen" />
    </#if>
  </#list>
</tbody>
</table>

# ${bundle.main_page} {#main_page}

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

<#if scope.scope_thirdCountry_status!false>
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

<#list processesInScope as process>

<#assign controller=process.findFirstLinked('process_controller')! />
<#assign jointControllership=process.findLinked('process_jointControllership') />
<#assign transmissions=process.findLinked('process_dataTransmission') />

# ${process.name} {#process_${process?counter}}
<#if process.process_processing_asProcessor!false>
<div class="pullup">
Auftragsverarbeitung i.S.d. Art. 30 II DS-GVO
</div>
</#if>

<div class="section">

## Prüfergebnis zur materiellen Rechtmäßigkeit {#process_opinionDPO_${process?counter}}

### Rechtmäßigkeit der Verarbeitung

Feststellungen
: ${process.process_opinionDPO_findings!} 

Empfehlungen
: ${process.process_opinionDPO_recommendations!} 
</div>

<div class="pagebreak"></div>

## Detailergebnisse {#process_details_${process?counter}}

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

${bundle.process_processingDetails_operatingStage}
: ${(bundle[process.process_processingDetails_operatingStage])!}
</@section>

<#if jointControllership?has_content>
<@section 'Gemeinsam für die Verarbeitung Verantwortliche Art. 26 DS-GVO'>
${jointControllership?map(item->item.name)?join(", ")}
</@section>
</#if>

<@section 'Zweckbestimmung der Datenverarbeitung'>
${process.process_intendedPurpose_intendedPurpose!}
</@section>

<@section 'Rechtsgrundlage für die Datenverarbeitung'>
${(process.process_dataProcessing_legalBasis?map(item->bundle[item])?join(', '))!}
</@section>

<@def "Sonstige Rechtsgrundlagen" process.process_dataProcessing_otherLegalBasis />

<@def "Erläuterungen" process.process_dataProcessing_explanation true />

<#assign processDataTypeLinks=process.getLinks('process_dataType')! />

<#if processDataTypeLinks?has_content>
<@section 'Datenkategorien'>
<#list processDataTypeLinks as dataTypeLink>
<#assign dataType=dataTypeLink.target />
<#assign dataOrigin=dataTypeLink.process_dataType_dataOrigin! />

#### ${dataType.name}

<@def bundle.process_dataType_dataOrigin dataOrigin?has_content?then(bundle[dataOrigin], "") true />

<@def bundle.process_dataType_comment dataTypeLink.process_dataType_comment />

<#if dataTypeLink.process_dataType_deletionPeriodCount?has_content && dataTypeLink.process_dataType_deletionPeriod?has_content>
${bundle.process_dataType_deletionPeriod}
: ${dataTypeLink.process_dataType_deletionPeriodCount} ${(bundle.getObject(dataTypeLink.process_dataType_deletionPeriod))}
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

<@def "Erläuterungen" process.process_informationsObligations_explanation />

</@section>

<#if transmissions?has_content>

<@section 'Art übermittelter Daten und deren Empfänger' 'process_transmissions_${process?counter}'>

<#list transmissions as transmission>

<#assign recipientType=transmission.process_recipient_type! />
<#assign transmissionDataTypes=transmission.findLinked('process_dataType') />

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

<@def "Erläuterungen" transmission.process_dataTransfer_explanation true />


<#macro recipient_section recipient_label recipient thirdCountryProcessing=false thirdCountryName="" thirdCountryGuarantees="" thirdCountryExplanation="">
<div class="section">
<@def recipient_label recipient.name true />

<@def "Datenübermittlung in Drittland", (thirdCountryProcessing?string(bundle.yes, bundle.no))!"", true />

<#if thirdCountryProcessing!false>
<@def "Name des Landes" thirdCountryName true />

<@def "Angabe geeigneter Garantien" thirdCountryGuarantees true />
</#if>

<@def "Erläuterungen" thirdCountryExplanation />

</div>
</#macro>

<#switch recipientType>

<#on "process_recipient_type_internal">
<#assign internalRecipientLinks=transmission.getLinks('process_internalRecipient')! />
#### Interne Empfänger
<#list internalRecipientLinks as link>
<@recipient_section "Interne Stelle" link.target, link.process_internalRecipient_thirdCountryProcessing link.process_internalRecipient_thirdCountryName link.process_internalRecipient_thirdCountryGuarantees link.process_internalRecipient_thirdCountryExplanation/>
</#list>

<#on "process_recipient_type_external">
<#assign externalRecipientLinks=transmission.getLinks('process_externalRecipient')! />
#### Externe Empfänger
<#list externalRecipientLinks as link>
<@recipient_section "Externe Stelle" link.target, link.process_externalRecipient_thirdCountryProcessing link.process_externalRecipient_thirdCountryName link.process_externalRecipient_thirdCountryGuarantees link.process_externalRecipient_thirdCountryExplanation/>
</#list>

<#on "process_recipient_type_processor">
<#assign processorsLinks=transmission.getLinks('process_processor')! />
#### Auftragnehmer / Dienstleister
<#list processorsLinks as link>
<@recipient_section "Auftragnehmer" link.target, link.process_processor_thirdCountryProcessing link.process_processor_thirdCountryName link.process_processor_thirdCountryGuarantees link.process_processor_thirdCountryExplanation/>
</#list>

<#default>
!!! UNBEKANNTER EMPFÄNGERTYP
</#switch>

</div>

</#list>
</@section>
</#if>

<@section 'Zugriffsberechtigte Personengruppen (Berechtigungsgruppen)'>
<@def "Ein Berechtigungskonzept ist vorhanden", (process.process_accessAuthorization_concept?string(bundle.yes, bundle.no))!"" true />

<#if process.process_accessAuthorization_concept!false>
<@def "Beschreibung des Berechtigungsverfahrens" process.process_accessAuthorization_description true />
</#if>
</@section>

<#assign relatedAssets=process.findLinked('process_requiredApplications') + process.findLinked('process_requiredITSystems') />
<#if relatedAssets?has_content>

<@section 'Systeminformationen über Hard- und Software'>
|:---|
| **Name** | **Beschreibung** |
<#list relatedAssets as asset>
| ${(asset.name)!} | ${asset.description!} |
</#list>
</@section>
</#if>

<#-- FIXME VEO-619/VEO-1175: maybe pass domain into report? -->
<#assign domain=domains?filter(it->it.name == 'DS-GVO')?filter(it->scope.domains?keys?seq_contains(it.id))?sort_by("createdAt")?last />

<@section 'Datenschutz-Folgenabschätzung'>
${bundle.piaMandatory}
: ${(process.domains[domain.id].decisionResults.piaMandatory.value?string(bundle.yes, bundle.no))!""}

<#if ((process.domains[domain.id].decisionResults.piaMandatory.value)!false) && process.process_opinionDPO_comment?has_content>
${bundle.process_opinionDPO_comment}
: ${process.process_opinionDPO_comment}
</#if>
</@section>


<#assign mitigations=process.risks?map(r->r.mitigation!)?filter(it -> it??)?filter(it -> it.parts?has_content) />
<#if mitigations?has_content>

<@section 'Technische und organisatorische Maßnahmen' 'process_toms_${process?counter}'  >

<#macro tomsection process objective title>
<#list mitigations as mitigation>
<#assign toms = dpRisk.getDescendants(mitigation)>
<#assign tomsinsection = toms?filter(t->t.control_dataProtection_objectives!?seq_contains(objective))!>
<#if tomsinsection?has_content>
<tbody>
<tr class="dark-gray tomsectiontitle">
  <td colspan="2">${title}</td>
</tr>
<#list tomsinsection as t>
<#assign statusTdStyle="">
<#assign tom_status_output="">
<#assign ri=dpRisk.getRI(process, t)!>
<#assign tom_status=(dpRisk.getImplementationStatus(ri))! />

<#if tom_status?has_content>
<#assign backgroundColor=tom_status.color />
<#assign statusTdStyleSvg='<svg xmlns="http://www.w3.org/2000/svg" height="1" width="1"><polygon points="0,0 0,1 1,1 1,0" style="fill:${backgroundColor};" /></svg>' />
<#assign statusTdStyle="background-repeat:no-repeat;background-size:5mm 100%;background-position:bottom left;background-image: url('data:image/svg+xml;base64,${base64(statusTdStyleSvg)}');padding-left: 7mm;">
<#assign tom_status_output=tom_status.label>
</#if>
<tr>
  <td style="${statusTdStyle}">${tom_status_output}</td>
  <td><dl class="tom"><dt>${t.name}</dt><dd>${ri.implementationStatement!}</dd></dl></td>
</tr>
</#list>
</tbody>
</#if>
</#list>
</#macro>

<table class="table toms">
  <thead>
    <tr>
      <td>Ums.</td>
      <td>Maßnahme</td>
    </tr>
  </thead>
  <@tomsection process 'control_dataProtection_objectives_pseudonymization', 'Pseudonymisierung'/>
  <@tomsection process 'control_dataProtection_objectives_confidentiality', 'Gewährleistung der Vertraulichkeit'/>
  <@tomsection process 'control_dataProtection_objectives_integrity', 'Gewährleistung der Integrität'/>
  <@tomsection process 'control_dataProtection_objectives_availability', 'Gewährleistung der Verfügbarkeit'/>
  <@tomsection process 'control_dataProtection_objectives_resilience', 'Gewährleistung der Belastbarkeit'/>
  <@tomsection process 'control_dataProtection_objectives_recoverability', 'Wiederherstellbarkeit'/>
  <@tomsection process 'control_dataProtection_objectives_effectiveness', 'Wirksamkeit der TOMs'/>
  <@tomsection process 'control_dataProtection_objectives_encryption', 'Verschlüsselung'/>
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
