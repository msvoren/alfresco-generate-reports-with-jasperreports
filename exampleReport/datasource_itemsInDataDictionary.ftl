<#macro countDocs node depth>
  <#if node.isContainer>
    <#list node.children as child>
       <#if child.isContainer && node.children?size != 0 >
       	     <@countDocs node=child depth=depth+1/>
       </#if>
       <#if child.isDocument>
       	     <#assign docsNo = docsNo + 1>
       </#if>
   </#list>
  </#if>
</#macro>

<items>
<#list companyhome.childByNamePath["Data Dictionary"].children?sort_by(['properties', 'name']) as item>
	<#assign docsNo = 0>
	<@countDocs node=item depth=0/>
	<item>
	    <name>${item.name!""}</name>
	    <creator>${item.properties.creator}</creator>
	    <created>${item.properties.created?string("dd-MM-yyyy HH:mm")}</created>
	    <description>${item.properties.description!''}</description>
	    <docs>${docsNo}</docs>
	</item>
</#list>
</items>

