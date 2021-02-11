<style>
  table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 3mm;
  }
  table, th, td {
    border: 1pt solid black;
  }
  th, td {
    padding: 1mm;
  }
  th {
    background-color: #eee;
  }
  
  div.pagebreak {
    page-break-after: always;
  }
</style>

# Verzeichnis von Verarbeitungstätigkeiten

TODO:

 * page numbers
 * PDF navigation (outline) 

## Hauptblatt

<table>
  <thead>
    <tr>
      <th colspan="2">Angaben zum Verantwortlichen</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Name/Bezeichnung des Verantwortlichen</td>
      <td>?</td>
    </tr>
    <tr>
      <td>Straße, PLZ/Ort</td>
      <td>?</td>
    </tr>
    <tr>
      <td>Telefon/Telefax</td>
      <td>?</td>
    </tr>
    <tr>
      <td>E-Mail-Adresse</td>
      <td>?</td>
    </tr>
    <tr>
      <td>Internet-Adresse/URL</td>
      <td>?</td>
    </tr>
  </tbody>
</table>

<table>
  <thead>
    <tr>
      <th colspan="2">Vertretung</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Leitung der verantwortlichen Stelle<br/>(einschließlich Vertreter)</td>
      <td>?</td>
    </tr>
    <tr>
      <td>Leitung der Datenverarbeitung</td>
      <td>?</td>
    </tr>
  </tbody>
</table>

<table>
  <thead>
    <tr>
      <th colspan="2">Datenschutzbeauftragte</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Name</td>
      <td>?</td>
    </tr>
    <tr>
      <td>Straße<br/>PLZ/Ort</td>
      <td>?</td>
    </tr>
    <tr>
      <td>Telefon/Telefax</td>
      <td>?</td>
    </tr>
    <tr>
      <td>E-Mail-Adresse</td>
      <td>?</td>
    </tr>
  </tbody>
</table>

<table>
  <thead>
    <tr>
      <th colspan="2">Angaben zur Vertretung innerhalb der EU</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Sitz des Verantwortlichen<br/>außerhalb der EU</td>
      <td>?</td>
    </tr>
  </tbody>
</table>

<table>
  <thead>
    <tr>
      <th colspan="2">Zuständige Aufsichtbehörde</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Zuständige Behörde</td>
      <td>?</td>
    </tr>
    <tr>
      <td>Straße<br/>PLZ/Ort</td>
      <td>?</td>
    </tr>
    <tr>
      <td>Telefon/Telefax</td>
      <td>?</td>
    </tr>
    <tr>
      <td>E-Mail-Adresse</td>
      <td>?</td>
    </tr>
  </tbody>
</table>

<div class="pagebreak"></div>

## Übersicht der Verarbeitungstätigkeiten

<#list data as process>
<span style="display:inline-block; width: 4cm;">Anlage Nr ${process?counter}:</span> ${process.name}  
</#list>

<div class="pagebreak"></div>

<#list data as process>
### ${process.name}

Abkürzung
: ${process.abbreviation!"&nbsp;"}

Beschreibung
: ${process.description!"&nbsp;"}


<#if process?has_next>

<div class="pagebreak"></div>

</#if>
</#list>


