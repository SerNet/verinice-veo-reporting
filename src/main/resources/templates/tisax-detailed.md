<#import "/libs/commons.md" as com>

<#assign table = com.table
         def = com.def
         multiline = com.multiline
         heading = com.heading />

<style>
<#include "styles/default.css">
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
  padding-left:0;
}

.fullwidth {
  width: 100%;
}

.nobreak {
  page-break-inside: avoid;
}

.maturityred {
  color: #f00;
}

.maturityorange {
  color: #f90;
}

.maturitygreen {
  color: #396;
}

table.maturitytable > tbody > tr > td:nth-child(3),
table.maturitytable > tbody > tr > td:nth-child(4) {
  text-align: center;
}

.paginated {
  -fs-table-paginate: paginate;
}

.maturitytable td p {
  margin-top: 0.8em;
  margin-bottom: 0.5em;
}

.maturitytable td p b {
  font-weight: 600;
}

a[href] {
  color: #767676;
}
</style>

<#assign domain=domains?filter(it->it.name == 'TISAX')?filter(it->isa.domains?keys?seq_contains(it.id))?sort_by("createdAt")?last />

<#assign scope = scopes?filter(it->it.domains[domain.id].subType == 'SCP_Organization')?filter(it->it.members?map(it->it._self)?seq_contains(isa._self))?first! />

<#assign usedControls = isa.getMembersWithType('control')>


<div class="footer-left">
  <table>
    <tr>
      <td>Scope: </td>
      <td>${scope.name!}</td>
    </tr>
    <tr>
      <td>${bundle.creation_date}: </td>
      <td>${.now?date}</td>
    </tr>
  </table>
</div>

<div class="cover">
<h1><@multiline bundle.title/></h1>
<p>powered by verinice</p>
</div>


# ${bundle.main_page} {#main_page}

<h2>Information Security Assessment<br/>${isa.name}<br/>detailliert</h2>
<div class="main_page">

<#if scope?has_content>

<@table bundle.scope_SCP_Organization_singular,
scope,
[{'name' : 'abbreviation name'},
'scope_address_address1',
'scope_address_address2',
{'scope_address_postcode, scope_address_city' : 'scope_address_postcode scope_address_city'},
{'scope_address_country, scope_address_state' : 'scope_address_country scope_address_state'},
'scope_identification_duns'
],
domain/>

</#if>


<@table bundle.scope_SCP_InformationSecurityAssessment_singular,
isa,
['scope_tisax_scopeID',
{'isa_scope' : 'description'},
'scope_tisax_assessmentDate'
],
domain/>

<#assign tisaxContact = isa.findFirstLinked('scope_tisaxContact') !>

<#if tisaxContact?has_content>

<@table 'Ansprechpartner',
  tisaxContact,
  [
   {'name' : 'person_generalInformation_givenName person_generalInformation_familyName'},
   'person_contactInformation_office / person_contactInformation_mobile',
   'person_contactInformation_email'
  ],
domain/>

</#if>

<#assign tisaxCreator = isa.findFirstLinked('scope_tisaxCreator') !>

<#if tisaxCreator?has_content>

<@table 'Ersteller',
  tisaxCreator,
  [
   {'name' : 'person_generalInformation_givenName person_generalInformation_familyName'}
  ],
domain/>

</#if>


<div style="margin-top:1cm; margin-bottom:2cm">
Unterschrift:
</div>

Version 6.0.2

</div>

<#function averageMaturity controls cutback=-1>
<#local maturities = controls?map(it->it.control_isaMaturity_assessment!)?filter(it->it?has_content)?map(it->it?keep_after_last('_')?number) />
<#if (maturities?size == 0)>
<#return 0>
</#if>
<#local sum = 0>
<#list maturities as it>
<#if (cutback>0)>
<#local sum = sum + [it, cutback]?min>
<#else>
<#local sum = sum + it>
</#if>>
</#list>
<#return sum / maturities?size>
</#function>


| ISA Ergebnisse    | | | |
|:---|:---|:---|:---|
| Ergebnis mit Kürzung auf Zielreifegrad  | ${averageMaturity(usedControls, 3)?string["0.00"]}  | Höchstes erreichbares Ergebnis:| ${3.00?string["0.00"]} |
{.table .fullwidth .paginated}

<#function unique_items seq>
  <#local result = []>
  <#list seq as item>
    <#if ! result?seq_contains(item)>
      <#local result = result + [item] />
    </#if>
  </#list>
  <#return result>
</#function>


<#assign chapters=unique_items(usedControls?filter(it->it.domains[domain.id].subType != 'CTL_ISAControlDataProtection')?map(c->c.abbreviation?keep_before(".")))?sort>

<object type="jfreechart/veo-spiderweb" style="margin-bottom: 2cm;width:17cm;height:15cm;margin:auto;" title="Ergebnis je Kapitel (ohne Kürzung)" alt="Diagramm: Ergebnis je Kapitel (ohne Kürzung)" interiorGap="0.4">
<#list chapters as chapter>
  <data row="Zielreifegrad" column="${chapter} ${bundle["chapter_label_"+chapter]}" value="3"/>
<#assign controls_chapter = usedControls?filter(c->c.abbreviation?starts_with(chapter))>
  <data row="Ergebnis" column="${chapter} ${bundle["chapter_label_"+chapter]}" value="${averageMaturity(controls_chapter)?c}"/>
</#list>
</object>

<div class="pagebreak" />

<#assign subchapters=unique_items(usedControls?filter(it->it.domains[domain.id].subType != 'CTL_ISAControlDataProtection')?map(c->c.abbreviation?keep_before_last(".")))?sort>

<object type="jfreechart/veo-spiderweb" style="margin-bottom: 2cm;width:17cm;height:15cm;margin:auto;" title="Ergebnis je Unterkapitel (ohne Kürzung)" alt="Diagramm: Ergebnis je Unterkapitel (ohne Kürzung)" interiorGap="0.6">
<#list subchapters as subchapter>
  <data row="Zielreifegrad" column="${subchapter} ${bundle["chapter_label_"+subchapter]}" value="3"/>
<#assign controls_subchapters = usedControls?filter(c->c.abbreviation?starts_with(subchapter))>
  <data row="Ergebnis" column="${subchapter} ${bundle["chapter_label_"+subchapter]}" value="${averageMaturity(controls_subchapters)?c}"/>
</#list>
</object>

<div class="pagebreak" />

<#macro maturitydisplay control>
<#local targetLevel = 3>
<#local val = control.control_isaMaturity_assessment!>
<#if val?has_content>
<#local level=val?keep_after_last('_') />
<#local diff = targetLevel - level?number>
<#local class="maturitygreen"/>
<#if (diff >= 2)>
<#local class="maturityred"/>
<#elseif (diff >= 1) >
<#local class="maturityorange"/>
</#if>
<span class="${class}">${level}</span>
</#if>
</#macro>

<#macro conditional control propertyName showAlways=false>
<#local val = control[propertyName]!>
<#if showAlways || val?has_content>
<p>
<b>${bundle[propertyName]}</b>
<br/>
<#if val?has_content><@multiline val/>
<#elseif showAlways>
</#if>
</p>
</#if>
</#macro>

<#macro conditionaldate control propertyName showAlways=false>
<#local val = control[propertyName]!>
<#if showAlways || val?has_content>
<p>
<b>${bundle[propertyName]}</b>
<br/>
${val?date.iso}
</p>
</#if>
</#macro>

<#macro link document>
<#local href = document.document_generalInformation_document! />
<#if href?has_content>
<a title=${document.name} href=${href}>${document.name}</a>
<#else>
${document.name}
</#if>
</#macro>


<#macro controltable controls>
<table class="table fullwidth maturitytable paginated">
<thead>
<tr>
<th>Nr.</th>
<th>Thema</th>
<th>Ziel-Reifegrad</th>
<th>Ergebnis</th>
</tr>
</thead>
<tbody>
<#list controls as control>
<#assign responsible = control.findFirstLinked('control_isaPersonResponsible')! />
<#assign referenceDocuments = control.findLinked('control_referenceDocument')! />
<tr class="break">
<td><b>${control.abbreviation}</b></td>
<td>
<b>${control.name}</b>
<#if responsible?has_content>
<p>
<b>${bundle.control_isaPersonResponsible}</b><br/>${responsible.name}
</p>
</#if>
<@conditional control 'control_isaMaturity_commentImplementation' true/>
<p>
<b>Referenzdokumentation</b><br/>
<#if referenceDocuments?has_content>
<#list referenceDocuments as referenceDocument>
<@link referenceDocument/><br>
</#list>
</#if>
</p>
<@conditional control 'control_isaFinding_comment'/>
<@conditionaldate control 'control_isaFinding_date'/>
<@conditional control 'control_isaFinding_maturity'/>
<@conditionaldate control 'control_isaFinding_solutionDate'/>
</td>
<td>3</td>
<td><@maturitydisplay control /></td>
</tr>
</#list>
</tbody>
</table>
</#macro>


<#assign isacontrols = usedControls?filter(it->it.domains[domain.id].subType == 'CTL_ISAControlInformationSecurity')?sort_by('abbreviation')>
<#assign prototypecontrols = usedControls?filter(it->it.domains[domain.id].subType == 'CTL_ISAControlPrototypeProtection')?sort_by('abbreviation')>
<#assign dataprotectioncontrols = usedControls?filter(it->it.domains[domain.id].subType == 'CTL_ISAControlDataProtection')?sort_by('abbreviation')>

# Information Security Assessment <br/> Ergebnisse - Informationssicherheit

|:---|:---|:---|:---|
| Ergebnis mit Kürzung auf Zielreifegrad  | ${averageMaturity(isacontrols, 3)?string["0.00"]}   | Höchstes erreichbares Ergebnis:| ${3.00?string["0.00"]}|
{.table .fullwidth}

## Detaillierter Report

<@controltable isacontrols />



<#if prototypecontrols?has_content>

<div class="pagebreak" />


# Information Security Assessment <br/> Ergebnisse - Prototypenschutz

|:---|:---|:---|:---|
| Ergebnis mit Kürzung auf Zielreifegrad  | ${averageMaturity(prototypecontrols, 3)?string["0.00"]}   | Höchstes erreichbares Ergebnis:| ${3.00?string["0.00"]} |
{.table .fullwidth}


## Details

<@controltable prototypecontrols />

</#if>

<#if dataprotectioncontrols?has_content>

<div class="pagebreak" />


# Information Security Assessment <br/> Ergebnisse - Zusätzliche Anforderungen an den Datenschutz

## Details

<table class="table fullwidth maturitytable paginated">
<thead>
<tr>
<th>Nr.</th>
<th>Thema</th>
<th>Bewertung</th>
</tr>
</thead>
<tbody>
<#list dataprotectioncontrols as control>
<#assign responsible = control.findFirstLinked('control_isaPersonResponsible')! />
<#assign referenceDocuments = control.findLinked('control_referenceDocument')! />
<tr class="break">
<td><b>${control.abbreviation}</b></td>
<td>
<b>${control.name}</b>
<#if responsible?has_content>
<p>
<b>${bundle.control_isaPersonResponsible}</b><br/>${responsible.name}
</p>
</#if>
<@conditional control 'control_isaDataProtection_commentImplementation' true/>
<@conditional control 'control_isaFinding_comment'/>
<@conditionaldate control 'control_isaFinding_date'/>
<p>
<b>Referenzdokumentation</b><br/>
<#if referenceDocuments?has_content>
<#list referenceDocuments as referenceDocument>
<@link referenceDocument/><br/>
</#list>
</#if>
</p>
<@conditional control 'control_isaFinding_maturity'/>
<@conditionaldate control 'control_isaFinding_solutionDate'/>
</td>
<td>${(bundle[control.control_isaDataProtection_assessment])!} </td>
</tr>
</#list>
</tbody>
</table>

</#if>