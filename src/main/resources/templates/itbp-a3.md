<#import "/libs/commons.md" as com>
<#import "/libs/itbp-commons.md" as icom>

<#assign table = com.table
         def = com.def
         multiline = com.multiline
         groupBySubType = com.groupBySubType
         sortModules = icom.sortModules
         title = icom.title />


<style>
<@com.defaultStyles />
h1, h2, h3, h4 {
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


table.used_modules {
  -fs-table-paginate: paginate;
}

table.used_modules th:first-child, table.used_modules td:first-child {
  width: 2cm;
}

table.used_modules th:last-child, table.used_modules td:last-child {
  width: 5cm;
}

.fullwidth {
  width: 100%;
}

.nobreak {
  page-break-inside: avoid;
}
</style>

<#assign scope = informationDomain/>
<#assign domain=domains?filter(it->it.name == 'IT-Grundschutz')?filter(it->scope.domains?keys?seq_contains(it.id))?sort_by("createdAt")?last />
<#assign institutions=scope.scopes?filter(it->it.hasSubType('SCP_Institution')) />

<#assign elementSubTypeGroups = groupBySubType(scope.members, 'process', domain)
+ groupBySubType(scope.members, 'asset', domain) />

<bookmarks>
  <bookmark name="${bundle.toc}" href="#toc"/>
  <bookmark name="${bundle.main_page}" href="#main_page"/>
  <bookmark name="${bundle.used_modules}" href="#used_modules"/>
  <bookmark name="${bundle.unused_modules}" href="#unused_modules"/>
  <bookmark name="${bundle.scope_SCP_InformationDomain_singular}" href="#information_domain"/>
  <#list elementSubTypeGroups as group>
    <bookmark name="${group.subTypePlural}" href="#${group.elementType}_${group.subType}">
    <#list group.elements as element>
      <bookmark name="${title(element)}" href="#${group.elementType}_${group.subType}_${element?counter}"/>
    </#list>
    </bookmark>
  </#list>
</bookmarks>


<div class="footer-left">
  <table>
    <tr>
      <td>${bundle.scope_SCP_InformationDomain_singular}: </td>
      <td>${scope.name}</td>
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
  <@tocitem 1 "used_modules" "2. ${bundle.used_modules}" />
  <@tocitem 1 "unused_modules" "4. ${bundle.unused_modules}" />
  <@tocitem 1 "information_domain" "4. ${bundle.scope_SCP_InformationDomain_singular}" />
  <#list elementSubTypeGroups as group>
      <@tocitem 1 "${group.elementType}_${group.subType}" "${group?counter+4}. ${group.subTypePlural}"/>
      <#list group.elements as element>
          <@tocitem 2 "${group.elementType}_${group.subType}_${element?counter}" "${element?counter}. ${title(element)}"/>
      </#list>
  </#list>
</tbody>
</table>

# ${bundle.main_page} {#main_page}

<div class="main_page">

<#if institutions?has_content>

<#list institutions as institution>
    <@table bundle.scope_SCP_Institution_singular,
    institution,
    ['name',
    'scope_address_address1',
    {'scope_address_postcode, scope_address_city' : 'scope_address_postcode scope_address_city'},
    'scope_contactInformation_phone',
    'scope_contactInformation_email',
    'scope_contactInformation_website'
    ]/>
</#list>
<#else>
${bundle.no_institutions}
</#if>

<@table bundle.scope_SCP_InformationDomain_singular,
scope,
['name',
'description',
'scope_protection_approach',
'status'
],
domain/>

</div>

# ${bundle.used_modules} {#used_modules}
<#assign complianceControlSubType = domain.controlImplementationConfiguration.complianceControlSubType>
<#assign relevantControlImplementations = scope.controlImplementations?filter(it->it.control.hasSubType(complianceControlSubType))>
<#list elementSubTypeGroups as group>
  <#list group.elements as item>
    <#list item.controlImplementations?filter(it->it.control.hasSubType(complianceControlSubType)) as ci>
      <#assign relevantControlImplementations = relevantControlImplementations + [ci]>
    </#list>
  </#list>
</#list>

<#assign usedModules = []>
<#assign usedModulesURIs = []>

<#list relevantControlImplementations as ci>
  <#if !(usedModulesURIs?seq_contains(ci.control._self))>
    <#assign usedModules = usedModules + [ci.control]>
    <#assign usedModulesURIs = usedModulesURIs + [ci.control._self]>
  </#if>
</#list>

<#function sortCIs cis>
  <#assign sortedModules = sortModules(cis?map(it->it.control))>
  <#return sortedModules?map(it->cis?filter(ci->ci.control._self == it._self)?first)>
</#function>

<#assign usedModules = sortModules(usedModules)>

|${bundle.abbreviation}| ${bundle.name}| ${bundle.number_of_occurrences}
|:---|:---|:---|
<#list usedModules as m>
|${m.abbreviation!}|${m.name}|${relevantControlImplementations?filter(it->it.control._self == m._self)?size}|
</#list>
{.table .fullwidth .used_modules}


<#assign allModules = [
  {"abbreviation": "1 ISMS.1", "name": "Sicherheitsmanagement"},
  {"abbreviation": "2 ORP.1", "name": "Organisation"},
  {"abbreviation": "2 ORP.2", "name": "Personal"},
  {"abbreviation": "2 ORP.3", "name": "Sensibilisierung und Schulung zur Informationssicherheit"},
  {"abbreviation": "2 ORP.4", "name": "Identitäts- und Berechtigungsmanagement"},
  {"abbreviation": "2 ORP.5", "name": "Compliance Management (Anforderungsmanagement)"},
  {"abbreviation": "3 CON.1", "name": "Kryptokonzept"},
  {"abbreviation": "3 CON.2", "name": "Datenschutz"},
  {"abbreviation": "3 CON.3", "name": "Datensicherungskonzept"},
  {"abbreviation": "3 CON.6", "name": "Löschen und Vernichten"},
  {"abbreviation": "3 CON.7", "name": "Informationssicherheit auf Auslandsreisen"},
  {"abbreviation": "3 CON.8", "name": "Software-Entwicklung"},
  {"abbreviation": "3 CON.9", "name": "Informationsaustausch"},
  {"abbreviation": "3 CON.10", "name": "Entwicklung von Webanwendungen"},
  {"abbreviation": "3 CON.11.1", "name": "Geheimschutz VS-NUR FÜR DEN DIENSTGEBRAUCH (VS-NfD)"},
  {"abbreviation": "4 OPS.1.1.1", "name": "Allgemeiner IT-Betrieb"},
  {"abbreviation": "4 OPS.1.1.2", "name": "Ordnungsgemäße IT-Administration"},
  {"abbreviation": "4 OPS.1.1.3", "name": "Patch- und Änderungsmanagement"},
  {"abbreviation": "4 OPS.1.1.4", "name": "Schutz vor Schadprogrammen"},
  {"abbreviation": "4 OPS.1.1.5", "name": "Protokollierung"},
  {"abbreviation": "4 OPS.1.1.6", "name": "Software-Tests und -Freigaben"},
  {"abbreviation": "4 OPS.1.1.7", "name": "Systemmanagement"},
  {"abbreviation": "4 OPS.1.2.2", "name": "Archivierung"},
  {"abbreviation": "4 OPS.1.2.4", "name": "Telearbeit"},
  {"abbreviation": "4 OPS.1.2.5", "name": "Fernwartung"},
  {"abbreviation": "4 OPS.1.2.6", "name": "NTP-Zeitsynchronisation"},
  {"abbreviation": "4 OPS.2.2", "name": "Cloud-Nutzung"},
  {"abbreviation": "4 OPS.2.3", "name": "Nutzung von Outsourcing"},
  {"abbreviation": "4 OPS.3.2", "name": "Anbieten von Outsourcing"},
  {"abbreviation": "5 DER.1", "name": "Detektion von sicherheitsrelevanten Ereignissen"},
  {"abbreviation": "5 DER.2.1", "name": "Behandlung von Sicherheitsvorfällen"},
  {"abbreviation": "5 DER.2.2", "name": "Vorsorge für die IT-Forensik"},
  {"abbreviation": "5 DER.2.3", "name": "Bereinigung weitreichender Sicherheitsvorfälle"},
  {"abbreviation": "5 DER.3.1", "name": "Audits und Revisionen"},
  {"abbreviation": "5 DER.3.2", "name": "Revisionen auf Basis des Leitfadens IS-Revision"},
  {"abbreviation": "5 DER.4", "name": "Notfallmanagement"},
  {"abbreviation": "6 APP.1.1", "name": "Office-Produkte"},
  {"abbreviation": "6 APP.1.2", "name": "Webbrowser"},
  {"abbreviation": "6 APP.1.4", "name": "Mobile Anwendungen (Apps)"},
  {"abbreviation": "6 APP.2.1", "name": "Allgemeiner Verzeichnisdienst"},
  {"abbreviation": "6 APP.2.2", "name": "Active Directory Domain Services"},
  {"abbreviation": "6 APP.2.3", "name": "OpenLDAP"},
  {"abbreviation": "6 APP.3.1", "name": "Webanwendungen und Webservices"},
  {"abbreviation": "6 APP.3.2", "name": "Webserver"},
  {"abbreviation": "6 APP.3.3", "name": "Fileserver"},
  {"abbreviation": "6 APP.3.4", "name": "Samba"},
  {"abbreviation": "6 APP.3.6", "name": "DNS-Server"},
  {"abbreviation": "6 APP.4.2", "name": "SAP-ERP-System"},
  {"abbreviation": "6 APP.4.3", "name": "Relationale Datenbanken"},
  {"abbreviation": "6 APP.4.4", "name": "Kubernetes"},
  {"abbreviation": "6 APP.4.6", "name": "SAP ABAP-Programmierung"},
  {"abbreviation": "6 APP.5.2", "name": "Microsoft Exchange und Outlook"},
  {"abbreviation": "6 APP.5.3", "name": "Allgemeiner E-Mail-Client und -Server"},
  {"abbreviation": "6 APP.5.4", "name": "Unified Communications und Collaboration (UCC)"},
  {"abbreviation": "6 APP.6", "name": "Allgemeine Software"},
  {"abbreviation": "6 APP.7", "name": "Entwicklung von Individualsoftware"},
  {"abbreviation": "7 SYS.1.1", "name": "Allgemeiner Server"},
  {"abbreviation": "7 SYS.1.2.2", "name": "Windows Server 2012"},
  {"abbreviation": "7 SYS.1.2.3", "name": "Windows Server"},
  {"abbreviation": "7 SYS.1.3", "name": "Server unter Linux und Unix"},
  {"abbreviation": "7 SYS.1.5", "name": "Virtualisierung"},
  {"abbreviation": "7 SYS.1.6", "name": "Containerisierung"},
  {"abbreviation": "7 SYS.1.7", "name": "IBM Z"},
  {"abbreviation": "7 SYS.1.8", "name": "Speicherlösungen"},
  {"abbreviation": "7 SYS.1.9", "name": "Terminalserver"},
  {"abbreviation": "7 SYS.2.1", "name": "Allgemeiner Client"},
  {"abbreviation": "7 SYS.2.2.3", "name": "Clients unter Windows"},
  {"abbreviation": "7 SYS.2.3", "name": "Clients unter Linux und Unix"},
  {"abbreviation": "7 SYS.2.4", "name": "Clients unter macOS"},
  {"abbreviation": "7 SYS.2.5", "name": "Client-Virtualisierung"},
  {"abbreviation": "7 SYS.2.6", "name": "Virtual Desktop Infrastructure"},
  {"abbreviation": "7 SYS.3.1", "name": "Laptops"},
  {"abbreviation": "7 SYS.3.2.1", "name": "Allgemeine Smartphones und Tablets"},
  {"abbreviation": "7 SYS.3.2.2", "name": "Mobile Device Management (MDM)"},
  {"abbreviation": "7 SYS.3.2.3", "name": "iOS (for Enterprise)"},
  {"abbreviation": "7 SYS.3.2.4", "name": "Android"},
  {"abbreviation": "7 SYS.3.3", "name": "Mobiltelefon"},
  {"abbreviation": "7 SYS.4.1", "name": "Drucker, Kopierer und Multifunktionsgeräte"},
  {"abbreviation": "7 SYS.4.3", "name": "Eingebettete Systeme"},
  {"abbreviation": "7 SYS.4.4", "name": "Allgemeines IoT-Gerät"},
  {"abbreviation": "7 SYS.4.5", "name": "Wechseldatenträger"},
  {"abbreviation": "8 IND.1", "name": "Prozessleit- und Automatisierungstechnik"},
  {"abbreviation": "8 IND.2.1", "name": "Allgemeine ICS-Komponente"},
  {"abbreviation": "8 IND.2.2", "name": "Speicherprogrammierbare Steuerung (SPS)"},
  {"abbreviation": "8 IND.2.3", "name": "Sensoren und Aktoren"},
  {"abbreviation": "8 IND.2.4", "name": "Maschine"},
  {"abbreviation": "8 IND.2.7", "name": "Safety Instrumented Systems"},
  {"abbreviation": "8 IND.3.2", "name": "Fernwartung im industriellen Umfeld"},
  {"abbreviation": "9 NET.1.1", "name": "Netzarchitektur und -design"},
  {"abbreviation": "9 NET.1.2", "name": "Netzmanagement"},
  {"abbreviation": "9 NET.2.1", "name": "WLAN-Betrieb"},
  {"abbreviation": "9 NET.2.2", "name": "WLAN-Nutzung"},
  {"abbreviation": "9 NET.3.1", "name": "Router und Switches"},
  {"abbreviation": "9 NET.3.2", "name": "Firewall"},
  {"abbreviation": "9 NET.3.3", "name": "VPN"},
  {"abbreviation": "9 NET.3.4", "name": "Network Access Control"},
  {"abbreviation": "9 NET.4.1", "name": "TK-Anlagen"},
  {"abbreviation": "9 NET.4.2", "name": "VoIP"},
  {"abbreviation": "9 NET.4.3", "name": "Faxgeräte und Faxserver"},
  {"abbreviation": "10 INF.1", "name": "Allgemeines Gebäude"},
  {"abbreviation": "10 INF.2", "name": "Rechenzentrum sowie Serverraum"},
  {"abbreviation": "10 INF.5", "name": "Raum sowie Schrank für technische Infrastruktur"},
  {"abbreviation": "10 INF.6", "name": "Datenträgerarchiv"},
  {"abbreviation": "10 INF.7", "name": "Büroarbeitsplatz"},
  {"abbreviation": "10 INF.8", "name": "Häuslicher Arbeitsplatz"},
  {"abbreviation": "10 INF.9", "name": "Mobiler Arbeitsplatz"},
  {"abbreviation": "10 INF.10", "name": "Besprechungs-, Veranstaltungs- und Schulungsräume"},
  {"abbreviation": "10 INF.11", "name": "Allgemeines Fahrzeug"},
  {"abbreviation": "10 INF.12", "name": "Verkabelung"},
  {"abbreviation": "10 INF.13", "name": "Technisches Gebäudemanagement"},
  {"abbreviation": "10 INF.14", "name": "Gebäudeautomation"}
] />

<#assign usedModulesAbbrs = usedModules?map(m->m.abbreviation) />
<#assign unusedModules = allModules?filter(m->!usedModulesAbbrs?seq_contains(m.abbreviation)) />

<#if unusedModules?has_content>

# ${bundle.unused_modules} {#unused_modules}

|${bundle.abbreviation}| ${bundle.name}
|:---|:---|:---|
<#list unusedModules as m>
|${m.abbreviation!}|${m.name}
</#list>
{.table .fullwidth .used_modules}

</#if>
<div class="pagebreak"></div>



<#macro moduleview targetObject>

<#assign moduleControlImplementations = sortCIs(targetObject.controlImplementations?filter(it->it.control.hasSubType(complianceControlSubType)))>

<#if moduleControlImplementations?has_content>

## ${bundle.control_CTL_Module_plural}

<#list moduleControlImplementations as moduleControlImplementation>
<div class="nobreak">

### ${title(moduleControlImplementation.control)}

<@def bundle.description moduleControlImplementation.description/>

<@def bundle.control_bpInformation_protectionSequence, (bundle[moduleControlImplementation.control.control_bpInformation_protectionSequence])!/>

<@def bundle.responsible, (moduleControlImplementation.responsible.name)!/>

</div>
</#list>

<#-- 
|${bundle.abbreviation}| ${bundle.name}| ${bundle.responsible} | ${bundle.implementation_status} 
|:---|:---|:---|
<#list moduleControlImplementations as moduleControlImplementation>
|${moduleControlImplementation.control.abbreviation!}|${moduleControlImplementation.control.name}|${(moduleControlImplementation.reponsible.name)!}|${bundle[moduleControlImplementation.implementationStatus]}|
</#list>
{.table .fullwidth .used_modules}
 -->
 
</#if>
</#macro>

# ${title(scope)} {#information_domain}

<@moduleview scope/>

<#list elementSubTypeGroups as group>

# ${group.subTypePlural} {#${group.elementType}_${group.subType}}

<#list group.elements as element>

## ${title(element)} {#${group.elementType}_${group.subType}_${element?counter}}

<@moduleview element/>

</#list>
<div class="pagebreak"></div>
</#list>