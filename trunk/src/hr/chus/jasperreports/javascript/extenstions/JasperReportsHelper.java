package hr.chus.jasperreports.javascript.extenstions;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * 
 * @author Jan Čustović
 *
 */
public class JasperReportsHelper extends BaseProcessorExtension {
	
	private NodeService nodeService;
	private ContentService contentService;
	
	
	public NodeRef generateXMLDataSourceReport(String xml, String xpathQuery, NodeRef jrxmlNodeRef, NodeRef parentOutputNodeRef, String output) throws IOException, JRException {
		NodeRef reportNodeRef = null;
		
		ContentReader contentReader = contentService.getReader(jrxmlNodeRef, ContentModel.PROP_CONTENT);
		InputStream jasperReportIS = new BufferedInputStream(contentReader.getContentInputStream(), 4096);
		JasperReport jasperReport = null;
		// TODO: Should generate report only once to save time. Maybe user can pass in forceRecompile if he wants to regenerate, 
		//       but otherwise after first generation try saving .jasper file somewhere and retrieve it if forceRecompile is false.
		try {
            jasperReport = JasperCompileManager.compileReport(jasperReportIS);
        } finally {
        	jasperReportIS.close();
        }
        JRXmlDataSource jrxmlDS = new JRXmlDataSource(new ByteArrayInputStream(xml.getBytes()), xpathQuery);
        Map<JRExporterParameter, String> parameters = new HashMap<JRExporterParameter, String>();
        JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, jrxmlDS);
        
		JRExporter exporter = null;
		
		String reportNodeName = String.valueOf(System.currentTimeMillis());
		
		if (output.equalsIgnoreCase("HTML")) {
			exporter = new JRHtmlExporter();
			reportNodeName += ".html";
		} else if (output.equalsIgnoreCase("XLS")) {
			exporter = new JRXlsExporter();
			reportNodeName += ".xls";
		} else if (output.equalsIgnoreCase("DOCX")) {
			exporter = new JRDocxExporter();
			reportNodeName += ".docx";
		} else if (output.equalsIgnoreCase("PDF")) {
			exporter = new JRPdfExporter();
			reportNodeName += ".pdf";
		} else {
			throw new JRException("Unknown output format.");
		}
		
        HashMap<QName, Serializable> contentProperties = new HashMap<QName, Serializable>();
		contentProperties.put(ContentModel.PROP_NAME, reportNodeName);
		QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(reportNodeName));
		
		reportNodeRef = nodeService.createNode(parentOutputNodeRef, ContentModel.ASSOC_CONTAINS, assocQName, ContentModel.PROP_CONTENT, contentProperties).getChildRef();
        
		ContentWriter contentWriter = contentService.getWriter(reportNodeRef, ContentModel.PROP_CONTENT, true);
		OutputStream reportOS = contentWriter.getContentOutputStream();
		
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, reportOS);
		
		try {
			exporter.exportReport();
		} catch (JRException e) {
			nodeService.deleteNode(reportNodeRef);
			throw e;
		} finally {
			reportOS.close();
		}
		return reportNodeRef;
	}


	// Getters & setters
	
	public void setNodeService(NodeService nodeService) { this.nodeService = nodeService; }
	
	public void setContentService(ContentService contentService) { this.contentService = contentService; }

}
