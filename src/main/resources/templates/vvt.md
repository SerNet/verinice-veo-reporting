<#import "/libs/commons.ftlh" as com>

<#assign to_user_presentable = com.to_user_presentable
 row = com.row
 table = com.table
 >

<#function resolve uri>
  <#local parts = uri?split("/")>
  <#local type = parts[parts?size-2]>
  <#local id = parts[parts?size-1]>
  <#switch type>
    <#case "assets">
      <#local filteredAssets = assets?filter(s -> s.id == id)>
      <#if (filteredAssets?size == 0)>
        <#stop "Cannot resolve ${uri}, asset with id ${id} not found">
      </#if>
	  <#return filteredAssets?first>
    <#case "persons">
      <#local filteredPersons = persons?filter(p -> p.id == id)>
      <#if (filteredPersons?size == 0)>
        <#stop "Cannot resolve ${uri}, person with id ${id} not found">
      </#if>
	  <#return filteredPersons?first>
    <#case "scopes">
      <#local filteredScopes = scopes?filter(s -> s.id == id)>
      <#if (filteredScopes?size == 0)>
        <#stop "Cannot resolve ${uri}, scope with id ${id} not found">
      </#if>
	  <#return filteredScopes?first>
    <#case "processes">
      <#local filteredProcesses = processes?filter(s -> s.id == id)>
      <#if (filteredProcesses?size == 0)>
        <#stop "Cannot resolve ${uri}, process with id ${id} not found">
      </#if>
	  <#return filteredProcesses?first>
    <#default>
      <#stop "Cannot resolve ${uri}, unhandled type ${type}">
  </#switch>
</#function>

<#function rels object name>
  <#local raw = object.getLinks(name)!>
  <#if (raw!?size > 0)>
    <#return raw?map(l -> resolve(l.target.targetUri))>
  </#if>
</#function>

<#function rel object name>
  <#local links = object.getLinks(name)!>
  <#if (links!?size > 0)>
    <#return resolve(links?first.target.targetUri)>
  </#if>
</#function>

<#--  OLD VERSION, recursive membership
<#function is_member_recursive scope entity>
  <#local filteredMembers = scope.members?filter(m -> m.targetUri?contains('/scopes/') || m.targetUri?contains('/processes/'))>
  <#local resolvedFilteredMembers = filteredMembers?map(m -> resolve(m.targetUri))>
  <#return resolvedFilteredMembers?map(member->member.id)?seq_contains(entity.id) || (resolvedFilteredMembers?filter(member->member.type=='scope' && is_member_recursive(member, entity))?size > 0)>
</#function>

<#assign processesInScope = processes?filter(p ->p.subType?values?seq_contains('PRO_DataProcessing'))?filter(p -> is_member_recursive(scope, p))>
-->

<#assign processesInScope = scope.members?filter(m -> m.targetUri?contains('/processes/'))?map(m -> resolve(m.targetUri))?filter(p ->p.subType?values?seq_contains('PRO_DataProcessing'))>

<style>
<#include "styles/default.css">
.section {
  page-break-inside: avoid;
}
.section h3 {
  margin-bottom: 1mm;
}
.section table td:first-child,
.section table th:first-child {
  padding-left: 0;
}
</style>

<bookmarks>
  <bookmark name="${bundle.main_page}" href="#main_page"> </bookmark>
  <bookmark name="${bundle.activities_overview}" href="#overview">
<#list processesInScope as process>
    <bookmark name="${process.name}" href="#process_${process?counter}">
      <bookmark name="Prüfergebnis zur materiellen Rechtmäßigkeit" href="#process_FIXME_${process?counter}" />
      <bookmark name="Detailergebnisse" href="#process_details_${process?counter}">
         <#if process.getLinks('process_dataTransmission')?has_content>
            <bookmark name="Art übermittelter Daten und deren Empfänger" href="#process_transmissions_${process?counter}"/>
         </#if>
        <bookmark name="Technische und organisatorische Maßnahmen" href="#process_toms_${process?counter}"/>
      </bookmark>
    </bookmark>
</#list>
  </bookmark>
</bookmarks>

<div class="cover">
<h1>${bundle.title}</h1>
<p>powered by verinice</p>
</div>

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


<#assign management=rel(scope, 'scope_management')! />
<#assign headOfDataProcessing=rel(scope, 'scope_headOfDataProcessing')! />


| Vertretung  ||
|:---|:---|
| Leitung der verantwortlichen Stelle<br/>(einschließlich Vertreter) | ${management.person_generalInformation_givenName!bundle.unknown} ${management.person_generalInformation_familyName!bundle.unknown} |
| Leitung der Datenverarbeitung |  ${headOfDataProcessing.person_generalInformation_givenName!bundle.unknown} ${headOfDataProcessing.person_generalInformation_familyName!bundle.unknown} |


<#assign dataProtectionOfficer=rel(scope, 'scope_dataProtectionOfficer')! />

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

<#assign controller=rel(process, 'process_controller')! />
<#assign jointControllership=rel(process, 'process_jointControllership')! />
<#assign transmissions=rels(process, 'process_dataTransmission')! />


# <span style="display:inline-block; width: 6cm;">Verarbeitung: </span>${process.name} {#process_${process?counter}}
<#if process.process_processing_asProcessor??>
Auftragsverarbeitung i.S.d. Art. 30 II DS-GVO {.text-center }
</#if>
## Prüfergebnis zur materiellen Rechtmäßigkeit {.text-center .underline #process_FIXME_${process?counter}}

### I. Rechtmäßigkeit der Verarbeitung{.underline}

#### 1. Feststellungen{.underline}
${process.process_opinionDPO_findings!bundle.unknown} 
#### 2. Empfehlungen{.underline}
${process.process_opinionDPO_recommendations!bundle.unknown} 
<div class="pagebreak"></div>

### II. Rechtmäßigkeit der technischen und organisatorischen Maßnahmen{.underline}
#### 1. Zertifizierung nach anerkannten Standard

#### 2. IT-Sicherheitskonzept

#### 3. Gesamtbeurteilung der Maßnahmen

<div class="pagebreak"></div>

## Detailergebnisse {#process_details_${process?counter} .text-center .underline}

<div class="section">

### Name des Unternehmens
${scope.name}

|:---|:---|
| Abteilung/Fachbereich<br/>${process.process_processingDetails_responsibleDepartment!bundle.unknown} | Leiter Fachabteilung<br/>? | 
| Datum der Befragung<br/>${(process.process_processingDetails_surveyConductedOn?date.iso)!bundle.unknown} ||
</div>

<#assign sectionCount = 1>

<#macro section title>
<div class="section">

### ${sectionCount}. ${title} { .sectionheader }
<#nested>
</div>
<#assign sectionCount++>
</#macro>


<@section 'Verarbeitungsangaben'>

|:---|:---|
| Bezeichnung der Verarbeitung<br/>${process.name} | Beschreibung der Verarbeitung<br/>${process.description!bundle.unknown} |
| Art der Verarbeitung<br/> ${(bundle[process.process_processingDetails_typeOfSurvey])!bundle.unknown} ||
| Auftragsverarbeitung i.S.d. Art. 30 II DS-GVO | ${(process.process_processing_asProcessor?string(bundle.yes, bundle.no))!bundle.unknown} |
</@section>

<@section 'Angaben zum gemeinsam Verantwortlichen'>
|:---|
| **Gemeinsam für die Verarbeitung Verantwortliche Art. 26 DS-GVO**<br/>${(jointControllership.name)!bundle.unknown} |
</@section>

<@section 'Zweckbestimmung der Datenverarbeitung'>
|:---|
| ${process.process_intendedPurpose_intendedPurpose!bundle.unknown} |
</@section>

<@section 'Rechtsgrundlage für die Datenverarbeitung'>
|:---|
| ${(process.process_dataProcessing_legalBasis?map(item->bundle[item])?join(', '))!bundle.unknown} |
| **Sonstige Rechtsgrundlagen:**{.underline} |
| ${process.process_dataProcessing_otherLegalBasis!bundle.unknown} |
| **Erläuterungen:**<br/> ${process.process_dataProcessing_explanation!bundle.unknown} |
</@section>

<#assign processDataTypeLinks=process.getLinks('process_dataType')! />

<#if processDataTypeLinks?has_content>
<@section 'Datenkategorien'>
|:---|
 **Art der verarbeiteten Daten / Datenkategorien** | **Herkunft der Daten** | **Bemerkungen:**|
<#list processDataTypeLinks as dataTypeLink>
<#assign dataType=resolve(dataTypeLink.target.targetUri) />
<#assign dataOrigin=dataTypeLink.process_dataType_dataOrigin! />
<#assign effectiveDataOrigin=(dataOrigin == 'process_dataType_dataOrigin_other')?then(dataTypeLink.process_dataType_otherDataOrigin!bundle.unknown,(bundle[dataTypeLink.process_dataType_dataOrigin])!bundle.unknown) />
| ${dataType.name} | ${effectiveDataOrigin} | ${dataTypeLink.process_dataType_comment!bundle.unknown} |
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


<@section 'Beschreibung der betroffenen Personengruppen'>
|:---|
| **Kreis der betroffenen Personengruppen** | 
| ${effectiveDataSubjects} | 
| **Bemerkungen:**{.underline}<br/> ? |
</@section>
</#if>

<@section 'Informationspflichten Art. 13, 14 DS-GVO'>
|:---|
| ${process.process_informationsObligations_status!bundle.unknown} |
| **Erläuterungen**{.underline}<br/> |
| ${process.process_informationsObligations_explanation!bundle.unknown} |
</@section>

<#if transmissions?has_content>

<@section 'Art übermittelter Daten und deren Empfänger { #process_transmissions_${process?counter} }'>

<#list transmissions as transmission>

<#assign recipientType=transmission.process_recipient_type! />
<#assign transmissionDataTypes=rels(transmission, 'process_dataType')! />

<#assign dataTransferLegalBasis=transmission.process_dataTransfer_legalBasis! />
<#assign hasOtherLegalBasis=dataTransferLegalBasis?seq_contains('process_dataTransfer_legalBasis_others') />
<#assign dataTransferLegalBasis=dataTransferLegalBasis?filter(v -> v != 'process_dataTransfer_legalBasis_others') />
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

#### Übertragung ${transmission.name} 

<#switch recipientType>
<#case "process_recipient_type_internal">
<#assign internalRecipients=rels(transmission, 'process_internalRecipient')! />

#### Interne Empfänger
|:---|
| **Interne Stelle** | **Art der Daten** | **Rechtsgrundlage für Datenübertragung** |
<#list internalRecipients as internalRecipient>
| ${(internalRecipient.name)!bundle.unknown} | ${transmissionDataTypes?map(t->t.name)?join(", ")} | ${effectiveDataTransferLegalBasis!bundle.unknown} |
</#list>
| **Erläuterungen:**{.underline}<br/> ${transmission.process_dataTransfer_explanation!bundle.unknown} |||  
<#break>

<#case "process_recipient_type_external">
<#assign externalRecipients=rels(transmission, 'process_externalRecipient')! />

#### Externe Empfänger
|:---|
| **Externe Stelle** | **Art der Daten** | **Rechtsgrundlage für Datenübertragung** |
<#list externalRecipients as externalRecipient>
| ${(externalRecipient.name)!bundle.unknown} | ${transmissionDataTypes?map(t->t.name)?join(", ")} | ${effectiveDataTransferLegalBasis!bundle.unknown} |
</#list>
| **Erläuterungen:**{.underline}<br/> ${transmission.process_dataTransfer_explanation!bundle.unknown} |||
<#break>

<#case "process_recipient_type_processor">
<#assign processorsLinks=transmission.getLinks('process_processor')! />


#### Auftragnehmer / Dienstleister
|:---|
| **Auftragnehmer** | **Art der Daten** | **Rechtsgrundlage für Datenübertragung** |
<#list processorsLinks as processorLink>
<#assign processor=resolve(processorLink.target.targetUri) />
| ${(processor.name)!bundle.unknown} | ${transmissionDataTypes?map(t->t.name)?join(", ")} | ${effectiveDataTransferLegalBasis!bundle.unknown} |
| **Erläuterungen:**{.underline}<br/> ${transmission.process_dataTransfer_explanation!bundle.unknown} |||

|:---|
| **Datenübermittlung in Drittland** |
| ${(process.process_processor_thirdCountryProcessing?string(bundle.yes, bundle.no))!bundle.unknown} |
| **Name des Staates** |
| ${process.process_processor_thirdCountryName!bundle.unknown} |
| **Angabe geeigneter Garantien**|
| ${process.process_processor_thirdCountryGuarantees!bundle.unknown} |
| **Erläuterungen:**{.underline}<br/> ${process.process_processor_thirdCountryExplanation!bundle.unknown}  |
</#list>

<#break>
<#default>
!!! UNBEKANNTER EMPFÄNGERTYP
</#switch>


</#list>
</@section>
</#if>


<@section 'Löschfristen'>
|:---|
| **Fristabhängige Löschung**<br/>?<br/>**Löschverfahren**<br>?<br/>**Erläuterung:**{.underline}<br>? |
</@section>

<@section 'Zugriffsberechtigte Personengruppen (Berechtigungsgruppen)'>
|:---|
| **Ein Berechtigungskonzept ist vorhanden**<br/>${(process.process_accessAuthorization_concept?string(bundle.yes, bundle.no))!bundle.unknown} |
| **Beschreibung des Berechtigungsverfahrens:**<br/>${process.process_accessAuthorization_description!bundle.unknown} |
</@section>

<@section 'Systeminformationen über Hard- und Software'>
|:---|
| **Name** {.text-center .underline}| **Typ** {.text-center .underline}| **Beschreibung** {.text-center .underline}|
</@section>

<@section 'Ort der Datenverarbeitung (intern, extern )'>
|:---|
| ? |
</@section>

<@section 'Betriebsstadium'>
|:---|
| ${(bundle[process.process_processingDetails_operatingStage])!bundle.unknown} |
</@section>

<@section 'Datenschutz-Folgenabschätzung erforderlich?'>
|:---|
| ${(process.process_opinionDPO_privacyImpactAssessment?string(bundle.yes, bundle.no))!bundle.unknown} |
</@section>

<@section 'Technische und organisatorische Maßnahmen{ #process_toms_${process?counter} }'  >
<table>
  <thead>
    <tr>
      <th colspan="3">14. Technische und organisatorische Maßnahmen</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td colspan="3">?</td>
    </tr>
    <tr class="tableheading">
      <td>Ums.</td>
      <td>Maßnahme</ins></td>
      <td>Anmerkungen</ins></td>
    </tr>
    <tr class="tomsection">
      <td colspan="3">Pseudonymisierung</td>
    </tr>
    <tr class="tomsection">
      <td colspan="3">Verschlüsselung</td>
    </tr>
    <tr class="tomsection">
      <td colspan="3">Gewährleistung der Vertraulichkeit</td>
    </tr>
    <tr class="tomsection">
      <td colspan="3">Gewährleistung der Integrität</td>
    </tr>
    <tr class="tomsection">
      <td colspan="3">Gewährleistung der Verfügbarkeit</td>
    </tr>
    <tr class="tomsection">
      <td colspan="3">Gewährleistung der Belastbarkeit</td>
    </tr>
    <tr class="tomsection">
      <td colspan="3">Wiederherstellbarkeit</td>
    </tr>
    <tr class="tomsection">
      <td colspan="3">Wirksamkeit der TOMs</td>
    </tr>
  </tbody>
</table> 
</@section>

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
