var reportsFolderName = "TestAlfrescoJasperReports";
	
var reports = companyhome.childByNamePath(reportsFolderName);
if (!reports) reports = companyhome.createFolder(reportsFolderName);
	
var ftl = companyhome.childByNamePath(reportsFolderName + "/datasource_itemsInDataDictionary.ftl");
var jrxml = companyhome.childByNamePath(reportsFolderName + "/report_itemsInDataDictionary.jrxml");
	
if (!ftl) logger.log("AlfrescoJasperReports - missing test ftl!");
if (!jrxml) logger.log("AlfrescoJasperReports - missing test jrxml!");

if (ftl && jrxml)
{
	var xmlDataSourceString = companyhome.processTemplate(ftl);

	//getting query from JRXML file dynamically. You don't have to do that, you as "hardcode" it when executing jasperReportsHelper command
	//	e4x for some reason doesn't work when creating xml from whole file content.. that 1st line is problematic..
	try {
	var query = new XML(jrxml.content.substring(jrxml.content.indexOf("<queryString"), jrxml.content.lastIndexOf("</queryString>") + 14));
	} catch(err) { logger.log("AlfrescoJasperReports - can't get query from report... "); var query = "/items/*"; } 
	
	try {	
		/*
		   jasperReportsHelper.generateXMLDataSourceReport( 	xmlDataSourceString, 
									xpath report query, 
									nodeRef object of japer report document located on repository, 
									nodeRef object of destination folder to place generated report,
									output format ["pdf", "xls", "docx"]
		   );
		*/

		var generatedReportNodeRef= jasperReportsHelper.generateXMLDataSourceReport( xmlDataSourceString, query, jrxml.nodeRef, reports.nodeRef, "pdf");
		var generatedReport = search.findNode(generatedReportNodeRef);
		logger.log("AlfrescoJasperReports - report generated. " + generatedReport.displayPath + "/" + generatedReport.name );
	} catch(err) { logger.log("AlfrescoJasperReports - error while generating report... :( " + err); }
}
