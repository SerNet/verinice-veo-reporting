<#function to_user_presentable val = "">
  <#if val?is_boolean>
    <#return val?then(bundle.yes, bundle.no)>
  </#if>
  <#if val?is_sequence>
    <#return val?map(v -> to_user_presentable(v))?join(", ")>
  </#if>
  <#return val>
</#function>

<#macro row object aspectDef>
  <#local labelDef = aspectDef?is_string?then(aspectDef, aspectDef?keys?first) />
  <#local valueDef = aspectDef?is_string?then(aspectDef, aspectDef?values?first) />
  <#local label =  labelDef?split('\\b', 'r')?map(v-> v?matches("[\\w]+")?then(bundle[v], v))?join('') />
  <#local val = valueDef?switch('name', object.name, valueDef?split('\\b', 'r')?map(v-> v?matches("[\\w]+")?then(to_user_presentable(object[v]), v))?join(''))/>
  | ${label} | ${val} |
</#macro>

<#macro table header object aspectDefs>
| ${header}  ||
|:------------|:-----|
<#list aspectDefs as aspectDef>
<@row object,aspectDef />
</#list>
</#macro>

<#macro def term definition="" alwaysShow=false>
<#if definition?has_content || alwaysShow>
${term}
<#if definition?has_content>
: ${to_user_presentable(definition)?replace("\n", "\n ")}
<#else>
: &nbsp;
</#if>
</#if>
</#macro>

<#function groupBySubType elements subTypes>
<#assign elementsBySubType = {}/>
<#list subTypes as subType>
<#assign elementsWithSubType = elements?filter(it->it.hasSubType(subType))?sort_by('name_naturalized')>
<#if elementsWithSubType?has_content>
<#assign elementsBySubType = elementsBySubType + {subType: elementsWithSubType} />
</#if>
</#list>
<#return elementsBySubType>
</#function>