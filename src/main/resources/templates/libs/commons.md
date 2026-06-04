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

<#function groupBySubType elements elementType domain>
<#assign etd = domain.elementTypeDefinitions[elementType]/>
<#assign elementsBySubType = []/>
<#assign subTypes = etd.subTypes?keys/>
<#return subTypes?map(subType -> {
    'elementType': elementType,
    'subType': subType,
    'elements': elements?filter(e -> e.type == elementType && e.domains[domain.id]?? && e.domains[domain.id].subType == subType)?sort_by("name_naturalized")?sort_by("abbreviation_naturalized"),
    'sortOrder': etd.subTypes[subType].sortKey!99999999,
    'subTypePlural': bundle[elementType+'_'+subType+'_plural']
})?filter(s -> s.elements?has_content)?sort_by("sortOrder")/>
</#function>

<#function status element domain>
  <#return bundle[element.type+'_'+element.domains[domain.id].subType+'_status_'+element.domains[domain.id].status] />
</#function>

<#macro heading text level>
  <#list 0..<level as i>#</#list> ${text}
</#macro>

<#macro defaultStyles landscape=false>
<#include "../styles/default.css">
<#include "../styles/pagecounter.css">
<#if landscape>
<#include "../styles/default_landscape.css">
<#include "../styles/pagecounter_landscape.css">
</#if>
</#macro>

<#function filterComplianceCIs targetObject domain>
    <#assign complianceControlSubTypes = domain.controlImplementationConfiguration.complianceControlSubTypes>
    <#local result = [] />
    <#list targetObject.controlImplementations as ci>
        <#if ci.control.domains?keys?seq_contains(domain.id)>
            <#local subType = ci.control.domains[domain.id].subType />
            <#if complianceControlSubTypes?seq_contains(subType)>
                <#local result = result + [ci] />
            </#if>
        </#if>
    </#list>
    <#return result/>
</#function>

<#function getDescendants composite maxSteps = 1000>
    <#local result = composite.parts>
    <#local resultURIs = result?map(it->it._self)>
    <#local currentStep=result>
    <#list 0..maxSteps as iteration>
        <#if currentStep?size==0><#break></#if>
        <#if iteration == maxSteps>
            <#stop "Failed to determine descendants for ${composite.name} in ${maxSteps} steps">
        </#if>
        <#local nextStep=[]>
        <#list currentStep as item>
            <#list item.parts as part>
                <#if !resultURIs?seq_contains(part._self)>
                    <#local result = result + [part]>
                    <#local resultURIs = resultURIs + [part._self]>
                    <#local nextStep = nextStep + [part]>
                </#if>
            </#list>
        </#list>
        <#local currentStep=nextStep>
    </#list>
    <#return result>
</#function>
