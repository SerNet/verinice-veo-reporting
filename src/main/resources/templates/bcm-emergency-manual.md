<#import "/libs/commons.md" as com>

<#assign document = target />
<#assign institution = (document.scopes?filter(s -> s.hasSubType("SCP_Institution"))?first)! />

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

.footer-left {
	padding: 30px 0;
}

@page {
    @bottom-right {
        border-top: 0.1mm solid #929292;
        font-family: Open Sans;
        font-size: 6pt;
        color: #767676;
    
        white-space: pre;

       content:
            '${(bundle.document_DOC_EmergencyManual_singular)?no_esc}, ${((institution.abbreviation)!)?no_esc} ${((institution.name!)!)?no_esc}\A'
            '${(bundle.document_DOC_EmergencyManual_singular)?no_esc} > ${(((bundle[document.document_docManagement_lifecycle]))!)?no_esc},\A'
            '${(bundle.document_docManagement_version)?no_esc}: ${((document.document_docManagement_version)!)?no_esc}\A'
            '${(bundle.valid_from)?no_esc}: ${((document.document_docManagement_dateOfApproval?date.iso)!)?no_esc}\A'
            '${(bundle.page)?no_esc} ' counter(page) ' ${(bundle.of)?no_esc} ' counter(pages);
    }
}
</style>

<div class="footer-left">
  ${bundle.creation_date}: ${.now?date}
</div>

<div class="cover">
  <h1>${bundle.document_DOC_EmergencyManual_singular}</h1>
  <p>powered by verinice</p>
</div>

# ${bundle.main_page}

<div class="main_page">

<#if institution?has_content>
<table class="table fullwidth">
<tbody>
<tr>
  <th colspan="2">${bundle.scope_SCP_Institution_singular}</th>
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
<p>${bundle.no_institution_linked}</p>
</#if>

</div>


# ${bundle.document_properties}

<#if document?has_content>
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.designation}</th>
  <th>${bundle.document_data}</th>
</tr>
</thead>
<tbody>
<tr>
  <td>${bundle.title}:</td>
  <td>${document.abbreviation!} ${document.name}</td>
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
  <td>${bundle.document_docAuthor}:</td>
  <td>${(document.findFirstLinked("document_docAuthor").name)!}</td>
</tr>
<tr>
  <td>${bundle.document_storageArchiving_location}:</td>
  <td>${document.document_storageArchiving_location!}</td>
</tr>
<tr>
  <td>${bundle.created_on}</td>
  <td>${.now?date}</td>
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
<#else>
<p>${bundle.no_document_linked}</p>
</#if>

<div class="pagebreak"></div>

# ${bundle.toc} {#toc}

<#macro tocitem level target text>
  <tr class="level${level}">
    <td>
      <a title="${bundle.jumpto} ${text}" href="#${target}">${text}</a>
    </td>
    <td>
      <span href="#${target}"/>
    </td>
  </tr>
</#macro>

<table class="toc">
<tbody>
  <@tocitem 1 "intro" "1 ${bundle.intro}" />
  <@tocitem 1 "sofort" "2 ${bundle.immediate_measures}" />
  <@tocitem 2 "allgemeinemaßnahmen" "2.1 ${bundle.document_emergencyManual_introductionImmediateMeasure}" />
  <@tocitem 2 "szenario_sofortmassnahmen" "2.2 ${bundle.scenario_specific_immediate_measures}" />
  <@tocitem 1 "alarmierung" "3 ${bundle.document_emergencyManual_alerting}" />
  <@tocitem 2 "detektion-meldung" "3.1 ${bundle.document_emergencyManual_detection}" />
  <@tocitem 2 "alarmierung-bao" "3.2 ${bundle.document_emergencyManual_alertBao}" />
  <@tocitem 2 "stabsraum" "3.3 ${bundle.staff_room}" />
  <@tocitem 1 "stabsarbeit" "4 ${bundle.document_emergencyManual_commandWork}" />
  <@tocitem 1 "geschaeftsf" "5 ${bundle.business_continuity}" />
  <@tocitem 1 "wiederanlauf" "6 ${bundle.restart_and_recovery}" />
  <@tocitem 1 "normalbetrieb" "7 ${bundle.document_emergencyManual_transferNormalOperation}" />
  <@tocitem 2 "massnahmen-ueberfuehrung" "7.1 ${bundle.document_emergencyManual_measureForTransition}" />
  <@tocitem 2 "deeskalation" "7.2 ${bundle.document_emergencyManual_deescalation}" />
  <@tocitem 2 "analyse-bewertung" "7.3 ${bundle.document_emergencyManual_evaluation}" />
  <@tocitem 1 "ueberpruefung" "8 ${bundle.document_emergencyManual_reviewUpdate}" />
  <@tocitem 1 "anhang" "9 ${bundle.appendix}" />
  <@tocitem 2 "geschaeftsordnung-stab" "9.1 ${bundle.document_rulesOfStaff}" />
  <@tocitem 2 "referenzdoc" "9.2 ${bundle.reference_documents}" />
  <@tocitem 2 "kommunikationsmedien" "9.3 ${bundle.document_communicationStaff}" />
  <@tocitem 2 "kontakte" "9.4 ${bundle.relevant_internal_and_external_contacts}" />
  
</tbody>
</table>

# 1 ${bundle.intro} {#intro}

## ${bundle.document_businessContinuityPlan_objective}

${document.document_emergencyManual_objective!}

## ${bundle.document_emergencyManual_scope}

${document.document_emergencyManual_scope!}

## ${bundle.document_emergencyManual_definition}

${document.document_emergencyManual_definition!}

# 2 ${bundle.immediate_measures} {#sofort}

## 2.1 ${bundle.document_emergencyManual_introductionImmediateMeasure} {#allgemeinemaßnahmen}

${document.document_emergencyManual_introductionImmediateMeasure!}

## 2.2 ${bundle.scenario_specific_immediate_measures} {#szenario_sofortmassnahmen}

${bundle.additional_scenario_specific_immediate_measures}

<#assign scenarios = document.findLinked("document_scenario") />

<#if scenarios?has_content>

<#list scenarios?filter(s -> s.hasSubType("SCN_EmergencyScenario")) as scenario>

### ${bundle.immediate_measures_for} ${scenario.abbreviation!""} ${scenario.name!""}

<#assign immediateMeasureComposites = scenario.getLinks("scenario_immediateMeasure")![] />

<#assign immediateMeasures = [] />

<#list immediateMeasureComposites as immediateMeasureLink>
  <#assign immediateMeasureComposite = immediateMeasureLink.target />

  <#list immediateMeasureComposite.parts![] as part>
    <#if part.hasSubType("CTL_ImmediateMeasure")>
      <#assign immediateMeasures = immediateMeasures + [part] />
    </#if>
  </#list>
</#list>

<#assign immediateMeasures = immediateMeasures?sort_by("abbreviation_naturalized") />

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.number}</th>
  <th>${bundle.abbreviation}</th>
  <th>${bundle.activity}</th>
  <th>${bundle.responsible}</th>
  <th>${bundle.completed}</th>
</tr>
</thead>
<tbody>

<#if immediateMeasures?has_content>

<#list immediateMeasures as measure>
<tr>
  <td>${measure?counter}</td>

  <td>${measure.abbreviation!}</td>

  <td>
    ${measure.name!}
    <#if measure.description?has_content>
      <br/>
      ${measure.description!}
    </#if>
  </td>

  <td>
    <#list measure.findLinked("control_responsible")![] as responsible>
      <#if responsible.hasSubType("PER_Person")>
        ${responsible.person_generalInformation_givenName!} ${responsible.person_generalInformation_familyName!}<br/>
      <#else>
        ${responsible.abbreviation!""} ${responsible.name!}<br/>
      </#if>
    </#list>
  </td>
  <td class="status-column">
    <span class="checkbox"></span>
</td>
</tr>
</#list>

<#else>
<tr>
  <td colspan="5">${bundle.no_immediate_measures_available}</td>
</tr>
</#if>

</tbody>
</table>

</#list>

<#else>
<p>${bundle.no_scenarios_linked}</p>
</#if>

<#macro linkedDocumentTable links caption emptyText>
<b>${caption}</b>

<#if links?has_content>

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.abbreviation}, ${bundle.name}</th>
  <th>${bundle.description}</th>
  <th>${bundle.reference}</th>
</tr>
</thead>
<tbody>

<#list links as link>
<tr>
  <td>${link.abbreviation!} ${link.name!}</td>
  <td>${link.description!}</td>
  <td>${link.document_generalInformation_document!}</td>
</tr>
</#list>

</tbody>
</table>

<#else>
<p>${emptyText}</p>
</#if>

</#macro>

# 3 ${bundle.document_emergencyManual_alerting} {#alarmierung}

${document.document_emergencyManual_alerting!}

<@linkedDocumentTable
document.findLinked("document_reportingChannels")
 bundle.reporting_channels_storage_location
 bundle.no_reporting_channels_linked />


## 3.1 ${bundle.document_emergencyManual_detection}  {#detektion-meldung}

${document.document_emergencyManual_detection!}

<@linkedDocumentTable
document.findLinked("document_keyQuestions")
 bundle.guiding_questions_storage_location
 bundle.no_guiding_questions_linked />

## 3.2 ${bundle.document_emergencyManual_alertBao}  {#alarmierung-bao}

${document.document_emergencyManual_alertBao!}

## 3.3  ${bundle.staff_room} {#stabsraum}

<b>${bundle.document_emergencyManual_commandCenter}</b><br/>
${document.document_emergencyManual_commandCenter!}

<b>${bundle.document_emergencyManual_equipment}</b><br/>
${document.document_emergencyManual_equipment!}

# 4 ${bundle.document_emergencyManual_commandWork} {#stabsarbeit}

${document.document_emergencyManual_commandWork!}

<@linkedDocumentTable
  document.findLinked("document_principles")
 bundle.principles_storage_location
 bundle.no_principles_linked />


# 5 ${bundle.business_continuity} {#geschaeftsf}

${document.document_emergencyManual_IntroductionGFP!}

## ${bundle.business_continuity_plans_list}

<#assign gfps = document.findLinked("document_gfp")![] />

<#if gfps?has_content>

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.control_bcSolution_category_gfp}</th>
  <th>${bundle.description}</th>
  <th>${bundle.contact_person}</th>
  <th>${bundle.reference}</th>
</tr>
</thead>
<tbody>

<#list gfps?sort_by("abbreviation_naturalized") as gfp>
<tr>
  <td>${gfp.abbreviation!} ${gfp.name!}</td>

  <td>${gfp.description!}</td>

  <td>
    <#list gfp.findLinked("document_docAuthor")![] as author>
      ${author.abbreviation!} ${author.name!}<br/>
    </#list>
  </td>

  <td>
    ${gfp.document_generalInformation_document!}<br/>

    <#if gfp.document_storageArchiving_location?has_content>
      ${bundle.document_storageArchiving_location}:<br/> 
      ${gfp.document_storageArchiving_location}
    </#if>
  </td>
</tr>
</#list>

</tbody>
</table>

<#else>
<p>${bundle.no_business_continuity_plans_linked}</p>
</#if>


# 6 ${bundle.restart_and_recovery} {#wiederanlauf}

${document.document_emergencyManual_IntroductionWAP!}

<b>${bundle.restart_and_recovery_plans_list}</b>

<#assign resources = document.findLinked("document_wap")![] />

<#assign wapDocuments = [] />

<#list resources as resource>
    <#assign wapDocument = (resource.findFirstLinked("asset_documentWap"))! />

    <#if wapDocument?has_content>
        <#assign wapDocuments = wapDocuments + [wapDocument] />
    </#if>
</#list>

<#if wapDocuments?has_content>

<table class="table fullwidth">
<thead>
<tr>
    <th>${bundle.asset_documentWap}</th>
    <th>${bundle.description}</th>
    <th>${bundle.contact_person}</th>
    <th>${bundle.reference}</th>
</tr>
</thead>
<tbody>

<#list wapDocuments?sort_by("abbreviation_naturalized") as wapDocument>
<tr>
  <td>${wapDocument.abbreviation!} ${wapDocument.name!}</td>
  <td>${wapDocument.description!}</td>
  <td>
    <#list wapDocument.findLinked("document_docAuthor")![] as author>
      ${author.abbreviation!} ${author.name!}<br/>
    </#list>
  </td>
  <td>
    ${wapDocument.document_generalInformation_document!}<br/>
    <#if wapDocument.document_storageArchiving_location?has_content>
      ${bundle.document_storageArchiving_location}:<br/> 
      ${wapDocument.document_storageArchiving_location!}
    </#if>
  </td>
</tr>

</#list>

</tbody>
</table>

<#else>
<p>${bundle.no_restart_and_recovery_plans_linked}</p>
</#if>

# 7 ${bundle.document_emergencyManual_transferNormalOperation} {#normalbetrieb}

${document.document_emergencyManual_transferNormalOperation!}

## 7.1.	${bundle.document_emergencyManual_measureForTransition} {#massnahmen-ueberfuehrung}

${document.document_emergencyManual_measureForTransition!}

## 7.2.	${bundle.document_emergencyManual_deescalation} {#deeskalation}

${document.document_emergencyManual_deescalation!}

## 7.3.	${bundle.document_emergencyManual_evaluation} {#analyse-bewertung}

${document.document_emergencyManual_evaluation!}

# 8 ${bundle.document_emergencyManual_reviewUpdate} {#ueberpruefung}

${document.document_emergencyManual_reviewUpdate!}

# 9 ${bundle.appendix} {#anhang}

## 9.1.	${bundle.document_rulesOfStaff} {#geschaeftsordnung-stab}

<@linkedDocumentTable
  document.findLinked("document_rulesOfStaff")
  bundle.staff_rules_storage_location
  bundle.no_staff_rules_linked />

## 9.2.	${bundle.reference_documents} {#referenzdoc}

<#assign referenceDocuments = document.getLinks("document_doc")![] />

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
  <td>${doc.abbreviation!} ${doc.name!}</td>
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

## 9.3.	${bundle.document_communicationStaff} {#kommunikationsmedien} 

<@linkedDocumentTable
  document.findLinked("document_communicationStaff")
  bundle.communication_media_storage_location
  bundle.no_communication_media_linked />

## 9.4	${bundle.relevant_internal_and_external_contacts} {#kontakte}

<b>${bundle.relevant_internal_contacts}</b>
<#assign internalContacts = document.findLinked("document_internalContact") />

<#if internalContacts?has_content>

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.document_role}</th>
  <th>${bundle.person_generalInformation_givenName}, ${bundle.person_generalInformation_familyName}</th>
  <th>${bundle.contact}</th>
</tr>
</thead>
<tbody>

<#list internalContacts as internalContact>
<tr>
  <td>${internalContact.abbreviation!} ${internalContact.name!}<br/> 
      ${internalContact.description!}</td>
  <td>
    ${bundle.person_generalInformation_givenName} 
    ${internalContact.person_generalInformation_givenName!}<br/> 
    ${bundle.person_generalInformation_familyName} 
    ${internalContact.person_generalInformation_familyName!}
  </td>
  <td>
    ${bundle.person_contactInformation_office} 
    ${internalContact.person_contactInformation_office!}<br/> 
    ${bundle.person_contactInformation_mobile} 
    ${internalContact.person_contactInformation_mobile!}<br/> 
    ${bundle.person_contactInformation_email} 
    ${internalContact.person_contactInformation_email!}
  </td>
</tr>
</#list>

</tbody>
</table>
<#else>
<p>${bundle.no_relevant_internal_contacts}</p>

</#if>

<b>${bundle.relevant_external_contacts}</b>

<#assign externalContacts = document.findLinked("document_externalContact") />

<#if externalContacts?has_content>

<#list externalContacts as provider>

<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.document_role}</th>
  <th>${bundle.contact}</th>
</tr>
</thead>
<tbody>

<tr>
  <td>
    ${provider.abbreviation!} ${provider.name!}<br/>
    ${provider.description!}
  </td>
  <td>
    ${bundle.scope_contactInformation_phone}: ${provider.scope_contactInformation_phone!}<br/>
    ${bundle.scope_contactInformation_email}: ${provider.scope_contactInformation_email!}
  </td>
</tr>

<#assign linkedPersons = (provider.members![])?filter(m -> m.hasSubType("PER_Person")) />

<#if linkedPersons?has_content>
<tr>
  <td colspan="4">
<table class="table fullwidth">
<thead>
<tr>
  <th>${bundle.contact_person}</th>
  <th>${bundle.person_contactInformation_office}</th>
  <th>${bundle.person_contactInformation_email}</th>
</tr>
</thead>
<tbody>
<#list linkedPersons as person>
<tr>
  <td>
    ${person.name!}<br/>
    ${bundle.person_generalInformation_givenName}: ${person.person_generalInformation_givenName!}<br/>
    ${bundle.person_generalInformation_familyName}: ${person.person_generalInformation_familyName!}
  </td>

  <td>
    <#if person.person_contactInformation_office?has_content || person.person_contactInformation_mobile?has_content>
      ${bundle.person_contactInformation_office}: ${person.person_contactInformation_office!}<br/>
      ${bundle.person_contactInformation_mobile}: ${person.person_contactInformation_mobile!}
    <#else>
      <p>${bundle.no_contact_information_available}</p>
    </#if>
  </td>

  <td>
    <#if person.person_contactInformation_email?has_content>
      ${person.person_contactInformation_email!}
    <#else>
      <p>${bundle.no_email_address_available}</p>
    </#if>
  </td>
</tr>
</#list>
</tbody>
</table>
  </td>
</tr>
</#if>

</tbody>
</table>

</#list>

<#else>
<p>${bundle.no_relevant_external_contacts}</p>
</#if>