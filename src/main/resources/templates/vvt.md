<#-- Print all booleans as true/false. This should be removed once we have proper custom aspect handling. -->
<#setting boolean_format="c">

<#function to_user_presentable val = bundle.unknown>
  <#if val?is_boolean>
    <#return val?then(bundle.yes, bundle.no)>
  </#if>
  <#if val?is_sequence>
    <#return val?map(v -> to_user_presentable(v))?join(", ")>
  </#if>
  <#return val>
</#function>

<style>
<#include "styles/default.css">
</style>

<bookmarks>
  <bookmark name="Hauptplatt" href="#hauptblatt"> </bookmark>
  <bookmark name="Übersicht der Verarbeitungstätigkeiten" href="#übersicht-der-verarbeitungstätigkeiten">
<#list processes as process>
    <bookmark name="${process.name}" href="#process_${process?counter}" />
</#list>
  </bookmark>
</bookmarks>


<div class="cover">
<h1>Verzeichnis der Verarbeitungstätigkeiten</h1>
<p>powered by verinice</p>

</div>

# Hauptblatt

| Angaben zum Verantwortlichen  ||
|:---|:---|
| Name/Bezeichnung des Verantwortlichen | ? |
| Straße, PLZ/Ort | ? |
| Telefon/Telefax | ? |
| E-Mail-Adresse | ? |
| Internet-Adresse/URL | ? |


| Vertretung  ||
|:---|:---|
| Leitung der verantwortlichen Stelle<br/>(einschließlich Vertreter) | ? |
| Leitung der Datenverarbeitung | ? |


| Datenschutzbeauftragte  ||
|:---|:---|
| Name | ? |
| Straße<br/>PLZ/Ort | ? |
| Telefon/Telefax | ? |
| E-Mail-Adresse | ? |


| Angaben zur Vertretung innerhalb der EU  ||
|:------------|:-----|
| Sitz des Verantwortlichen<br/>außerhalb der EU | ? |


| Zuständige Aufsichtbehörde  ||
|:---|:---|
| Zuständige Behörde | ? |
| Straße<br/>PLZ/Ort | ? |
| Telefon/Telefax | ? |
| E-Mail-Adresse | ? |


<div class="pagebreak"></div>

# Übersicht der Verarbeitungstätigkeiten

<#list processes as process>
<span style="display:inline-block; width: 4cm;">Anlage Nr ${process?counter}:</span> ${process.name}  
</#list>

<div class="pagebreak"></div>

<#list processes as process>
## <center>Anlage No. ${process?counter}</center>
## <a id="process_${process?counter}"/> <span style="display:inline-block; width: 5cm;">Verarbeitung: </span>${process.name}
## <center><ins>Prüfergebnis zur materiellen Rechtmäßigkeit</ins></center>

### <ins>I. Rechtmäßigkeit der automatisierten Verarbeitung</ins>
?
#### <ins>1. Risiken und Feststellungen</ins>
? 
#### <ins>2. Votum</ins>
?
<div class="pagebreak"></div>

### <ins>II. Rechtmäßigkeit der technischen und organisatorischen Maßnahmen</ins>
#### Zertifizierung nach anerkannten Standard

#### IT-Sicherheitskonzept

#### Gesamtbeurteilung der Maßnahmen

<div class="pagebreak"></div>

## <center><ins>Detailergebnisse</ins></center>

|:---|:---|:---|
| <ins>**Name des Unternehmens**</ins><br/>? |||
| Abteilung/Fachbereich<br/>? | Leiter Fachabteilung<br/>? | Mitarbeiterzahl<br/>? |
| Datum der Befragung<br/> ? |||

| 1. Angaben zur Verarbeitungstätigkeit ||
|:---|:---|
| Übergeordneter Geschäftsprozess / Verfahren<br/>? | Bezeichnung der Verarbeitung / Verfahrensbeschreibung<br/>? |
| Art der Verarbeitung<br/> ? ||
| Auftragsverarbeitung i.S.d. Art. 30 II DS-GVO | ? |

| 2. Angaben zum gemeinsam Verantwortlichen |
|:---|
| **Gemeinsam für die Verarbeitung Verantwortliche Art. 26 DS-GVO**<br/>? |

| 3. Zweckbestimmung der Datenverarbeitung |
|:---|
| ? |

| 4. Rechtsgrundlage für die Datenverarbeitung |
|:---|
| ? |
| <ins>**Vorrangige Rechtsvorschriften:**</ins> |
| ? |
| <ins>**Vorrangige Rechtsvorschriften:**</ins> |
| ? |
| **Erläuterungen:**<br/> ? |

| 5. Beschreibung der betroffenen Personengruppen und Daten oder Datenkategorien |||
|:---|
| **Kreis der betroffenen Personengruppen** | **Art der verarbeiteten Daten / Datenkategorien** | **Herkunft der Daten** |
| ? | ? | ? |
| <ins>**Bemerkungen:**</ins><br/> ? |||

| 6. Benachrichtigung Betroffener |
|:---|
| ? |
| <ins>**Grund für Nichtbenachrichtigung:**</ins><br/> |
| ? |

| 7. Datenverarbeitung besonders sensitiver Daten? |
|:---|
| ? |
| **Art besonders sensitiver Daten** |
| ? |
| **Rechtsgrundlage für die Datenverarbeitung sensitiver Daten** |
| ? |
| <ins>**Bemerkungen**</ins><br/> |
| ? |
| **Meldeverfahren nach Art. 33, 34 DS-GVO** |
| ? |
| **Schweigepflichtiger Personenkreis nach § 203 StGB?** |
| ? |

| 8. Art übermittelter Daten und deren Empfänger |||
|:---|
| **Interne Empfänger** |||
| **Interne Stelle (Org.-Einheit)** | **Art der Daten** | **Zweck des Datentransfers** |
| ? | ? | ? |
| <ins>**Erläuterungen:**</ins><br/> ? |||

|:---|
| **Externe Empfänger** |||
| **Externe Stelle** | **Art der Daten** | **Zweck des Datentransfers** |
| ? | ? | ? |
| <ins>**Erläuterungen:**</ins><br/> ? |||

|:---|
| **Auftragnehmer / Dienstleister** |||
| **Auftragnehmer** | **Art der Daten** | **Zweck des Datentransfers** |
| ? | ? | ? |
| <ins>**Erläuterungen:**</ins><br/> ? |||

|:---|
| **Datenübermittlung in Drittland** |||||
| **Name des Staates** | **Art der Daten** | **Zweck der Übermittlung** | **Emfängerkategorie** | **Rechtsgrundlage** |
| ? | ? | ? |? |? |
| **Angabe geeigneter Garantien**|||||
| <ins>**Erläuterungen:**</ins><br/> ? |||||

| 9. Löschfristen |
|:---|
| **Fristabhängige Löschung**<br/>?<br/>**Löschverfahren**<br>?<br/><ins>**Erläuterung:**</ins><br>? |

| 10. Zugriffsberechtigte Personengruppen (Berechtigungsgruppen) |
|:---|
| **Ein Berechtigungskonzept ist vorhanden**<br/>? |
| **Beschreibung des Berechtigungsverfahrens:**<br/>? |

| 11. Systeminformationen über Hard- und Software |||
|:---|
| <center><ins>**Name</center>**</center></ins> | <center><ins>**Typ**</center></ins> | <center><ins>**Beschreibungs**</center></ins> |

| 12. Ort der Datenverarbeitung (intern, extern ) |
|:---|
| ? |

| 13. Status des Verfahrens |
|:---|
| ? |

| 14. Datenschutz-Folgenabschätzung erforderlich? |
|:---|
| ? |

<table>
  <thead>
    <tr>
      <th colspan="3">15. Technische und organisatorische Maßnahmen</th>
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


<#if process?has_next>

<div class="pagebreak"></div>

</#if>
</#list>

</body>
</html>
