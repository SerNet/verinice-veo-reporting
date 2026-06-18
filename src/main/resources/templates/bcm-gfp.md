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
            '${(bundle.valid_from)?no_esc} ${((document.document_docManagement_dateOfApproval?date.iso)!)?no_esc}\A'
            '${(bundle.page)?no_esc} ' counter(page) ' ${(bundle.of)?no_esc} ' counter(pages);
    }
}
</style>

<div class="footer-left">
  ${institution.abbreviation!} ${institution.name!}<br/>
  ${orgUnit.name!}<br/>
  Erstelldatum: ${.now?date}
</div>

<div class="cover">
  <h1>${bundle.control_emergencyMeasureClassification_planType_gfp}</h1>
  <h2>
    ${bundle.document_process}:</br> 
    ${process.abbreviation!} ${process.name!}
  </h2>
  <p>powered by verinice</p>
</div>

# Hauptblatt

<div class="main_page">
<table>
<tr>
     <b>${bundle.scope_SCP_Institution_singular}</b>
    </tr>
    <tr>
    <tr>
      <td>${bundle.name}:</td>
      <td>${institution.abbreviation!} ${institution.name!}</td>
    </tr>
    <tr>
  <td>${bundle.scope_address_address1}:</td>
  <td>
    ${institution.scope_address_address1!}
  </td>
   <tr>
  <td>${bundle.scope_address_postcode}, ${bundle.scope_address_city}:</td>
  <td>
    ${institution.scope_address_postcode!}
    ${institution.scope_address_city!}
  </td>
   </tr>
    <tr>
       <td>${bundle.scope_contactInformation_phone}/${bundle.scope_contactInformation_fax}:</td>
      <td>${institution.scope_contactInformation_phone!} / ${institution.scope_contactInformation_fax!}</td>
    </tr>
    <tr>
        <td>${bundle.scope_contactInformation_email}:</td>
        <td>${institution.scope_contactInformation_email!}</td>
    </tr>
    <tr>
        <td>${bundle.scope_contactInformation_website}:</td>
        <td>${institution.scope_contactInformation_website!}</td>
    </tr>
  </table>

  <b>${bundle.responsible_organizational_unit}</b></br>
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
  <th>${bundle.designation}</th>
  <th>${bundle.document_data}</th>
</tr>
</thead>
<tbody>
<tr>
  <td>${bundle.document_process}:</td>
  <td>${process.abbreviation!} ${process.name!}</td>
</tr>
<tr>
  <td>${bundle.document_docManagement_classification}:</td>
  <td>${(bundle[document.document_docManagement_classification])!}</td>
</tr>
<tr>
  <td>${bundle.document_docManagement_version}:</td>
  <td>${document.document_docManagement_version!}</td>
</tr>
<tr>
  <td>${bundle.responsible}:</td>
  <td>${(orgUnit.findFirstLinked("scope_manager").name)!}</td>
</tr>
<tr>
  <td>${bundle.document_storageArchiving_location}:</td>
  <td>${document.document_storageArchiving_location!}</td>
</tr>
<tr>
  <td>${bundle.document_businessContinuityPlan_targetGroup}:</td>
  <td>${document.document_businessContinuityPlan_targetGroup!}</td>
</tr>
<tr>
  <td>${bundle.created_on}:</td>
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

# ${bundle.toc} {#toc}

<#macro tocitem level target text>
  <tr class="level${level}">
    <td>
      <a title="${bundle.jump_to} ${text}" href="#${target}">${text}</a>
    </td>
    <td>
      <span href="#${target}"/>
    </td>
  </tr>
</#macro>

<table class="toc">
<tbody>
  <@tocitem 1 "ziel" "1 ${bundle.objective_gfp}" />
  <@tocitem 1 "geltung" "2 ${bundle.document_businessContinuityPlan_scope}" />
  <@tocitem 1 "aktivierung" "3 ${bundle.activation_business_continuity_plan}" />
  <@tocitem 1 "kommunikationsmatrix" "4 ${bundle.internal_communication_matrix}" />
  <@tocitem 1 "rueckfuehrung" "5 ${bundle.return_criteria}" />
  <@tocitem 1 "zeitkritische-prozesse" "6 ${bundle.time_critical_business_processes}" />
  <@tocitem 1 "pflichten-rechte" "7 ${bundle.special_duties_rights_authorities_emergency_operation}" />
  <@tocitem 1 "meldepflichten" "8 ${bundle.notification_and_reporting_obligations_emergency_operation}" />
  <@tocitem 1 "gfp-massnahmen" "9 ${bundle.business_continuity_measures_emergency}" />

<#list document.getLinks("document_process")![] as processLink>
<#assign process = processLink.target />

<#list process.getLinks("process_bcSolution")![] as solutionLink>
<#assign solution = solutionLink.target />
<#assign scenario = solution.findFirstLinked("control_scenario")! />

<#if scenario?has_content>
  <@tocitem 2 "szenario_${solutionLink?counter}" "9.${solutionLink?counter} Szenario: ${scenario.name!}" />
  <@tocitem 3 "ausfallstrategien_${solutionLink?counter}" "9.${solutionLink?counter}.1 Übersicht Ausfallstrategien" />
  <@tocitem 3 "massnahmen-kompensieren_${solutionLink?counter}" "9.${solutionLink?counter}.2 ${bundle.compensation_measures_for} ${(scenario.name)!}" />
  <@tocitem 3 "rollen-notbetrieb_${solutionLink?counter}" "9.${solutionLink?counter}.3 Notwendige Rollen/Funktionen und Arbeitsplätze im NOTBETRIEB" />
</#if>

</#list>
</#list>
  <@tocitem 1 "wichtige-kontakte" "10 ${bundle.important_contacts}" />
  <@tocitem 2 "interne-kontakte" "10.1 ${bundle.relevant_internal_contacts}" />
  <@tocitem 2 "externe-kontakte" "10.2 ${bundle.relevant_external_contacts}" />
  <@tocitem 1 "referenzdokumente" "11 ${bundle.reference_documents}" />
</tbody>
</table>

# 1 ${bundle.objective_gfp} {#ziel}

${document.document_businessContinuityPlan_objective!}

# 2 ${bundle.document_businessContinuityPlan_scope} {#geltung}

${document.document_businessContinuityPlan_scope!}

# 3 ${bundle.activation_business_continuity_plan} {#aktivierung}

${document.document_businessContinuityPlan_activationCriteria!}

${document.document_businessContinuityPlan_activationProcess!}

# 4 ${bundle.internal_communication_matrix} {#kommunikationsmatrix}

${bundle.following_emergency_teams_are_alerted}:

<#list document.getLinks("document_process")![] as processLink>
<#assign process = processLink.target />

<#if process.getLinks("process_organizationalUnit")?has_content>
<#list process.getLinks("process_organizationalUnit") as orgLink>
<#assign orgUnit = orgLink.target />
<#assign manager = orgUnit.findFirstLinked("scope_manager")! />

<b>${bundle.responsible_organizational_unit_manager}: </b><br>
${(manager.abbreviation)!}
${(manager.name)!}<br>
${bundle.person_generalInformation_givenName}: ${manager.person_generalInformation_givenName!}<br/> 
${bundle.person_generalInformation_familyName}: ${manager.person_generalInformation_familyName!}<br/>
${bundle.person_contactInformation_mobile}: ${manager.person_contactInformation_mobile!}<br/>
${bundle.person_contactInformation_email}: ${manager.person_contactInformation_email!}<br/>

<b>${bundle.scope_emergencyTeam}:</b>
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.scope_emergencyTeam}</th>
  <th>${bundle.scope_emergencyTeam_role}</th>
  <th>${bundle.contact_details}</th>
  <th>${bundle.scope_emergencyTeam_accessibility}</th>
</tr>
</thead>
<tbody>
<#list orgUnit.getLinks("scope_emergencyTeam")![] as emergencyTeamLink>
<#assign emergTeam = emergencyTeamLink.target />
<tr>
  <td>${emergTeam.abbreviation!} ${emergTeam.name!}</td>
  <td>${emergencyTeamLink.scope_emergencyTeam_role!}</td>
  <td>
  ${bundle.person_contactInformation_office}: ${emergTeam.person_contactInformation_office!}<br>
  ${bundle.person_contactInformation_mobile}: ${emergTeam.person_contactInformation_mobile!}<br>
  ${bundle.person_contactInformation_email}: ${emergTeam.person_contactInformation_email!}<br>
  </td>
  <td>${emergencyTeamLink.scope_emergencyTeam_accessibility!}</td>
</tr>
</#list>
</tbody>
</table>
<#list orgUnit.getLinks("scope_emergencyTeam")![] as emergencyTeamLink>
<#assign emergTeam = emergencyTeamLink.target />

<#if emergTeam.parts?has_content>
### ${bundle.contact_person_for} ${emergTeam.name!}
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.person_generalInformation_givenName}, ${bundle.person_generalInformation_familyName}</th>
  <th>${bundle.contact_details}</th>
  <th>${bundle.person_contactInformation_email}</th>
  <th>${bundle.status}</th>
</tr>
</thead>
<tbody>
<#list emergTeam.parts as person>
<tr>
  <td>${person.abbreviation!} ${person.name!}</td>
  <td>
     ${bundle.person_contactInformation_office}: ${person.person_contactInformation_office!}<br/>
     ${bundle.person_contactInformation_mobile}: ${person.person_contactInformation_mobile!}
  </td>
  <td> ${bundle.person_contactInformation_email}: ${person.person_contactInformation_email!}</td>
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
${bundle.no_organizational_unit_assigned_to_process}
</#if>
</#list>

# 5 ${bundle.return_criteria} {#rueckfuehrung}

<#if document.document_businessContinuityPlan_returnCriteria?has_content>
${document.document_businessContinuityPlan_returnCriteria}
<#else>
${bundle.no_return_criteria_defined}
</#if>

# 6 ${bundle.time_critical_business_processes} {#zeitkritische-prozesse}

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.document_process}</th>
  <th>${bundle.mtpd_mta_hours}</th>
  <th>${bundle.emergency_operation_level}</th>
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
  <td colspan="3"><b>${bundle.process_dependencies}:</b></td>
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
  <td colspan="3">${bundle.no_process_dependencies_linked}</td>
</tr>
</#if>

</#list>
<#else>
<tr>
  <td colspan="3">${bundle.no_business_processes_linked}</td>
</tr>
</#if>
</tbody>
</table>

# 7 ${bundle.special_duties_rights_authorities_emergency_operation} {#pflichten-rechte}

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
  <td colspan="3">${bundle.no_special_roles_linked}</td>
</tr>
</#if>
</tbody>
</table>

# 8 ${bundle.notification_and_reporting_obligations_emergency_operation} {#meldepflichten}

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.document_role}</th>
  <th>${bundle.eporting_obligations}</th>
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
  <td colspan="2">${bundle.no_reporting_obligations_linked}</td>
</tr>
</#if>
</tbody>
</table>

<#macro measuresFor partsSolution phaseValue>
<#list partsSolution.parts![] as measure>
<#if (measure.control_emergencyMeasureClassification_phase!"") == phaseValue>

${measure.description!}

<#if measure.parts?has_content>
<ul>
<#list measure.parts?sort_by("abbreviation_naturalized") as activity>
<li>${activity.abbreviation!} ${activity.name!}</li>
</#list>
</ul>
</#if>

</#if>
</#list>
</#macro>
# 9 ${bundle.business_continuity_measures_emergency} {#gfp-massnahmen}

${bundle.emergency_measures_for_scenarios}:

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

## 9.${solutionLink?counter} ${bundle.failure_scenario}: ${scenario.name!} {#szenario_${solutionLink?counter}}

### 9.${solutionLink?counter}.1 ${bundle.overview_failure_strategies} {#ausfallstrategien_${solutionLink?counter}}

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.short_code}</th>
  <th>${bundle.abbreviation}</th>
  <th>${bundle.control_bcstrategy}</th>
</tr>
</thead>
<tbody>

<#assign strategies = solution.findLinked("control_bcstrategy")![] />
<#if strategies?has_content>
<#list strategies?sort_by("abbreviation_naturalized") as strategy>
<tr>
  <td>${strategy?counter}</td>
  <td>${strategy.abbreviation!}</td>
  <td>${strategy.name!}</td>
</tr>
</#list>
<#else>
<tr>
  <td colspan="3">${bundle.no_bc_strategies_linked}</td>
</tr>
</#if>
</tbody>
</table>

### 9.${solutionLink?counter}.2 ${bundle.compensation_measures_for} "${scenario.name!}" {#massnahmen-kompensieren_${solutionLink?counter}}

<table class="table fullwidth small-table">
<thead>
<tr>
  <th>${bundle.role}</th>
  <th>${bundle.control_bcstrategy}</th>
  <th>${bundle.process_emergencyRelevantResource_rto}</th>
  <th>${bundle.measures_restart}</th>
  <th>${bundle.measures_emergency_operation}</th>
  <th>${bundle.measures_return_to_normal}</th>
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
  <td colspan="6">${bundle.no_bc_solutions_or_measures_linked}</td>
</tr>
</#if>
</tbody>
</table>

### 9.${solutionLink?counter}.3 ${bundle.emergency_operation_roles} ${scenario.name!} {#rollen-notbetrieb_${solutionLink?counter}}

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.role}</th>
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
  <td colspan="3">${bundle.no_emergency_operation_roles_linked}</td>
</tr>
</#if>
</tbody>
</table>
</#if>
</#list>
<#else>
<p>${bundle.no_bc_solutions_linked}</p>
</#if>
</#list>
<#else>
<p>${bundle.no_business_processes_linked}</p>
</#if>

# 10 ${bundle.important_contacts} {#wichtige-kontakte}

## 10.1 ${bundle.relevant_internal_contacts} {#interne-kontakte}

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.number}</th>
  <th>${bundle.control_internalContactPerson}</th>
  <th>${bundle.document_internalContact_description}</th>
  <th>${bundle.status}</th>
</tr>
</thead>
<tbody>

<#if document.getLinks("document_internalContact")?has_content>
<#list document.getLinks("document_internalContact") as contactLink>
<#assign contact = contactLink.target />
<tr>
  <td>${contactLink?counter}</td>
  <td>
    ${contact.name!}<br/>
    ${bundle.person_generalInformation_givenName!}: ${contact.person_generalInformation_givenName!}<br/>
    ${bundle.person_generalInformation_familyName!}: ${contact.person_generalInformation_familyName!}<br/>
    ${bundle.person_contactInformation_mobile!}: ${contact.person_contactInformation_mobile!}<br/>
    ${bundle.person_contactInformation_email!}: ${contact.person_contactInformation_email!}<br/>
  </td>
  <td>${contactLink.document_internalContact_description!}</td>
  <td class="status-column">
    <span class="checkbox"></span>
</td>
</tr>
</#list>
<#else>
<tr>
  <td colspan="4">${bundle.no_internal_contacts_linked}</td>
</tr>
</#if>
</tbody>
</table>

## 10.2 ${bundle.relevant_external_contacts} {#externe-kontakte}

<#if document.getLinks("document_externalContact")?has_content>
<#list document.getLinks("document_externalContact") as externalContactLink>
<#assign contact = externalContactLink.target />

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.number}</th>
  <th>${bundle.scope_SCP_ExternalServiceProvider_singular} </th>
  <th>${bundle.document_externalContact_description}</th>
  <th>${bundle.status}</th>
</tr>
</thead>
<tbody>
<tr>
  <td>${externalContactLink?counter}</td>
  <td>
    ${contact.abbreviation!} ${contact.name!}<br/>
    Kontaktinformationen:<br/>
    ${bundle.scope_address_address1}:<br/>
    ${contact.scope_address_address1!}<br/>
    Postleitzahl, Stadt: ${contact.scope_address_postcode!} ${contact.scope_address_city!}<br/>
    ${bundle.scope_contactInformation_phone}: ${contact.scope_contactInformation_phone!}<br/>
    ${bundle.person_contactInformation_email}: ${contact.scope_contactInformation_email!}
  </td>
  <td>${externalContactLink.document_externalContact_description!}</td>
  <td class="status-column">
    <span class="checkbox"></span>
</td>
</tr>
<tr>
  <td colspan="4">
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

<#if contact.members?has_content>
<#assign providerPersons = contact.members?filter(m -> m.hasSubType("PER_Person")) />

<#if providerPersons?has_content>
<#list providerPersons as person>
<tr>
  <td>${person.name!}<br/>
    ${bundle.person_generalInformation_givenName}: ${person.person_generalInformation_givenName!}<br/>
    ${bundle.person_generalInformation_familyName}: ${person.person_generalInformation_familyName!}<br/>
  </td>
  <td>
    ${bundle.person_contactInformation_office}: ${person.person_contactInformation_office!}<br/>
    ${bundle.person_contactInformation_mobile}: ${person.person_contactInformation_mobile!}
  </td>
  <td>${person.person_contactInformation_email!}</td>
  <td class="status-column">
    <span class="checkbox"></span>
  </td>
</tr>
</#list>
</#if>
</#if>
</#list>
</tbody>
</table>
 </td>
</tr>
</tbody>
</table>

<#else>
<tr>
  <td colspan="4">${bundle.no_external_contacts_linked}</td>
</tr>
</#if>

# 11 ${bundle.reference_documents} {#referenzdokumente}

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
<#list document.getLinks("document_doc") as docLink>
<#assign refDoc = docLink.target />

<tr>
  <td>${refDoc.abbreviation!}</td>
  <td>${refDoc.name!}</td>
  <td>${refDoc.document_generalInformation_document !}</td>
  <td>${refDoc.document_storageArchiving_location!}</td>
</tr>
</#list>
<#else>
<tr>
  <td colspan="4">${bundle.no_external_reference_documents_linked}</td>
</tr>
</#if>
</tbody>
</table>