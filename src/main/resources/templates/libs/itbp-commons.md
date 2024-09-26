<#function sortModules modules>
  <#-- apply fancy ITBP sorting -->
  <#assign step1 = modules?sort_by('name_naturalized')?sort_by('abbreviation_naturalized')>
  <#assign sortInfo = step1?map(it->
    {"url":it._self, 
     "key":it.abbreviation_naturalized
       ?replace('^ISMS', ' A', 'r')
       ?replace('^ORP', ' B', 'r')
       ?replace('^CON', ' C', 'r')
       ?replace('^OPS', ' D', 'r')
       ?replace('^DER', ' E', 'r')
       ?replace('^APP', ' F', 'r')
       ?replace('^SYS', ' G', 'r')
       ?replace('^IND', ' H', 'r')
       ?replace('^NET', ' I', 'r')
       ?replace('^INF', ' J', 'r')
    }
  )?sort_by('key')>
  <#assign step2 = sortInfo?map(it->modules?filter(module->module._self == it.url)?first)>
  <#return step2?filter(it->it.abbreviation?has_content)+step2?filter(it->!it.abbreviation?has_content)>
</#function>

<#function title element>
<#if element.abbreviation?has_content>
  <#return "${element.abbreviation} ${element.name}">
  <#else>
  <#return element.name>
</#if>
</#function>

<#function controlTitle element>
<#local result = element.name>
<#if element.control_bpInformation_protectionApproach?has_content>
  <#local result = "[${bundle[element.control_bpInformation_protectionApproach]}] ${result}">
</#if>
<#if element.abbreviation?has_content>
  <#local result = "${element.abbreviation} ${result}">
</#if>
<#return result>
</#function>