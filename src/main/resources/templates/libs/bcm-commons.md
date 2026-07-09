<#function formatDuration value>
  <#if !value?has_content>
    <#return "" />
  </#if>

  <#assign duration = value?string />

  <#assign weeks = 0 />
  <#assign days = 0 />
  <#assign hours = 0 />
  <#assign minutes = 0 />
  <#assign seconds = 0 />

  <#if duration?matches(".*(\\d+)W.*")>
    <#assign weeks = duration?replace(".*?(\\d+)W.*", "$1", "r")?number />
  </#if>

  <#if duration?matches(".*(\\d+)D.*")>
    <#assign days = duration?replace(".*?(\\d+)D.*", "$1", "r")?number />
  </#if>

  <#if duration?matches(".*T.*(\\d+)H.*")>
    <#assign hours = duration?replace(".*?T.*?(\\d+)H.*", "$1", "r")?number />
  </#if>

  <#if duration?matches(".*T.*(\\d+)M.*")>
    <#assign minutes = duration?replace(".*?T.*?(\\d+)M.*", "$1", "r")?number />
  </#if>

  <#if duration?matches(".*T.*(\\d+)S.*")>
    <#assign seconds = duration?replace(".*?T.*?(\\d+)S.*", "$1", "r")?number />
  </#if>

  <#assign result = "" />

  <#if weeks gt 0>
    <#assign result += weeks + " " + bundle.duration_weeks + " " />
  </#if>
  <#if days gt 0>
    <#assign result += days + " " + bundle.duration_days + " " />
  </#if>
  <#if hours gt 0>
    <#assign result += hours + " " + bundle.duration_hours + " " />
  </#if>
  <#if minutes gt 0>
    <#assign result += minutes + " " + bundle.duration_minutes + " " />
  </#if>
  <#if seconds gt 0>
    <#assign result += seconds + " " + bundle.duration_seconds />
  </#if>

  <#return result?trim />
</#function>