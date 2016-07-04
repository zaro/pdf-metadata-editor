package pmedit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.transform.TransformerException;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.jempbox.xmp.XMPSchemaPDF;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class MetadataInfo {

	protected static final String[] MD_FIELD_GROUPS = new String[] { "basic", "xmpBasic", "xmpPdf",
			"xmpDc" };

	// Copy of java.util.functions.Function from Java8
	public interface Function<T, R> {
	    R apply(T t);
	}

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

		public boolean atLaestOne() {
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
		public String title;
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
		public boolean title = true;
		public boolean baseURL = true;
		public boolean rating = true;
		public boolean label = true;
		public boolean nickname = true;
		public boolean identifiers = true;
		public boolean advisories = true;
		public boolean metadataDate = true;

		public boolean atLeastOne() {
			return creatorTool || createDate || modifyDate || title || baseURL || rating || label || nickname
					|| identifiers || advisories || metadataDate;
		}
		
		public void setAll(boolean value){
			creatorTool = value;
			createDate = value;
			modifyDate = value;
			title = value;
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
	public Basic basic ;
	public XmpBasic xmpBasic ;
	public XmpPdf xmpPdf ;
	public XmpDublinCore xmpDc;

	public BasicEnabled basicEnabled ;
	public XmpBasicEnabled xmpBasicEnabled ;
	public XmpPdfEnabled xmpPdfEnabled ;
	public XmpDublinCoreEnabled xmpDcEnabled;

	public MetadataInfo() {
		super();
		clear();
	}
	
	public void clear() {
		this.basic =  new Basic();
		this.xmpBasic = new XmpBasic();
		this.xmpPdf = new XmpPdf();
		this.xmpDc  = new XmpDublinCore();	
		
		this.basicEnabled = new BasicEnabled();
		this.xmpBasicEnabled = new XmpBasicEnabled();
		this.xmpPdfEnabled = new XmpPdfEnabled();
		this.xmpDcEnabled = new XmpDublinCoreEnabled();
	}

	public void loadFromPDF(PDDocument document) throws IOException {
		PDDocumentInformation info = document.getDocumentInformation();

		// Basic info
		basic.title = info.getTitle();
		basic.author = info.getAuthor();
		basic.subject = info.getSubject();
		basic.keywords = info.getKeywords();
		basic.creator = info.getCreator();
		basic.producer = info.getProducer();
		basic.creationDate = info.getCreationDate();
		basic.modificationDate = info.getModificationDate();
		basic.trapped = info.getTrapped();

		// Load XMP catalog
		PDDocumentCatalog catalog = document.getDocumentCatalog();
		PDMetadata metadata = catalog.getMetadata();
		if (metadata != null) {
			// XMP Basic
			XMPMetadata xmp = XMPMetadata.load(metadata.createInputStream());
			XMPSchemaBasic bi = xmp.getBasicSchema();
			if (bi != null) {

				xmpBasic.creatorTool = bi.getCreatorTool();
				xmpBasic.createDate = bi.getCreateDate();
				xmpBasic.modifyDate = bi.getModifyDate();
				xmpBasic.title = bi.getTitle();
				xmpBasic.baseURL = bi.getBaseURL();
				xmpBasic.rating = bi.getRating();
				xmpBasic.label = bi.getLabel();
				xmpBasic.nickname = bi.getNickname();
				xmpBasic.identifiers = bi.getIdentifiers();
				xmpBasic.advisories = bi.getAdvisories();
				xmpBasic.metadataDate = bi.getMetadataDate();
			}

			// XMP PDF
			XMPSchemaPDF pi = xmp.getPDFSchema();
			if (pi != null) {
				xmpPdf.pdfVersion = pi.getPDFVersion();
				xmpPdf.keywords = pi.getKeywords();
				xmpPdf.producer = pi.getProducer();
			}

			// XMP Dublin Core
			XMPSchemaDublinCore dc = xmp.getDublinCoreSchema();
			if (dc != null) {
				xmpDc.title = dc.getTitle();
				xmpDc.description = dc.getDescription();
				xmpDc.creators = dc.getCreators();
				xmpDc.contributors = dc.getContributors();
				xmpDc.coverage = dc.getCoverage();
				xmpDc.dates = dc.getDates();
				xmpDc.format = dc.getFormat();
				xmpDc.identifier = dc.getIdentifier();
				xmpDc.languages = dc.getLanguages();
				xmpDc.publishers = dc.getPublishers();
				xmpDc.relationships = dc.getRelationships();
				xmpDc.rights = dc.getRights();
				xmpDc.source = dc.getSource();
				xmpDc.subjects = dc.getSubjects();
				xmpDc.types = dc.getTypes();
			}
		}

		//System.err.println("Loaded:");
		//System.err.println(toYAML());

	}

	public void loadFromPDF(File pdfFile) throws FileNotFoundException,
			IOException {
		PDDocument document = null;

		document = PDDocument.load(new FileInputStream(pdfFile));

		loadFromPDF(document);

		if (document != null) {
			try {
				document.close();
			} catch (Exception e) {

			}
		}
	}

	public void saveToPDF(PDDocument document, File pdfFile) throws Exception {
		if(!(basicEnabled.atLaestOne() || xmpBasicEnabled.atLeastOne() || xmpPdfEnabled.atLeastOne() || xmpDcEnabled.atLeastOne())){
			return;
		}
		//System.err.println("Saving:");
		//System.err.println(toYAML());
		// Basic info
		if(basicEnabled.atLaestOne()){
			PDDocumentInformation info = document.getDocumentInformation();
			if(basicEnabled.title){
				info.setTitle(basic.title);
			}
			if(basicEnabled.author){
				info.setAuthor(basic.author);
			}
			if(basicEnabled.subject){
				info.setSubject(basic.subject);
			}
			if(basicEnabled.keywords){
				info.setKeywords(basic.keywords);
			}
			if(basicEnabled.creator){
				info.setCreator(basic.creator);
			}
			if(basicEnabled.producer){
				info.setProducer(basic.producer);
			}
			if(basicEnabled.creationDate){
				info.setCreationDate(basic.creationDate);
			}
			if(basicEnabled.modificationDate){
				info.setModificationDate(basic.modificationDate);
			}
			if(basicEnabled.trapped){
				info.setTrapped(basic.trapped);
			}
			document.setDocumentInformation(info);
		}
		
		// XMP
		PDDocumentCatalog catalog = document.getDocumentCatalog();
		PDMetadata metadata = catalog.getMetadata();
		XMPMetadata xmp = null;
		if (metadata != null) {
			xmp = XMPMetadata.load(metadata.createInputStream());
		}
		if (xmp == null) {
			xmp = new XMPMetadata();
		}
		// XMP Basic
		if(xmpBasicEnabled.atLeastOne()){
			XMPSchemaBasic bi = xmp.getBasicSchema();
			if (bi == null) {
				bi = xmp.addBasicSchema();
			}
			if(xmpBasicEnabled.advisories){
				if (bi.getAdvisories() != null) {
					for (String a : bi.getAdvisories()) {
						bi.removeAdvisory(a);
					}
				}
				if (xmpBasic.advisories != null) {
					for (String a : xmpBasic.advisories) {
						bi.addAdvisory(a);
					}
				}
			}
			if(xmpBasicEnabled.baseURL){
				bi.setBaseURL(xmpBasic.baseURL);
			}
			if(xmpBasicEnabled.createDate){
				bi.setCreateDate(null); //Workaround for some PDFs where date is not saved
				bi.setCreateDate(xmpBasic.createDate);
			}
			if(xmpBasicEnabled.modifyDate){
				bi.setModifyDate(null); //Workaround for some PDFs where date is not saved
				bi.setModifyDate(xmpBasic.modifyDate);
			}
			if(xmpBasicEnabled.creatorTool){
				bi.setCreatorTool(xmpBasic.creatorTool);
			}
			if(xmpBasicEnabled.identifiers){
				if (bi.getIdentifiers() != null) {
					for (String i : bi.getIdentifiers()) {
						bi.removeIdentifier(i);
					}
				}
				if (xmpBasic.identifiers != null) {
					for (String i : xmpBasic.identifiers) {
						bi.addIdentifier(i);
					}
				}
			}
			if(xmpBasicEnabled.label){
				bi.setLabel(xmpBasic.label);
			}
			if(xmpBasicEnabled.metadataDate){
				bi.setMetadataDate(null); //Workaround for some PDFs where date is not saved
				bi.setMetadataDate(xmpBasic.metadataDate);
			}
			if(xmpBasicEnabled.nickname){
				bi.setNickname(xmpBasic.nickname);
			}
			if(xmpBasicEnabled.rating){
				bi.setRating(xmpBasic.rating);
			}
			if(xmpBasicEnabled.title){
				bi.setTitle(xmpBasic.title);
			}
		}
		// XMP PDF
		if(xmpPdfEnabled.atLeastOne()){
			XMPSchemaPDF pi = xmp.getPDFSchema();
			if (pi == null) {
				pi = xmp.addPDFSchema();
			}
			if(xmpPdfEnabled.keywords){
				pi.setKeywords(xmpPdf.keywords);
			}
			if(xmpPdfEnabled.producer){
				pi.setProducer(xmpPdf.producer);
			}
			if(xmpPdfEnabled.pdfVersion){
				pi.setPDFVersion(xmpPdf.pdfVersion);
			}
		}
		// XMP Dublin Core
		if(xmpDcEnabled.atLeastOne()){
			XMPSchemaDublinCore dc = xmp.getDublinCoreSchema();
			if (dc == null) {
				dc = xmp.addDublinCoreSchema();
			}
			if(xmpDcEnabled.title){
				dc.setTitle(xmpDc.title);
			}
			// 
			if(xmpDcEnabled.contributors){
				if (dc.getContributors() != null) {
					for (String i : dc.getContributors()) {
						dc.removeContributor(i);
					}
				}
				if (xmpDc.contributors != null) {
					for (String i : xmpDc.contributors) {
						dc.addContributor(i);
					}
				}
			}
			//
			if(xmpDcEnabled.publishers){
				if (dc.getPublishers() != null) {
					for (String i : dc.getPublishers()) {
						dc.removePublisher(i);
					}
				}
				if (xmpDc.publishers != null) {
					for (String i : xmpDc.publishers) {
						dc.addPublisher(i);
					}
				}
			}
			//
			if(xmpDcEnabled.relationships){
				if (dc.getRelationships() != null) {
					for (String i : dc.getRelationships()) {
						dc.removeRelation(i);
					}
				}
				if (xmpDc.relationships != null) {
					for (String i : xmpDc.relationships) {
						dc.addRelation(i);
					}
				}
			}
			//
			if(xmpDcEnabled.subjects){
				if (dc.getSubjects() != null) {
					for (String i : dc.getSubjects()) {
						dc.removeSubject(i);
					}
				}
				if (xmpDc.subjects != null) {
					for (String i : xmpDc.subjects) {
						dc.addSubject(i);
					}
				}
			}
			if(xmpDcEnabled.types){
				// dc.removeType is undefined!
				//if(dc.getTypes() != null){
				//	for(String i: dc.getTypes()){
				//		dc.removeType(i);
				//	}
				//}
				if (xmpDc.types != null) {
					for (String i : xmpDc.types) {
						dc.addType(i);
					}
				}
			}
			//
			if(xmpDcEnabled.languages){
				if (dc.getLanguages() != null) {
					for (String i : dc.getLanguages()) {
						dc.removeLanguage(i);
					}
				}
				if (xmpDc.languages != null) {
					for (String i : xmpDc.languages) {
						dc.addLanguage(i);
					}
				}
			}
			//
			if(xmpDcEnabled.creators){
				if (dc.getCreators() != null) {
					for (String i : dc.getCreators()) {
						dc.removeCreator(i);
					}
				}
				if (xmpDc.creators != null) {
					for (String i : xmpDc.creators) {
						dc.addCreator(i);
					}
				}
			}
			//
			if(xmpDcEnabled.coverage){
				dc.setCoverage(xmpDc.coverage);
			}
			if(xmpDcEnabled.format){
				dc.setFormat(xmpDc.format);
			}
			if(xmpDcEnabled.identifier){
				dc.setIdentifier(xmpDc.identifier);
			}
			if(xmpDcEnabled.rights){
				dc.setRights(xmpDc.rights);
			}
			if(xmpDcEnabled.source){
				dc.setSource(xmpDc.source);
			}
			if(xmpDcEnabled.description){
				dc.setDescription(xmpDc.description);
			}
			if(xmpDcEnabled.dates){
				if (dc.getDates() != null) {
					for (Calendar date : dc.getDates()) {
						dc.removeDate(date);
					}
				}
				if (xmpDc.dates != null) {
					for (Calendar date : xmpDc.dates) {
						dc.addDate(date);
					}
				}
			}
		}
		// Do the save
		if( xmpBasicEnabled.atLeastOne() || xmpPdfEnabled.atLeastOne() || xmpDcEnabled.atLeastOne()){
			PDMetadata metadataStream = new PDMetadata(document);
			try {
				metadataStream.importXMPMetadata(xmp.asByteArray());
			} catch (TransformerException e) {
				throw new Exception("Failed to save document:" + e.getMessage());
			}
			catalog.setMetadata(metadataStream);
		}
		try {
			document.save(pdfFile.getAbsolutePath());
		} catch (COSVisitorException e) {
			throw new Exception("Failed to save document:" + e.getMessage());
		}

	}

	public void saveToPDF(File pdfFile) throws Exception {
		PDDocument document = null;

		document = PDDocument.load(new FileInputStream(pdfFile));

		saveToPDF(document, pdfFile);

		if (document != null) {
			try {
				document.close();
			} catch (Exception e) {

			}
		}

	}

	public void copyBasicToXMP() {
		xmpPdf.keywords = basic.keywords;
		xmpPdf.producer = basic.producer;
		xmpPdfEnabled.keywords = basicEnabled.keywords;
		xmpPdfEnabled.producer = basicEnabled.producer;
		

		xmpBasic.creatorTool = basic.creator;
		xmpBasicEnabled.creatorTool = basicEnabled.creator;

		xmpDc.title = basic.title;
		xmpDc.description = basic.subject;
		xmpDc.creators = Arrays.asList(new String[] { basic.author });
		xmpDcEnabled.title = basicEnabled.title;
		xmpDcEnabled.description = basicEnabled.subject;
		xmpDcEnabled.creators = basicEnabled.author;
	}

	public void copyXMPToBasic() {
		basic.keywords = xmpPdf.keywords;
		basic.producer = xmpPdf.producer;
		basicEnabled.keywords = xmpPdfEnabled.keywords;
		basicEnabled.producer = xmpPdfEnabled.producer;

		basic.creator = xmpBasic.creatorTool;
		basicEnabled.creator = xmpBasicEnabled.creatorTool;

		basic.title = xmpDc.title;
		basic.subject = xmpDc.description;
		String author = "";
		if( xmpDc.creators != null){
			String delim = "";
			for(String creator: xmpDc.creators){
				author += delim + creator;
				delim = ", ";
			}
		} else {
			author = null;
		}
		basic.author = author;
		basicEnabled.title = xmpDcEnabled.title;
		basicEnabled.subject = xmpDcEnabled.description;
		basicEnabled.author = xmpDcEnabled.creators;

	}

	private <T> T formatItem(T s) {
		return s;
	}

	private <T> String itemListToText(List<T> slist, String separator) {
		if (slist == null) {
			return null;
		}
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < slist.size(); ++i) {
			b.append(formatItem(slist.get(i)));
			if (i < (slist.size() - 1)) {
				b.append(separator);
			}
		}
		return b.toString();
	}


	public Map<String, Object> asFlatMap() {
		return asFlatMap(new Function<Object, Object>() {
			@Override
			public Object apply(Object t) {
				return t;
			}
		});
	}

    private static final SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	public Map<String, String> asFlatStringMap() {
		return asFlatMap(new Function<Object, String>() {
			@Override
			public String apply(Object t) {
				if (t != null){
					if(t instanceof Calendar) {
						return isoDateFormat.format(((Calendar)t).getTime());
					}
					return t.toString();
				}
				return null;
			}
		});
	}

	protected Object getObject(String id) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if (id.length() == 0 ) return null;
		StringTokenizer st = new StringTokenizer(id, ".");
		Object current = this;
		while (st.hasMoreTokens()) {
			Field group;
			group = current.getClass().getField(st.nextToken());
			current = group.get(current);

		}
		return current;
	}
	
	public Object get(String id) {
		try {
			return getObject(id);
		} catch (NoSuchFieldException e) {
			System.err.println("Metadata.get(" + id + ")");
			e.printStackTrace();
			return null;
		} catch (SecurityException e) {
			System.err.println("Metadata.get(" + id + ")");
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			System.err.println("Metadata.get(" + id + ")");
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			System.err.println("Metadata.get(" + id + ")");
			e.printStackTrace();
			return null;
		}
	
	}
	
	public String getString(String id){
		try {
			Object o = getObject(id);
			if (o != null){
				if (o instanceof List<?>) {
					return ",".join(",", ((List<String>) o));
				}
				if (o instanceof Calendar) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					return sdf.format(((Calendar)o).getTime());
				}
				return o.toString();
			}
		} catch (NoSuchFieldException e) {
		} catch (SecurityException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
		return null;
	
	}

	public void set(String id, Object value) {
		StringTokenizer st = new StringTokenizer(id, ".");
		Object current = this;
		while (current != null) {
			try {
				Field field = current.getClass().getField(st.nextToken());
				if (!st.hasMoreTokens()) {
					field.set(current, value);
					return;
				}
				current = field.get(current);
			} catch (NoSuchFieldException e) {
				System.err.println("Metadata.set(" + id + ", "+ value.toString() + ")");
				throw new RuntimeException(e);
			} catch (SecurityException e) {
				System.err.println("Metadata.set(" + id + ", "+ value.toString() + ")");
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				System.err.println("Metadata.set(" + id + ", "+ value.toString() + ")");
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				System.err.println("Metadata.set(" + id + ", "+ value.toString() + ")");
				throw new RuntimeException(e);
			}

		}
	}
	
	public void setAppend(String id, Object value) {
		StringTokenizer st = new StringTokenizer(id, ".");
		Object current = this;
		while (current != null) {
			try {
				Field field = current.getClass().getField(st.nextToken());
				if (!st.hasMoreTokens()) {
					if(field.getType().isAssignableFrom(List.class)){
						List<Object> l = (List<Object>) field.get(current);
						if(l != null){
							l.add(value);
						} else {
							l = new ArrayList<Object>();
							l.add(value);
							field.set(current, l);
						}
					} else {
						field.set(current, value);
					}
					return;
				}
				current = field.get(current);
			} catch (NoSuchFieldException e) {
				System.err.println("Metadata.set(" + id + ", "+ value.toString() + ")");
				throw new RuntimeException(e);
			} catch (SecurityException e) {
				System.err.println("Metadata.set(" + id + ", "+ value.toString() + ")");
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				System.err.println("Metadata.set(" + id + ", "+ value.toString() + ")");
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				System.err.println("Metadata.set(" + id + ", "+ value.toString() + ")");
				throw new RuntimeException(e);
			}

		}
	}
	
	public Class<?> getFieldType(String id){
		StringTokenizer st = new StringTokenizer(id, ".");
		Object current = this;
		while (current != null) {
			try {
				Field field = current.getClass().getField(st.nextToken());
				if (!st.hasMoreTokens()) {
					return field.getType();
				}
				current = field.get(current);
			} catch (NoSuchFieldException e) {
				System.err.println("Metadata.getFieldType(" + id + ")");
				throw new RuntimeException(e);
			} catch (SecurityException e) {
				System.err.println("Metadata.getFieldType(" + id + ")");
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				System.err.println("Metadata.getFieldType(" + id  + ")");
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				System.err.println("Metadata.getFieldType(" + id  + ")");
				throw new RuntimeException(e);
			}
		}
		return null;
	}
	
	public void setEnabled(boolean value){
		basicEnabled.setAll(value);
		xmpBasicEnabled.setAll(value);
		xmpPdfEnabled.setAll(value);
		xmpDcEnabled.setAll(value);
	}
	
	public void setEnabled(String id, boolean value) {
		StringTokenizer st = new StringTokenizer(id, ".");
		Object current = this;
		String name = st.nextToken() + "Enabled";
		while (current != null) {
			try {
				Field field = current.getClass().getField(name);
				if (!st.hasMoreTokens()) {
					field.set(current, value);
					return;
				}
				current = field.get(current);
				name = st.nextToken();
			} catch (NoSuchFieldException e) {
				System.err.println("Metadata.setEnabled(" + id + ", "+ value + ")");
				throw new RuntimeException(e);
			} catch (SecurityException e) {
				System.err.println("Metadata.setEnabled(" + id + ", "+ value + ")");
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				System.err.println("Metadata.setEnabled(" + id + ", "+ value + ")");
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				System.err.println("Metadata.setEnabled(" + id + ", "+ value + ")");
				throw new RuntimeException(e);
			}

		}
	}
	public boolean isEnabled(String id) {
		StringTokenizer st = new StringTokenizer(id, ".");
		Object current = this;
		String name = st.nextToken() + "Enabled";
		while (current != null) {
			try {
				Field field = current.getClass().getField(name);
				current = field.get(current);
				if (!st.hasMoreTokens()) {
					return (Boolean)current;
				}
				name = st.nextToken();
			} catch (NoSuchFieldException e) {
				System.err.println("Metadata.isEnabled(" + id + ")");
				throw new RuntimeException(e);
			} catch (SecurityException e) {
				System.err.println("Metadata.isEnabled(" + id + ")");
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				System.err.println("Metadata.isEnabled(" + id + ")");
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				System.err.println("Metadata.isEnabled(" + id + ")");
				throw new RuntimeException(e);
			}
		}
		return false;
	}

	public List<String> keys(){
		ArrayList<String> ret = new ArrayList<String>();
		for (String fieldName : MD_FIELD_GROUPS) {
			Field group;
			Object groupObj;
			try {
				Field[] f = this.getClass().getFields();
				group = this.getClass().getField(fieldName);
				groupObj = group.get(this);
			} catch (NoSuchFieldException e) {
				continue;
			} catch (SecurityException e) {
				continue;
			} catch (IllegalArgumentException e) {
				continue;
			} catch (IllegalAccessException e) {
				continue;
			}
			Class klass = group.getType();
			for (Field field : klass.getFields()) {
				try {
					ret.add( fieldName + "." + field.getName() );
				} catch (IllegalArgumentException e) {
				}
			}
		}
		return ret;
	}
	
	public <T> Map<String, T> asFlatMap(Function<Object, T> convertor) {
		LinkedHashMap<String, T> map = new LinkedHashMap<String, T>();

		for (String fieldName : MD_FIELD_GROUPS) {
			Field group;
			Object groupObj;
			try {
				Field[] f = this.getClass().getFields();
				group = this.getClass().getField(fieldName);
				groupObj = group.get(this);
			} catch (NoSuchFieldException e) {
				continue;
			} catch (SecurityException e) {
				continue;
			} catch (IllegalArgumentException e) {
				continue;
			} catch (IllegalAccessException e) {
				continue;
			}
			Class klass = group.getType();
			for (Field field : klass.getFields()) {
				try {
					Object o = field.get(groupObj);
					map.put(fieldName + "." + field.getName(),
							convertor.apply(o));
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
			}
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
	
	public void enableOnlyNonNull(){
		Map<String, Object> values = asFlatMap();
		basicEnabled.setAll(false);
		xmpBasicEnabled.setAll(false);
		xmpPdfEnabled.setAll(false);
		xmpDcEnabled.setAll(false);
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
						return isoDateFormat.format(((Calendar)t).getTime());
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
	
	public static MetadataInfo getSampleMetadata(){
		MetadataInfo md = new MetadataInfo();
		// Spec is at : http://partners.adobe.com/public/developer/en/xmp/sdk/XMPspecification.pdf
		md.basic.title = "Dracula"; 
		md.basic.author = "Bram Stoker";
		md.basic.subject = "Horror tales, Epistolary fiction, Gothic fiction (Literary genre), Vampires -- Fiction, Dracula, Count (Fictitious character) -- Fiction, Transylvania (Romania) -- Fiction, Whitby (England) -- Fiction"; 
		md.basic.keywords = "Horror, Gothic, Vampires";
		md.basic.creator = "Adobe InDesign CS4 (6.0.6)";
		md.basic.producer = "Adobe PDF Library 9.0";
		md.basic.creationDate = Calendar.getInstance();
		md.basic.modificationDate = Calendar.getInstance(); 
		md.basic.trapped = "True";

		md.xmpBasic.creatorTool = "Adobe InDesign CS4 (6.0.6)";
		md.xmpBasic.createDate = md.basic.creationDate;
		md.xmpBasic.modifyDate = md.basic.modificationDate;
		md.xmpBasic.title = md.basic.title;
		md.xmpBasic.baseURL = "https://www.gutenberg.org/";
		md.xmpBasic.rating = 3; 
		md.xmpBasic.label = "Horror Fiction Collection";
		md.xmpBasic.nickname = "dracula";
		md.xmpBasic.identifiers = Arrays.asList("Dracula_original_edition");
		//md.xmpBasic.advisories ; 
		md.xmpBasic.metadataDate = Calendar.getInstance();

		md.xmpPdf.pdfVersion = "1.5"; 
		md.xmpPdf.keywords = md.basic.keywords;
		md.xmpPdf.producer = "Adobe PDF Library 9.0";

		md.xmpDc.title = md.basic.title;
		md.xmpDc.description = "The famous Bram Stocker book"; 
		md.xmpDc.creators = new ArrayList<String>();
		md.xmpDc.creators.add("Bram Stocker");
		md.xmpDc.subjects = Arrays.asList(md.basic.subject.split("\\s*,\\s*"));
		
		/*
		md.xmpDc.contributors;
		md.xmpDc.coverage 
		md.xmpDc.dates = 
		md.xmpDc.format = 
		md.xmpDc.identifier = 
		md.xmpDc.languages = 
		md.xmpDc.publishers = 
		md.xmpDc.relationships = 
		md.xmpDc.rights = 
		md.xmpDc.source = 
		md.xmpDc.types = 
		*/
		return md;
	}


}
