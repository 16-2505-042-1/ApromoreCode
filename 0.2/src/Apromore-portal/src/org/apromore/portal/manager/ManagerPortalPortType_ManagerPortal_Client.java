
package org.apromore.portal.manager;

/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 2.2.9
 * Tue May 03 11:34:41 CEST 2011
 * Generated source version: 2.2.9
 * 
 */

public final class ManagerPortalPortType_ManagerPortal_Client {

    private static final QName SERVICE_NAME = new QName("http://www.apromore.org/manager/service_portal", "ManagerPortalService");

    private ManagerPortalPortType_ManagerPortal_Client() {
    }

    public static void main(String args[]) throws Exception {
        URL wsdlURL = ManagerPortalService.WSDL_LOCATION;
        if (args.length > 0) { 
            File wsdlFile = new File(args[0]);
            try {
                if (wsdlFile.exists()) {
                    wsdlURL = wsdlFile.toURI().toURL();
                } else {
                    wsdlURL = new URL(args[0]);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
      
        ManagerPortalService ss = new ManagerPortalService(wsdlURL, SERVICE_NAME);
        ManagerPortalPortType port = ss.getManagerPortal();  
        
        {
        System.out.println("Invoking writeUser...");
        org.apromore.portal.model_manager.WriteUserInputMsgType _writeUser_payload = new org.apromore.portal.model_manager.WriteUserInputMsgType();
        org.apromore.portal.model_manager.UserType _writeUser_payloadUser = new org.apromore.portal.model_manager.UserType();
        java.util.List<org.apromore.portal.model_manager.SearchHistoriesType> _writeUser_payloadUserSearchHistories = new java.util.ArrayList<org.apromore.portal.model_manager.SearchHistoriesType>();
        org.apromore.portal.model_manager.SearchHistoriesType _writeUser_payloadUserSearchHistoriesVal1 = new org.apromore.portal.model_manager.SearchHistoriesType();
        _writeUser_payloadUserSearchHistoriesVal1.setSearch("Search940719103");
        _writeUser_payloadUserSearchHistoriesVal1.setNum(Integer.valueOf(-1692835386));
        _writeUser_payloadUserSearchHistories.add(_writeUser_payloadUserSearchHistoriesVal1);
        _writeUser_payloadUser.getSearchHistories().addAll(_writeUser_payloadUserSearchHistories);
        _writeUser_payloadUser.setFirstname("Firstname1999595206");
        _writeUser_payloadUser.setLastname("Lastname-842584486");
        _writeUser_payloadUser.setEmail("Email878990507");
        _writeUser_payloadUser.setUsername("Username1239976349");
        _writeUser_payloadUser.setPasswd("Passwd73936234");
        _writeUser_payload.setUser(_writeUser_payloadUser);
        org.apromore.portal.model_manager.WriteUserOutputMsgType _writeUser__return = port.writeUser(_writeUser_payload);
        System.out.println("writeUser.result=" + _writeUser__return);


        }
        {
        System.out.println("Invoking editProcessData...");
        org.apromore.portal.model_manager.EditProcessDataInputMsgType _editProcessData_payload = new org.apromore.portal.model_manager.EditProcessDataInputMsgType();
        _editProcessData_payload.setProcessName("ProcessName1974940744");
        _editProcessData_payload.setId(Integer.valueOf(723569657));
        _editProcessData_payload.setDomain("Domain-1830564673");
        _editProcessData_payload.setOwner("Owner-1263055769");
        _editProcessData_payload.setRanking("Ranking-885117481");
        _editProcessData_payload.setNewName("NewName-1564675169");
        _editProcessData_payload.setPreName("PreName-298593773");
        org.apromore.portal.model_manager.EditProcessDataOutputMsgType _editProcessData__return = port.editProcessData(_editProcessData_payload);
        System.out.println("editProcessData.result=" + _editProcessData__return);


        }
        {
        System.out.println("Invoking writeEditSession...");
        org.apromore.portal.model_manager.WriteEditSessionInputMsgType _writeEditSession_payload = new org.apromore.portal.model_manager.WriteEditSessionInputMsgType();
        org.apromore.portal.model_manager.EditSessionType _writeEditSession_payloadEditSession = new org.apromore.portal.model_manager.EditSessionType();
        _writeEditSession_payloadEditSession.setUsername("Username-1318590437");
        _writeEditSession_payloadEditSession.setNativeType("NativeType1990400469");
        _writeEditSession_payloadEditSession.setProcessId(Integer.valueOf(1771953607));
        _writeEditSession_payloadEditSession.setProcessName("ProcessName2008857875");
        _writeEditSession_payloadEditSession.setVersionName("VersionName920097960");
        _writeEditSession_payloadEditSession.setDomain("Domain650691983");
        _writeEditSession_payloadEditSession.setWithAnnotation(Boolean.valueOf(false));
        _writeEditSession_payloadEditSession.setAnnotation("Annotation-487375698");
        _writeEditSession_payload.setEditSession(_writeEditSession_payloadEditSession);
        org.apromore.portal.model_manager.WriteEditSessionOutputMsgType _writeEditSession__return = port.writeEditSession(_writeEditSession_payload);
        System.out.println("writeEditSession.result=" + _writeEditSession__return);


        }
        {
        System.out.println("Invoking exportFormat...");
        org.apromore.portal.model_manager.ExportFormatInputMsgType _exportFormat_payload = new org.apromore.portal.model_manager.ExportFormatInputMsgType();
        _exportFormat_payload.setFormat("Format1136559371");
        _exportFormat_payload.setProcessId(Integer.valueOf(-878325678));
        _exportFormat_payload.setProcessName("ProcessName1244665723");
        _exportFormat_payload.setVersionName("VersionName1203315823");
        _exportFormat_payload.setAnnotationName("AnnotationName1776490349");
        _exportFormat_payload.setWithAnnotations(Boolean.valueOf(true));
        _exportFormat_payload.setOwner("Owner1419601263");
        org.apromore.portal.model_manager.ExportFormatOutputMsgType _exportFormat__return = port.exportFormat(_exportFormat_payload);
        System.out.println("exportFormat.result=" + _exportFormat__return);


        }
        {
        System.out.println("Invoking readNativeTypes...");
        org.apromore.portal.model_manager.ReadNativeTypesInputMsgType _readNativeTypes_payload = new org.apromore.portal.model_manager.ReadNativeTypesInputMsgType();
        _readNativeTypes_payload.setEmpty("Empty-1475873827");
        org.apromore.portal.model_manager.ReadNativeTypesOutputMsgType _readNativeTypes__return = port.readNativeTypes(_readNativeTypes_payload);
        System.out.println("readNativeTypes.result=" + _readNativeTypes__return);


        }
        {
        System.out.println("Invoking readDomains...");
        org.apromore.portal.model_manager.ReadDomainsInputMsgType _readDomains_payload = new org.apromore.portal.model_manager.ReadDomainsInputMsgType();
        _readDomains_payload.setEmpty("Empty1971933881");
        org.apromore.portal.model_manager.ReadDomainsOutputMsgType _readDomains__return = port.readDomains(_readDomains_payload);
        System.out.println("readDomains.result=" + _readDomains__return);


        }
        {
        System.out.println("Invoking mergeProcesses...");
        org.apromore.portal.model_manager.MergeProcessesInputMsgType _mergeProcesses_payload = new org.apromore.portal.model_manager.MergeProcessesInputMsgType();
        _mergeProcesses_payload.setProcessName("ProcessName1951277309");
        _mergeProcesses_payload.setVersionName("VersionName193565404");
        _mergeProcesses_payload.setUsername("Username-642650075");
        org.apromore.portal.model_manager.ProcessVersionIdsType _mergeProcesses_payloadProcessVersionIds = new org.apromore.portal.model_manager.ProcessVersionIdsType();
        java.util.List<org.apromore.portal.model_manager.ProcessVersionIdType> _mergeProcesses_payloadProcessVersionIdsProcessVersionId = new java.util.ArrayList<org.apromore.portal.model_manager.ProcessVersionIdType>();
        org.apromore.portal.model_manager.ProcessVersionIdType _mergeProcesses_payloadProcessVersionIdsProcessVersionIdVal1 = new org.apromore.portal.model_manager.ProcessVersionIdType();
        _mergeProcesses_payloadProcessVersionIdsProcessVersionIdVal1.setProcessId(Integer.valueOf(1290021230));
        _mergeProcesses_payloadProcessVersionIdsProcessVersionIdVal1.setVersionName("VersionName-1793125725");
        _mergeProcesses_payloadProcessVersionIdsProcessVersionId.add(_mergeProcesses_payloadProcessVersionIdsProcessVersionIdVal1);
        _mergeProcesses_payloadProcessVersionIds.getProcessVersionId().addAll(_mergeProcesses_payloadProcessVersionIdsProcessVersionId);
        _mergeProcesses_payload.setProcessVersionIds(_mergeProcesses_payloadProcessVersionIds);
        _mergeProcesses_payload.setAlgorithm("Algorithm676500600");
        org.apromore.portal.model_manager.ParametersType _mergeProcesses_payloadParameters = new org.apromore.portal.model_manager.ParametersType();
        java.util.List<org.apromore.portal.model_manager.ParameterType> _mergeProcesses_payloadParametersParameter = new java.util.ArrayList<org.apromore.portal.model_manager.ParameterType>();
        org.apromore.portal.model_manager.ParameterType _mergeProcesses_payloadParametersParameterVal1 = new org.apromore.portal.model_manager.ParameterType();
        _mergeProcesses_payloadParametersParameterVal1.setName("Name1491645430");
        _mergeProcesses_payloadParametersParameterVal1.setValue(0.8392270522723666);
        _mergeProcesses_payloadParametersParameter.add(_mergeProcesses_payloadParametersParameterVal1);
        _mergeProcesses_payloadParameters.getParameter().addAll(_mergeProcesses_payloadParametersParameter);
        _mergeProcesses_payload.setParameters(_mergeProcesses_payloadParameters);
        org.apromore.portal.model_manager.MergeProcessesOutputMsgType _mergeProcesses__return = port.mergeProcesses(_mergeProcesses_payload);
        System.out.println("mergeProcesses.result=" + _mergeProcesses__return);


        }
        {
        System.out.println("Invoking readUser...");
        org.apromore.portal.model_manager.ReadUserInputMsgType _readUser_payload = new org.apromore.portal.model_manager.ReadUserInputMsgType();
        _readUser_payload.setUsername("Username2008489956");
        org.apromore.portal.model_manager.ReadUserOutputMsgType _readUser__return = port.readUser(_readUser_payload);
        System.out.println("readUser.result=" + _readUser__return);


        }
        {
        System.out.println("Invoking writeAnnotation...");
        org.apromore.portal.model_manager.WriteAnnotationInputMsgType _writeAnnotation_payload = new org.apromore.portal.model_manager.WriteAnnotationInputMsgType();
        javax.activation.DataHandler _writeAnnotation_payloadNative = null;
        _writeAnnotation_payload.setNative(_writeAnnotation_payloadNative);
        _writeAnnotation_payload.setEditSessionCode(Integer.valueOf(-373139028));
        _writeAnnotation_payload.setAnnotationName("AnnotationName1974985398");
        _writeAnnotation_payload.setIsNew(Boolean.valueOf(true));
        _writeAnnotation_payload.setProcessId(Integer.valueOf(984155937));
        _writeAnnotation_payload.setVersion("Version-1226529872");
        _writeAnnotation_payload.setNativeType("NativeType-794577114");
        org.apromore.portal.model_manager.WriteAnnotationOutputMsgType _writeAnnotation__return = port.writeAnnotation(_writeAnnotation_payload);
        System.out.println("writeAnnotation.result=" + _writeAnnotation__return);


        }
        {
        System.out.println("Invoking readAllUsers...");
        org.apromore.portal.model_manager.ReadAllUsersInputMsgType _readAllUsers_payload = new org.apromore.portal.model_manager.ReadAllUsersInputMsgType();
        _readAllUsers_payload.setEmpty("Empty610613987");
        org.apromore.portal.model_manager.ReadAllUsersOutputMsgType _readAllUsers__return = port.readAllUsers(_readAllUsers_payload);
        System.out.println("readAllUsers.result=" + _readAllUsers__return);


        }
        {
        System.out.println("Invoking deleteEditSession...");
        org.apromore.portal.model_manager.DeleteEditSessionInputMsgType _deleteEditSession_payload = new org.apromore.portal.model_manager.DeleteEditSessionInputMsgType();
        _deleteEditSession_payload.setEditSessionCode(Integer.valueOf(1210406723));
        org.apromore.portal.model_manager.DeleteEditSessionOutputMsgType _deleteEditSession__return = port.deleteEditSession(_deleteEditSession_payload);
        System.out.println("deleteEditSession.result=" + _deleteEditSession__return);


        }
        {
        System.out.println("Invoking searchForSimilarProcesses...");
        org.apromore.portal.model_manager.SearchForSimilarProcessesInputMsgType _searchForSimilarProcesses_payload = new org.apromore.portal.model_manager.SearchForSimilarProcessesInputMsgType();
        _searchForSimilarProcesses_payload.setProcessId(1276102429);
        _searchForSimilarProcesses_payload.setVersionName("VersionName970700068");
        _searchForSimilarProcesses_payload.setAlgorithm("Algorithm-914548450");
        org.apromore.portal.model_manager.ParametersType _searchForSimilarProcesses_payloadParameters = new org.apromore.portal.model_manager.ParametersType();
        java.util.List<org.apromore.portal.model_manager.ParameterType> _searchForSimilarProcesses_payloadParametersParameter = new java.util.ArrayList<org.apromore.portal.model_manager.ParameterType>();
        org.apromore.portal.model_manager.ParameterType _searchForSimilarProcesses_payloadParametersParameterVal1 = new org.apromore.portal.model_manager.ParameterType();
        _searchForSimilarProcesses_payloadParametersParameterVal1.setName("Name1927491171");
        _searchForSimilarProcesses_payloadParametersParameterVal1.setValue(0.7918813528567086);
        _searchForSimilarProcesses_payloadParametersParameter.add(_searchForSimilarProcesses_payloadParametersParameterVal1);
        _searchForSimilarProcesses_payloadParameters.getParameter().addAll(_searchForSimilarProcesses_payloadParametersParameter);
        _searchForSimilarProcesses_payload.setParameters(_searchForSimilarProcesses_payloadParameters);
        org.apromore.portal.model_manager.SearchForSimilarProcessesOutputMsgType _searchForSimilarProcesses__return = port.searchForSimilarProcesses(_searchForSimilarProcesses_payload);
        System.out.println("searchForSimilarProcesses.result=" + _searchForSimilarProcesses__return);


        }
        {
        System.out.println("Invoking deleteProcessVersions...");
        org.apromore.portal.model_manager.DeleteProcessVersionsInputMsgType _deleteProcessVersions_payload = new org.apromore.portal.model_manager.DeleteProcessVersionsInputMsgType();
        java.util.List<org.apromore.portal.model_manager.ProcessVersionIdentifierType> _deleteProcessVersions_payloadProcessVersionIdentifier = new java.util.ArrayList<org.apromore.portal.model_manager.ProcessVersionIdentifierType>();
        org.apromore.portal.model_manager.ProcessVersionIdentifierType _deleteProcessVersions_payloadProcessVersionIdentifierVal1 = new org.apromore.portal.model_manager.ProcessVersionIdentifierType();
        java.util.List<java.lang.String> _deleteProcessVersions_payloadProcessVersionIdentifierVal1VersionName = new java.util.ArrayList<java.lang.String>();
        _deleteProcessVersions_payloadProcessVersionIdentifierVal1.getVersionName().addAll(_deleteProcessVersions_payloadProcessVersionIdentifierVal1VersionName);
        _deleteProcessVersions_payloadProcessVersionIdentifierVal1.setProcessid(Integer.valueOf(-525823699));
        _deleteProcessVersions_payloadProcessVersionIdentifier.add(_deleteProcessVersions_payloadProcessVersionIdentifierVal1);
        _deleteProcessVersions_payload.getProcessVersionIdentifier().addAll(_deleteProcessVersions_payloadProcessVersionIdentifier);
        org.apromore.portal.model_manager.DeleteProcessVersionsOutputMsgType _deleteProcessVersions__return = port.deleteProcessVersions(_deleteProcessVersions_payload);
        System.out.println("deleteProcessVersions.result=" + _deleteProcessVersions__return);


        }
        {
        System.out.println("Invoking importProcess...");
        org.apromore.portal.model_manager.ImportProcessInputMsgType _importProcess_payload = new org.apromore.portal.model_manager.ImportProcessInputMsgType();
        javax.activation.DataHandler _importProcess_payloadProcessDescription = null;
        _importProcess_payload.setProcessDescription(_importProcess_payloadProcessDescription);
        _importProcess_payload.setProcessName("ProcessName1564835033");
        _importProcess_payload.setVersionName("VersionName-1887618120");
        _importProcess_payload.setNativeType("NativeType-504933651");
        _importProcess_payload.setDomain("Domain-509634602");
        _importProcess_payload.setUsername("Username-1542773333");
        _importProcess_payload.setLastUpdate("LastUpdate523879387");
        _importProcess_payload.setCreationDate("CreationDate207304672");
        _importProcess_payload.setDocumentation("Documentation-1979489086");
        org.apromore.portal.model_manager.ImportProcessOutputMsgType _importProcess__return = port.importProcess(_importProcess_payload);
        System.out.println("importProcess.result=" + _importProcess__return);


        }
        {
        System.out.println("Invoking readProcessSummaries...");
        org.apromore.portal.model_manager.ReadProcessSummariesInputMsgType _readProcessSummaries_payload = new org.apromore.portal.model_manager.ReadProcessSummariesInputMsgType();
        _readProcessSummaries_payload.setSearchExpression("SearchExpression-1879297473");
        org.apromore.portal.model_manager.ReadProcessSummariesOutputMsgType _readProcessSummaries__return = port.readProcessSummaries(_readProcessSummaries_payload);
        System.out.println("readProcessSummaries.result=" + _readProcessSummaries__return);


        }
        {
        System.out.println("Invoking readEditSession...");
        org.apromore.portal.model_manager.ReadEditSessionInputMsgType _readEditSession_payload = new org.apromore.portal.model_manager.ReadEditSessionInputMsgType();
        _readEditSession_payload.setEditSessionCode(Integer.valueOf(741103709));
        org.apromore.portal.model_manager.ReadEditSessionOutputMsgType _readEditSession__return = port.readEditSession(_readEditSession_payload);
        System.out.println("readEditSession.result=" + _readEditSession__return);


        }
        {
        System.out.println("Invoking updateProcess...");
        org.apromore.portal.model_manager.UpdateProcessInputMsgType _updateProcess_payload = new org.apromore.portal.model_manager.UpdateProcessInputMsgType();
        javax.activation.DataHandler _updateProcess_payloadNative = null;
        _updateProcess_payload.setNative(_updateProcess_payloadNative);
        _updateProcess_payload.setEditSessionCode(Integer.valueOf(-756226792));
        _updateProcess_payload.setUsername("Username24236232");
        _updateProcess_payload.setNativeType("NativeType-1215689384");
        _updateProcess_payload.setProcessId(Integer.valueOf(1293210161));
        _updateProcess_payload.setDomain("Domain-673701160");
        _updateProcess_payload.setPreVersion("PreVersion1048171633");
        org.apromore.portal.model_manager.UpdateProcessOutputMsgType _updateProcess__return = port.updateProcess(_updateProcess_payload);
        System.out.println("updateProcess.result=" + _updateProcess__return);


        }

        System.exit(0);
    }

}
