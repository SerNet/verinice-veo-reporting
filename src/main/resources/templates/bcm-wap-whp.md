<#import "/libs/commons.md" as com>

<#assign resource = target />
<#assign institution = (resource.scopes?filter(s -> s.hasSubType("SCP_Institution"))?first)! />
<#assign wapDocument = resource.findFirstLinked("asset_documentWap")! />
<#assign bcSolution = resource.findFirstLinked("asset_bcSolution")! />

<style>
<@com.defaultStyles />

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

.checkbox {
  display: inline-block;
  width: 8pt;
  height: 8pt;
  border: 1px solid grey;
}

.status-column {
  text-align: center;
}

table.small-table {
  table-layout: fixed;
  font-size: 80%;
}

table.small-table th,
table.small-table td {
  white-space: normal;
}

.footer-left {
	padding: 22px 0;
}

@page {
    @bottom-right {
        border-top: 0.1mm solid #929292;
        font-family: Open Sans;
        font-size: 6pt;
        color: #767676;
    
        white-space: pre;

         content:
            '${(bundle.title)?no_esc}\A'
            '${(bundle.wap_whp_for_resource)?no_esc}: ${((resource.name)!)?no_esc}\A'
            '${(bundle.document_docManagement_version)?no_esc}: ${((wapDocument.document_docManagement_version)!)?no_esc}\A'
            '${(bundle.valid_from)?no_esc}: ${((wapDocument.document_docManagement_dateOfApproval?date.iso)!)?no_esc}\A'
            '${(bundle.page)?no_esc} ' counter(page) ' ${(bundle.of)?no_esc} ' counter(pages);
    }
}
</style>

<div class="footer-left">
  ${institution.name!}<br/>
  ${bundle.creation_date}: ${.now?date}
</div>

<div class="cover">
  <h1>${bundle.title}</h1>
  <h2>${bundle.wap_whp_for_resource}:<br/>${resource.abbreviation!} ${resource.name!}</h2>
  <p>powered by verinice</p>
</div>

# ${bundle.main_page}

<div class="main_page">

<#if institution?has_content>
<table class="table fullwidth">
<tbody>
<tr>
  <th colspan="2">Institution</th>
</tr>
<tr>
  <td>${bundle.name}:</td>
  <td>${institution.abbreviation!} ${institution.name!}</td>
</tr>
<tr>
  <td>${bundle.scope_address_address1}:</td>
  <td>${institution.scope_address_address1!}</td>
</tr>
<tr>
  <td>${bundle.scope_address_postcode}, ${bundle.scope_address_city}:</td>
  <td>${institution.scope_address_postcode!}, ${institution.scope_address_city!}</td>
</tr>
<tr>
  <td>${bundle.scope_contactInformation_phone} / ${bundle.scope_contactInformation_fax}:</td>
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
</tbody>
</table>
<#else>
<p>${bundle.no_institution_linked_to_resource}</p>
</#if>

</div>

# ${bundle.document_properties}

<#if wapDocument?has_content>
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.designation}</th>
  <th>${bundle.document_data}</th>
</tr>
</thead>
<tbody>
<tr>
  <td>${bundle.document_docManagement_classification}:</td>
  <td>${(bundle[wapDocument.document_docManagement_classification])!}</td>
</tr>
<tr>
  <td>${bundle.document_docManagement_version}:</td>
  <td>${wapDocument.document_docManagement_version!}</td>
</tr>
<tr>
  <td>${bundle.responsible}:</td>
  <td>${(resource.findFirstLinked("asset_responsiblePerson").name)!}</td>
</tr>
<tr>
  <td>${bundle.document_storageArchiving_location}:</td>
  <td>${wapDocument.document_storageArchiving_location!}</td>
</tr>
<tr>
  <td>${bundle.created_on}</td>
  <td>${.now?date}</td>
</tr>
<tr>
  <td>${bundle.document_docAuthor}:</td>
  <td>${(wapDocument.findFirstLinked("document_docAuthor").name)!}</td>
</tr>
<tr>
  <td>${bundle.document_revision_last}:</td>
  <td>${(wapDocument.document_revision_last?date.iso)!}</td>
</tr>
<tr>
  <td>${bundle.document_revision_next}:</td>
  <td>${(wapDocument.document_revision_next?date.iso)!}</td>
</tr>
<tr>
  <td>${bundle.document_docManagement_dateOfApproval}:</td>
  <td>${(wapDocument.document_docManagement_dateOfApproval?date.iso)!}</td>
</tr>
<tr>
  <td>${bundle.document_docApprovalThrough}:</td>
  <td>${(wapDocument.findFirstLinked("document_docApprovalThrough").name)!}</td>
</tr>
</tbody>
</table>
<#else>
<p>${bundle.no_wap_whp_document_linked_to_resource}</p>
</#if>

<div class="pagebreak"></div>

# ${bundle.resource_information}

${bundle.resource_plan_description}

<table class="table fullwidth">
<tbody>
<tr>
  <td>${bundle.name}</td>
  <td>${resource.name!}</td>
</tr>
<tr>
  <td>${bundle.description}</td>
  <td>${resource.description!}</td>
</tr>
<tr>
  <td>${bundle.resource_owner}</td>
  <td>${(resource.findFirstLinked("asset_responsiblePerson").name)!}</td>
</tr>
<tr>
  <td>${bundle.rta_rto}</td>
  <td>${resource.asset_biaParameterComparison_rta!} / ${resource.asset_resourceBia_rto!}</td>
</tr>
<tr>
  <td>${bundle.rpa_rto}</td>
  <td>${resource.asset_biaParameterComparison_rpa!} / ${resource.asset_resourceBia_rpo!}</td>
</tr>
</tbody>
</table>

<div class="pagebreak"></div>

# ${bundle.toc} {#toc}

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
  <@tocitem 1 "allgemeine-informationen" "1 ${bundle.general_information}" />
  <@tocitem 2 "zielsetzung" "1.1 ${bundle.objective}" />
  <@tocitem 2 "aktivierungsprozess" "1.2 ${bundle.activation_process}" />

  <@tocitem 1 "voraussetzungen-wiederanlauf" "2 ${bundle.prerequisites_restart}" />

  <@tocitem 1 "wiederanlauf" "3 ${bundle.resource_restart}" />
  <@tocitem 2 "ablaufplan-wiederanlauf" "3.1 ${bundle.restart_schedule}" />
  <@tocitem 2 "durchfuehrung-wiederanlauf" "3.2 ${bundle.restart_execution}" />
  <@tocitem 2 "funktionstests-notbetrieb" "3.3 ${bundle.functional_tests_emergency_operation}" />
  <@tocitem 2 "notbetrieb-einschraenkungen" "3.4 ${bundle.control_emergencyOperatingLevel_description}" />

  <@tocitem 1 "voraussetzungen-wiederherstellung" "4 ${bundle.prerequisites_recovery}" />

  <@tocitem 1 "wiederherstellung" "5 ${bundle.resource_recovery}" />
  <@tocitem 2 "ablaufplan-wiederherstellung" "5.1 ${bundle.recovery_schedule}" />
  <@tocitem 2 "durchfuehrung-wiederherstellung" "5.2 ${bundle.recovery_execution}" />
  <@tocitem 2 "rueckfuehrung-normalbetrieb" "5.3 ${bundle.return_to_normal_operation}" />

  <@tocitem 1 "nachbereitung-dokumentation" "6 ${bundle.follow_up_documentation}" />

  <@tocitem 1 "anhang" "7 ${bundle.appendix}" />
  <@tocitem 2 "referenzdokumente" "7.1 ${bundle.reference_documents}" />
</tbody>
</table>

<#if !bcSolution?has_content>
<p>${bundle.no_bc_solution_linked_to_resource}</p>
<#else>

# 1 ${bundle.general_information} {#allgemeine-informationen}

${bundle.wap_whp_introduction}

## 1.1 ${bundle.objective} {#zielsetzung}

<#if bcSolution.control_scopeWAP_objective?has_content>
${bcSolution.control_scopeWAP_objective}
<#else>
${bundle.wap_whp_purpose}
</#if>

<#assign scenario = bcSolution.findFirstLinked("control_scenario")! />

${bundle.wap_whp_applies_to_scenario}:

<#if scenario?has_content>

<p>${scenario.name!}</p>
<#else>
<p>${bundle.no_failure_scenario_linked}</p>
</#if>


## 1.2 ${bundle.activation_process} {#aktivierungsprozess}

${bundle.wap_whp_activation_prerequisites}:

<#if bcSolution.control_scopeWAP_activationProcess?has_content>
${bcSolution.control_scopeWAP_activationProcess}
<#else>
<p>${bundle.no_activation_process_defined}</p>
</#if>

<#macro personContactRows members >
    <#list members?filter(m -> m.hasSubType("PER_Person")) as person>
<tr>
  <td>
    ${person.name!}<br/>
   ${bundle.person_generalInformation_givenName}: ${person.person_generalInformation_givenName!}<br/>
   ${bundle.person_generalInformation_familyName}: ${person.person_generalInformation_familyName!}<br/>
  </td>
  <td>
  <#if person.person_contactInformation_office?has_content || person.person_contactInformation_mobile?has_content>
     ${(bundle.person_contactInformation_office!)}: ${person.person_contactInformation_office!}<br/>
    ${(bundle.person_contactInformation_mobile!)}: ${person.person_contactInformation_mobile!}
    <#else>
    <p>${bundle.no_contact_information_available}</p>
</#if>
  </td>
   <td>
   <#if person.person_contactInformation_email?has_content >
       ${bundle.person_contactInformation_email!}: ${person.person_contactInformation_email!}
        <#else>
        <p>${bundle.no_email_address_available}</p>
    </#if>
    </td>

</tr>
</#list>

</#macro>

<#macro importantContacts planSuffix>
<#-- planSuffix: "wap" oder "whp" -->

#### ${bundle.relevant_external_contacts}

<#assign externalContactLinks = bcSolution.getLinks("control_externalContact")![] />
<#assign hasExternalContacts = false />

<#list externalContactLinks as externalContactLink>
  <#if (externalContactLink.control_externalContact_forWapOrWhp![])?seq_contains("control_externalContact_forWapOrWhp_${planSuffix}")>
    <#assign hasExternalContacts = true />
  </#if>
</#list>

<#if hasExternalContacts>

<#list externalContactLinks as externalContactLink>
<#if (externalContactLink.control_externalContact_forWapOrWhp![])?seq_contains("control_externalContact_forWapOrWhp_${planSuffix}")>
<#assign provider = externalContactLink.target />
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.external_service_provider}</th>
  <th>${bundle.description}</th>
</tr>
</thead>
<tbody>
<tr>
  <td>
    ${provider.abbreviation!} ${provider.name!}<br/>
    ${bundle.scope_contactInformation_phone}: ${provider.scope_contactInformation_phone!}<br/>
    ${bundle.scope_contactInformation_email}: ${provider.scope_contactInformation_email!}<br/>
  </td>
  <td>${externalContactLink.control_externalContact_description!}</td>
</tr>

<#if provider.members?has_content>
<tr>
  <td colspan="4">
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.person_generalInformation_givenName}, ${bundle.person_generalInformation_familyName}</th>
  <th>${bundle.person_contactInformation_office} (Büro, Mobile)</th>
  <th>${bundle.person_contactInformation_email}</th>
</tr>
</thead>
<tbody>

<@personContactRows provider.members![] />

</tbody>
</table>
  </td>
</tr>
</#if>

</#if>
</#list>
</tbody>
</table>
<#else>
<p>${bundle.no_relevant_external_contacts}</p>
</#if>


#### ${bundle.relevant_internal_organizational_units}

<#assign internalOeLinks = bcSolution.getLinks("control_internalContactOE")![] />
<#assign hasInternalOeContacts = false />

<#list internalOeLinks as internalOeLink>
  <#if (internalOeLink.control_internalContactOE_forWapOrWhp![])?seq_contains("control_internalContactOE_forWapOrWhp_${planSuffix}")>
    <#assign hasInternalOeContacts = true />
  </#if>
</#list>

<#if hasInternalOeContacts>
<#list internalOeLinks as internalOeLink>
<#if (internalOeLink.control_internalContactOE_forWapOrWhp![])?seq_contains("control_internalContactOE_forWapOrWhp_${planSuffix}")>
<#assign oe = internalOeLink.target />
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.organizational_unit}</th>
  <th>${bundle.comment}</th>
</tr>
</thead>
<tbody>

<tr>
  <td>${oe.abbreviation!} ${oe.name!}</td>
  <td>${internalOeLink.control_internalContactOE_description!}</td>
</tr>

<#if oe.members?has_content>
<tr>
  <td colspan="4">
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.person_generalInformation_givenName}, ${bundle.person_generalInformation_familyName}</th>
  <th>${bundle.person_contactInformation_office} (Büro, Mobile)</th>
  <th>${bundle.person_contactInformation_email}</th>
</tr>
</thead>
<tbody>

<@personContactRows oe.members![] />

</tbody>
</table>
  </td>
</tr>
</#if>

</#if>
</#list>
</tbody>
</table>
<#else>
<p>${bundle.no_relevant_internal_organizational_units}</p>
</#if>

#### ${bundle.relevant_internal_persons}

<#assign internalPersonLinks = bcSolution.getLinks("control_internalContactPerson")![] />
<#assign hasInternalPersons = false />

<#list internalPersonLinks as internalPersonLink>
  <#if (internalPersonLink.control_internalContactPerson_forWapOrWhp![])?seq_contains("control_internalContactPerson_forWapOrWhp_${planSuffix}")>
    <#assign hasInternalPersons = true />
  </#if>
</#list>

<#if hasInternalPersons>
<#list internalPersonLinks as internalPersonLink>
<#if (internalPersonLink.control_internalContactPerson_forWapOrWhp![])?seq_contains("control_internalContactPerson_forWapOrWhp_${planSuffix}")>
<#assign person = internalPersonLink.target />
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.relevant_role_person}</th>
  <th>${bundle.comment}</th>
</tr>
</thead>
<tbody>

<tr>
  <td>
    ${person.abbreviation!} ${person.name!}<br/>
    ${bundle.person_generalInformation_givenName}: ${person.person_generalInformation_givenName!}<br/>
    ${bundle.person_generalInformation_familyName}: ${person.person_generalInformation_familyName!}<br/>
    ${bundle.person_contactInformation_mobile}: ${person.person_contactInformation_mobile!}<br/>
    ${bundle.person_contactInformation_email}: ${person.person_contactInformation_email!}
  </td>
  <td>${internalPersonLink.control_internalContactPerson_description!}</td>
</tr>

<#if person.parts?has_content>
<tr>
  <td colspan="4">
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.person_generalInformation_givenName}, ${bundle.person_generalInformation_familyName}</th>
  <th>${bundle.person_contactInformation_office} (Büro, Mobile)</th>
  <th>${bundle.person_contactInformation_email}</th>
</tr>
</thead>
<tbody>

<@personContactRows person.parts![] />

</tbody>
</table>
  </td>
</tr>
</#if>

</#if>
</#list>
</tbody>
</table>
<#else>
<p>${bundle.no_relevant_internal_persons}</p>
</#if>



</#macro>

<#macro technicalRequirements linkName descriptionField emptyText>
<#assign technicalRequirementLinks = bcSolution.getLinks(linkName)![] />

<#if technicalRequirementLinks?has_content>
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.number}</th>
  <th>${bundle.abbreviation},${bundle.name}</th>
  <th>${bundle.technical_prerequisites}/ ${bundle.comment}</th>
  <th>${bundle.status}</th>
</tr>
</thead>
<tbody>
<#list technicalRequirementLinks as requirementLink>
<tr>
  <td>${requirementLink?counter}</td>
  <td>
    ${requirementLink.target.abbreviation!} ${requirementLink.target.name!}
  </td>
  <td>${requirementLink[descriptionField]!}</td>
  <td class="status-column"><span class="checkbox"></span></td>
</tr>
</#list>
</tbody>
</table>
<#else>
<p>${emptyText}</p>
</#if>
</#macro>

# 2 ${bundle.prerequisites_restart} {#voraussetzungen-wiederanlauf}

## <b>${bundle.general_prerequisites}</b>

<#if bcSolution.control_scopeWAP_genRequirementWAP?has_content>
${bcSolution.control_scopeWAP_genRequirementWAP}
<#else>
<p>${bundle.no_general_prerequisites_defined}</p>
</#if>

## <b>${bundle.organizational_prerequisites}</b></br>
### <b>${bundle.required_documents_approvals}</b>

<#assign organizationalRequirementDocs = bcSolution.getLinks("control_organizationalRequirementDoc")![] />
<#if organizationalRequirementDocs?has_content>

<#list organizationalRequirementDocs as docLink>
<#assign reqDoc = docLink.target />

  ${reqDoc.abbreviation!} ${reqDoc.name!}
  ${reqDoc.document_generalInformation_document!}
  ${docLink.control_organizationalRequirementDoc_description!}
</#list>
<#else>
<p>${bundle.no_required_documents_approvals_linked}</p>
</#if>

### ${bundle.important_contacts}

<@importantContacts "wap" />

## ${bundle.technical_prerequisites}

<@technicalRequirements
  "control_technicalRequirement"
  "control_technicalRequirement_description"
  bundle.no_technical_restart_prerequisites_defined />

  <!-- macro for 3.1 and 5.1 -->
  <#macro activityPlanTable activities title emptyText>

<#if activities?has_content>
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.number}</th>
  <th>${title}</th>
  <th>${bundle.responsible}</th>
  <th>${bundle.control_activityImplementationInfo_expectedDuration}</th>
  <th>${bundle.status}</th>
</tr>
</thead>
<tbody>

<#list activities?sort_by("abbreviation_naturalized") as activity>
<tr>
  <td>${activity.abbreviation!}</td>
  <td>${activity.name!}</td>
  <td>${(activity.findFirstLinked("control_responsible").name)!}</td>
  <td>
    ${activity.control_activityImplementationInfo_expectedDuration!}
    ${(bundle[activity.control_activityImplementationInfo_unitOfTime1])!}
  </td>
  <td class="status-column"><span class="checkbox"></span></td>
</tr>
</#list>

</tbody>
</table>
<#else>
<p>${emptyText}</p>
</#if>

</#macro>

<!-- macro for 3.2 and 5.2 -->
<#macro activityDetailsTable activities emptyText>

<#if activities?has_content>

<#list activities?sort_by("abbreviation_naturalized") as activity>

<b>${activity.abbreviation!} ${activity.name!}</b>

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.properties}</th>
  <th>${bundle.explanation}</th>
</tr>
</thead>
<tbody>
<tr>
  <td>${bundle.number}</td>
  <td>${activity.abbreviation!}</td>
</tr>
<tr>
  <td>${bundle.activity}</td>
  <td>${activity.name!}</td>
</tr>
<tr>
  <td>${bundle.description}</td>
  <td>${activity.description!}</td>
</tr>
<tr>
  <td>${bundle.control_activityImplementationInfo_expectedDuration}</td>
  <td>
    ${activity.control_activityImplementationInfo_expectedDuration!}
    ${(bundle[activity.control_activityImplementationInfo_unitOfTime1])!}
  </td>
</tr>
<tr>
  <td>${bundle.control_activityImplementationInfo_expectedOperatingTime}</td>
  <td>
    ${activity.control_activityImplementationInfo_expectedOperatingTime!}
    ${(bundle[activity.control_activityImplementationInfo_unitOfTime2])!}
  </td>
</tr>
<tr>
  <td>${bundle.control_activityImplementationInfo_maximumDuration}</td>
  <td>
    ${activity.control_activityImplementationInfo_maximumDuration!}
    ${(bundle[activity.control_activityImplementationInfo_unitOfTime3])!}
  </td>
</tr>
<tr>
  <td>${bundle.control_document}</td>
  <td>
    <#list activity.getLinks("control_document")![] as docLink>
      ${docLink.target.name!}<br/>
    </#list>
  </td>
</tr>
<tr>
  <td>${bundle.control_performingSystem}</td>
  <td>
    <#list activity.getLinks("control_performingSystem")![] as systemLink>
      ${systemLink.target.abbreviation!} ${systemLink.target.name!}
      ${systemLink.control_performingSystem_description!}<br/>
    </#list>
  </td>
</tr>
<tr>
  <td>${bundle.control_activityImplementationInfo_testSteps}</td>
  <td>${activity.control_activityImplementationInfo_testSteps!}</td>
</tr>
<tr>
  <td>${bundle.control_downstreamActivities}</td>
  <td>
    <#list activity.getLinks("control_downstreamActivities")![] as downstreamLink>
      ${downstreamLink.target.abbreviation!}
      ${downstreamLink.target.name!}<br/>
    </#list>
  </td>
</tr>
<tr>
  <td>${bundle.control_activityImplementationInfo_comment}</td>
  <td>${activity.control_activityImplementationInfo_comment!}</td>
</tr>
</tbody>
</table>

<br/>

</#list>

<#else>
<p>${emptyText}</p>
</#if>

</#macro>

<#macro activityOverview activities emptyText>

<#if activities?has_content >

<table class="table small-table">
<thead>
<tr>
  <th>${bundle.number}</th>
  <th>${bundle.activity}</th>
  <th>${bundle.description}</th>
  <th>${bundle.responsible}</th>
</tr>
</thead>
<tbody>

<#list activities?sort_by("abbreviation_naturalized") as activity>
<tr>
  <td>${activity.abbreviation!}</td>
  <td>${activity.name!}</td>
  <td>
  <#if activity.parts?has_content>
    <#list activity.parts?sort_by("abbreviation_naturalized") as part>
      ${part.abbreviation!} ${part.name!}<br/>
    </#list>
  <#else>
    ${activity.description!}
  </#if>
</td>
<td>
  <#list activity.findLinked("control_responsible")![] as responsible>
    ${responsible.name!}<br/>
  </#list>
</td>
</tr>
</#list>

</tbody>
</table>

<#else>
<p>${emptyText}</p>
</#if>

</#macro>

# 3 ${bundle.resource_restart} {#wiederanlauf}

<#assign restartActivities = [] />

<#list bcSolution.parts![] as measure>
  <#list measure.parts![] as childMeasure>
    <#if (childMeasure.control_emergencyMeasureClassification_phase!) == "control_emergencyMeasureClassification_phase_restart"
      && (childMeasure.control_emergencyMeasureClassification_planType!) == "control_emergencyMeasureClassification_planType_wap"
      && (childMeasure.control_emergencyMeasureClassification_typeOfImplementation!) == "control_emergencyMeasureClassification_typeOfImplementation_operational"
      && (childMeasure.control_emergencyMeasureClassification_category!) == "control_emergencyMeasureClassification_category_activity">
      <#assign restartActivities = restartActivities + [childMeasure] />
    </#if>
  </#list>
</#list>

<#assign testActivities = [] />

<#list bcSolution.parts![] as measure>
  <#if (measure.control_emergencyMeasureClassification_phase!) == "control_emergencyMeasureClassification_phase_emergencyMode"
    && (measure.control_emergencyMeasureClassification_planType!) == "control_emergencyMeasureClassification_planType_wap"
    && (measure.control_emergencyMeasureClassification_typeOfImplementation!) == "control_emergencyMeasureClassification_typeOfImplementation_test">

    <#assign testActivities = testActivities + [measure] />
</#if>

</#list>

## 3.1 ${bundle.restart_schedule} {#ablaufplan-wiederanlauf}

<@activityPlanTable
  restartActivities
  bundle.restart_activity
  bundle.no_restart_activities_linked />

## 3.2 ${bundle.restart_execution} {#durchfuehrung-wiederanlauf}

<@activityDetailsTable
  restartActivities
  bundle.no_restart_execution_activities_defined />

# 3.3 ${bundle.functional_tests_emergency_operation} {#funktionstests-notbetrieb}

${bundle.emergency_operation_test_activities}:

<@activityOverview
  testActivities
  bundle.no_emergency_operation_test_activities_defined />

# 3.4 ${bundle.control_emergencyOperatingLevel_description} {#notbetrieb-einschraenkungen}

${bundle.emergency_operation_description}:

## ${bundle.limitations}

<#if bcSolution.control_emergencyOperatingLevel_description?has_content>
${bcSolution.control_emergencyOperatingLevel_description!}
<#else>
<p>${bundle.no_limitations_defined}</p>
</#if>

## ${bundle.control_emergencyOperatingLevel_additionalMeasure}

<#if bcSolution.control_emergencyOperatingLevel_additionalMeasure?has_content>
${bcSolution.control_emergencyOperatingLevel_additionalMeasure!}
<#else>
<p>${bundle.no_additional_measures_defined}</p>
</#if>

## ${bundle.comment}

<#if bcSolution.control_emergencyOperatingLevel_comment?has_content>
${bcSolution.control_emergencyOperatingLevel_comment!}
<#else>
<p>${bundle.no_comments_defined}</p>
</#if>

# 4 ${bundle.prerequisites_recovery} {#voraussetzungen-wiederherstellung}

## ${bundle.general_prerequisites}

${bcSolution.control_scopeWAP_genRequirementWHP!bundle.no_general_recovery_prerequisites_defined}

## ${bundle.organizational_prerequisites}

### ${bundle.required_documents_approvals}

<#assign whpOrganizationalRequirementDocs = bcSolution.getLinks("control_whpOrganizationalRequirementDoc")![] />

<#if whpOrganizationalRequirementDocs?has_content>
<#list whpOrganizationalRequirementDocs as docLink>
<#assign reqDoc = docLink.target />

<b>${reqDoc.abbreviation!} ${reqDoc.name!}</b><br/>
${reqDoc.document_generalInformation_document!}<br/>
${docLink.control_whpOrganizationalRequirementDoc_desc!}<br/>

</#list>
<#else>
<p>${bundle.no_required_recovery_documents_approvals_linked}</p>
</#if>

### ${bundle.important_contacts}

<@importantContacts "whp" />

## ${bundle.technical_prerequisites}

<@technicalRequirements
  "control_whpTechRequirement"
  "control_whpOrganizationalRequirementDoc_desc"
  "${bundle.no_technical_recovery_prerequisites_defined}" />

# 5 ${bundle.prerequisites_recovery} {#wiederherstellung}

<#assign recoveryActivities = [] />

<#list bcSolution.parts![] as measure>
  <#list measure.parts![] as childMeasure>
    <#if (childMeasure.control_emergencyMeasureClassification_phase!) == "control_emergencyMeasureClassification_phase_recovery"
      && (childMeasure.control_emergencyMeasureClassification_planType!) == "control_emergencyMeasureClassification_planType_wap"
      && (childMeasure.control_emergencyMeasureClassification_typeOfImplementation!) == "control_emergencyMeasureClassification_typeOfImplementation_operational"
      && (childMeasure.control_emergencyMeasureClassification_category!) == "control_emergencyMeasureClassification_category_activity">
      <#assign recoveryActivities = recoveryActivities + [childMeasure] />
    </#if>
  </#list>
</#list>

<#assign returnToNormalActivities = [] />

<#list bcSolution.parts![] as measure>

  <#if (measure.control_emergencyMeasureClassification_phase!) == "control_emergencyMeasureClassification_phase_returnToNormal"
    && (measure.control_emergencyMeasureClassification_planType!) == "control_emergencyMeasureClassification_planType_wap">

    <#assign returnToNormalActivities = returnToNormalActivities + [measure] />
  </#if>

</#list>

## 5.1 ${bundle.recovery_schedule} {#ablaufplan-wiederherstellung}

<@activityPlanTable
  recoveryActivities
  bundle.recovery_activity
  bundle.no_recovery_activities_linked />

## 5.2 ${bundle.recovery_execution} {#durchfuehrung-wiederherstellung}

<@activityDetailsTable
  recoveryActivities
  bundle.no_recovery_execution_activities_defined />

## 5.3 ${bundle.return_to_normal_operation} {#rueckfuehrung-normalbetrieb}

${bundle.return_to_normal_operation_activities}:

<@activityOverview
  returnToNormalActivities
  bundle.no_return_to_normal_operation_activities_defined />

# 6 ${bundle.follow_up_documentation} {#nachbereitung-dokumentation}

<#assign bcSolutionParts = bcSolution.parts![] />
<#assign improvementMeasures = bcSolutionParts?filter(p -> p.hasSubType("CTL_MeasureImprovement")) />

<#if improvementMeasures?has_content>
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.number}</th>
  <th>${bundle.control_actionPlanMeasure}</th>
  <th>${bundle.description}</th>
  <th>${bundle.control_responsibleOffice}</th>
</tr>
</thead>
<tbody>

<#list improvementMeasures as measure>
<tr>
  <td>${measure?counter}</td>
  <td>
    ${measure.abbreviation!}
    ${measure.name!}
  </td>
  <td>
    <#if measure.description?has_content>
      ${measure.description!}
    <#else>
      ${measure.control_correctiveImprovementMeasure_content!}
    </#if>
  </td>
  <td>
    ${(measure.findFirstLinked("control_responsibleOffice").name)!}
  </td>
</tr>
</#list>

</tbody>
</table>
<#else>
<p>${bundle.no_corrective_improvement_measures_linked}</p>
</#if>

# 7 ${bundle.appendix} {#anhang}

# 7.1 ${bundle.reference_documents} {#referenzdokumente}

${bundle.reference_documents_description}:

<#assign referenceDocuments = bcSolution.getLinks("control_bcmDocument")![] />

<#if referenceDocuments?has_content>

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.document}</th>
  <th>${bundle.storage_location_reference}</th>
</tr>
</thead>
<tbody>

<#list referenceDocuments as docLink>
<#assign doc = docLink.target />

<tr>
  <td>
    ${doc.abbreviation!}
    ${doc.name!}
  </td>
  <td>
    ${bundle.link}:
    ${doc.document_generalInformation_document!}<br/>
    ${bundle.document_storageArchiving_location}:
    ${doc.document_storageArchiving_location!}
  </td>
</tr>

</#list>

</tbody>
</table>

<#else>
<p>${bundle.no_reference_documents_linked}</p>
</#if>

</#if>