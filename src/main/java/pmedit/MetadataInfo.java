package pmedit;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.jempbox.xmp.XMPSchemaPDF;
import org.apache.jempbox.xmp.XMPSchemaRightsManagement;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import pmedit.CommandLine.ParseError;
import pmedit.MdStruct.StructType;

public class MetadataInfo {

	// Copy of java.util.functions.Function from Java8
	public interface Function<T, R> {
	    R apply(T t);
	}
	
	public static class FileInfo {
		public String name;
		public String nameWithExt;
		public Long sizeBytes;
		public String size;
		public String createTime;
		public String modifyTime;
		public String fullPath;
		
	}

	public static class FileInfoEnabled {
		public boolean name = false;
		public boolean nameWithExt = false;
		public boolean sizeBytes = false;
		public boolean size = false;
		public boolean createTime = false;
		public boolean modifyTime = false;
		public boolean fullPath = false;

		public boolean atLeastOne() {
			return false;
		}
		
		public void setAll(boolean value){
			name = false;
			nameWithExt = false;
			sizeBytes = false;
			size = false;
			createTime = false;
			modifyTime = false;
			fullPath = false;
		}
	};

	public static class Basic {
		public String title;
		public String author;
		public String subject;
		public String keywords;
		public String creator;
		public String producer;
		public Calendar creationDate;
		public Calendar modificationDate;
		public String trapped;
	};

	public static class BasicEnabled {
		public boolean title = true;
		public boolean author = true;
		public boolean subject = true;
		public boolean keywords = true;
		public boolean creator = true;
		public boolean producer = true;
		public boolean creationDate = true;
		public boolean modificationDate = true;
		public boolean trapped = true;

		public boolean atLeastOne() {
			return title || author || subject || keywords || creator || producer || creationDate || modificationDate
					|| trapped;
		}
		
		public void setAll(boolean value){
			title = value;
			author = value;
			subject = value;
			keywords = value;
			creator = value;
			producer = value;
			creationDate = value;
			modificationDate = value;
			trapped = value;			
		}
	};

	public static class XmpBasic {
		public String creatorTool;
		public Calendar createDate;
		public Calendar modifyDate;
		public String baseURL;
		public Integer rating;
		public String label;
		public String nickname;
		public List<String> identifiers;
		public List<String> advisories;
		public Calendar metadataDate;
	};
	
	public static class XmpBasicEnabled {
		public boolean creatorTool = true;
		public boolean createDate = true;
		public boolean modifyDate = true;
		public boolean baseURL = true;
		public boolean rating = true;
		public boolean label = true;
		public boolean nickname = true;
		public boolean identifiers = true;
		public boolean advisories = true;
		public boolean metadataDate = true;

		public boolean atLeastOne() {
			return creatorTool || createDate || modifyDate || baseURL || rating || label || nickname
					|| identifiers || advisories || metadataDate;
		}
		
		public void setAll(boolean value){
			creatorTool = value;
			createDate = value;
			modifyDate = value;
			baseURL = value;
			rating = value;
			label = value;
			nickname = value;
			identifiers = value;
			advisories = value;
			metadataDate = value;		
		}
	};
	
	public static class XmpPdf {
		public String pdfVersion;
		public String keywords;
		public String producer;
	};

	public static class XmpPdfEnabled {
		public boolean pdfVersion = true;
		public boolean keywords = true;
		public boolean producer = true;
		public boolean atLeastOne() {
			return pdfVersion || keywords || producer;
		}
		public void setAll(boolean value){
			pdfVersion = value;
			keywords = value;
			producer = value;	
		}
	};

	public static class XmpDublinCore {
		public String title;
		public String description;
		public List<String> creators;
		public List<String> contributors;
		public String coverage;
		@FieldID(value = "dates", type= FieldID.FieldType.DateField)
		public List<Calendar> dates;
		public String format;
		public String identifier;
		public List<String> languages;
		public List<String> publishers;
		public List<String> relationships;
		public String rights;
		public String source;
		public List<String> subjects;
		public List<String> types;
	};

	public static class XmpDublinCoreEnabled {
		public boolean title = true;
		public boolean description = true;
		public boolean creators = true;
		public boolean contributors = true;
		public boolean coverage = true;
		public boolean dates = true;
		public boolean format = true;
		public boolean identifier = true;
		public boolean languages = true;
		public boolean publishers = true;
		public boolean relationships = true;
		public boolean rights = true;
		public boolean source = true;
		public boolean subjects = true;
		public boolean types = true;

		public boolean atLeastOne() {
			return title || description || creators || contributors || coverage || dates || format || identifier
					|| languages || publishers || relationships || rights || source || subjects || types;
		}
		public void setAll(boolean value){
			title = value;
			description = value;
			creators = value;
			contributors = value;
			coverage = value;
			dates = value;
			format = value;
			identifier = value;
			languages = value;
			publishers = value;
			relationships = value;
			rights = value;
			source = value;
			subjects = value;
			types = value;	
		}
	};

	public static class XmpRights {
		public String certificate;
		public Boolean marked;
		public List<String> owner;
		public String copyright;
		public String usageTerms;
		public String webStatement;
	}
	
	public static class XmpRightsEnabled {
		public boolean certificate = true;
		public boolean marked = true;
		public boolean owner = true;
		public boolean copyright = true;
		public boolean usageTerms = true;
		public boolean webStatement = true;
		
		public boolean atLeastOne() {
			return certificate || marked || owner || usageTerms || webStatement;
		}
		
		public void setAll(boolean value){
			certificate = value;
			marked  = value;
			owner = value;
			usageTerms = value;
			webStatement = value;
		}
	}


	@MdStruct
	public Basic doc;
	@MdStruct
	public XmpBasic basic ;
	@MdStruct
	public XmpPdf pdf ;
	@MdStruct
	public XmpDublinCore dc;
	@MdStruct
	public XmpRights rights;
	@MdStruct(name="file", type=MdStruct.StructType.MdStruct, access=MdStruct.Access.ReadOnly)
	public FileInfo file;

	@MdStruct(name="doc", type=MdStruct.StructType.MdEnableStruct)
	public BasicEnabled docEnabled ;
	@MdStruct(name="basic", type=MdStruct.StructType.MdEnableStruct)
	public XmpBasicEnabled basicEnabled ;
	@MdStruct(name="pdf", type=MdStruct.StructType.MdEnableStruct)
	public XmpPdfEnabled pdfEnabled ;
	@MdStruct(name="dc", type=MdStruct.StructType.MdEnableStruct)
	public XmpDublinCoreEnabled dcEnabled;
	@MdStruct(name="rights", type=MdStruct.StructType.MdEnableStruct)
	public XmpRightsEnabled rightsEnabled;
	@MdStruct(name="file", type=MdStruct.StructType.MdEnableStruct, access=MdStruct.Access.ReadOnly)
	public FileInfoEnabled fileEnabled;

	public MetadataInfo() {
		super();
		clear();
	}
	
	public void clear() {
		this.doc =  new Basic();
		this.basic = new XmpBasic();
		this.pdf = new XmpPdf();
		this.dc  = new XmpDublinCore();	
		this.rights = new XmpRights();
		this.file = new FileInfo();
		
		this.docEnabled = new BasicEnabled();
		this.basicEnabled = new XmpBasicEnabled();
		this.pdfEnabled = new XmpPdfEnabled();
		this.dcEnabled = new XmpDublinCoreEnabled();
		this.rightsEnabled = new XmpRightsEnabled();
		this.fileEnabled = new FileInfoEnabled();
	}

	protected void loadFromPDF(PDDocument document) throws IOException {
		PDDocumentInformation info = document.getDocumentInformation();

		// Basic info
		doc.title = info.getTitle();
		doc.author = info.getAuthor();
		doc.subject = info.getSubject();
		doc.keywords = info.getKeywords();
		doc.creator = info.getCreator();
		doc.producer = info.getProducer();
		doc.creationDate = info.getCreationDate();
		doc.modificationDate = info.getModificationDate();
		doc.trapped = info.getTrapped();

		// Load XMP catalog
		PDDocumentCatalog catalog = document.getDocumentCatalog();
		PDMetadata meta = catalog.getMetadata();


		if (meta != null) {
			// Workaround stupid jempbox not recognizing ISO dates with subsecond granularity
			InputStream is= meta.createInputStream();
			java.util.Scanner s = new java.util.Scanner(is);
			s.useDelimiter("\\A");
			String xmpData = s.hasNext() ? s.next() : "";
			s.close();
			is.close();
			// remove subsecond digits from dates like 2017-03-26T18:50:09.184000+02:00
			String re = "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})\\.\\d+([-+Z])";
			xmpData = xmpData.replaceAll(re, "$1$2");
			InputStream xmpDataStream = new ByteArrayInputStream(xmpData.getBytes());

			// Load the metadata
			XMPMetadata xmp = XMPMetadata.load(xmpDataStream);

			// XMP Basic
			XMPSchemaBasic bi = xmp.getBasicSchema();
			if (bi != null) {

				basic.creatorTool = bi.getCreatorTool();
				basic.createDate = bi.getCreateDate();
				basic.modifyDate = bi.getModifyDate();
				basic.baseURL = bi.getBaseURL();
				basic.rating = bi.getRating();
				basic.label = bi.getLabel();
				basic.nickname = bi.getNickname();
				basic.identifiers = bi.getIdentifiers();
				basic.advisories = bi.getAdvisories();
				basic.metadataDate = bi.getMetadataDate();
			}

			// XMP PDF
			XMPSchemaPDF pi = xmp.getPDFSchema();
			if (pi != null) {
				pdf.pdfVersion = pi.getPDFVersion();
				pdf.keywords = pi.getKeywords();
				pdf.producer = pi.getProducer();
			}

			// XMP Dublin Core
			XMPSchemaDublinCore dcS = xmp.getDublinCoreSchema();
			if (dcS != null) {
				dc.title = dcS.getTitle();
				dc.description = dcS.getDescription();
				dc.creators = dcS.getCreators();
				dc.contributors = dcS.getContributors();
				dc.coverage = dcS.getCoverage();
				dc.dates = dcS.getDates();
				dc.format = dcS.getFormat();
				dc.identifier = dcS.getIdentifier();
				dc.languages = dcS.getLanguages();
				dc.publishers = dcS.getPublishers();
				dc.relationships = dcS.getRelationships();
				dc.rights = dcS.getRights();
				dc.source = dcS.getSource();
				dc.subjects = dcS.getSubjects();
				dc.types = dcS.getTypes();
			}
			
			// XMP Rights
			XMPSchemaRightsManagement ri = xmp.getRightsManagementSchema();
			if (ri != null){
				rights.certificate = ri.getCertificateURL();
				// rights.marked  = ri.getMarked(); // getMarked() return false on null value
				rights.marked = ri.getBooleanProperty("xmpRights:Marked");
				rights.owner = ri.getOwners();
				rights.usageTerms = ri.getUsageTerms();
				rights.copyright = ri.getCopyright();
				rights.webStatement = ri.getWebStatement();
			}
		}

		//System.err.println("Loaded:");
		//System.err.println(toYAML());

	}

	protected static String hrSizes[] = new String[]{ "B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
	public void loadFromPDF(File pdfFile) throws FileNotFoundException,
			IOException {
		
		loadPDFFileInfo(pdfFile);

		PDDocument document = null;
		FileInputStream inputStream = new FileInputStream(pdfFile);
		document = PDDocument.load(inputStream);
		loadFromPDF(document);

		if (document != null) {
			try {
				document.close();
			} catch (Exception e) {

			}
		}
		inputStream.close();
	}
	

	public void loadPDFFileInfo(File pdfFile) throws FileNotFoundException, IOException {
		file.fullPath = pdfFile.getAbsolutePath();
		file.nameWithExt = pdfFile.getName();
		BasicFileAttributes attrs = Files.readAttributes(pdfFile.toPath(), BasicFileAttributes.class);
		file.sizeBytes = attrs.size();
		file.createTime = attrs.creationTime().toString();
		file.modifyTime = attrs.lastModifiedTime().toString();
		
		// filename w/o extension
		if(file.nameWithExt != null) {
			int dotPos = file.nameWithExt.lastIndexOf('.');
			if(dotPos >= 0){
				file.name = file.nameWithExt.substring(0, dotPos);
			} else {
				file.name = file.nameWithExt;
			}
		}
		// human readable file size
		double size = file.sizeBytes;
		int idx;
		for(idx=0; idx < hrSizes.length; ++idx) {
			if( size < 1000 ) {
				break;
			}
			size /= 1000;
		}
		file.size = String.format("%.2f%s", size, hrSizes[idx]);
	}

	protected void saveToPDF(PDDocument document, File pdfFile) throws Exception {
		if(!(docEnabled.atLeastOne() || basicEnabled.atLeastOne() || pdfEnabled.atLeastOne() || dcEnabled.atLeastOne() || rightsEnabled.atLeastOne())){
			return;
		}
		//System.err.println("Saving:");
		//System.err.println(toYAML());
		// Basic info
		if(docEnabled.atLeastOne()){
			PDDocumentInformation info = document.getDocumentInformation();
			if(docEnabled.title){
				info.setTitle(doc.title);
			}
			if(docEnabled.author){
				info.setAuthor(doc.author);
			}
			if(docEnabled.subject){
				info.setSubject(doc.subject);
			}
			if(docEnabled.keywords){
				info.setKeywords(doc.keywords);
			}
			if(docEnabled.creator){
				info.setCreator(doc.creator);
			}
			if(docEnabled.producer){
				info.setProducer(doc.producer);
			}
			if(docEnabled.creationDate){
				info.setCreationDate(doc.creationDate);
			}
			if(docEnabled.modificationDate){
				info.setModificationDate(doc.modificationDate);
			}
			if(docEnabled.trapped){
				info.setTrapped(doc.trapped);
			}
			document.setDocumentInformation(info);
		}
		
		// XMP
		PDDocumentCatalog catalog = document.getDocumentCatalog();
		PDMetadata meta = catalog.getMetadata();
		
		XMPMetadata xmpOld = null;
		if (meta != null) {
			xmpOld = XMPMetadata.load(meta.createInputStream());
		}
		XMPMetadata xmpNew = new XMPMetadata();
		// XMP Basic
		XMPSchemaBasic biOld = xmpOld != null ? xmpOld.getBasicSchema() : null;
		boolean atLeastOneXmpBasicSet = false;
		if(basicEnabled.atLeastOne() || (biOld != null)) {
			XMPSchemaBasic bi = xmpNew.addBasicSchema();
			
			if(basicEnabled.advisories){
				if(basic.advisories != null){
					for (String a : basic.advisories) {
						bi.addAdvisory(a);
						atLeastOneXmpBasicSet = true;
					}
				}
			} else if(biOld != null){
				List<String> old = biOld.getAdvisories(); 
				if(old != null){
					for(String a: old){
						bi.addAdvisory(a);
						atLeastOneXmpBasicSet = true;
					}
				}
			}
			
			if(basicEnabled.baseURL){
				if(basic.baseURL != null){
					bi.setBaseURL(basic.baseURL);
					atLeastOneXmpBasicSet = true;
				}
			} else if(biOld != null){
				String baseUrl = biOld.getBaseURL();
				if(baseUrl != null){
					bi.setBaseURL(baseUrl);
					atLeastOneXmpBasicSet = true;
				}
			}
			
			if(basicEnabled.createDate){
				if(basic.createDate != null){
					bi.setCreateDate(basic.createDate);
					atLeastOneXmpBasicSet = true;
				}
			} else if(biOld != null){
				Calendar old = biOld.getCreateDate();
				if(old != null){
					bi.setCreateDate(old);
					atLeastOneXmpBasicSet = true;
				}
			}
			
			if(basicEnabled.modifyDate){
				if(basic.modifyDate != null){
					bi.setModifyDate(basic.modifyDate);
					atLeastOneXmpBasicSet = true;
				}
			} else if(biOld != null){
				Calendar old = biOld.getModifyDate();
				if(old != null){
					bi.setModifyDate(old);
					atLeastOneXmpBasicSet = true;
				}
			} 
			
			if(basicEnabled.creatorTool){
				if(basic.creatorTool != null){
					bi.setCreatorTool(basic.creatorTool);
					atLeastOneXmpBasicSet = true;
				}
			} else if(biOld != null){
				String old = biOld.getCreatorTool();
				if(old != null){
					bi.setCreatorTool(old);
					atLeastOneXmpBasicSet = true;
				}
			}
			
			if(basicEnabled.identifiers){
				if (basic.identifiers != null) {
					for (String i : basic.identifiers) {
						bi.addIdentifier(i);
						atLeastOneXmpBasicSet = true;
					}
				}
			} else if(biOld != null){
				List<String> old=biOld.getIdentifiers();
				if(old != null){
					for(String a: old){
						bi.addIdentifier(a);
						atLeastOneXmpBasicSet = true;
					}
				}
			}
			
			if(basicEnabled.label){
				if(basic.label != null){
					bi.setLabel(basic.label);
					atLeastOneXmpBasicSet = true;
				}
			} else if(biOld != null){
				String old = biOld.getLabel();
				if(old != null){
					bi.setLabel(old);
					atLeastOneXmpBasicSet = true;
				}
			}
			
			if(basicEnabled.metadataDate){
				if(basic.metadataDate != null){
					bi.setMetadataDate(basic.metadataDate);
					atLeastOneXmpBasicSet = true;
				}
			} else if(biOld != null){
				Calendar old = biOld.getMetadataDate();
				if(old != null){
					bi.setMetadataDate(old);
					atLeastOneXmpBasicSet = true;
				}
			}
			
			if(basicEnabled.nickname){
				if(basic.nickname != null){
					bi.setNickname(basic.nickname);
					atLeastOneXmpBasicSet = true;
				}
			} else if(biOld != null){
				String old = biOld.getNickname();
				if(old != null){
					bi.setNickname(old);
					atLeastOneXmpBasicSet = true;
				}
			}
			if(basicEnabled.rating){
				if(basic.rating != null){
					bi.setRating(basic.rating);
					atLeastOneXmpBasicSet = true;
				}
			} else if(biOld != null){
				Integer old = biOld.getRating();
				if(old != null){
					bi.setRating(old);
					atLeastOneXmpBasicSet = true;
				}
			}
		}
		// XMP PDF
		XMPSchemaPDF piOld = xmpOld != null ? xmpOld.getPDFSchema() : null;
		boolean atLeastOneXmpPdfSet = false;
		if(pdfEnabled.atLeastOne() || (piOld != null)){
			XMPSchemaPDF pi = xmpNew.addPDFSchema();

			if(pdfEnabled.keywords){
				if(pdf.keywords != null){
					pi.setKeywords(pdf.keywords);
					atLeastOneXmpPdfSet = true;
				}
			} else if(piOld != null){
				String old = piOld.getKeywords();
				if(old != null){
					pi.setKeywords(old);
					atLeastOneXmpPdfSet = true;
				}
			}
			
			if(pdfEnabled.producer){
				if(pdf.producer != null){
					pi.setProducer(pdf.producer);
					atLeastOneXmpPdfSet = true;
				}
			} else if(piOld != null){
				String old = piOld.getProducer();
				if(old != null){
					pi.setProducer(old);
					atLeastOneXmpPdfSet = true;
				}
			}
			
			if(pdfEnabled.pdfVersion){
				if(pdf.pdfVersion != null){
					pi.setPDFVersion(pdf.pdfVersion);
					atLeastOneXmpPdfSet = true;
				}
			} else if(piOld != null){
				String old = piOld.getPDFVersion();
				if(old != null){
					pi.setPDFVersion(old);
					atLeastOneXmpPdfSet = true;
				}
			}
		}
		
		// XMP Dublin Core
		XMPSchemaDublinCore dcOld = xmpOld != null ? xmpOld.getDublinCoreSchema() : null;
		boolean atLeastOneXmpDcSet = false;
		if(dcEnabled.atLeastOne() || (dcOld != null)){
			XMPSchemaDublinCore dcS = xmpNew.addDublinCoreSchema();
			
			if(dcEnabled.title){
				if(dc.title != null){
					dcS.setTitle(dc.title);
					atLeastOneXmpDcSet = true;
				}
			} else if(dcOld != null){
				String old = dcOld.getTitle();
				if(old != null){
					dcS.setTitle(old);
					atLeastOneXmpDcSet = true;
				}
			}
			// 
			if(dcEnabled.contributors){
				if(dc.contributors != null){
					for (String i : dc.contributors) {
						dcS.addContributor(i);
						atLeastOneXmpDcSet = true;
					}
				}
			}else if(dcOld != null){
				List<String> old= dcOld.getContributors();
				if(old != null){
					for(String a: old){
						dcS.addContributor(a);;
						atLeastOneXmpDcSet = true;
					}
				}
			}
			//
			if(dcEnabled.publishers){
				if(dc.publishers != null){
					for (String i : dc.publishers) {
						dcS.addPublisher(i);
						atLeastOneXmpDcSet = true;
					}
				}
			}else if(dcOld != null) {
				List<String> old= dcOld.getPublishers();
				if(old != null){
					for(String a: old){
						dcS.addPublisher(a);;
						atLeastOneXmpDcSet = true;
					}
				}
			}
			//
			if(dcEnabled.relationships){
				if(dc.relationships != null){
					for (String i : dc.relationships) {
						dcS.addRelation(i);
						atLeastOneXmpDcSet = true;
					}
				}
			}else if(dcOld != null) {
				List<String> old= dcOld.getRelationships();
				if(old != null){
					for(String a: old){
						dcS.addRelation(a);;
						atLeastOneXmpDcSet = true;
					}
				}
			}
			//
			if(dcEnabled.subjects){
				if(dc.subjects != null){
					for (String i : dc.subjects) {
						dcS.addSubject(i);
						atLeastOneXmpDcSet = true;
					}
				}
			} else if(dcOld != null) {
				List<String> old= dcOld.getSubjects();
				if(old != null){
					for(String a: old){
						dcS.addSubject(a);;
						atLeastOneXmpDcSet = true;
					}
				}
			}
			//
			if(dcEnabled.types){
				if (dc.types != null){
					for (String i : dc.types) {
						dcS.addType(i);
						atLeastOneXmpDcSet = true;
					}
				}
			} else if(dcOld != null) {
				List<String> old= dcOld.getTypes();
				if(old != null){
					for(String a: old){
						dcS.addType(a);;
						atLeastOneXmpDcSet = true;
					}
				}
			}
			//
			if(dcEnabled.languages){
				if(dc.languages != null){
					for (String i : dc.languages) {
						dcS.addLanguage(i);
						atLeastOneXmpDcSet = true;
					}
				}
			} else if(dcOld != null) {
				List<String> old= dcOld.getLanguages();
				if(old != null){
					for(String a: old){
						dcS.addLanguage(a);;
						atLeastOneXmpDcSet = true;
					}
				}
			}
			//
			if(dcEnabled.creators ){
				if (dc.creators != null) {
					for (String i : dc.creators) {
						dcS.addCreator(i);
						atLeastOneXmpDcSet = true;
					}
				}
			} else if(dcOld != null) {
				List<String> old= dcOld.getCreators();
				if(old != null){
					for(String a: old){
						dcS.addCreator(a);;
						atLeastOneXmpDcSet = true;
					}
				}
			}
			//
			if(dcEnabled.coverage){
				if(dc.coverage != null){
					dcS.setCoverage(dc.coverage);
					atLeastOneXmpDcSet = true;
				}
			} else if(dcOld != null){
				String old = dcOld.getCoverage();
				if(old != null){
					dcS.setCoverage(old);
					atLeastOneXmpDcSet = true;
				}
			}
			
			if(dcEnabled.format){
				if(dc.format != null){
					dcS.setFormat(dc.format);
					atLeastOneXmpDcSet = true;
				}
			} else if(dcOld != null){
				String old = dcOld.getFormat();
				if(old != null){
					dcS.setFormat(old);
					atLeastOneXmpDcSet = true;
				}
			}
			
			if(dcEnabled.identifier){
				if(dc.identifier != null){
					dcS.setIdentifier(dc.identifier);
					atLeastOneXmpDcSet = true;
				}
			} else if(dcOld != null){
				String old = dcOld.getIdentifier();
				if(old != null){
					dcS.setIdentifier(old);
					atLeastOneXmpDcSet = true;
				}
			}
			
			if(dcEnabled.rights){
				if(dc.rights != null){
					dcS.setRights(null, dc.rights);
					atLeastOneXmpDcSet = true;
				}
			} else if(dcOld != null){
				List<String> rll = dcOld.getRightsLanguages();
				if(rll != null){
					for(String rl: rll){
						String rights = dcOld.getRights(rl);
						if(rights != null){
							dcS.setRights(rl, rights);
							atLeastOneXmpDcSet = true;
						}
					}
				}
			}
			
			if(dcEnabled.source){
				if(dc.source != null){
					dcS.setSource(dc.source);
					atLeastOneXmpDcSet = true;
				}
			} else if(dcOld != null){
				String old = dcOld.getSource();
				if(old != null){
					dcS.setSource(old);
					atLeastOneXmpDcSet = true;
				}
			}
			
			if(dcEnabled.description){
				if(dc.description != null){
					dcS.setDescription(dc.description);
					atLeastOneXmpDcSet = true;
				}
			} else if(dcOld != null){
				String old = dcOld.getDescription();
				if(old != null){
					dcS.setDescription(old);
					atLeastOneXmpDcSet = true;
				}
			}
			
			if(dcEnabled.dates){
				if (dc.dates != null) {
					for (Calendar date : dc.dates) {
						dcS.addDate(date);
						atLeastOneXmpDcSet = true;
					}
				}
			} else if(dcOld != null) {
				List<Calendar> old= dcOld.getDates();
				if(old != null){
					for(Calendar a: old){
						dcS.addDate(a);;
						atLeastOneXmpDcSet = true;
					}
				}
			}
		}
		
		// XMP Rights
		XMPSchemaRightsManagement riOld = xmpOld != null ? xmpOld.getRightsManagementSchema() : null;
		boolean atLeastOneXmpRightsSet = false;
		if(rightsEnabled.atLeastOne() || (riOld != null)){
			XMPSchemaRightsManagement ri = xmpNew.addRightsManagementSchema();
			
			if(rightsEnabled.certificate){
				if(rights.certificate != null){
					ri.setCertificateURL(rights.certificate);;
					atLeastOneXmpRightsSet = true;
				}
			} else if(riOld != null){
				String old = riOld.getCertificateURL();
				if(old != null){
					ri.setCertificateURL(old);
					atLeastOneXmpRightsSet = true;
				}
			}

			if(rightsEnabled.marked){
				if(rights.marked != null){
					ri.setMarked(rights.marked);;
					atLeastOneXmpRightsSet = true;
				}
			} else if(riOld != null){
				Boolean old = riOld.getMarked();
				if(old != null){
					ri.setMarked(old);
					atLeastOneXmpRightsSet = true;
				}
			}
			
			if(rightsEnabled.owner ){
				if (rights.owner != null) {
					for (String i : rights.owner) {
						ri.addOwner(i);
						atLeastOneXmpRightsSet = true;
					}
				}
			} else if(riOld != null) {
				List<String> old= riOld.getOwners();
				if(old != null){
					for(String a: old){
						ri.addOwner(a);
						atLeastOneXmpRightsSet = true;
					}
				}
			}

			if(rightsEnabled.copyright){
				if(rights.copyright != null){
					ri.setCopyright(rights.copyright);;
					atLeastOneXmpRightsSet = true;
				}
			} else if(riOld != null){
				String old = riOld.getCopyright();
				if(old != null){
					ri.setCopyright(old);
					atLeastOneXmpRightsSet = true;
				}
			}
			
			if(rightsEnabled.usageTerms){
				if(rights.usageTerms != null){
					ri.setUsageTerms(rights.usageTerms);
					atLeastOneXmpRightsSet = true;
				}
			} else if(riOld != null){
				String old = riOld.getUsageTerms();
				if(old != null){
					ri.setUsageTerms(old);
					atLeastOneXmpRightsSet = true;
				}
			}

			if(rightsEnabled.webStatement){
				if(rights.webStatement != null){
					ri.setWebStatement(rights.webStatement);
					atLeastOneXmpRightsSet = true;
				}
			} else if(riOld != null){
				String old = riOld.getWebStatement();
				if(old != null){
					ri.setWebStatement(old);
					atLeastOneXmpRightsSet = true;
				}
			}

		}

		// Do the save
		if( basicEnabled.atLeastOne() || pdfEnabled.atLeastOne() || dcEnabled.atLeastOne() || rightsEnabled.atLeastOne() ||
				atLeastOneXmpBasicSet || atLeastOneXmpPdfSet || atLeastOneXmpDcSet || atLeastOneXmpRightsSet){
			PDMetadata metadataStream = new PDMetadata(document);
			try {
				metadataStream.importXMPMetadata(xmpNew.asByteArray());
			} catch (TransformerException e) {
				throw new Exception("Failed to save document:" + e.getMessage());
			}
			catalog.setMetadata(metadataStream);
		}
		document.save(pdfFile.getAbsolutePath());
	}
	
	public void saveAsPDF(File pdfFile) throws Exception {
		saveAsPDF(pdfFile, null);
	}
	
	public void saveAsPDF(File pdfFile, File newFile) throws Exception {
		PDDocument document = null;

		FileInputStream inputStream = new FileInputStream(pdfFile);
		document = PDDocument.load(inputStream);

		saveToPDF(document, newFile == null ? pdfFile : newFile);

		if (document != null) {
			try {
				document.close();
			} catch (Exception e) {

			}
		}
		inputStream.close();
	}

	public void copyDocToXMP() {
		pdf.keywords = doc.keywords;
		pdf.producer = doc.producer;
		pdfEnabled.keywords = docEnabled.keywords;
		pdfEnabled.producer = docEnabled.producer;
		
		basic.createDate = doc.creationDate;
		basic.modifyDate = doc.modificationDate;
		basicEnabled.createDate = docEnabled.creationDate;
		basicEnabled.modifyDate = docEnabled.modificationDate;

		basic.creatorTool = doc.creator;
		basicEnabled.creatorTool = docEnabled.creator;

		dc.title = doc.title;
		dc.description = doc.subject;
		dc.creators = Arrays.asList(new String[] { doc.author });
		dcEnabled.title = docEnabled.title;
		dcEnabled.description = docEnabled.subject;
		dcEnabled.creators = docEnabled.author;
	}

	public void copyXMPToDoc() {
		doc.keywords = pdf.keywords;
		doc.producer = pdf.producer;
		docEnabled.keywords = pdfEnabled.keywords;
		docEnabled.producer = pdfEnabled.producer;

		doc.creationDate= basic.createDate;
		doc.modificationDate = basic.modifyDate;
		docEnabled.creationDate = basicEnabled.createDate;
		docEnabled.modificationDate = basicEnabled.modifyDate;
		
		
		doc.creator = basic.creatorTool;
		docEnabled.creator = basicEnabled.creatorTool;

		doc.title = dc.title;
		doc.subject = dc.description;
		String author = "";
		if( dc.creators != null){
			String delim = "";
			for(String creator: dc.creators){
				author += delim + creator;
				delim = ", ";
			}
		} else {
			author = null;
		}
		doc.author = author;
		docEnabled.title = dcEnabled.title;
		docEnabled.subject = dcEnabled.description;
		docEnabled.author = dcEnabled.creators;

	}
		
	public void setEnabled(boolean value){
		docEnabled.setAll(value);
		basicEnabled.setAll(value);
		pdfEnabled.setAll(value);
		dcEnabled.setAll(value);
		rightsEnabled.setAll(value);
	}
	
	public void setEnabled(String id, boolean value) {
		_setObjectEnabled(id, value);
	}
	public boolean isEnabled(String id) {
		return _getObjectEnabled(id);
	}

	public static List<String> keys(){
		return new ArrayList<String>(_mdFields.keySet());
	}

	public static boolean keyIsWritable(String key){
		FieldDescription fd = getFieldDescription(key);
		return (fd != null) ? fd.isWritable :  false;
	}
	
	public <T> Map<String, T> asFlatMap(Function<Object, T> convertor) {
		LinkedHashMap<String, T> map = new LinkedHashMap<String, T>();

		for (String fieldName : keys()) {
				Object o = get(fieldName);
				map.put(fieldName ,convertor.apply(o));
		}
		return map;
	}

	public Map<String, Object> asFlatMap() {
		return asFlatMap(new Function<Object, Object>() {
			@Override
			public Object apply(Object t) {
				return t;
			}
		});
	}

	public Map<String, String> asFlatStringMap() {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();

		for (String fieldName : keys()) {
				map.put(fieldName ,getString(fieldName));
		}
		return map;
	}

	
	public void  fromFlatMap(Map<String, Object> map, Function<Object, Object> convertor) {
		for (String fieldName : keys()) {
			if(map.containsKey(fieldName)){
				set(fieldName, convertor.apply(map.get(fieldName)));
			}
		}
	}

	public MetadataInfo clone(){
		MetadataInfo md = new MetadataInfo();
		md.copyFrom(this);
		return md;
	}

	public void copyFrom(MetadataInfo other){
		for (String fieldName : keys()) {
			set(fieldName, other.get(fieldName));
		}
	}
	
	public void copyUnset(MetadataInfo other){
		for (String fieldName : keys()) {
			Object o = get(fieldName);
			if( o == null){
				set(fieldName, other.get(fieldName));
			}
		}		
	}

	public void copyUnsetExpanded(MetadataInfo other, MetadataInfo expandInfo){
		for (String fieldName : keys()) {
			Object o = get(fieldName);
			if( o == null){
				Object otherVal = other.get(fieldName);
				if (otherVal instanceof String) {
					TemplateString ts = new TemplateString((String)otherVal);
					otherVal = ts.process(expandInfo);
				}
				set(fieldName, otherVal);
			}
		}		
	}

	public void expand(MetadataInfo expandInfo){
		for (String fieldName : keys()) {
			Object o = get(fieldName);
			if( o != null){
				Object expandedVal = get(fieldName);
				if (expandedVal instanceof String) {
					TemplateString ts = new TemplateString((String)expandedVal);
					expandedVal = ts.process(expandInfo);
				}
				set(fieldName, expandedVal);
			}
		}		
	}
	
	public void enableOnlyNonNull(){
		Map<String, Object> values = asFlatMap();
		docEnabled.setAll(false);
		basicEnabled.setAll(false);
		pdfEnabled.setAll(false);
		dcEnabled.setAll(false);
		rightsEnabled.setAll(false);
		for(Map.Entry<String, Object> entry: values.entrySet()){
			if( entry.getValue() != null){
				setEnabled(entry.getKey(), true);
			}
		}
		
	}
	
	public String toJson() {
		return toJson(0);
	}
	
	public String toJson(int indent) {
		Map<String, Object> map = asFlatMap(new Function<Object, Object>() {
			@Override
			public Object apply(Object t) {
				if (t != null){
					if(t instanceof Calendar) {
						return DateFormat.formatDateTimeFull((Calendar)t);
					}
				}
				return t;
			}
		});
		StringBuilder sb = new StringBuilder();
		String istr=new String(new char[indent]).replace("\0", " ");
		sb.append("{");
		if(indent > 0){
			sb.append("\n");
		}
		Set<String> keySet = map.keySet(); 
		int count = 0;
		for(String key: keySet){
			sb.append(istr);
			sb.append('"');
			sb.append(JSONObject.escape(key));
			sb.append("\":");
			if(indent > 0){
				sb.append(" ");
			}
			Object val = map.get(key);
			sb.append(JSONValue.toJSONString(val));
			if(++count < keySet.size()){
				sb.append(",");
			}
			if(indent > 0){
				sb.append("\n");
			}
		}
		sb.append("}");
		return sb.toString();
	}
	
	public String toYAML() {
		return toYAML(false);
	}
	public String toYAML(boolean pretty) {
		DumperOptions options = new DumperOptions();
		if(!pretty){
			options.setWidth(0xFFFF);
		}
		Yaml yaml = new Yaml(options);
		return yaml.dump(asFlatMap());
	}

	public void fromYAML(String yamlString) {
		Yaml yaml = new Yaml();
		Map<String, Object> map = (Map<String, Object>) yaml.load(yamlString);
		fromFlatMap(map, new Function<Object, Object>() {
			@Override
			public Object apply(Object t) {
				if(t instanceof Date){
					  Calendar cal = Calendar.getInstance();
					  cal.setTime((Date)t);
					  return cal;				
				}
				return t;
			}
		});
	}
	
	public boolean isEquivalent(MetadataInfo other){
 		for(Entry<String, List<FieldDescription>> e: _mdFields.entrySet()){
 			// Skip file.* fields, as they are read only and come from file metadata
 			if(e.getKey().startsWith("file.")){
 				continue;
 			}
			// Skip "dc.dates" for now as loading them from PDF is broken in xmpbox <= 2.0.2
			//if("dc.dates".equals(e.getKey())){
			//	continue;
			//}
 			// Skip "basic.label" for now as it cannot be loaded in jempbox <= 1.8.12
			if("basic.label".equals(e.getKey())){
				continue;
			}
			Object t = get(e.getKey());
			Object o = other.get(e.getKey());
			FieldDescription fd = e.getValue().get(e.getValue().size()-1);
			if(t == null){
				if(o == null){
					continue;
				}else {
					return false;
				}
			}
			if( fd.isList && (fd.type == FieldID.FieldType.DateField)){
				List<Calendar> tl = (List<Calendar>) t;
				List<Calendar> ol = (List<Calendar>) o;
				if(tl.size() != ol.size()){
					return false;
				}
				for(int i=0; i< tl.size(); ++i) {
					Calendar tc = tl.get(i);
					Calendar oc = ol.get(i);
					if(tc == null){
						if(oc == null){
							continue;
						}else {
							return false;
						}
					}
					if( (tc.getTimeInMillis()/1000) != (oc.getTimeInMillis()/1000) ){
						return false;
					}
				}
					
			} else if(t instanceof Calendar && o instanceof Calendar){
				if( (((Calendar)t).getTimeInMillis()/1000) != (((Calendar)o).getTimeInMillis()/1000) ){
					return false;
				}
			}else if( !t.equals(o) ) {
				return false;
			}
		}
		return true;
	}
	
	public String asPersistenceString(){
		Map<String, Object> map = asFlatMap();
		// Don't store null values as they are the default
		for(String key: _mdFields.keySet()){
			if( map.get(key) == null){
				map.remove(key);
			}
		}
		Map<String, Boolean> enabledMap = new LinkedHashMap<String, Boolean>();
		// Don't store true values as they are the default
		for(String keyEnabled: _mdEnabledFields.keySet()){
			if(!isEnabled(keyEnabled)){
				enabledMap.put(keyEnabled, false);
			}
		}
		if(enabledMap.size() > 0) {
			map.put("_enabled", enabledMap);
		}
		
		DumperOptions options = new DumperOptions();

		options.setWidth(0xFFFF);
		Yaml yaml = new Yaml(options);
		return yaml.dump(map);
	}
	
	public static MetadataInfo fromPersistenceString(String yamlString) {
		Yaml yaml = new Yaml();
		Map<String, Object> map = (Map<String, Object>) yaml.load(yamlString);
		MetadataInfo md =new MetadataInfo();
		md.fromYAML(yamlString);

		Object enMap = map.get("_enabled");
		if(enMap != null && Map.class.isAssignableFrom(enMap.getClass())) {
			Map<String, Object> enabledMap = (Map<String, Object>) enMap;
			
			for (String fieldName : _mdEnabledFields.keySet()) {
				if(enabledMap.containsKey(fieldName)){
					md.setEnabled(fieldName, (Boolean) enabledMap.get(fieldName));
				}
			}
		}
		
		return md;
	}
	
	//////////////////////////////
	public static class FieldDescription {
		public final String name;
		final Field field;
		public final FieldID.FieldType type;
		public final boolean isList;
		public final boolean isWritable;
		public FieldDescription(String name, Field field, FieldID.FieldType type, boolean isWritable){
			this.name = name;
			this.field = field;
			this.type = type;
			this.isWritable = isWritable;
			isList = List.class.isAssignableFrom(field.getType());
		}

		public FieldDescription(String name, Field field, boolean isWritable){
			Class<?> klass = field.getType();
			if(Boolean.class.isAssignableFrom(klass)){
				this.type = FieldID.FieldType.BoolField;
			} else if(Calendar.class.isAssignableFrom(klass)){
				this.type = FieldID.FieldType.DateField;
			}else if(Integer.class.isAssignableFrom(klass)){
				this.type = FieldID.FieldType.IntField;
			}else if(Long.class.isAssignableFrom(klass)){
				this.type = FieldID.FieldType.LongField;
			} else  {
				this.type = FieldID.FieldType.StringField;
			}
			this.name = name;
			this.field = field;
			this.isWritable = isWritable;
			isList = List.class.isAssignableFrom(klass);
		}
		
		public String makeStringFromValue(Object value){
			if( value == null){
				return "";
			}
			if(isList){
				return ListFormat.humanReadable((List)value);
			} else if(type == FieldID.FieldType.DateField){
				return DateFormat.formatDateTime((Calendar)value);
			} else if(type == FieldID.FieldType.BoolField){
				return ((Boolean)value) ? "true" : "false";
			} else {
				return value.toString();
			}
		}
		
		public Object makeValueFromString(String value){
			if(value == null){
				return null;
			}
			if(isList){
				if(type == FieldID.FieldType.StringField){
					return Arrays.asList(value);
				} else if(type == FieldID.FieldType.TextField){
					return Arrays.asList(value.split("\n"));
				} else if(type == FieldID.FieldType.IntField ){
					// TODO: possible allow comma separated interger list
					return Arrays.asList(Integer.parseInt(value));
				} else if(type == FieldID.FieldType.BoolField ){
					// TODO: possible allow comma separated boolean list
					String v = value.toLowerCase().trim();
					Boolean b = null;
					if (v.equals("true") || v.equals("yes")) b = true;
					if (v.equals("false") || v.equals("no")) b =  false;
					return Arrays.asList( b );
				} else if(type == FieldID.FieldType.DateField){
					List<Calendar> rval = new ArrayList<Calendar>();
					for(String line:value.split("\n")){
						try {
							rval.add(DateFormat.parseDate(line.trim()));
						} catch (ParseError e) {
							throw new RuntimeException("makeValueFromString() Invalid date format:" + line);
						}
					}
					return rval;
				}
			} else {
				if(type == FieldID.FieldType.StringField){
					return value;
				} else if(type == FieldID.FieldType.TextField){
					return value;
				} else if(type == FieldID.FieldType.IntField ){
					return Integer.parseInt(value);
				} else if(type == FieldID.FieldType.BoolField ){
					String v = value.toLowerCase().trim();
					if (v.equals("true") || v.equals("yes")) return true;
					if (v.equals("false") || v.equals("no")) return false;
					return null;
				} else if(type == FieldID.FieldType.DateField){
					try {
						return DateFormat.parseDate(value);
					} catch (ParseError e) {
						throw new RuntimeException("makeValueFromString() Invalid date format:" + value);
					}
				}
			}
			throw new RuntimeException("makeValueFromString() :Don't know how to convert to type:" + type);
		}
	}
	protected static void traverseFields(List<FieldDescription> ancestors, boolean all, Class<?> klass, MdStruct.StructType mdType,  Function<List<FieldDescription>, Void> f) {
		for (Field field : klass.getFields()) {
			MdStruct mdStruct = field.getAnnotation(MdStruct.class);
			if (mdStruct != null && mdStruct.type() == mdType) {
				String prefix = ancestors.size() > 0 ? ancestors.get(ancestors.size()-1).name : "";
				if(prefix.length() > 0){
					prefix += ".";
				}
				String name = mdStruct.name().length() > 0 ? mdStruct.name() : field.getName();
				FieldDescription t = new FieldDescription(prefix + name, field, null, mdStruct.access() == MdStruct.Access.ReadWrite);
				List<FieldDescription> a = new ArrayList<FieldDescription>(ancestors);
				a.add(t);
				traverseFields(a, true, field.getType(), mdType, f);
			} else {
				FieldID fieldId = field.getAnnotation(FieldID.class);
				boolean isParentWritable = (ancestors.size() > 0) ? ancestors.get(ancestors.size()-1).isWritable : true;
				if(fieldId != null){
					String prefix = ancestors.size() > 0 ? ancestors.get(ancestors.size()-1).name : "";
					if(prefix.length() > 0){
						prefix += ".";
					}
					FieldDescription t =new FieldDescription(prefix + fieldId.value(), field, fieldId.type(), isParentWritable);
					List<FieldDescription> a = new ArrayList<FieldDescription>(ancestors);
					a.add(t);
					f.apply(a);
				} else if( all ) {
					String prefix = ancestors.size() > 0 ? ancestors.get(ancestors.size()-1).name : "";
					if(prefix.length() > 0){
						prefix += ".";
					}
					FieldDescription t =new FieldDescription(prefix + field.getName(), field, isParentWritable);
					List<FieldDescription> a = new ArrayList<FieldDescription>(ancestors);
					a.add(t);
					f.apply(a);
				}
			}
		}
	}
	
	final static Map<String, List<FieldDescription>> _mdFields;
	final static Map<String, List<FieldDescription>> _mdEnabledFields;
	static {
		_mdFields = new LinkedHashMap<String, List<FieldDescription>>();
		_mdEnabledFields = new LinkedHashMap<String, List<FieldDescription>>();
		traverseFields(new ArrayList<FieldDescription>(), false, MetadataInfo.class, StructType.MdStruct, new Function<List<FieldDescription>, Void>() {
			@Override
			public Void apply(List<FieldDescription> t) {
				if(t.size()>0){
					_mdFields.put(t.get(t.size()-1).name, t);
				}
				return null;
			}
		});
		traverseFields(new ArrayList<FieldDescription>(), false, MetadataInfo.class, StructType.MdEnableStruct, new Function<List<FieldDescription>, Void>() {
			@Override
			public Void apply(List<FieldDescription> t) {
				if(t.size()>0){
					_mdEnabledFields.put(t.get(t.size()-1).name, t);
				}
				return null;
			}
		});
	}
	
	protected Object _getStructObject(String id,  Map<String, List<FieldDescription>> mdFields, boolean parent, boolean toString, boolean useDefault, Object defaultValue) {
		List<FieldDescription> fields = mdFields.get(id);
		if(fields == null || fields.size() == 0){
			if(useDefault){
				return defaultValue;
			}
			throw new RuntimeException("_getStructObject('"+id+"') No such field");
		}
		Object current = this;
		FieldDescription fieldD = null;
		for(int i=0 ; i < fields.size() - (parent?1:0); ++i){
			try {
				fieldD = fields.get(i);
				current = fieldD.field.get(current);
			} catch (IllegalArgumentException e) {
				if(useDefault){
					return defaultValue;
				}
				throw new RuntimeException("_getStructObject('"+id+"') IllegalArgumentException:" + e.toString());
			} catch (IllegalAccessException e) {
				if(useDefault){
					return defaultValue;
				}
				throw new RuntimeException("_getStructObject('"+id+"') IllegalAccessException" + e.toString());
			}
		}
		if(toString){
			return fieldD.makeStringFromValue(current);
		}
		return current;
	}
	public Object get(String id) {
		return _getStructObject(id, _mdFields, false, false, false, null);
	}
	public String getString(String id) {
		return (String) _getStructObject(id, _mdFields, false, true, false, null);
	}

	public Object get(String id, Object defaultValue) {
		return _getStructObject(id, _mdFields, false, false, true, defaultValue);
	}
	public String getString(String id, String defaultValue) {
		return (String) _getStructObject(id, _mdFields, false, true, true, defaultValue);
	}

	protected boolean _getObjectEnabled(String id) {
		return (Boolean)_getStructObject(id, _mdEnabledFields, false, false, true, false);
	}

	protected void _setStructObject(String id,  Object value, boolean append, boolean fromString, Map<String, List<FieldDescription>> mdFields) {
		List<FieldDescription> fields = mdFields.get(id);
		if(fields == null || fields.size() == 0){
			throw new RuntimeException("_setStructObject('"+id+"') No such field");
		}
		Object current = _getStructObject(id, mdFields, true, false, false, null);
		if(current == null){
			throw new RuntimeException("_setStructObject('"+id+"') No such field");
		}
		try {
			FieldDescription fieldD = fields.get(fields.size()-1);
			if(fromString && (value != null)){
				value = fieldD.makeValueFromString(value.toString());
			}
			if(fieldD.isList && append){
				List<Object> l = (List<Object>) fieldD.field.get(current);
				if(l == null){
					l = new ArrayList<Object>();
				} 
				if(List.class.isAssignableFrom(value.getClass())){
					l.addAll((List)value);
				} else {
					l.add(value);
				}
				fieldD.field.set(current, l);
			} else {
				fieldD.field.set(current, value);
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("_setStructObject('"+id+"') IllegalArgumentException:" + e.toString());
		} catch (IllegalAccessException e) {
			throw new RuntimeException("_setStructObject('"+id+"') IllegalAccessException" + e.toString());
		}
	}

	public void set(String id, Object value) {
		_setStructObject(id, value, false, false, _mdFields);
	}
	public void setAppend(String id, Object value) {
		_setStructObject(id, value, true, false, _mdFields);
	}

	public void setFromString(String id, String value) {
		_setStructObject(id, value, false, true, _mdFields);
	}
	public void setAppendFromString(String id, String value) {
		_setStructObject(id, value, true, true, _mdFields);
	}

	protected void _setObjectEnabled(String id, boolean value) {
		_setStructObject(id, value, false, false, _mdEnabledFields);
	}

	public static FieldDescription getFieldDescription(String id){
		List<FieldDescription> fields = _mdFields.get(id);
		if( fields.size() > 0){
			return fields.get(fields.size()-1);
		}
		return null;
	}
	//////////////////////////////
	
	public static MetadataInfo getSampleMetadata(){
		MetadataInfo md = new MetadataInfo();
		// Spec is at : http://partners.adobe.com/public/developer/en/xmp/sdk/XMPspecification.pdf
		md.doc.title = "Dracula"; 
		md.doc.author = "Bram Stoker";
		md.doc.subject = "Horror tales, Epistolary fiction, Gothic fiction (Literary genre), Vampires -- Fiction, Dracula, Count (Fictitious character) -- Fiction, Transylvania (Romania) -- Fiction, Whitby (England) -- Fiction"; 
		md.doc.keywords = "Horror, Gothic, Vampires";
		md.doc.creator = "Adobe InDesign CS4 (6.0.6)";
		md.doc.producer = "Adobe PDF Library 9.0";
		md.doc.creationDate = DateFormat.parseDateOrNull("2012-12-12 00:00:00");
		md.doc.modificationDate = DateFormat.parseDateOrNull("2012-12-13 00:00:00");
		md.doc.trapped = "True";

		md.basic.creatorTool = "Adobe InDesign CS4 (6.0.6)";
		md.basic.createDate = md.doc.creationDate;
		md.basic.modifyDate = md.doc.modificationDate;
		md.basic.baseURL = "https://www.gutenberg.org/";
		md.basic.rating = 3; 
		md.basic.label = "Horror Fiction Collection";
		md.basic.nickname = "dracula";
		md.basic.identifiers = Arrays.asList("Dracula_original_edition");
		//md.xmpBasic.advisories ; 
		md.basic.metadataDate = DateFormat.parseDateOrNull("2012-12-14 00:00:00");

		md.pdf.pdfVersion = "1.5"; 
		md.pdf.keywords = md.doc.keywords;
		md.pdf.producer = "Adobe PDF Library 9.0";

		md.dc.title = md.doc.title;
		md.dc.description = "The famous Bram Stocker book"; 
		md.dc.creators = new ArrayList<String>();
		md.dc.creators.add("Bram Stocker");
		md.dc.subjects = Arrays.asList(md.doc.subject.split("\\s*,\\s*"));
		
		return md;
	}


}
