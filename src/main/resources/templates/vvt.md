<style>
  table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 3mm;
  }
  table th:first-child, table td:first-child {
    width: 7cm;
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
  @page {
	@top-center {
	  content: 'Verzeichnis von Verarbeitungstätigkeiten';	
	}
	@bottom-center {
	  content: 'Seite ' counter(page) ' von ' counter(pages);;	
	}
  }
</style>

<bookmarks>
  <bookmark name="Hauptplatt" href="#hauptblatt"> </bookmark>
  <bookmark name="Übersicht der Verarbeitungstätigkeiten" href="#übersicht-der-verarbeitungstätigkeiten">
<#list data as process>
    <bookmark name="${process.name}" href="#process_${process?counter}" />
</#list>
  </bookmark>
</bookmarks>

# Verzeichnis von Verarbeitungstätigkeiten


## Hauptblatt

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

## Übersicht der Verarbeitungstätigkeiten

<#list data as process>
<span style="display:inline-block; width: 4cm;">Anlage Nr ${process?counter}:</span> ${process.name}  
</#list>

<div class="pagebreak"></div>

<#list data as process>

### <a id="process_${process?counter}"/> ${process.name}

Abkürzung
: ${process.abbreviation!"&nbsp;"}

Beschreibung
: ${process.description!"&nbsp;"}

<#if (process.customAspects!?size > 0)>

#### Custom aspects

<ul>

<#list process.customAspects as id, customAspect>

<li>${id}

<#list customAspect.attributes as k, v>

${k}
: ${v!"&nbsp;"}

</#list>


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
