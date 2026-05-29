<#import "/libs/commons.md" as com>

<#assign document = target />
<#assign process = document.findFirstLinked("document_process")! />
<#assign orgUnit = (process.findFirstLinked("process_organizationalUnit"))! />
<#assign institution = (orgUnit.scopes?filter(s -> s.hasSubType("SCP_Institution"))?first)! />

<style>
<@com.defaultStyles />
h1, h2, h3, h4, h5, h6 {
  page-break-after: avoid;
}

td {
  vertical-align: top;
}

.main_page {
  page-break-after: always;
}

.main_page table th:first-child, .main_page table td:first-child {
  width: 8cm;
}


.fullwidth {
  width: 100%;
}

table.small-table {
  table-layout: fixed;
  font-size: 80%;
}

table.small-table th,
table.small-table td {
  white-space: normal;
}

.checkbox {
    display: inline-block;
    width: 8pt;
    height: 8pt;
    border: 1px solid grey;
}

.status-column {
    text-align: center;
}

@page {
    @bottom-right {
        border-top: 0.1mm solid #929292;
        font-family: Open Sans;
        font-size: 6pt;
        color: #767676;
    
        white-space: pre;

        content:
            '${bundle.control_emergencyMeasureClassification_planType_gfp}\A'
            '${bundle.document_process}: ${((process.abbreviation)!)?no_esc} ${((process.name)!)?no_esc}\A'
            '${((bundle[document.document_docManagement_lifecycle])!)?no_esc}, Version: ${((document.document_docManagement_version)!)?no_esc}\A'
            'Gültig ab: ${((document.document_docManagement_dateOfApproval?date.iso)!)?no_esc}\A'
            'Seite ' counter(page) ' von ' counter(pages);
    }
}
</style>

<div class="footer-left">
  ${(institution.abbreviation)!} ${(institution.name)!}<br/>
  ${orgUnit.name!}<br/>
  Erstelldatum: ${.now?date}
</div>

<div class="cover">
  <h1>${bundle.control_emergencyMeasureClassification_planType_gfp}</h1>
  <h2>
    ${bundle.document_process}: </br> ${((process.abbreviation)! )?no_esc} ${((process.name)! )?no_esc}
  </h2>
  <p>powered by verinice</p>
</div>

# Hauptblatt

<div class="main_page">
<table>
<tr>
     <b>Institution</b>
    </tr>
    <tr>
    <tr>
      <td>${bundle.name}:</td>
      <td>${(institution.abbreviation)!} ${(institution.name)!}</td>
    </tr>
    <tr>
  <td>${bundle.scope_address_address1}:</td>
  <td>
    ${(institution.scope_address_address1)! }
  </td>
   <tr>
  <td>${bundle.scope_address_postcode}, ${bundle.scope_address_city}:</td>
  <td>
    ${(institution.scope_address_postcode)! }
    ${(institution.scope_address_city)! }
  </td>
   </tr>
    <tr>
       <td>${bundle.scope_contactInformation_phone}/${bundle.scope_contactInformation_fax}:</td>
      <td>${(institution.scope_contactInformation_phone)! } / ${(institution.scope_contactInformation_fax)! }</td>
    </tr>
    <tr>
        <td>${bundle.scope_contactInformation_email}:</td>
        <td>${(institution.scope_contactInformation_email)! }</td>
    </tr>
    <tr>
        <td>${bundle.scope_contactInformation_website}:</td>
        <td>${(institution.scope_contactInformation_website)! }</td>
    </tr>
  </table>

  <b>Zuständige Organisationseinheit</b></br>
  <table>
    <tr>
        <td>${bundle.name}:</td>
        <td>${orgUnit.abbreviation!} ${orgUnit.name!}</td>
    </tr>
      <tr>
        <td>${bundle.control_internalContactOE_description}:</td>
        <td> ${orgUnit.description!}</td>
    </tr>
  </table>
   </br>
  

</div>

# Dokumenteigenschaften

<table class="table fullwidth">
<thead>
<tr>
  <th>Kennzeichnung</th>
  <th>Dokumentdaten</th>
</tr>
</thead>
<tbody>
<tr>
  <td>${bundle.document_process}:</td>
  <td>${(process.abbreviation)!} ${(process.name)!}</td>
</tr>
<tr>
  <td>${bundle.document_docManagement_classification}:</td>
  <td>${(bundle[document.document_docManagement_classification])!}</td>
</tr>
<tr>
  <td>${bundle.document_docManagement_version}:</td>
  <td>${document.document_docManagement_version! }</td>
</tr>
<tr>
  <td>Zuständig:</td>
  <td>${(orgUnit.findFirstLinked("scope_manager").name)!}</td>
</tr>
<tr>
  <td>${bundle.document_storageArchiving_location}:</td>
  <td>${document.document_storageArchiving_location! }</td>
</tr>
<tr>
  <td>${bundle.document_businessContinuityPlan_targetGroup}:</td>
  <td>${document.document_businessContinuityPlan_targetGroup! }</td>
</tr>
<tr>
  <td>Erstellt am:</td>
  <td>${.now?date}</td>
</tr>
<tr>
  <td>${bundle.document_docAuthor}:</td>
  <td>${(document.findFirstLinked("document_docAuthor").name)!}</td>
</tr>
<tr>
  <td>${bundle.document_revision_last}:</td>
  <td>${(document.document_revision_last?date.iso)!}</td>
</tr>
<tr>
  <td>${bundle.document_revision_next}:</td>
  <td>${(document.document_revision_next?date.iso)!}</td>
</tr>
<tr>
   <td>${bundle.document_docManagement_dateOfApproval}:</td>
   <td>${(document.document_docManagement_dateOfApproval?date.iso)!}</td>
</tr>
<tr>
  <td>${bundle.document_docApprovalThrough}:</td>
  <td>${(document.findFirstLinked("document_docApprovalThrough").name)!}</td>
</tr>
</tbody>
</table>

<div class="pagebreak"></div>

# Inhaltsverzeichnis {#toc}

<#macro tocitem level target text>
  <tr class="level${level}">
    <td>
      <a title="Springe zu ${text}" href="#${target}">${text}</a>
    </td>
    <td>
      <span href="#${target}"/>
    </td>
  </tr>
</#macro>

<table class="toc">
<tbody>
  <@tocitem 1 "ziel" "1 Zielstellung des GFP" />
  <@tocitem 1 "geltung" "2 Geltungsbereich" />
  <@tocitem 1 "aktivierung" "3 Aktivierung Geschäftsfortführungsplan" />
  <@tocitem 1 "kommunikationsmatrix" "4 Interne Kommunikationsmatrix" />
  <@tocitem 1 "rueckfuehrung" "5 Rückführungskriterien" />
  <@tocitem 1 "zeitkritische-prozesse" "6 Zeitkritische Geschäftsprozesse" />
  <@tocitem 1 "pflichten-rechte" "7 Besondere Pflichten, Rechte und Kompetenzen im NOTBETRIEB" />
  <@tocitem 1 "meldepflichten" "8 Fachspezifische Melde- und Berichtspflichten im NOTBETRIEB" />
  <@tocitem 1 "gfp-massnahmen" "9 Geschäftsfortführungsmaßnahmen im Notfall" />
<#list document.getLinks("document_process")![] as processLink>
<#assign process = processLink.target />

<#list process.getLinks("process_bcSolution")![] as solutionLink>
<#assign solution = solutionLink.target />
<#assign scenario = solution.findFirstLinked("control_scenario")! />

<#if scenario?has_content>
  <@tocitem 2 "szenario_${solutionLink?counter}" "9.${solutionLink?counter} Szenario: ${(scenario.name)!}" />
  <@tocitem 3 "ausfallstrategien_${solutionLink?counter}" "9.${solutionLink?counter}.1 Übersicht Ausfallstrategien" />
  <@tocitem 3 "massnahmen-kompensieren_${solutionLink?counter}" "9.${solutionLink?counter}.2 Maßnahmen um ${(scenario.name)!} zu kompensieren" />
  <@tocitem 3 "rollen-notbetrieb_${solutionLink?counter}" "9.${solutionLink?counter}.3 Notwendige Rollen/Funktionen und Arbeitsplätze im NOTBETRIEB" />
</#if>

</#list>
</#list>
  <@tocitem 1 "wichtige-kontakte" "10 Wichtige Kontakte" />
  <@tocitem 2 "interne-kontakte" "10.1 Relevante interne Kontakte" />
  <@tocitem 2 "externe-kontakte" "10.2 Relevante externe Kontakte" />
  <@tocitem 1 "referenzdokumente" "11 Referenzdokumente" />
</tbody>
</table>

# 1 Zielstellung des GFP {#ziel}

${document.document_businessContinuityPlan_objective!}

# 2 ${bundle.document_businessContinuityPlan_scope}: {#geltung}

${document.document_businessContinuityPlan_scope!}

# 3 Aktivierung Geschäftsfortführungsplan {#aktivierung}

${document.document_businessContinuityPlan_activationCriteria!}

${document.document_businessContinuityPlan_activationProcess!}

# 4 Interne Kommunikationsmatrix {#kommunikationsmatrix}

Nach Aktivierung des Geschäftsfortführungsplans werden über die leitende Person folgende Notfallteams alarmiert:

<#list document.getLinks("document_process")![] as processLink>
<#assign process = processLink.target />

<#if process.getLinks("process_organizationalUnit")?has_content>
<#list process.getLinks("process_organizationalUnit") as orgLink>
<#assign orgUnit = orgLink.target />
<#assign manager = orgUnit.findFirstLinked("scope_manager")! />

<b>Leitende Person der zuständigen Organisationseinheit: </b><br>
${(manager.abbreviation)!}
${(manager.name)!}<br>
${(manager.person_generalInformation_givenName)!} ${(manager.person_generalInformation_familyName)!}<br>
${(manager.person_contactInformation_mobile)!}<br>
${(manager.person_contactInformation_email)!}<br>

<b>${bundle.scope_emergencyTeam}:</b>
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.scope_emergencyTeam}</th>
  <th>${bundle.scope_emergencyTeam_role}</th>
  <th>Kontaktdaten</th>
  <th>${bundle.scope_emergencyTeam_accessibility}</th>
</tr>
</thead>
<tbody>
<#list orgUnit.getLinks("scope_emergencyTeam")![] as emergencyTeamLink>
<#assign emergTeam = emergencyTeamLink.target />
<tr>
  <td>${emergTeam.abbreviation!} ${emergTeam.name!}</td>
  <td>${(emergencyTeamLink.scope_emergencyTeam_role)!}</td>
  <td>
  ${(emergTeam.scope_contactInformation_phone)!}<br>
  ${(emergTeam.person_contactInformation_mobile)!}<br>
  ${(emergTeam.person_contactInformation_email)!}<br>
  </td>
  <td>${emergencyTeamLink.scope_emergencyTeam_accessibility!}</td>
</tr>
</#list>
</tbody>
</table>
<#list orgUnit.getLinks("scope_emergencyTeam")![] as emergencyTeamLink>
<#assign emergTeam = emergencyTeamLink.target />

<#if emergTeam.parts?has_content>
### Ansprechpartner für ${emergTeam.name!}
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.person_generalInformation_givenName}, ${bundle.person_generalInformation_familyName}</th>
  <th>${bundle.person_contactInformation_office} (Büro, Mobile)</th>
  <th>${bundle.person_contactInformation_email}</th>
  <th>${bundle.status}</th>
</tr>
</thead>
<tbody>
<#list emergTeam.parts as person>
<tr>
  <td>${person.name!}</td>
  <td>
    ${person.person_contactInformation_office!}<br/>
    ${person.person_contactInformation_mobile!}
  </td>
  <td>${person.person_contactInformation_email!}</td>
  <td class="status-column">
    <span class="checkbox"></span>
</td>
</tr>
</#list>
</tbody>
</table>
</#if>
</#list>
</#list>
<#else>
Dem Geschäftsprozess wurde keine Organisationseinheit zugeordnet.
</#if>
</#list>

# 5 Rückführungskriterien {#rueckfuehrung}

<#if document.document_businessContinuityPlan_returnCriteria?has_content>
${document.document_businessContinuityPlan_returnCriteria}
<#else>
Es sind keine Rückführungskriterien definiert.
</#if>

# 6 Zeitkritische Geschäftsprozesse {#zeitkritische-prozesse}

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.document_process}</th>
  <th>MTPD/MTA (Stunden)</th>
  <th>Notbetriebsniveau/Fokus im NOTBETRIEB</th>
</tr>
</thead>
<tbody>

<#assign processLinks = document.getLinks("document_process")![] />

<#if processLinks?has_content>
<#list processLinks as processLink>
<tr>
  <td>${processLink.target.abbreviation!} ${processLink.target.name!}</td>
  <td>${processLink.target.process_bia_mtpd1!}</td>
  <td>${processLink.target.process_bia_mbco!}</td>
</tr>

<#assign dependencyLinks = processLink.target.getLinks("process_emergencyRelevant")![] />

<#if dependencyLinks?has_content>
<tr>
  <td colspan="3"><b>Prozessabhängigkeiten:</b></td>
</tr>
<tr>
  <td colspan="3">
    <table class="table fullwidth">
      <tbody>
      <#list dependencyLinks as dependencyLink>
        <tr>
          <td>${bundle.process}:</td>
          <td>${dependencyLink.target.abbreviation!} ${dependencyLink.target.name!}</td>
        </tr>
        <tr>
          <td>${bundle.process_emergencyRelevant_dependency}:</td>
          <td>${(bundle[dependencyLink.process_emergencyRelevant_dependency])!}</td>
        </tr>
        <tr>
          <td>${bundle.process_emergencyRelevant_requiredMtpd}:</td>
          <td>${dependencyLink.process_emergencyRelevant_requiredMtpd!}</td>
        </tr>
        <tr>
          <td>${bundle.process_emergencyRelevant_description}:</td>
          <td>${dependencyLink.process_emergencyRelevant_description!}</td>
        </tr>
      </#list>
      </tbody>
    </table>
  </td>
</tr>
<#else>
<tr>
  <td colspan="3">Keine Prozessabhängigkeiten verknüpft.</td>
</tr>
</#if>

</#list>
<#else>
<tr>
  <td colspan="3">Keine Geschäftsprozesse verknüpft.</td>
</tr>
</#if>
</tbody>
</table>

# 7 Besondere Pflichten, Rechte und Kompetenzen im NOTBETRIEB {#pflichten-rechte}

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.document_role}</th>
  <th>${bundle.document_role_specialDuties}</th>
  <th>${bundle.document_role_specialRights}</th>
</tr>
</thead>
<tbody>

<#assign roleLinks = document.getLinks("document_role")![] />

<#if roleLinks?has_content>
<#list roleLinks as roleLink>
<tr>
  <td>${roleLink.target.name!}</td>
  <td>${roleLink.document_role_specialDuties!}</td>
  <td>${roleLink.document_role_specialRights!}</td>
</tr>
</#list>
<#else>
<tr>
  <td colspan="3">Keine Rollen mit besonderen Pflichten, Rechten oder Kompetenzen verknüpft.</td>
</tr>
</#if>
</tbody>
</table>

# 8 Fachspezifische Melde- und Berichtspflichten im NOTBETRIEB {#meldepflichten}

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.document_role}</th>
  <th>Besondere Melde- und Berichtspflichten im NOTBETRIEB</th>
</tr>
</thead>
<tbody>

<#assign roleLinks = document.getLinks("document_role")![] />

<#if roleLinks?has_content>
<#list roleLinks as roleLink>
<tr>
  <td>${roleLink.target.name!}</td>
  <td>${roleLink.document_role_notificationRequirement!}</td>
</tr>
</#list>
<#else>
<tr>
  <td colspan="2">Keine Melde- und Berichtspflichten verknüpft.</td>
</tr>
</#if>
</tbody>
</table>

<#macro measuresFor partsSolution phaseValue>
<#list partsSolution.parts![] as measure>
<#if (measure.control_emergencyMeasureClassification_phase!"") == phaseValue>

${(measure.description!"")?no_esc}<br/>

<#if measure.parts?has_content>
<ul>
<#list measure.parts?sort_by("name") as activity>
  <li>${(activity.name!"")?no_esc}</li>
</#list>
</ul>
</#if>

</#if>
</#list>
</#macro>
# 9 Geschäftsfortführungsmaßnahmen im Notfall {#gfp-massnahmen}

Folgende Maßnahmen sollten schnellstmöglich umgesetzt werden, wenn eines oder mehrere der Ausfallszenarien eintreten:

<#assign processLinks = document.getLinks("document_process")![] />

<#if processLinks?has_content>
<#list processLinks as processLink>
<#assign process = processLink.target />

<#assign solutionLinks = process.getLinks("process_bcSolution")![] />

<#if solutionLinks?has_content>
<#list solutionLinks as solutionLink>
<#assign solution = solutionLink.target />

<#assign scenarioLinks = solution.getLinks("control_scenario")![] />

<#if scenarioLinks?has_content>
<#assign scenario = scenarioLinks[0].target />

## 9.${solutionLink?counter} Szenario: ${scenario.name!} {#szenario_${solutionLink?counter}}

### 9.${solutionLink?counter}.1 Übersicht Ausfallstrategien {#ausfallstrategien_${solutionLink?counter}}

<table class="table fullwidth">
<thead>
<tr>
  <th>Kürzel</th>
  <th>${bundle.abbreviation}</th>
  <th>${bundle.control_bcstrategy}</th>
</tr>
</thead>
<tbody>

<#assign strategies = solution.getLinks("control_bcstrategy")![] />
<#if strategies?has_content>
<#list strategies as strategyLink>
<#assign strategy = strategyLink.target />
<tr>
  <td>${strategyLink?counter}</td>
  <td>${strategy.abbreviation!}</td>
  <td>${strategy.name!}</td>
</tr>
</#list>
<#else>
<tr>
  <td colspan="2">Keine BC-Strategien verknüpft.</td>
</tr>
</#if>
</tbody>
</table>

### 9.${solutionLink?counter}.2 Maßnahmen um "${scenario.name!}" zu kompensieren {#massnahmen-kompensieren_${solutionLink?counter}}

<table class="table fullwidth small-table">
<thead>
<tr>
  <th>Rolle</th>
  <th>${bundle.control_bcstrategy}</th>
  <th>${bundle.process_emergencyRelevantResource_rto}</th>
  <th>Maßnahmen, um den NOTBETRIEB zu erreichen (Wiederanlauf in den NOTBETRIEB)</th>
  <th>Maßnahmen für die Geschäfts-&shy;fortführung (NOTBETRIEB)</th>
  <th>Maßnahmen zur Rückführung in den Normalbetrieb (Nacharbeiten im Störbetrieb)</th>
</tr>
</thead>
<tbody>
<#if solution.parts?has_content>
<#list solution.parts as partsSolution>
<tr>
  <td>${(partsSolution.getLinks("control_resource")[0].target.name)!}</td>
<td>
<#list partsSolution.getLinks("control_bcstrategy")![] as strategyLink>
  <li>${strategyLink.target.abbreviation!""} ${strategyLink.target.name!""}<br/></li>
  
</#list>
</td>
  <td></td>
<td><@measuresFor partsSolution "control_emergencyMeasureClassification_phase_restart" /></td>

<td><@measuresFor partsSolution "control_emergencyMeasureClassification_phase_emergencyMode" /></td>

<td><@measuresFor partsSolution "control_emergencyMeasureClassification_phase_returnToNormal" /></td>
</tr>
</#list>
<#else>
<tr>
  <td colspan="6">Keine BC-Lösungen oder Maßnahmen für dieses Szenario vorhanden.</td>
</tr>
</#if>
</tbody>
</table>

### 9.${solutionLink?counter}.3 Notwendige Rollen/Funktionen und Arbeitsplätze im NOTBETRIEB bei ${scenario.name!} {#rollen-notbetrieb_${solutionLink?counter}}

<table class="table fullwidth">
<thead>
<tr>
  <th>Rolle</th>
  <th>Anmerkung</th>
  <th>${bundle.control_roleGFP_staffRequirement}</th>
</tr>
</thead>
<tbody>

<#assign roles = solution.getLinks("control_roleGFP")![] />
<#if roles?has_content>
<#list roles as roleLink>
<tr>
  <td>${roleLink.target.name!}</td>
  <td>${roleLink.control_roleGFP_comment!}</td>
  <td>${roleLink.control_roleGFP_staffRequirement!}</td>
</tr>
</#list>
<#else>
<tr>
  <td colspan="3">Keine Rollen/Funktionen für den Notbetrieb verknüpft.</td>
</tr>
</#if>
</tbody>
</table>
</#if>
</#list>
<#else>
<p>Für den Geschäftsprozess sind keine BC-Lösungen verknüpft.</p>
</#if>
</#list>
<#else>
<p>Keine Geschäftsprozesse verknüpft.</p>
</#if>

# 10 Wichtige Kontakte {#wichtige-kontakte}

## 10.1 Relevante interne Kontakte {#interne-kontakte}

<table class="table fullwidth">
<thead>
<tr>
  <th>Nr.</th>
  <th>Relevante Rolle/Person</th>
  <th>${bundle.document_internalContact_description}</th>
  <th>${bundle.status}</th>
</tr>
</thead>
<tbody>

<#if (document.getLinks("document_internalContact")![])?has_content>
<#list document.getLinks("document_internalContact")![] as contactLink>
<#assign contact = contactLink.target />
<tr>
  <td>${contactLink?counter}</td>
  <td>
    ${(contact.name)!}<br/>
    ${contact.person_generalInformation_familyName!}
    ${contact.person_contactInformation_mobile!}
    ${contact.person_contactInformation_email!}
  </td>
  <td>${contactLink.document_internalContact_description!}</td>
  <td class="status-column">
    <span class="checkbox"></span>
</td>
</tr>
</#list>
<#else>
<tr>
  <td colspan="4">Keine internen Kontakte verknüpft.</td>
</tr>
</#if>
</tbody>
</table>

## 10.2 Relevante externe Kontakte {#externe-kontakte}

<table class="table fullwidth">
<thead>
<tr>
  <th>Nr.</th>
  <th>${bundle.scope_SCP_ExternalServiceProvider_singular} </th>
  <th>${bundle.document_externalContact_description}</th>
  <th>${bundle.status}</th>
</tr>
</thead>
<tbody>

<#if (document.getLinks("document_externalContact")![])?has_content>
<#list document.getLinks("document_externalContact")![] as externalContactLink>
<#assign contact = externalContactLink.target />

<tr>
  <td>${externalContactLink?counter}</td>
  <td>
    ${(externalContactLink.target.name)!}<br/>
    ${contact.person_generalInformation_familyName!}
    ${contact.person_contactInformation_mobile!}
    ${contact.person_contactInformation_email!}
  </td>
  <td>${externalContactLink.document_externalContact_description!}</td>
  <td class="status-column">
    <span class="checkbox"></span>
</td>
</tr>
</#list>
<#else>
<tr>
  <td colspan="4">Keine externen Kontakte verknüpft.</td>
</tr>
</#if>
</tbody>
</table>

<#if document.getLinks("document_externalContact")?has_content>
### Ansprechpartner externer Kontakte
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.person_generalInformation_givenName}, ${bundle.person_generalInformation_familyName}</th>
  <th>${bundle.person_contactInformation_office} (Büro, Mobile)</th>
  <th>${bundle.person_contactInformation_email}</th>
  <th>${bundle.status}</th>
</tr>
</thead>
<tbody>

<#list document.getLinks("document_externalContact")![] as externalContactLink>
<#assign provider = externalContactLink.target />
<#list provider.members as person>
<tr>
  <td>${person.name!}</td>
  <td>
    ${person.person_contactInformation_office!}<br/>
    ${person.person_contactInformation_mobile!}
  </td>
  <td>${person.person_contactInformation_email!}</td>
  <td class="status-column">
    <span class="checkbox"></span>
</td>
</tr>
</#list>
</#list>
</#if>
</tbody>
</table>

# 11 Referenzdokumente {#referenzdokumente}

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.abbreviation}</th>
  <th>${bundle.name}</th>
  <th>URL</th>
  <th>${bundle.document_storageArchiving_location}</th>
</tr>
</thead>
<tbody>

<#if document.getLinks("document_doc")?has_content>
<#list document.getLinks("document_doc")![] as docLink>
<#assign refDoc = docLink.target />

<tr>
  <td>${refDoc.abbreviation!}</td>
  <td>${refDoc.name!}</td>
  <td></td>
  <td></td>
</tr>
</#list>
<#else>
<tr>
  <td colspan="4">Keine externen Referenzdokumente verknüpft.</td>
</tr>
</#if>
</tbody>
</table>