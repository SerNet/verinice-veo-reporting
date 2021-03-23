<#function to_user_presentable val = bundle.unknown>
  <#if val?is_boolean>
    <#return val?then(bundle.yes, bundle.no)>
  </#if>
  <#if val?is_sequence>
    <#return val?map(v -> to_user_presentable(v))?join(", ")>
  </#if>
  <#return val>
</#function>

<#function row object aspectDef>
  <#local labelDef = aspectDef?is_string?then(aspectDef, aspectDef?keys?first) />
  <#local valueDef = aspectDef?is_string?then(aspectDef, aspectDef?values?first) />
  <#local label =  labelDef?split('\\b', 'r')?map(v-> v?matches("[\\w]+")?then(bundle[v], v))?join('') />
  <#local val = valueDef?switch('name', object.name, valueDef?split('\\b', 'r')?map(v-> v?matches("[\\w]+")?then(to_user_presentable(object[v]), v))?join(''))/>
  <#return "| ${label} | ${val} |">
</#function>

<#macro table header object aspectDefs>
| ${header}  ||
|:------------|:-----|
<#list aspectDefs as aspectDef>
${row(object,aspectDef)}
</#list>
</#macro>
