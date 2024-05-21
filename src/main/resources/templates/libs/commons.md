<#function to_user_presentable val = "">
  <#if val?is_boolean>
    <#return val?then(bundle.yes, bundle.no)>
  </#if>
  <#if val?is_sequence>
    <#return val?map(v -> to_user_presentable(v))?join(", ")>
  </#if>
  <#return val>
</#function>

<#macro row object aspectDef domain = "">
  <#local labelDef = aspectDef?is_string?then(aspectDef, aspectDef?keys?first) />
  <#local valueDef = aspectDef?is_string?then(aspectDef, aspectDef?values?first) />
  <#local label =  labelDef?split('\\b', 'r')?map(v-> v?matches("[\\w]+")?then(bundle[v], v))?join('') />
  <#local val = getValue(valueDef, object, domain)/>
  | ${label} | <@multiline val/> |
</#macro>

<#macro multiline string>
    <#local lines = (string!'')?split("\n")/>
    <#list lines as line>${line}<br/></#list>
</#macro>

<#function getValue valueDef element domain>
    <#if valueDef == 'name'>
        <#return element.name/>
    </#if>
    <#if valueDef == 'status'>
        <#return status(element, domain)/>
    </#if>
  <#return valueDef?split('\\b', 'r')?map(v-> v?matches("[\\w]+")?then(getSingleValue(v, element), v))?join('')/>
</#function>

<#function getSingleValue valueDef element>
    <#assign val = element[valueDef]!/>
    <#if val?is_string && (val?length > 0) && val?contains(valueDef) && bundle?keys?seq_contains(val)>
        <#return bundle[val]/>
    </#if>
    <#return to_user_presentable(element[valueDef]!)/>
</#function>

<#macro table header object aspectDefs domain="">
| ${header}  ||
|:------------|:-----|
<#list aspectDefs as aspectDef>
<@row object,aspectDef,domain />
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

<!-- TODO verinice-veo#2773 use sort order from domain -->
<#assign orderedSubTypes = [
'PRO_BusinessProcess',
"AST_Information",
"AST_Application",
"AST_IT-System",
"AST_ICS-System",
"AST_Device",
"AST_Network"
]/>

<#function groupBySubType elements elementType domain>
<#assign etd = domain.elementTypeDefinitions[elementType]/>
<#assign elementsBySubType = []/>
<#assign subTypes = etd.subTypes?keys/>
<#return subTypes?map(subType -> {
    'elementType': elementType,
    'subType': subType,
    'elements': elements?filter(e -> e.type == elementType && e.domains[domain.id]?? && e.domains[domain.id].subType == subType)?sort_by("name_naturalized")?sort_by("abbreviation_naturalized"),
    'sortOrder': orderedSubTypes?seq_contains(subType)?then(orderedSubTypes?seq_index_of(subType), 99999999),
    'subTypePlural': bundle[elementType+'_'+subType+'_plural']
})?filter(s -> s.elements?has_content)?sort_by("sortOrder")/>
</#function>

<#function status element domain>
  <#return bundle[element.type+'_'+element.domains[domain.id].subType+'_status_'+element.domains[domain.id].status] />
</#function>
